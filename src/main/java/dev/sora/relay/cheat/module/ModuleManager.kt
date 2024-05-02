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
		registerModule(ModuleKillAura2())
		registerModule(ModuleKillAura3())
		registerModule(ModuleTargets())
		registerModule(ModuleSurround())
		registerModule(ModuleCrystalAura())
		registerModule(ModuleHitbox())
		registerModule(ModuleCriticals())
		registerModule(ModuleGodMode())
		registerModule(ModuleComboOneHit())

		//misc
		registerModule(ModuleSpammer())
		registerModule(ModuleBGM())
		registerModule(ModuleDisabler())
		registerModule(ModuleNoSkin())
		registerModule(ModuleDeviceSpoof())
		registerModule(ModuleResourcePackSpoof())
		registerModule(ModuleMiner())
		registerModule(ModuleBbzL())
		registerModule(ModuleCrasher())
		registerModule(ModuleAntiKick())
		registerModule(ModuleSpawn())
		registerModule(ModuleFastCommand())
		registerModule(ModuleChat())

		//movement
		registerModule(ModuleTp())
		registerModule(ModuleFlight())
		registerModule(ModuleNoFall())
		registerModule(ModuleFastBreak())
		registerModule(ModuleBlink())
		registerModule(ModuleBlockFly())
		registerModule(ModuleAirJump())
		registerModule(ModuleClip())
		registerModule(ModuleSpeed())
		registerModule(ModuleFightBot())
		registerModule(ModuleFX())
		registerModule(ModuleClickTP())
		registerModule(ModuleSprint()) 

		//visual
		registerModule(ModuleNoHurtCam())
		registerModule(ModuleAntiBlind())
		registerModule(ModuleHitEffect())
		registerModule(ModuleNoFireCam())
		registerModule(ModuleTextSpoof())

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
