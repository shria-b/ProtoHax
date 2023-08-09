package dev.sora.relay.cheat.module.impl.visual

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.cheat.module.impl.combat.ModuleTargets
import dev.sora.relay.cheat.value.NamedChoice
import dev.sora.relay.game.GameSession
import dev.sora.relay.game.entity.Entity
import dev.sora.relay.game.entity.EntityPlayer
import dev.sora.relay.game.event.EventPacketOutbound
import dev.sora.relay.game.registry.BlockDefinition
import dev.sora.relay.utils.timing.MillisecondTimer
import org.cloudburstmc.math.vector.Vector2f
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.nbt.NbtMap
import org.cloudburstmc.protocol.bedrock.data.LevelEvent
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.AddEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket
import kotlin.random.Random

class ModuleHitEffect : CheatModule("HitEffect", CheatCategory.VISUAL) {

	private var effectValue by listValue("Effect", Effect.values(), Effect.CRITICAL)
	private var delayValue by intValue("Delay", 400, 0..2000)
	private val effectTimer = MillisecondTimer()
	private val onPacketOutbound = handle<EventPacketOutbound> {(effectTimer.hasTimePassed(delayValue))
		if (packet is InventoryTransactionPacket && packet.transactionType == InventoryTransactionType.ITEM_USE_ON_ENTITY && packet.actionType == 1) {
			val target = session.level.entityMap[packet.runtimeEntityId] ?: return@handle
			if (with(moduleManager.getModule(ModuleTargets::class.java)) { target.isTarget() }) {
				effectValue.deployEffect(session, target)
				effectTimer.reset()
			}
		}
	}

	private enum class Effect(override val choiceName: String) : NamedChoice {
		CRITICAL("Critical") {
			override fun deployEffect(session: GameSession, target: Entity) {
				session.netSession.inboundPacket(AnimatePacket().apply {
					runtimeEntityId = target.runtimeEntityId
					action = AnimatePacket.Action.CRITICAL_HIT
				})
			}
		},
		BLOOD("Blood") {

			private val definition by lazy { BlockDefinition(0, "minecraft:redstone_block", NbtMap.EMPTY) }

			override fun deployEffect(session: GameSession, target: Entity) {
				session.netSession.inboundPacket(LevelEventPacket().apply {
					type = LevelEvent.PARTICLE_DESTROY_BLOCK
					position = if (target is EntityPlayer) target.vec3PositionFeet.add(0f, 1f, 0f) else target.vec3Position
					data = session.blockMapping.getRuntimeByDefinition(definition)
				})
			}
		},
		LIGHTNING("Lightning") {
			override fun deployEffect(session: GameSession, target: Entity) {
				val entityId = Random.nextLong()
				session.netSession.inboundPacket(
					AddEntityPacket().apply
					{
						uniqueEntityId = entityId
						runtimeEntityId = entityId
						identifier = "minecraft:lightning_bolt"
						entityType = 0
						position = Vector3f.from(
							target.vec3Position.x,
							target.vec3Position.y - 1.62f,
							target.vec3Position.z
						)
						motion = Vector3f.ZERO
						rotation = Vector2f.ZERO
						bodyRotation = 0f
					})}
		},
		TOTEM("Totem") {
			override fun deployEffect(session: GameSession, target: Entity) {
				session.sendPacket(LevelEventPacket().apply {
					type = LevelEvent.SOUND_TOTEM_USED
					position = target.vec3Position
				})
			}},
		LAVA("Lava") {

			private val definition by lazy { BlockDefinition(0, "minecraft:lava", NbtMap.builder()
				.putInt("liquid_depth", 15).build()) }

			override fun deployEffect(session: GameSession, target: Entity) {
				session.netSession.inboundPacket(LevelEventPacket().apply {
					type = LevelEvent.PARTICLE_DESTROY_BLOCK
					position = if (target is EntityPlayer) target.vec3PositionFeet.add(0f, 1f, 0f) else target.vec3Position
					data = session.blockMapping.getRuntimeByDefinition(definition)
				})
			}
		};

		abstract fun deployEffect(session: GameSession, target: Entity)
	}
}
