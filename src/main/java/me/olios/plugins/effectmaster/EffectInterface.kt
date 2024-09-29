package me.olios.plugins.effectmaster

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
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

        // create border item
        val borderItem = ItemStack(Material.WHITE_STAINED_GLASS_PANE)
        val borderItemMeta = borderItem.itemMeta
        borderItemMeta.displayName(Component.text(""))

        // create info item
        val infoItem: ItemStack = configItemStack("Items.InfoItem.material", "Items.InfoItem.name", "Items.InfoItem.lore")

        // create effect items
        val effect1Item: ItemStack = configItemStack("Items.Effect1.material", "Items.Effect1.name", "Items.Effect1.lore")
        val effect2Item: ItemStack = configItemStack("Items.Effect2.material", "Items.Effect2.name", "Items.Effect2.lore")
        val effect3Item: ItemStack = configItemStack("Items.Effect3.material", "Items.Effect3.name", "Items.Effect3.lore")
        val effect4Item: ItemStack = configItemStack("Items.Effect4.material", "Items.Effect4.name", "Items.Effect4.lore")

        // set items
        inventory.setItem(0, infoItem)
        inventory.setItem(1, borderItem)
        inventory.setItem(2, effect1Item)
        inventory.setItem(3, effect2Item)
        inventory.setItem(4, effect3Item)
        inventory.setItem(5, effect4Item)
        inventory.setItem(6, borderItem)
        inventory.setItem(7, borderItem)
        inventory.setItem(8, borderItem)

        player.openInventory(inventory) // Open the GUI
    }

    private fun configItemStack(materialPath: String, namePath: String, lorePath: String): ItemStack {
        val material = Material.getMaterial(materialPath) ?: return ItemStack(Material.BARRIER)
        val itemStack = ItemStack(material)
        val itemMeta: ItemMeta = itemStack.itemMeta

        val displayName: String = config.getString(namePath).toString()
        val lore = config.getList(lorePath)

        val displayNameComponent = MiniMessage.miniMessage().deserialize(displayName)
        val loreComponent: List<Component> = lore?.map { MiniMessage.miniMessage().deserialize(it as String) } ?: listOf(Component.text(""))

        itemMeta.displayName(displayNameComponent)
        itemMeta.lore(loreComponent)

        itemStack.itemMeta = itemMeta

        return itemStack
    }


}