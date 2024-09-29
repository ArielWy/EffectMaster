package me.olios.plugins.effectmaster.listeners

import me.olios.plugins.effectmaster.EffectHandler
import me.olios.plugins.effectmaster.EffectMaster
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

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

        effectInteraction(event, item)
    }

    private fun effectInteraction(event: InventoryClickEvent, itemStack: ItemStack) {
        val player = event.whoClicked as Player
        val itemMeta = itemStack.itemMeta ?: return
        val itemDisplayName = itemMeta.displayName() ?: return

        // Check each effect item in the config
        val effectItems = listOf("Effect1", "Effect2", "Effect3", "Effect4")
        for ((index, effectItem) in effectItems.withIndex()) {
            val configDisplayName = config.getString("Items.$effectItem.name") ?: continue
            val displayNameComponent = MiniMessage.miniMessage().deserialize(configDisplayName)

            if (itemDisplayName != displayNameComponent) continue

            // The item matches the configured display name
            val effects: List<PotionEffectType> = EffectHandler(plugin, player).getEffects() ?: return
            EffectHandler(plugin, player).increaseEffect(index + 1)
            player.sendMessage("Increased effect: ${effects[index].name}")
            usedItem(player, event.clickedInventory)
        }
    }

    private fun usedItem(player: Player, inv: Inventory?) {
        // Set the main hand slot to air
        val airItem = ItemStack(Material.AIR)
        player.inventory.setItemInMainHand(airItem)

        // close the inventory
        inv?.close()

        // remove inv from the companion object
        EffectMaster.playerInventory.remove(player.uniqueId)
    }
}