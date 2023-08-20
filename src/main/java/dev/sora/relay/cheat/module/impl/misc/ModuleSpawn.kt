package dev.sora.relay.cheat.module.impl.misc

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.game.event.EventPacketOutbound
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.RespawnPacket

class ModuleSpawn : CheatModule("Spawn", CheatCategory.MISC) {
	private var teleportValue by boolValue("Teleport", false)

	private val onPacketOutbound = handle<EventPacketOutbound> {
		val packet = this.packet
		if(packet is RespawnPacket){
			val pos = session.player.vec3Position
			session.chat("Spawn!")
			session.sendPacketToClient(MovePlayerPacket().apply {
				mode = MovePlayerPacket.Mode.NORMAL
				isOnGround = session.player.onGround
				rotation = session.player.vec3Rotation
				position = packet.position
				runtimeEntityId = session.player.runtimeEntityId
			})
			session.sendPacketToClient(MovePlayerPacket().apply {
				mode = MovePlayerPacket.Mode.NORMAL
				isOnGround = session.player.onGround
				rotation = session.player.vec3Rotation
				position = pos
				runtimeEntityId = session.player.runtimeEntityId
			})
			if(teleportValue){
				session.player.teleport(pos)
			}
		}
	}
}
