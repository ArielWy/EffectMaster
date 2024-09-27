package me.olios.plugins.effectmaster

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
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
            player.removePotionEffect(effectType)
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
                player.removePotionEffect(effectType)
                player.addPotionEffect(PotionEffect(effectType, -1, currentLevel - 2))
            }
        } else noEffectBan() // Ban the player if they don't have any effects
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

        println("effectNames: $effectNames")

        val effects = effectNames.mapNotNull { PotionEffectType.getByName(it) } // get them as a potion type (if exist)

        println("effects: $effects")

        if (effects.size != 4) { // if the potion effect list isn't 4 (some potions isn't right) warn the console and return
            potionEffectErrorMessage()
            return null
        }

        return effects
    }

    private fun potionEffectErrorMessage() {
        val message = config.getString("Messages.NoEffectsError")

        plugin.logger.severe(message)
    }

    private fun noEffectBan() {
        // Get the config values
        val configBanDate = config.getInt("General.BanTime")
        val configBanMessage = config.getString("Messages.BanMessage")

        val banExpires = LocalDateTime.now().plusDays(configBanDate.toLong()) // Get the ban expires date
        val instant = banExpires.atZone(ZoneId.systemDefault()).toInstant() // Convert LocalDateTime to Instant
        val date = Date.from(instant) // Convert Instant to java.util.Date

        // if toggled, broadcast to all the players
        if (config.getBoolean("General.NoEffectBroadcast")) {
            val banMessage: String = config.getString("Messages.BanMessage").toString()

            val playerNamePlaceholder = Placeholder.parsed("player", player.name)
            val resolvedMessage = MiniMessage.miniMessage().deserialize(banMessage, TagResolver.resolver(playerNamePlaceholder))
            Bukkit.broadcast(resolvedMessage)
        }

        applyInitialEffects() // set initial effect after ban

        // Ban the player
        player.banPlayer(configBanMessage, date)
    }
}