package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.panel;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PanelItem {

    private ItemStack item;
    private ClickEventListener clickListener;
    private ItemBuilder itemBuilder;


    public PanelItem(ItemStack item) {
        this.item = item;
        this.clickListener = (e, p) -> {};
        this.itemBuilder = (p) -> item;
    }

    public PanelItem(ItemBuilder builder, ClickEventListener click) {
        this.item = null;
        this.clickListener = click;
        this.itemBuilder = builder;
    }

    public ItemStack getItemStack() {
        return item;
    }

    public PanelItem setClickListener(ClickEventListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    public PanelItem setClickListener(Runnable onClickLeft) {
        this.clickListener = (event, player) -> {
            if (event.getClick().isLeftClick())
                onClickLeft.run();
        };
        return this;
    }

    public PanelItem setItemBuilder(ItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
        return this;
    }

    public ClickEventListener getClickListener() {
        return clickListener;
    }

    public ItemBuilder getItemBuilder() {
        return itemBuilder;
    }

    public PanelItem clone() {
        PanelItem item = new PanelItem(this.item.clone());
        item.setClickListener(clickListener);
        item.setItemBuilder(itemBuilder);
        return item;
    }



    public interface ClickEventListener {
        void click(InventoryClickEvent event, Player player);
    }

    public interface ItemBuilder {
        ItemStack build(Player player);
    }


    public static PanelItem createItem(Material material, String name) {
        return PanelItem.createItem(material, name, null, false);
    }


    public static PanelItem createItem(Material material, String name, @Nullable List<String> loreLines) {
        return PanelItem.createItem(material, name, loreLines, false);
    }

    public static PanelItem createItem(Material material, String name, @Nullable List<String> loreLines, boolean glowing) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (loreLines != null)
                meta.setLore(loreLines);
            if (glowing)
                meta.addEnchant(Enchantment.DURABILITY, 1, false);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return new PanelItem(item);
    }

    public static PanelItem createBlankItem() {
        return createItem(Material.BLACK_STAINED_GLASS_PANE, ChatColor.RESET.toString());
    }


}
