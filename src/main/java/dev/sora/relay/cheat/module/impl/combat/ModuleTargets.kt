package dev.sora.relay.cheat.module.impl.combat

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.cheat.value.NamedChoice
import dev.sora.relay.game.GameSession
import dev.sora.relay.game.entity.Entity
import dev.sora.relay.game.entity.EntityLocalPlayer
import dev.sora.relay.game.entity.EntityOther
import dev.sora.relay.game.entity.EntityPlayer
import dev.sora.relay.game.event.*
import dev.sora.relay.utils.timing.MillisecondTimer
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import java.util.concurrent.LinkedBlockingQueue

class ModuleTargets : CheatModule("Targets", CheatCategory.COMBAT, canToggle = false) {

	private var targetPlayersValue by boolValue("TargetPlayers", true)
	private var targetHostileEntitiesValue by boolValue("TargetHostileEntities", false)
	private var targetNeutralEntitiesValue by boolValue("TargetNeutralEntities", false)
	private var antiBotModeValue by listValue("AntiBotMode", AntiBotMode.values(), AntiBotMode.NONE)
	private var teamCheckModeValue by listValue("TeamCheckMode", TeamCheckMode.values(), TeamCheckMode.NONE)
	private val rangeValue by floatValue("Range", 150f, 20f..300f).visible { teamCheckModeValue == TeamCheckMode.ROUND }

	private var attackTimer = MillisecondTimer()
	private var previousAttack: Entity? = null
		get() {
			if (attackTimer.hasTimePassed(3000)) {
				field = null
			}

			return field
		}
		private set

	fun Entity.isTarget(): Boolean {
		return if (this == session.player) false
		else if (targetPlayersValue && this is EntityPlayer) !this.isBot() && !this.isTeammate()
		else this is EntityOther && ((targetHostileEntitiesValue && this.isHostile) || (targetNeutralEntitiesValue && this.isNeutral))
	}

	fun EntityPlayer.isBot(): Boolean {
		if (this is EntityLocalPlayer) return false

		return when (antiBotModeValue) {
			AntiBotMode.PLAYER_LIST -> {
				val playerList = session.level.playerList[this.uuid] ?: return true
				playerList.name.isBlank()
			}
			AntiBotMode.NONE -> false
			AntiBotMode.NAME_TAG -> {
				if (this.username.isBlank())
					return true

				if (session.player.displayName.isBlank() != this.displayName.isBlank())
					return true

				if (session.player.displayName.contains("\n") != this.displayName.contains("\n"))
					return true

				return false
			}
		}
	}

	private var nameList: LinkedBlockingQueue<Entity>? = null
	private var list:List<Entity>?= null
	private var selfName = ""
	fun EntityPlayer.isTeammate(): Boolean {
		if (this is EntityLocalPlayer) return false

		return when (teamCheckModeValue) {
			TeamCheckMode.NAME_TAG -> {
				val thePlayerNameTag = session.player.displayName
				val targetNameTag = this.displayName

				if (thePlayerNameTag.length <= 2 || targetNameTag.length <= 2)
					return false

				if (!thePlayerNameTag.contains("§") || !targetNameTag.contains("§"))
					return false

				return thePlayerNameTag.subSequence(0, 2) == targetNameTag.subSequence(0, 2)
			}
			TeamCheckMode.NAME -> {
				if(nameList.isNullOrEmpty()) return false
				return nameList!!.filterIsInstance<EntityPlayer>().any { it.username.equals(this.username, true) }
			}
			TeamCheckMode.ROUND -> {
				if(list.isNullOrEmpty()) return false
				return list!!.filterIsInstance<EntityPlayer>().any { it.username.equals(this.username, true) }
			}
			TeamCheckMode.NONE -> false
		}
	}

	override fun onEnable() {
		super.onEnable()
		if(!nameList.isNullOrEmpty()) nameList!!.clear()
		selfName = if (session.player.metadata[EntityDataTypes.NAME].toString().contains("\n")) session.player.metadata[EntityDataTypes.NAME].toString().replace("\n", " ") else session.player.metadata[EntityDataTypes.NAME].toString()
		session.chat("Your Name: $selfName")
		if(teamCheckModeValue == TeamCheckMode.NAME){
			nameList = LinkedBlockingQueue<Entity>()
			nameList!!.clear()
			for (entity in session.level.entityMap.values.filter { it is EntityPlayer && !it.isBot() }) {
				val a = entity as EntityPlayer
				if(session.player.username.contains(a.username.substring(0,4))) {
					session.chat("Added to teams " + a.username)
					nameList!!.add(entity)
				}
			}
		}
		if(teamCheckModeValue == TeamCheckMode.ROUND){
			list=session.level.entityMap.values.filter { it is EntityPlayer && it.distanceSq(session.player) < rangeValue && !it.isBot() }
			for (entity in list!!) {
				val a = entity as EntityPlayer
				session.chat("Added to teams "+a.username)
			}
		}
	}

	private val handlePacketOutbound = handleConstant<EventPacketOutbound> {
		if (packet is InventoryTransactionPacket && packet.transactionType == InventoryTransactionType.ITEM_USE_ON_ENTITY && packet.actionType == 1) {
			val entity = session.level.entityMap[packet.runtimeEntityId] ?: return@handleConstant
			if (!entity.isTarget()) {
				return@handleConstant
			}
			if (previousAttack != entity) {
				session.eventManager.emit(EventTargetChange(session, entity))
			}
			previousAttack = entity
			attackTimer.reset()
		}
	}

	private val handleEntityDespawn = handleConstant<EventEntityDespawn> {
		if (previousAttack != null && entity == previousAttack && !attackTimer.hasTimePassed(3000)) {
			previousAttack = null
			session.eventManager.emit(EventTargetKilled(session, entity))
		}
	}

	@Suppress("unchecked_cast")
	protected inline fun <reified T : GameEvent> handleConstant(noinline handler: Handler<T>) {
		handlers.add(EventHook(T::class.java, handler) as EventHook<in GameEvent>)
	}

	class EventTargetChange(session: GameSession, val target: Entity) : GameEvent(session, "target_changed")

	class EventTargetKilled(session: GameSession, val target: Entity) : GameEvent(session, "target_killed")

	private enum class AntiBotMode(override val choiceName: String) : NamedChoice {
		PLAYER_LIST("PlayerList"),
		NAME_TAG("NameTag"),
		NONE("None")

	}

	private enum class TeamCheckMode(override val choiceName: String) : NamedChoice {
		NAME_TAG("NameTag"),
		NAME("Name"),
		ROUND("Round"),
		NONE("None")
	}
}
