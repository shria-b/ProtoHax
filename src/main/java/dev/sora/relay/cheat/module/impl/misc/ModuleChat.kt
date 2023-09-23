package dev.sora.relay.cheat.module.impl.misc

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.game.event.EventPacketOutbound
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

class ModuleChat: CheatModule("Chat", CheatCategory.MISC) {
	private var messageValue by stringValue("Message", " | ProtoHax")

	private val onPacketOutbound = handle<EventPacketOutbound> {
		if(packet is TextPacket){
			if(packet.type == TextPacket.Type.CHAT){
				packet.message += messageValue
			}
		}
	}
}
