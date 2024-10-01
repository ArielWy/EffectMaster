package me.olios.plugins.effectmaster.listeners

import me.olios.plugins.effectmaster.DefineItem
import me.olios.plugins.effectmaster.handler.EffectHandler
import me.olios.plugins.effectmaster.EffectMaster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

class PlayerDeathEvent(private val plugin: EffectMaster): Listener {
    val config = plugin.config

    @EventHandler
    fun onPlayerDeath(event: org.bukkit.event.entity.PlayerDeathEvent) {
        val player = event.player
        val killer = player.killer

        if (killer is Player)
            giveItem(killer)

        EffectHandler(plugin, player).decreaseRandomEffect()
    }

    private fun giveItem(player: Player) {
        val item: ItemStack = DefineItem(player, plugin).retrieve() ?: return
        val inv = player.inventory
        val remainingItems = inv.addItem(item)

        // If there are remaining items, drop them at the player's location
        if (remainingItems.isNotEmpty()) {
            for (remainingItem in remainingItems.values) {
                player.world.dropItemNaturally(player.location, remainingItem)
            }
        }
    }

}