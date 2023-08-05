package dev.sora.relay.cheat.module.impl.combat

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.game.event.EventPacketOutbound
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class ModuleGodMode : CheatModule("GodMode", CheatCategory.COMBAT) {
	private var highValue by intValue("High", 5, -20..20)
	private var attackCheckValue by boolValue("AttackCheck", false)
	private val onPacketOutbound = handle<EventPacketOutbound> {
		val attackPacket = this.packet
		if (attackCheckValue) {
			if (!(attackPacket is InventoryTransactionPacket && attackPacket.transactionType == InventoryTransactionType.ITEM_USE_ON_ENTITY)) {
				if (packet is MovePlayerPacket) {
					godModeForMove(packet)
				}
				if (packet is PlayerAuthInputPacket) {
					godModeForAuth(packet)
				}
			}
		} else {
			if (packet is MovePlayerPacket) {
				godModeForMove(packet)
			}
			if (packet is PlayerAuthInputPacket) {
				godModeForAuth(packet)
			}
		}
	}

	private fun godModeForMove(packet: MovePlayerPacket) {
		packet.position = Vector3f.from(
			packet.position.x.toDouble(),
			packet.position.y + highValue.toDouble(),
			packet.position.z.toDouble()
		)
	}

	private fun godModeForAuth(packet: PlayerAuthInputPacket) {
		packet.position = Vector3f.from(
			packet.position.x.toDouble(),
			packet.position.y + highValue.toDouble(),
			packet.position.z.toDouble()
		)
	}
}
