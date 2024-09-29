package me.olios.plugins.effectmaster.listeners

import me.olios.plugins.effectmaster.EffectInterface
import me.olios.plugins.effectmaster.EffectMaster
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.io.File

class PlayerInteractEvent(private val plugin: EffectMaster): Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return
        // define values
        val player = event.player
        val itemStack = player.inventory.itemInMainHand
        val item: ItemStack? = retrieve()

        if (itemStack != item) return

        EffectInterface(plugin).openGUI(player)
    }


    private fun retrieve(): ItemStack? {
        val defineFile = File(plugin.dataFolder, "define.yml")
        val defineConfig: FileConfiguration = YamlConfiguration.loadConfiguration(defineFile)

        // Retrieving the value
        val loadedItemStack: ItemStack? = defineConfig.getItemStack("Item")
        var item: ItemStack? = null
        if (loadedItemStack != null) {
            try {
                item = loadedItemStack
            } catch (e: Exception) {
                // Handle any exceptions during deserialization
                e.printStackTrace()
            }
        }
        return item
    }
}