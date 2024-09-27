package me.olios.plugins.effectmaster

import me.olios.plugins.effectmaster.commands.EffectMasterCommand
import me.olios.plugins.effectmaster.listeners.PlayerDeathEvent
import me.olios.plugins.effectmaster.listeners.PlayerJoinEvent
import me.olios.plugins.effectmaster.listeners.PlayerRespawnEvent
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class EffectMaster : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()

        registerListeners()
        registerCommands()
    }

    private fun registerListeners() {
        Bukkit.getServer().pluginManager.registerEvents(PlayerJoinEvent(this), this)
        Bukkit.getServer().pluginManager.registerEvents(PlayerDeathEvent(this), this)
        Bukkit.getServer().pluginManager.registerEvents(PlayerRespawnEvent(this), this)
    }

    private fun registerCommands() {
        getCommand("effectmaster")?.setExecutor(EffectMasterCommand(this))
    }
}