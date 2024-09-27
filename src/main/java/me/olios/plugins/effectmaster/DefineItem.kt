package me.olios.plugins.effectmaster

import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File
import java.io.IOException

class DefineItem(private val player: Player, private val plugin: EffectMaster) {
    private val defineFile: File = File(plugin.dataFolder, "define.yml")
    private val defineConfig: FileConfiguration = YamlConfiguration.loadConfiguration(defineFile)
    private val configPath: String = "Item"

    init {
        // Load or create the define.yml configuration file
        if (!defineFile.exists())
            plugin.saveResource("define.yml", false)
    }

    fun getItem() {
        val item = retrieve()
        if (item != null) {
            player.inventory.setItemInMainHand(item)
            sendMessage("§agive §2${player.name} the §9$configPath§a as §6{§e${item.type}§6}")
        }
        else sendMessage("§cThe §9$configPath§c does not found in the config file!")
    }

    fun checkForItem() {
        val item: ItemStack = player.inventory.itemInMainHand
        if (item.type == Material.AIR) isCanceled()
        else defineItem(item)
    }

    private fun isCanceled() {
        player.sendMessage("§cYou are not holding anything in your hand!")  // Send message to the player
    }

    private fun defineItem(item: ItemStack) {
        defineConfig.set(configPath, item)

        try { // try to save the file
            defineConfig.save(defineFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (defineConfig.getItemStack(configPath) == item) {
            sendMessage("§aDefine the §9$configPath§a successfully as §6{§e${defineConfig.getString(configPath)}§6}")
        } else {
            sendMessage("§cThe §9$configPath§a is not found in the config file")
        }
    }

    private fun retrieve(): ItemStack? {
        val defineFile = File(plugin.dataFolder, "define.yml")
        val defineConfig: FileConfiguration = YamlConfiguration.loadConfiguration(defineFile)

        // Retrieving the value
        val loadedItemStack: ItemStack? = defineConfig.getItemStack(configPath)
        var configItem: ItemStack? = null
        if (loadedItemStack != null) {
            try {
                configItem = loadedItemStack
            } catch (e: Exception) {
                // Handle any exceptions during deserialization
                e.printStackTrace()
            }
        }
        return configItem
    }

    private fun sendMessage(message: String) {
        plugin.logger.info("[BanBook] $message") // Log to console with prefix
        player.sendMessage(message) // Send to player
    }
}