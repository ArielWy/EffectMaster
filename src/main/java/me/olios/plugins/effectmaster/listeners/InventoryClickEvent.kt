package me.olios.plugins.effectmaster.listeners

import me.olios.plugins.effectmaster.handler.EffectHandler
import me.olios.plugins.effectmaster.EffectMaster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickEvent(private val plugin: EffectMaster): Listener {
    val config = plugin.config

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val playerUUID = player.uniqueId
        val item = event.currentItem?: return
        val inventory = event.clickedInventory
        val gui = EffectMaster.playerInventory[playerUUID]

        if (inventory != gui) return  // the player doesn't interact with the gui
        event.isCancelled = true  // Prevent people from interacting with their inventory

        EffectHandler(plugin, player).effectInteraction(event, item)
    }
}