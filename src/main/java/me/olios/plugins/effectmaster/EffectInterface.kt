package me.olios.plugins.effectmaster

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class EffectInterface(private val plugin: EffectMaster) {
    private val config = plugin.config

    fun openGUI(player: Player) {
        val inventory = Bukkit.createInventory(null, 18, "Effect Level Up")

        EffectMaster.playerInventory[player.uniqueId] = inventory

        // Create border item
        val borderItem = createItem(Material.WHITE_STAINED_GLASS_PANE, "")

        // Create info and effect items
        val infoItem = configItemStack("Items.InfoItem.material", "Items.InfoItem.name", "Items.InfoItem.lore")
        val effectItems = listOf(
            configItemStack("Items.Effect1.material", "Items.Effect1.name", "Items.Effect1.lore"),
            configItemStack("Items.Effect2.material", "Items.Effect2.name", "Items.Effect2.lore"),
            configItemStack("Items.Effect3.material", "Items.Effect3.name", "Items.Effect3.lore"),
            configItemStack("Items.Effect4.material", "Items.Effect4.name", "Items.Effect4.lore")
        )

        // Set items in inventory
        inventory.setItem(0, infoItem)
        effectItems.forEachIndexed { index, item -> inventory.setItem(2 + index, item) }
        for (i in listOf(1, 6, 7, 8)) {
            inventory.setItem(i, borderItem)
        }

        player.openInventory(inventory) // Open the GUI
    }

    private fun createItem(material: Material, displayName: String): ItemStack {
        val itemStack = ItemStack(material)
        val itemMeta = itemStack.itemMeta
        itemMeta.displayName(Component.text(displayName))
        itemStack.itemMeta = itemMeta
        return itemStack
    }

    private fun configItemStack(materialPath: String, namePath: String, lorePath: String): ItemStack {
        val material = Material.getMaterial(config.getString(materialPath).toString()) ?: return ItemStack(Material.BARRIER)
        val itemStack = ItemStack(material)
        val itemMeta: ItemMeta = itemStack.itemMeta

        val displayName: String = config.getString(namePath).toString()
        val lore = config.getList(lorePath)



        // deserialize the displayName and lore using MiniMessage
        val displayNameComponent = MiniMessage.miniMessage().deserialize(displayName)

        val loreComponent = lore?.map { line ->
            MiniMessage.miniMessage().deserialize(line as String)
        } ?: listOf(Component.text(""))

        itemMeta.displayName(displayNameComponent)
        itemMeta.lore(loreComponent)

        itemStack.itemMeta = itemMeta

        return itemStack
    }


}