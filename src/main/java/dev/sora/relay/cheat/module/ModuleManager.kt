package dev.sora.relay.cheat.module

import dev.sora.relay.cheat.module.impl.combat.*
import dev.sora.relay.cheat.module.impl.misc.*
import dev.sora.relay.cheat.module.impl.movement.*
import dev.sora.relay.cheat.module.impl.visual.*
import dev.sora.relay.game.GameSession

class ModuleManager(private val session: GameSession) {

	val modules = mutableListOf<CheatModule>()

	fun registerModule(module: CheatModule) {
		module.session = session
		module.moduleManager = this
		modules.add(module)
		module.register(session.eventManager)
	}

	fun init() {
		//combat
		registerModule(ModuleVelocity())
		registerModule(ModuleKillAura())
		registerModule(ModuleTargets())
		registerModule(ModuleSurround())
		registerModule(ModuleCrystalAura())
		registerModule(ModuleHitbox())
		registerModule(ModuleCriticals())
		registerModule(ModuleGodMode())

		//misc
		registerModule(ModuleSpammer())
		registerModule(ModuleBGM())
		registerModule(ModuleDisabler())
		registerModule(ModuleNoSkin())
		registerModule(ModuleDeviceSpoof())
		registerModule(ModuleResourcePackSpoof())
		registerModule(ModuleMiner())
		registerModule(ModuleInventoryHelper())

		//movement
		registerModule(ModuleFly())
		registerModule(ModuleNoFall())
		registerModule(ModuleFastBreak())
		registerModule(ModuleBlink())
		registerModule(ModuleBlockFly())
		registerModule(ModuleAirJump())
		registerModule(ModuleClip())
		registerModule(ModuleSpeed())
		registerModule(ModuleOpFightBot())
		registerModule(ModuleClickTP())

		//visual
		registerModule(ModuleNoHurtCam())
		registerModule(ModuleAntiBlind())
		registerModule(ModuleHitEffect())
		registerModule(ModuleNoFireCam())

	}

	inline fun <reified T : CheatModule> getModule(t: Class<T>): T {
		return modules.filterIsInstance<T>().first()
	}

	fun getModuleByName(name:String):CheatModule?{
		for (module in modules) {
			if(module.name.equals(name,true)) return module
		}
		return null
	}
}
