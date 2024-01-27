package dev.sora.relay.cheat.module.impl.movement

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.cheat.value.Choice
import dev.sora.relay.game.GameSession
import dev.sora.relay.game.entity.data.Effect
import dev.sora.relay.game.event.EventPacketInbound
import dev.sora.relay.game.event.EventPacketOutbound
import dev.sora.relay.game.event.EventTick
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.*
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import org.cloudburstmc.protocol.bedrock.packet.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ModuleFlight : CheatModule("Flight", CheatCategory.MOVEMENT) {

	private var modeValue by choiceValue(
		"Mode",
		arrayOf(Vanilla("Vanilla"), Mineplex(), Jetpack(), Glide(), YPort(), Motion(), Teleport(), Jump()),
		"Vanilla"
	)
	private var verticalSpeedValue by floatValue("Vertical Speed", 1.5f, 0.1f..5f)
	private var horizontalSpeedValue by floatValue("Horizontal Speed", 1.5f, 0.1f..5f)
	private var addValue by floatValue("Add", -0.02f, -0.2f..0.2f)
	private var pressJumpValue by boolValue("PressJump", true)
	private var glideSpoofValue by boolValue("GlideSpoof", true)

	private var launchY = 0f
	private var gliding = false
	private val canFly: Boolean
		get() = !pressJumpValue || session.player.inputData.contains(PlayerAuthInputData.JUMP_DOWN)

	private val abilityPacket = UpdateAbilitiesPacket().apply {
		playerPermission = PlayerPermission.OPERATOR
		commandPermission = CommandPermission.OWNER
		abilityLayers.add(AbilityLayer().apply {
			layerType = AbilityLayer.Type.BASE
			abilitiesSet.addAll(Ability.values())
			abilityValues.addAll(
				arrayOf(
					Ability.BUILD,
					Ability.MINE,
					Ability.DOORS_AND_SWITCHES,
					Ability.OPEN_CONTAINERS,
					Ability.ATTACK_PLAYERS,
					Ability.ATTACK_MOBS,
					Ability.OPERATOR_COMMANDS,
					Ability.MAY_FLY,
					Ability.FLY_SPEED,
					Ability.WALK_SPEED
				)
			)
			walkSpeed = 0.1f
			flySpeed = 0.15f
		})
	}

	fun hurt(session: GameSession) {
		session.sendPacketToClient(EntityEventPacket().apply {
			runtimeEntityId = session.player.runtimeEntityId
			type = EntityEventType.HURT
		})
	}

	override fun onEnable() {
		super.onEnable()
		launchY = session.player.posY
		if(glideSpoofValue){
			session.sendPacket(PlayerActionPacket().apply {
				action = PlayerActionType.START_GLIDE
				runtimeEntityId = session.player.runtimeEntityId
			})
		}
		gliding = true
	}

	override fun onDisable() {
		super.onDisable()
		if(glideSpoofValue){
			session.sendPacket(PlayerActionPacket().apply {
				action = PlayerActionType.STOP_GLIDE
				runtimeEntityId = session.player.runtimeEntityId
			})
		}
		gliding = false
	}

	private val onPacketOutbound = handle<EventPacketOutbound> {
		if(glideSpoofValue) {
			if (packet is PlayerActionPacket) {
				if (packet.action == PlayerActionType.STOP_GLIDE) {
					cancel()
				}
			} else if (packet is PlayerAuthInputPacket) {
				if (!gliding) {
					packet.inputData.add(PlayerAuthInputData.NORTH_JUMP)
					packet.inputData.add(PlayerAuthInputData.JUMP_DOWN)
					packet.inputData.add(PlayerAuthInputData.JUMPING)
					packet.inputData.add(PlayerAuthInputData.WANT_UP)
					packet.inputData.add(PlayerAuthInputData.START_GLIDING)
					gliding = true
				}
			}
		}
	}

	private open inner class Vanilla(choiceName: String) : Choice(choiceName) {
		private var hurtValue by boolValue("Hurt", false)

		override fun onEnable() {
			if (session.netSessionInitialized) {
				abilityPacket.abilityLayers[0].flySpeed = horizontalSpeedValue * 0.03f
				session.netSession.inboundPacket(abilityPacket.apply {
					uniqueEntityId = session.player.uniqueEntityId
				})
			}
			if (hurtValue) hurt(session)
		}

		private val handlePacketInbound = handle<EventPacketInbound> {
			if (packet is UpdateAbilitiesPacket) {
				cancel()
				abilityPacket.abilityLayers[0].flySpeed = horizontalSpeedValue * 0.03f
				session.netSession.inboundPacket(abilityPacket.apply {
					uniqueEntityId = session.player.uniqueEntityId
				})
			} else if (packet is StartGamePacket) {
				abilityPacket.abilityLayers[0].flySpeed = horizontalSpeedValue * 0.03f
				session.netSession.inboundPacket(abilityPacket.apply {
					uniqueEntityId = session.player.uniqueEntityId
				})
			}
		}

		private val handlePacketOutbound = handle<EventPacketOutbound> {
			if (packet is RequestAbilityPacket && packet.ability == Ability.FLYING) {
				cancel()
			}
		}
	}

	private inner class Mineplex : Vanilla("Mineplex") {

		private var motionValue by boolValue("MineplexMotion", false)

		private val handleTick = handle<EventTick> {
			if (session.player.tickExists % 10 == 0L) {
				session.netSession.inboundPacket(abilityPacket.apply {
					uniqueEntityId = session.player.uniqueEntityId
				})
			}
			if (!canFly) {
				launchY = session.player.posY
				return@handle
			}
			val player = session.player
			val yaw = Math.toRadians(player.rotationYaw.toDouble()).toFloat()
			val value = horizontalSpeedValue
			if (motionValue) {
				session.netSession.inboundPacket(SetEntityMotionPacket().apply {
					runtimeEntityId = session.player.runtimeEntityId
					motion = Vector3f.from(-sin(yaw) * value, 0f, +cos(yaw) * value)
				})
			} else {
				player.teleport(player.posX - sin(yaw) * value, launchY, player.posZ + cos(yaw) * value)
			}
		}

		private val handlePacketInbound = handle<EventPacketOutbound> {
			if (packet is RequestAbilityPacket && packet.ability == Ability.FLYING) {
				session.netSession.inboundPacket(abilityPacket.apply {
					uniqueEntityId = session.player.uniqueEntityId
				})
				cancel()
			} else if (packet is PlayerAuthInputPacket && canFly) {
				packet.position = packet.position.let {
					Vector3f.from(it.x, launchY, it.z)
				}
			}
		}
	}

	private inner class Jetpack : Choice("Jetpack") {

		private val handleTick = handle<EventTick> {
			if (!canFly) {
				return@handle
			}

			session.netSession.inboundPacket(SetEntityMotionPacket().apply {
				runtimeEntityId = session.player.runtimeEntityId

				val calcYaw: Double = (session.player.rotationYawHead + 90) * (PI / 180)
				val calcPitch: Double = (session.player.rotationPitch) * -(PI / 180)

				motion = Vector3f.from(
					cos(calcYaw) * cos(calcPitch) * horizontalSpeedValue,
					sin(calcPitch) * verticalSpeedValue,
					sin(calcYaw) * cos(calcPitch) * horizontalSpeedValue
				)
			})
		}
	}

	private inner class Glide : Choice("Glide") {

		override fun onDisable() {
			if (session.netSessionInitialized) {
				session.netSession.inboundPacket(MobEffectPacket().apply {
					event = MobEffectPacket.Event.REMOVE
					runtimeEntityId = session.player.runtimeEntityId
					effectId = Effect.SLOW_FALLING
				})
			}
		}

		private val handleTick = handle<EventTick> {
			if (session.player.tickExists % 20 != 0L) return@handle
			session.netSession.inboundPacket(MobEffectPacket().apply {
				runtimeEntityId = session.player.runtimeEntityId
				setEvent(MobEffectPacket.Event.ADD)
				effectId = Effect.SLOW_FALLING
				amplifier = 0
				isParticles = false
				duration = 360000
			})
		}
	}


	private inner class YPort : Choice("YPort") {

		private var flag = true

		override fun onDisable() {
			flag = true
		}

		private val onTick = handle<EventTick> {
			if (canFly) {
				val angle = Math.toRadians(session.player.rotationYaw.toDouble()).toFloat()

				session.netSession.inboundPacket(SetEntityMotionPacket().apply {
					runtimeEntityId = session.player.runtimeEntityId
					motion =
						Vector3f.from(
							-sin(angle) * horizontalSpeedValue,
							if (flag) 0.42f else -0.42f,
							cos(angle) * horizontalSpeedValue
						)
				})
				flag = !flag
			}
		}
	}

	private inner class Motion : Choice("Motion") {
		private val onTick = handle<EventTick> {
			val session = this.session
			val player = session.player
			val yaw = player.direction

			var motionX = 0f
			var motionY = addValue
			var motionZ = 0f

			if (session.player.inputData.contains(PlayerAuthInputData.WANT_UP)) {
				motionY = verticalSpeedValue
			} else if (session.player.inputData.contains(PlayerAuthInputData.WANT_DOWN)) {
				motionY = -verticalSpeedValue
			}
			if (player.isHorizontallyMove()) {
				motionX = ((-sin(yaw) * horizontalSpeedValue).toFloat())
				motionZ = ((cos(yaw) * horizontalSpeedValue).toFloat())
			}

			session.netSession.inboundPacket(SetEntityMotionPacket().apply {
				runtimeEntityId = session.player.runtimeEntityId
				motion = Vector3f.from(motionX, motionY, motionZ)
			})
		}
	}

	private inner class Teleport : Choice("Teleport") {
		private val handleTick = handle<EventTick> {
			val session = this.session
			val yaw = session.player.direction
			if (session.player.inputData.contains(PlayerAuthInputData.WANT_UP)) {
				launchY += verticalSpeedValue
			} else if (session.player.inputData.contains(PlayerAuthInputData.WANT_DOWN)) {
				launchY -= verticalSpeedValue
			}
			if (session.player.isHorizontallyMove()) {
				session.player.teleport(
					(session.player.posX - sin(yaw) * horizontalSpeedValue).toFloat(),
					launchY,
					(session.player.posZ + cos(yaw) * horizontalSpeedValue).toFloat()
				)
			} else {
				session.player.teleport(session.player.posX, launchY, session.player.posZ)
			}
		}
	}

	private inner class Jump : Choice("Jump") {
		private var jumpHighValue by floatValue("Jump High", 0.42f, 0f..5f)
		private var boostValue by boolValue("Motion Boost", false)
		private var hurtValue by boolValue("Hurt", false)

		private val handleTick = handle<EventTick> {
			val player = session.player
			val yaw = player.direction
			var motionX = 0f
			var motionZ = 0f
			if (player.isHorizontallyMove()) {
				motionX =
					if (boostValue) {
						((-sin(yaw) * horizontalSpeedValue).toFloat())
					} else {
						player.motionX
					}
				motionZ =
					if (boostValue) {
						((cos(yaw) * horizontalSpeedValue).toFloat())
					} else {
						player.motionZ
					}
			}
			if (player.posY < launchY) {
				session.sendPacketToClient(SetEntityMotionPacket().apply {
					runtimeEntityId = session.player.runtimeEntityId
					motion = Vector3f.from(motionX, jumpHighValue, motionZ)
				})
				if (hurtValue) {
					hurt(session)
				}
			}
		}
	}
}
