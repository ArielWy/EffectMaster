package me.olios.plugins.effectmaster

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class EffectHandler(private val plugin: EffectMaster, private val player: Player) {
    private val config = plugin.config
    private val dataContainer = player.persistentDataContainer

    fun applyInitialEffects() {
        val effects: List<PotionEffectType> = getEffects() ?: return

        effects.forEachIndexed { index, effectType ->
            player.addPotionEffect(PotionEffect(effectType, Int.MAX_VALUE, 1, true))
            saveEffectLevel(index + 1, 1)
        }
    }

    fun reloadEffects() {
        val effects: List<PotionEffectType> = getEffects() ?: return

        effects.forEachIndexed { index, effectType ->
            val level = loadEffectLevel(index + 1)
            player.addPotionEffect(PotionEffect(effectType, Int.MAX_VALUE, level, true))
            saveEffectLevel(index + 1, 1)
        }
    }

    private fun saveEffectLevel(effectId: Int, level: Int) {
        val key = NamespacedKey(plugin, "effect_$effectId")
        dataContainer.set(key, PersistentDataType.INTEGER, level)
    }

    private fun loadEffectLevel(effectId: Int): Int {
        val key = NamespacedKey(plugin, "effect_$effectId")
        return dataContainer.get(key, PersistentDataType.INTEGER) ?: 1 // Default to 1 if not found
    }

    private fun getEffects():  List<PotionEffectType>? {
        val effectNames = config.getStringList("Effects") // get all the effects from the config
        val effects = effectNames.mapNotNull { PotionEffectType.getByName(it) } // get them as a potion type (if exist)

        if (effects.size != 4) { // if the potion effect list isn't 4 (some potions isn't right) warn the console and return
            potionEffectErrorMessage()
            return null
        }

        return effects
    }

    private fun potionEffectErrorMessage() {
        val message = config.getString("Messages.NoEffects")

        plugin.logger.warning(message)
    }
}