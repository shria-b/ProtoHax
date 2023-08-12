package dev.sora.relay.cheat.module.impl.combat

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.cheat.value.NamedChoice
import dev.sora.relay.game.event.EventPacketOutbound
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket

class ModuleCriticals:CheatModule("Criticals",CheatCategory.COMBAT) {
	private var modeValue by listValue("Mode", Mode.values(), Mode.VANILLA)

	private enum class Mode(override val choiceName: String) : NamedChoice {
		VANILLA("Vanilla"),
		MOVEPACKET("MovePacket"),
		JUMP("Jump"),
		TPJUMP("TPJump")
	}

	private var onPacketOutbound = handle<EventPacketOutbound> {
		val packet = this.packet
		val player = session.player
		if (packet is InventoryTransactionPacket && packet.transactionType == InventoryTransactionType.ITEM_USE_ON_ENTITY) {
			when (modeValue) {
				Mode.VANILLA -> {
					val packetVanilla = this.packet
					if (packetVanilla is MovePlayerPacket) {
						packetVanilla.isOnGround = false
					}
				}
				Mode.MOVEPACKET -> {
					session.sendPacket(MovePlayerPacket().apply {
						runtimeEntityId = player.runtimeEntityId
						position = player.vec3Position.add(0f, Math.random().toFloat(), 0f)
						rotation = player.vec3Rotation
						isOnGround = false
					})
				}
				Mode.JUMP -> {
					if(player.onGround) {
						session.sendPacketToClient(SetEntityMotionPacket().apply {
							motion = Vector3f.from(
								player.motionX.toDouble(),
								0.42,
								player.motionZ.toDouble()
							)
							runtimeEntityId = player.runtimeEntityId
						})
					}
				}
				Mode.TPJUMP -> {
					if(player.onGround){
						player.teleport(player.posX, player.posY + 0.42f, player.posZ)
					}
				}
			}
		}
	}
}
