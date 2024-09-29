package me.olios.plugins.effectmaster

import me.olios.plugins.effectmaster.commands.EffectMasterCommand
import me.olios.plugins.effectmaster.listeners.*
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class EffectMaster : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()

        registerListeners()
        registerCommands()
    }

    companion object {
        val playerInventory: MutableMap<UUID, Inventory> = mutableMapOf()
    }

    private fun registerListeners() {
        Bukkit.getServer().pluginManager.registerEvents(PlayerJoinEvent(this), this)
        Bukkit.getServer().pluginManager.registerEvents(PlayerDeathEvent(this), this)
        Bukkit.getServer().pluginManager.registerEvents(PlayerRespawnEvent(this), this)
        Bukkit.getServer().pluginManager.registerEvents(PlayerInteractEvent(this), this)
        Bukkit.getServer().pluginManager.registerEvents(InventoryClickEvent(this), this)
    }

    private fun registerCommands() {
        getCommand("effectmaster")?.setExecutor(EffectMasterCommand(this))
    }
}