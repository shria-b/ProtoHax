package dev.sora.relay.cheat.module.impl.combat

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.cheat.value.Choice
import dev.sora.relay.game.event.EventPacketInbound
import dev.sora.relay.game.utils.constants.Attribute
import org.cloudburstmc.protocol.bedrock.data.AttributeData
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAttributesPacket

class ModuleVelocity : CheatModule("Velocity", CheatCategory.COMBAT) {

    private var modeValue by choiceValue("Mode", arrayOf(Vanilla, Simple, Resistance), Vanilla)
	private var resistanceValue by floatValue("Resistance", 0f, 0f..1f).visible { modeValue == Resistance }

	override fun onEnable() {
		if(modeValue == Resistance){
			session.netSession.inboundPacket(UpdateAttributesPacket().apply {
				runtimeEntityId = session.player.runtimeEntityId
				attributes.add(AttributeData(Attribute.KNOCKBACK_RESISTANCE,0f, 1f,resistanceValue,0f))
			})
		}
	}

	override fun onDisable() {
		if(modeValue == Resistance){
			session.netSession.inboundPacket(UpdateAttributesPacket().apply {
				runtimeEntityId = session.player.runtimeEntityId
				attributes.add(session.player.attributes[Attribute.KNOCKBACK_RESISTANCE])
			})
		}
	}

	private object Vanilla : Choice("Vanilla") {

		private val handlePacketInbound = handle<EventPacketInbound> {
			if (packet is SetEntityMotionPacket) {
				cancel()
			}
		}
	}

	private object Simple : Choice("Simple") {

		private var horizontalValue by floatValue("Horizontal", 0f, 0f..1f)
		private var verticalValue by floatValue("Vertical", 0f, 0f..1f)

		private val handlePacketInbound = handle<EventPacketInbound> {
			if (packet is SetEntityMotionPacket) {
				packet.motion = packet.motion.mul(horizontalValue, verticalValue, horizontalValue)
			}
		}
	}

	private object Resistance : Choice("Resistance") {

		private val handlePacketInbound = handle<EventPacketInbound> {
			if (packet is UpdateAttributesPacket) {
				if(packet.runtimeEntityId == session.player.runtimeEntityId){
					packet.attributes.forEach {
						if(it.name == Attribute.KNOCKBACK_RESISTANCE){
							cancel()
						}
					}
				}
			}
		}
	}
}
