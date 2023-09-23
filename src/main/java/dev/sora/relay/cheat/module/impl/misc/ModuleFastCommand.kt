package dev.sora.relay.cheat.module.impl.misc

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginData
import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginType
import org.cloudburstmc.protocol.bedrock.packet.CommandRequestPacket

class ModuleFastCommand: CheatModule("FastCommand", CheatCategory.MISC) {
	private var commandValue by stringValue("Command", "/")
	override fun onEnable() {
		super.onEnable()
		session.sendPacket(CommandRequestPacket().apply {
			command = commandValue
			commandOriginData = CommandOriginData(CommandOriginType.PLAYER, session.player.uuid,"",-1)
			isInternal = false
		})
	}
}
