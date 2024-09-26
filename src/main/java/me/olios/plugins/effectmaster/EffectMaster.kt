package me.olios.plugins.effectmaster

import me.olios.plugins.effectmaster.listeners.PlayerDeathEvent
import me.olios.plugins.effectmaster.listeners.PlayerJoinEvent
import me.olios.plugins.effectmaster.listeners.PlayerRespawnEvent
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class EffectMaster : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()

        registerListeners()
    }

    private fun registerListeners() {
        Bukkit.getServer().pluginManager.registerEvents(PlayerJoinEvent(this), this)
        Bukkit.getServer().pluginManager.registerEvents(PlayerDeathEvent(this), this)
        Bukkit.getServer().pluginManager.registerEvents(PlayerRespawnEvent(this), this)
    }
}