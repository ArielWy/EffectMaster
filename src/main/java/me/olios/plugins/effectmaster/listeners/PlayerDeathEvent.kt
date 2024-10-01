package me.olios.plugins.effectmaster.listeners

import me.olios.plugins.effectmaster.handler.EffectHandler
import me.olios.plugins.effectmaster.EffectMaster
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerDeathEvent(private val plugin: EffectMaster): Listener {

    @EventHandler
    fun onPlayerDeath(event: org.bukkit.event.entity.PlayerDeathEvent) {
        val player = event.player

        EffectHandler(plugin, player).decreaseRandomEffect()
    }
}