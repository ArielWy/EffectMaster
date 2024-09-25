package me.olios.plugins.effectmaster.listeners

import me.olios.plugins.effectmaster.EffectHandler
import me.olios.plugins.effectmaster.EffectMaster
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinEvent(private val plugin: EffectMaster): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        if (player.hasPlayedBefore())
            EffectHandler(plugin, player).reloadEffects()
        else EffectHandler(plugin, player).applyInitialEffects()


    }
}