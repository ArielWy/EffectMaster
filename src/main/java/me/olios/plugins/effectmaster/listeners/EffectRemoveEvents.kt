package me.olios.plugins.effectmaster.listeners

import me.olios.plugins.effectmaster.EffectMaster
import me.olios.plugins.effectmaster.handler.EffectHandler
import org.bukkit.Bukkit.getServer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityResurrectEvent
import org.bukkit.event.player.PlayerItemConsumeEvent

class EffectRemoveEvents(private val plugin: EffectMaster): Listener {

    @EventHandler
    fun onPlayerConsume(event: PlayerItemConsumeEvent) {
        val player = event.player

        getServer().scheduler.runTaskLater(plugin, Runnable {
            EffectHandler(plugin, player).reloadEffects()
        }, 5)
    }

    @EventHandler
    fun onEntityResurrect(event: EntityResurrectEvent) {
        val player = event.entity
        if (player !is Player) return

        getServer().scheduler.runTaskLater(plugin, Runnable {
            EffectHandler(plugin, player).reloadEffects()
        }, 5)
    }
}