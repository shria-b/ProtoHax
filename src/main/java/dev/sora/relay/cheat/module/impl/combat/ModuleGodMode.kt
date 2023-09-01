package dev.sora.relay.cheat.module.impl.combat

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.cheat.value.Choice
import dev.sora.relay.game.event.EventPacketOutbound
import dev.sora.relay.game.event.EventTick
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket


class ModuleGodMode : CheatModule("GodMode", CheatCategory.COMBAT) {
	private var modeValue by choiceValue("Mode", arrayOf(Packet(), Packet2()), "SendPacket")
	private var highValue by intValue("High", 5, -20..20)

	private inner class Packet : Choice("SendPacket"){
		private val onTick = handle<EventTick> {
			session.sendPacket(MovePlayerPacket().apply {
				runtimeEntityId = session.player.runtimeEntityId
				position = session.player.vec3Position.add(0f, highValue.toFloat(), 0f)
				rotation = session.player.vec3Rotation
				mode = MovePlayerPacket.Mode.NORMAL
				isOnGround = session.player.onGround
				tick = session.player.tickExists
			})
		}
	}

	private inner class Packet2 : Choice("CatchPacket"){
		private var authValue by boolValue("AuthPacket", false)
		private val onPacketOutbound = handle<EventPacketOutbound> {
			val attackPacket = this.packet
			if (attackPacket is InventoryTransactionPacket && attackPacket.transactionType == InventoryTransactionType.ITEM_USE_ON_ENTITY) {
				val movePacket = this.packet
				if (movePacket is MovePlayerPacket) {
					movePacket.position = session.player.vec3Position.add(0f, highValue.toFloat(), 0f)
				}
				if (authValue) {
					val authPacket = this.packet
					if (authPacket is PlayerAuthInputPacket) {
						authPacket.position = session.player.vec3Position.add(0f, highValue.toFloat(), 0f)
					}
				}
			}
		}
	}
}
