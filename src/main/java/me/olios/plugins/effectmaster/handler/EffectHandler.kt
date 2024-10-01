package me.olios.plugins.effectmaster.handler

import me.olios.plugins.effectmaster.EffectMaster
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.random.Random

class EffectHandler(private val plugin: EffectMaster, private val player: Player) {
    private val config = plugin.config
    private val dataContainer = player.persistentDataContainer

    fun applyInitialEffects() {
        val effects: List<PotionEffectType> = getEffects() ?: return

        effects.forEachIndexed { index, effectType ->
            player.addPotionEffect(PotionEffect(effectType, -1, 0, true))
            saveEffectLevel(index + 1, 1)
        }
    }

    fun reloadEffects() {
        val effects: List<PotionEffectType> = getEffects() ?: return

        effects.forEachIndexed { index, effectType ->
            val level: Int = loadEffectLevel(index + 1) ?: return
            removeEffect(effectType)
            if (level > 0) player.addPotionEffect(PotionEffect(effectType, -1, level - 1, true))
        }
    }

    fun decreaseRandomEffect() {
        val effects: List<PotionEffectType> = getEffects() ?: return

        // Filter effects with level greater than 0
        val eligibleEffects = effects.filterIndexed { index, _ ->
            val level = loadEffectLevel(index + 1) ?: return
            level > 0
        }

        // player.sendMessage("effect list: $eligibleEffects")

        // Choose a random eligible effect
        if (eligibleEffects.isNotEmpty()) {
            val randomIndex = Random.nextInt(eligibleEffects.size)
            val effectType = eligibleEffects[randomIndex]

            // Find the correct index for the chosen effect
            val effectIndex = effects.indexOf(effectType) + 1

            // Decrease the level
            val currentLevel: Int = loadEffectLevel(effectIndex) ?: return
            if (currentLevel > 0) {
                saveEffectLevel(effectIndex, currentLevel - 1)
                removeEffect(effectType)
                player.addPotionEffect(PotionEffect(effectType, -1, currentLevel - 2))
            }
        } else noEffectBan() // Ban the player if they don't have any effects
    }

