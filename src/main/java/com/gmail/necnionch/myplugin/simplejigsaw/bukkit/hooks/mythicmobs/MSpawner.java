package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.mythicmobs;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.google.common.collect.Lists;
import io.lumine.mythic.bukkit.utils.numbers.RandomInt;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class MSpawner {

    private final String name;
    private final String mobName;
    private final String level;

    public MSpawner(String name, String mobName, String level) {
        this.name = name;
        this.mobName = mobName;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public String getMobName() {
        return mobName;
    }

    public String getLevelString() {
        return level;
    }

    public RandomInt getLevel() {
        return new RandomInt(level);
    }


    public void setPersistentData(PersistentDataContainer data) {
        data.set(makeKey("name"), PersistentDataType.STRING, name);
        data.set(makeKey("mobName"), PersistentDataType.STRING, mobName);
        data.set(makeKey("level"), PersistentDataType.STRING, level);
    }

    public ItemStack createBlockItem() {
        ItemStack itemStack = new ItemStack(Material.SPAWNER);
        ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());

        itemMeta.setDisplayName(ChatColor.DARK_AQUA + "MythicMobs " + name + "スポナー 配置ブロック");
        itemMeta.setLore(Lists.newArrayList(
                ChatColor.WHITE + "CustomStructuresの建造物として生成された時に、",
                ChatColor.WHITE + "このブロックはMythicMobsスポナーとして配置されます。",
                "",
                ChatColor.YELLOW + "設定内容:",
                ChatColor.GRAY + "  スポナー名: " + ChatColor.WHITE + name,
                ChatColor.GRAY + "  Mob名: " + ChatColor.WHITE + mobName,
                ChatColor.GRAY + "  Mobレベル: " + ChatColor.WHITE + level
        ));

        PersistentDataContainer data = itemMeta.getPersistentDataContainer();
        setPersistentData(data);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static MSpawner fromPersistentData(PersistentDataContainer data) {
        String name = data.get(makeKey("name"), PersistentDataType.STRING);
        String mobName = data.get(makeKey("mobName"), PersistentDataType.STRING);
        String level = data.get(makeKey("level"), PersistentDataType.STRING);

        if (name == null)
            return null;

        return new MSpawner(name, mobName, level);
    }

    public static MSpawner fromBlockItem(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return null;

        PersistentDataContainer data = itemMeta.getPersistentDataContainer();
        return fromPersistentData(data);
    }


    private static NamespacedKey makeKey(String key) {
        return new NamespacedKey(JavaPlugin.getPlugin(SimpleJigsawPlugin.class), key);
    }

}
