package dev.sora.relay.cheat.module.impl.combat

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.game.event.EventPacketOutbound
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket

class ModuleComboHit : CheatModule("ComboHit", CheatCategory.COMBAT) {
	private val amountValue by intValue("Amount", 10, 1..100)

	private val onPacketOutbound = handle<EventPacketOutbound> {
		val packet = this.packet

		if (packet is InventoryTransactionPacket && packet.transactionType == InventoryTransactionType.ITEM_USE_ON_ENTITY) {
			repeat(amountValue) {
				session.sendPacket(packet)
			}
		}
	}
}