    fun effectInteraction(event: InventoryClickEvent, itemStack: ItemStack) {
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
            EffectHandler(plugin, player).increaseEffect(index + 1)
        }
    }

    private fun increaseEffect(effectIndex: Int) {
        val effects: List<PotionEffectType> = getEffects() ?: return

        val effectType = effects[effectIndex - 1]

        // Load the current level and the config level boundaries
        val currentLevel: Int = loadEffectLevel(effectIndex) ?: 0
        val levelRequirement: Int = config.getInt("General.levelRequirement").takeIf { it != 0 } ?: 2
        val maxEffectLevel: Int = config.getInt("General.maxEffectLevel").takeIf { it != 0 } ?: 5

        // Check if the effect can be upgraded
        if (currentLevel >= maxEffectLevel) {
            maxEffectLevel(maxEffectLevel)
            return
        }

        // Check if all effects meet the level requirement
        val allEffectsMeetRequirement = effects.all { effect ->
            val effectLevel = loadEffectLevel(effects.indexOf(effect) + 1) ?: 1
            effectLevel >= levelRequirement
        }

        if (currentLevel >= levelRequirement && !allEffectsMeetRequirement) {
            levelRequirement(levelRequirement)
            return
        }

        // Upgrade the effect level
        saveEffectLevel(effectIndex, currentLevel + 1)
        removeEffect(effectType)
        player.addPotionEffect(PotionEffect(effectType, -1, currentLevel))

        // Set the main hand slot to air
        val airItem = ItemStack(Material.AIR)
        player.inventory.setItemInMainHand(airItem)

        // close the inventory
        player.inventory.close()

        // remove inv from the companion object
        EffectMaster.playerInventory.remove(player.uniqueId)

        player.sendMessage("Increased effect: ${effects[effectIndex - 1].name}") //send message to the player
    }

    fun saveEffectLevel(effectId: Int, level: Int) {
        val key = NamespacedKey(plugin, "effect_$effectId")
        dataContainer.set(key, PersistentDataType.INTEGER, level)
    }

    private fun loadEffectLevel(effectId: Int): Int? {
        val key = NamespacedKey(plugin, "effect_$effectId")

        if (!dataContainer.has(key)) {
            applyInitialEffects()
            return null
        }

        return dataContainer.get(key, PersistentDataType.INTEGER) ?: 1 // Default to 1 if not found
    }

    fun getEffects():  List<PotionEffectType>? {
        val effectsSection = config.getConfigurationSection("Effects") // get all the effects from the config
        val effectNames = effectsSection?.getKeys(false)?.mapNotNull { key -> effectsSection.getString(key) } ?: emptyList()

        val effects = effectNames.mapNotNull { PotionEffectType.getByName(it) } // get them as a potion type (if exist)

        if (effects.size != 4) { // if the potion effect list isn't 4 (some potions isn't right) warn the console and return
            potionEffectErrorMessage()
            return null
        }

        return effects
    }

    private fun potionEffectErrorMessage() {
        val message = config.getString("Messages.noEffectsError")

        plugin.logger.severe(message)
    }

    private fun noEffectBan() {
        // Get the config values
        val configBanDate = config.getInt("General.banTime")
        val configBanMessage = config.getString("Messages.banMessage")

        val banExpires = LocalDateTime.now().plusDays(configBanDate.toLong()) // Get the ban expires date
        val instant = banExpires.atZone(ZoneId.systemDefault()).toInstant() // Convert LocalDateTime to Instant
        val date = Date.from(instant) // Convert Instant to java.util.Date

        // if toggled, broadcast to all the players
        if (config.getBoolean("General.banBroadcast")) {
            val banMessage: String = config.getString("Messages.broadcastBanMessage").toString()

            val playerNamePlaceholder = Placeholder.parsed("player", player.name)
            val resolvedMessage = MiniMessage.miniMessage().deserialize(banMessage, TagResolver.resolver(playerNamePlaceholder))
            Bukkit.broadcast(resolvedMessage)
        }

        applyInitialEffects() // set initial effect after ban

        // Ban the player
        player.banPlayer(configBanMessage, date)
    }

    private fun removeEffect(effectType: PotionEffectType) {
        // allow the plugin to remove the effect
        player.setMetadata("allowEffectRemoval", FixedMetadataValue(plugin, true))

        // remove the effect
        player.removePotionEffect(effectType)
    }

    private fun levelRequirement(level: Int) {
        val message: String = config.getString("Messages.levelRequirementMessage").toString()
        val placeholder = mapOf(
            "level" to level.toString(),
        )

        player.inventory.close()
        sendMessage(message, placeholder)
    }

    private fun maxEffectLevel(maxLevel: Int) {
        val message: String = config.getString("Messages.maxEffectLevelMessage").toString()
        val placeholder = mapOf(
            "max_level" to maxLevel.toString(),
        )

        player.inventory.close()
        sendMessage(message, placeholder)
    }

    private fun sendMessage(message: String, placeholders: Map<String, String> = emptyMap(), log: Boolean = true) {
        // Convert the map to a list of TagResolvers
        val resolvers = placeholders.map { (key, value) ->
            Placeholder.parsed(key, value)
        }

        val resolver = TagResolver.resolver(*resolvers.toTypedArray()) // Create the final TagResolver

        val messageComponent = MiniMessage.miniMessage().deserialize(message, resolver) // Deserialize the message with the resolver

        if (log) { // Log to console if needed
            val plainTextMessage: String = PlainTextComponentSerializer.plainText().serialize(messageComponent)
            plugin.logger.info(plainTextMessage)
        }

        // Send to the player
        player.sendMessage(messageComponent)
    }
}