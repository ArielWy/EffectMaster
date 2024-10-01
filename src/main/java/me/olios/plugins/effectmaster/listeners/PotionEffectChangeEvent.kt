package me.olios.plugins.effectmaster.listeners

import me.olios.plugins.effectmaster.handler.EffectHandler
import me.olios.plugins.effectmaster.EffectMaster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.potion.PotionEffectType

class PotionEffectChangeEvent(private val plugin: EffectMaster): Listener {

    @EventHandler
    fun onPotionEffectChange(event: EntityPotionEffectEvent) {
        if (event.action != EntityPotionEffectEvent.Action.REMOVED) return

        val player: Player =  if (event.entity is Player) event.entity as Player else return
        val effects: List<PotionEffectType> = EffectHandler(plugin, player).getEffects() ?: return

        if (!effects.contains(event.modifiedType)) return

        if (player.hasMetadata("allowEffectRemoval")) { // allow the plugin to remove the effects
            player.removeMetadata("allowEffectRemoval", plugin)
            return
        }

            event.isCancelled = true // cancel when trying to remove the effects
    }
}