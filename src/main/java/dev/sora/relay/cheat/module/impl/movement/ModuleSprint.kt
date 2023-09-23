package dev.sora.relay.cheat.module.impl.movement

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.cheat.value.Choice
import dev.sora.relay.game.event.EventPacketOutbound
import dev.sora.relay.game.event.EventTick
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket

class ModuleSprint: CheatModule("Sprint", CheatCategory.MOVEMENT) {
	private var modeValue by choiceValue("Mode", arrayOf(Packet(), Packet2()), "SendPacket")

	private inner class Packet : Choice("SendPacket") {
		private val onTick = handle<EventTick> {
			if(!session.player.isSprinting){
				session.netSession.outboundPacket(PlayerActionPacket().apply {
					runtimeEntityId = session.player.runtimeEntityId
					action = PlayerActionType.START_SPRINT
				})
			}
		}
	}

	private inner class Packet2 : Choice("KeepSprint") {
		private val onPacketOutbound = handle<EventPacketOutbound> {
			if(packet is PlayerActionPacket){
				if(packet.action == PlayerActionType.STOP_SPRINT){
					cancel()
				}
			}
		}
	}
}
