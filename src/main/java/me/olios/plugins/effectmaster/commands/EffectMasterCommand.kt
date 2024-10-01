package me.olios.plugins.effectmaster.commands

import me.olios.plugins.effectmaster.DefineItem
import me.olios.plugins.effectmaster.handler.EffectHandler
import me.olios.plugins.effectmaster.EffectMaster
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class EffectMasterCommand(private val plugin: EffectMaster): CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        val command = p3?.getOrElse(0) { return false }?.lowercase()
        if (sender !is Player) return false

        val effects = EffectHandler(plugin, sender).getEffects()
        val effectMap = effects?.mapIndexed { index, effect -> effect.name.lowercase() to index + 1 }?.toMap()

        val player: Player = if (p3 != null && p3.size > 3) Bukkit.getPlayer(p3[3]) ?: sender else sender
        val effectLevel: Int = if (p3 != null && p3.size > 2) p3[2].toInt() else 1

        when (command) {
            "setlevel" -> {
                val effectName = p3[1].lowercase()
                if (effectName == "all") {
                    effects?.forEachIndexed { index, _ ->
                        EffectHandler(plugin, player).saveEffectLevel(index + 1, effectLevel)
                        EffectHandler(plugin, player).reloadEffects()
                        sender.sendMessage("effect: $effectName, level: $effectLevel")
                    }
                } else {
                    val effectIndex = effectMap?.get(effectName)
                    if (effectIndex != null) {
                        EffectHandler(plugin, player).saveEffectLevel(effectIndex, effectLevel)
                        EffectHandler(plugin, player).reloadEffects()
                        sender.sendMessage("effect: $effectName, level: $effectLevel")
                    } else {
                        sender.sendMessage("Invalid effect name: $effectName")
                    }
                }
            }
            "defineitem" -> DefineItem(player, plugin).checkForItem()

            "getitem" -> DefineItem(player, plugin).getItem()

            "check" -> {
                player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, true))
            }
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?
    ): MutableList<String>? {
        if (p1.name.equals("effectmaster", ignoreCase = true)) {
            if (p3!!.size == 1) {
                return mutableListOf("setlevel", "defineitem", "getitem")
            } else if (p3.size == 2 && p3[0].equals("setlevel", ignoreCase = true)) {
                if (sender !is Player) return null
                val effects = EffectHandler(plugin, sender).getEffects()
                val effectsNames = effects?.map { it.name.lowercase() }?.toMutableList()
                effectsNames?.add("all")
                return effectsNames
            }
        }
        return null
    }

}