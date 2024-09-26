package me.olios.plugins.effectmaster.listeners

import me.olios.plugins.effectmaster.EffectHandler
import me.olios.plugins.effectmaster.EffectMaster
import org.bukkit.Bukkit.getServer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent

class PlayerRespawnEvent(private val plugin: EffectMaster): Listener {

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player

        getServer().scheduler.runTaskLater(plugin, Runnable {
            EffectHandler(plugin, player).reloadEffects()
        }, 20)
    }
}