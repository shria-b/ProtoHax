package dev.sora.relay.cheat.module.impl.combat

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.cheat.value.Choice
import dev.sora.relay.game.event.EventPacketOutbound
import dev.sora.relay.game.event.EventTick
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket


class ModuleGodMode : CheatModule("GodMode", CheatCategory.COMBAT) {
	private var modeValue by choiceValue("Mode", arrayOf(Packet(), Packet2()), "Packet")
	private var highValue by intValue("High", 5, -20..20)

	private inner class Packet : Choice("Packet"){
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

	private inner class Packet2 : Choice("Packet2"){
		private var authValue by boolValue("AuthPacket", false)
		private val onPacketOutbound = handle<EventPacketOutbound> {
			if(packet is MovePlayerPacket){
				packet.position = packet.position.add(0f, highValue.toFloat(), 0f)
			}
			if(authValue){
				if(packet is PlayerAuthInputPacket){
					packet.position = packet.position.add(0f, highValue.toFloat(), 0f)
				}
			}
		}
	}
}
