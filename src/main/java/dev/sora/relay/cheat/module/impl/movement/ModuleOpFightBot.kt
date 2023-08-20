package dev.sora.relay.cheat.module.impl.movement

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.cheat.module.impl.combat.ModuleTargets
import dev.sora.relay.cheat.value.NamedChoice
import dev.sora.relay.game.event.EventTick
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class ModuleOpFightBot : CheatModule("OPFightBot", CheatCategory.MOVEMENT) {

    private var modeValue by listValue("Mode", Mode.values(), Mode.STRAFE)
    private var rangeValue by floatValue("Range", 1.5f, 1.5f..4f)
	private var yOffsetValue by floatValue("Y Offset", 0.5f, -5f..5f)
	private var passiveValue by boolValue("Passive", false)
    private var horizontalSpeedValue by floatValue("HorizontalSpeed", 5f, 1f..7f)
    private var verticalSpeedValue by floatValue("VerticalSpeed", 4f, 1f..7f)
    private var strafeSpeedValue by intValue("StrafeSpeed", 20, 10..90).visible { modeValue == Mode.STRAFE }
	private var speedValue by floatValue("Speed", 0.5f, 0.1f..5f).visible { modeValue == Mode.SPEED }

	private val handleTick = handle<EventTick> {
		val moduleTargets = moduleManager.getModule(ModuleTargets::class.java)
		val target = session.level.entityMap.values.filter { with(moduleTargets) { it.isTarget() } }
			.minByOrNull { it.distanceSq(session.player) } ?: return@handle
		if(target.distance(session.player) < 5) {
			val direction = Math.toRadians(when(modeValue) {
				Mode.RANDOM -> Math.random() * 360
				Mode.STRAFE -> ((session.player.tickExists * strafeSpeedValue) % 360).toDouble()
				Mode.BEHIND -> target.rotationYaw + 180.0
				Mode.SPEED -> ((session.player.tickExists * strafeSpeedValue) % 360).toDouble()
			}).toFloat()
			if(modeValue != Mode.SPEED) {
				session.player.teleport(
					target.posX - sin(direction) * rangeValue,
					target.posY + yOffsetValue,
					target.posZ + cos(direction) * rangeValue
				)
			} else{
				if(session.player.onGround){
					session.netSession.inboundPacket(SetEntityMotionPacket().apply {
						runtimeEntityId = session.player.runtimeEntityId
						motion = Vector3f.from((-sin(direction) * speedValue), 0.42f, (cos(direction) * speedValue))
					})
				}else {
					session.netSession.inboundPacket(SetEntityMotionPacket().apply {
						runtimeEntityId = session.player.runtimeEntityId
						motion = Vector3f.from((-sin(direction) * speedValue),session.player.motionY - (yOffsetValue + 0.1265f),(cos(direction) * speedValue))
					})
				}
			}
		} else if (!passiveValue) {
			val direction = atan2(target.posZ - session.player.posZ, target.posX - session.player.posX) - Math.toRadians(90.0).toFloat()
			session.player.teleport(session.player.posX - sin(direction) * horizontalSpeedValue,
				target.posY.coerceIn(session.player.posY - verticalSpeedValue, session.player.posY + verticalSpeedValue),
				session.player.posZ + cos(direction) * horizontalSpeedValue)
		}
	}

	private enum class Mode(override val choiceName: String) : NamedChoice {
        RANDOM("Random"),
        STRAFE("Strafe"),
        BEHIND("Behind"),
		SPEED("Speed")
    }
}
