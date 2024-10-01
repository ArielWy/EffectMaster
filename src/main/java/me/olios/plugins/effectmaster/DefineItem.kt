package me.olios.plugins.effectmaster

import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.io.File
import java.io.IOException

class DefineItem(private val player: Player, private val plugin: EffectMaster) {
    private val defineFile: File = File(plugin.dataFolder, "define.yml")
    private val defineConfig: FileConfiguration = YamlConfiguration.loadConfiguration(defineFile)
    private val configPath: String = "Item"
    private val config = plugin.config

    init {
        // Load or create the define.yml configuration file
        if (!defineFile.exists())
            plugin.saveResource("define.yml", false)
    }

    fun getItem() {
        val item = retrieve()

        if (item != null) {
            player.inventory.setItemInMainHand(item)
            val message: String = config.getString("Messages.getItemMessage").toString()
            val placeholder = mapOf(
                "player" to player.name,
                "item" to item.type.toString(),
            )
            sendMessage(message, placeholder)
        } else {
            val message: String = config.getString("Messages.itemNotFoundMessage").toString()
            sendMessage(message)
        }
    }

    fun checkForItem() {
        val item: ItemStack = player.inventory.itemInMainHand
        if (item.type == Material.AIR) isCanceled()
        else defineItem(item)
    }

    private fun isCanceled() {
        val message: String = config.getString("Messages.noItemInHandMessage").toString()
        sendMessage(message, log = false)
    }

    private fun defineItem(item: ItemStack) {
        defineConfig.set(configPath, item)

        try { // try to save the file
            defineConfig.save(defineFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (defineConfig.getItemStack(configPath) == item) {
            val message: String = config.getString("Messages.itemDefinedMessage").toString()
            val placeholder = mapOf(
                "item" to item.toString(),
            )
            sendMessage(message, placeholder)
        } else {
            val message: String = config.getString("Messages.itemNotFoundMessage").toString()
            sendMessage(message)
        }
    }

    fun retrieve(): ItemStack? {
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

    private fun sendMessage(message: String, placeholders: Map<String, String> = emptyMap(), log: Boolean = true) {
        // Convert the map to a list of TagResolvers
        val resolvers = placeholders.map { (key, value) ->
            Placeholder.parsed(key, value)
        }

        val resolver = TagResolver.resolver(*resolvers.toTypedArray()) // Create the final TagResolver

        val messageComponent = MiniMessage.miniMessage().deserialize(message, resolver) // Deserialize the message with the resolver

        if (log) // Log to console
            plugin.logger.info("$messageComponent")

        // Send to the player
        player.sendMessage(messageComponent)
    }

}