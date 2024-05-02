package dev.Sora.relay.cheat.module.impl.movement

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import org.cloudburstmc.math.vector.Vector3d

class ModuleTp: CheatCategory("Tp",CheatCategory.MOVEMENT,canToggle=false){
  private val x by stringValue("x")
  private val y by stringValue("y")
  private val z by stringValue("z")
  override fun onEnable() {
    if(!session.netSessionInitialized) return
    
    val player = session.player
    val tpPos = Vector3d.from(
      x.toDouble(),
      y.toDouble(),
      z.toDouble()
    )
    player.teleport(tpPos)
  }
}
