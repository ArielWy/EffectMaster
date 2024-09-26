package me.olios.plugins.effectmaster.commands

import me.olios.plugins.effectmaster.EffectHandler
import me.olios.plugins.effectmaster.EffectMaster
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class EffectControlCommand(private val plugin: EffectMaster): CommandExecutor, TabCompleter {


    override fun onCommand(sender: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        val command = p3?.getOrElse(0) { return false }?.lowercase()
        if (sender !is Player) return false

        val player: Player = if (p3 != null && p3.size > 2) Bukkit.getPlayer(p3[2]) ?: sender else sender

        val effectLevel: Int = p3?.get(1)?.toInt() ?: 1

        when (command) {
            "all" -> {
                EffectHandler(plugin, player).saveEffectLevel(1, effectLevel)
                EffectHandler(plugin, player).saveEffectLevel(2, effectLevel)
                EffectHandler(plugin, player).saveEffectLevel(3, effectLevel)
                EffectHandler(plugin, player).saveEffectLevel(4, effectLevel)
            }
            "1" -> EffectHandler(plugin, player).saveEffectLevel(1, effectLevel)
            "2" -> EffectHandler(plugin, player).saveEffectLevel(2, effectLevel)
            "3" -> EffectHandler(plugin, player).saveEffectLevel(3, effectLevel)
            "4" -> EffectHandler(plugin, player).saveEffectLevel(4, effectLevel)
            else -> {}
        }

        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?
    ): MutableList<String>? {
        if (p1.name.equals("effectcontrol", ignoreCase = true)) {
            if (p3!!.size == 1) {
                return mutableListOf("all", "1", "2", "3", "4")
            }
            else (p3.size == 2)
                return mutableListOf("1", "2", "3", "4", "5")
        }

        return null
    }
}