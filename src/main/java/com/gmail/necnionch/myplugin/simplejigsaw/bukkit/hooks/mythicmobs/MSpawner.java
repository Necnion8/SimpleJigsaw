package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.mythicmobs;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.google.common.collect.Lists;
import io.lumine.mythic.bukkit.utils.numbers.RandomInt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class MSpawner {

    private final String name;
    private final String mobName;
    private final String level;
    private @Nullable BlockData replaceBlock;

    public MSpawner(String name, String mobName, String level, @Nullable BlockData replaceBlock) {
        this.name = name;
        this.mobName = mobName;
        this.level = level;
        this.replaceBlock = replaceBlock;
    }

    public MSpawner(String name, String mobName, String level) {
        this(name, mobName, level, null);
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

    public BlockData getReplaceBlockOrAir() {
        return getReplaceBlock().orElseGet(Material.AIR::createBlockData);
    }

    public Optional<BlockData> getReplaceBlock() {
        return Optional.ofNullable(replaceBlock);
    }

    public void setReplaceBlock(@Nullable BlockData replaceBlock) {
        this.replaceBlock = replaceBlock;
    }

    public void setPersistentData(PersistentDataContainer data) {
        data.set(makeKey("name"), PersistentDataType.STRING, name);
        data.set(makeKey("mobName"), PersistentDataType.STRING, mobName);
        data.set(makeKey("level"), PersistentDataType.STRING, level);

        if (replaceBlock != null) {
            data.set(makeKey("replaceBlock"), PersistentDataType.STRING, replaceBlock.getAsString(true));
        }
    }

    public ItemStack createBlockItem() {
        ItemStack itemStack = new ItemStack(Material.SPAWNER);
        ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());

        itemMeta.setDisplayName(ChatColor.DARK_AQUA + "MythicMobs " + name + "スポナー 配置ブロック");
        itemMeta.setLore(Lists.newArrayList(
                ChatColor.WHITE + "建造物として生成された時に、",
                ChatColor.WHITE + "このブロックはMythicMobsスポナーとして配置されます。",
                "",
                ChatColor.YELLOW + "設定内容:",
                ChatColor.GRAY + "  スポナー名: " + ChatColor.WHITE + name,
                ChatColor.GRAY + "  Mob名: " + ChatColor.WHITE + mobName,
                ChatColor.GRAY + "  Mobレベル: " + ChatColor.WHITE + level,
                ChatColor.GRAY + "  置換先ブロック: " + ChatColor.WHITE + getReplaceBlockOrAir().getAsString(true)
        ));

        PersistentDataContainer data = itemMeta.getPersistentDataContainer();
        setPersistentData(data);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static MSpawner fromPersistentData(PersistentDataContainer data) {
        return fromPersistentData(data, true);
    }

    public static MSpawner fromPersistentData(PersistentDataContainer data, boolean ignoreErrors) throws IllegalArgumentException {
        String name = data.get(makeKey("name"), PersistentDataType.STRING);
        String mobName = data.get(makeKey("mobName"), PersistentDataType.STRING);
        String level = data.get(makeKey("level"), PersistentDataType.STRING);
        String replaceBlockString = data.get(makeKey("replaceBlock"), PersistentDataType.STRING);

        if (name == null)
            return null;

        BlockData replaceBlock = null;
        if (replaceBlockString != null && !replaceBlockString.isEmpty()) {
            try {
                replaceBlock = Bukkit.createBlockData(replaceBlockString);
            } catch (IllegalArgumentException e) {
                if (!ignoreErrors)
                    throw e;
            }
        }

        return new MSpawner(name, mobName, level, replaceBlock);
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
