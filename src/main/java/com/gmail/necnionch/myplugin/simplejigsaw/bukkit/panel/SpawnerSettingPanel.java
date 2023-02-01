package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.panel;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.mythicmobs.MSpawner;
import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class SpawnerSettingPanel extends Panel implements Listener {

    private final SimpleJigsawPlugin plugin = JavaPlugin.getPlugin(SimpleJigsawPlugin.class);
    private final MSpawner setting;
    private final CreatureSpawner spawner;
    private boolean inspectReplaceBlock;

    public SpawnerSettingPanel(Player player, MSpawner setting, CreatureSpawner spawner) {
        super(player, 54, "スポナー設定", new ItemStack(Material.AIR));
        this.setting = setting;
        this.spawner = spawner;
    }

    @Override
    public PanelItem[] build() {
        PanelItem[] slots = new PanelItem[getSize()];

        Material replaceType = setting.getReplaceBlockOrAir().getMaterial();
        if (!replaceType.isItem() || replaceType.isAir())
            replaceType = Material.STONE;

        List<String> lines = Lists.newArrayList(
                ChatColor.GRAY + "現在の設定: " + ChatColor.WHITE + setting.getReplaceBlockOrAir().getAsString(true),
                ChatColor.GRAY + "(注) 現在 ブロックの向きやNBTは保持できません"
        );

        slots[22] = PanelItem.createItem(replaceType, ChatColor.GOLD + "置換ブロックの変更", lines)
                .setClickListener(this::startInspectReplaceBlock);

        slots[25] = PanelItem.createBlankItem()
                .setItemBuilder((p) -> setting.createBlockItem())
                .setClickListener((e, p) -> {
                    e.getView().setCursor(setting.createBlockItem());
                });

        return slots;
    }

    private void startInspectReplaceBlock() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        inspectReplaceBlock = true;

        getPlayer().sendBlockChange(spawner.getLocation(), Material.AIR.createBlockData());
        showBlockBox(spawner.getLocation());
        getPlayer().sendMessage(ChatColor.YELLOW + "スポナーの位置に置換ブロックを配置してください");
        getPlayer().sendMessage(ChatColor.GRAY + "(空気ブロックに設定したい場合はスニークしてください)");
        destroy(true);
    }

    private void stopInspectReplaceBlock(@Nullable BlockData blockData) {
        inspectReplaceBlock = false;
        HandlerList.unregisterAll(this);

        if (blockData != null) {
            setting.setReplaceBlock(blockData);
            setting.setPersistentData(spawner.getPersistentDataContainer());
            spawner.update();
            getPlayer().sendMessage(ChatColor.GREEN + "設定を更新しました");
        }

        if (getPlayer().isOnline()) {
            open();
            getPlayer().sendBlockChange(spawner.getLocation(), spawner.getLocation().getBlock().getBlockData());
        }
    }

    private void showBlockBox(Location location) {
        Player player = getPlayer();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        Particle.DustOptions dust = new org.bukkit.Particle.DustOptions(Color.AQUA, .2f);
        int count = 6;

        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
            if (!inspectReplaceBlock) {
                task.cancel();
                return;
            }
            for (int i = 0; i < count; i++) {
                float w = 1f / count * i;
                player.spawnParticle(Particle.REDSTONE, x + w, y, z, 1, 0, 0, 0, 0, dust);
                player.spawnParticle(Particle.REDSTONE, x + w, y + 1, z, 1, 0, 0, 0, 0, dust);
                player.spawnParticle(Particle.REDSTONE, x + w, y + 1, z + 1, 1, 0, 0, 0, 0, dust);
                player.spawnParticle(Particle.REDSTONE, x + w, y, z + 1, 1, 0, 0, 0, 0, dust);
                player.spawnParticle(Particle.REDSTONE, x, y, z + w, 1, 0, 0, 0, 0, dust);
                player.spawnParticle(Particle.REDSTONE, x, y + 1, z + w, 1, 0, 0, 0, 0, dust);
                player.spawnParticle(Particle.REDSTONE, x + 1, y, z + w, 1, 0, 0, 0, 0, dust);
                player.spawnParticle(Particle.REDSTONE, x + 1, y + 1, z + w, 1, 0, 0, 0, 0, dust);
                player.spawnParticle(Particle.REDSTONE, x, y + w, z, 1, 0, 0, 0, 0, dust);
                player.spawnParticle(Particle.REDSTONE, x + 1, y + w, z, 1, 0, 0, 0, 0, dust);
                player.spawnParticle(Particle.REDSTONE, x, y + w, z + 1, 1, 0, 0, 0, 0, dust);
                player.spawnParticle(Particle.REDSTONE, x + 1, y + w, z + 1, 1, 0, 0, 0, 0, dust);
            }
        }, 0, 1);
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        if (!getPlayer().equals(event.getPlayer()) || !Action.RIGHT_CLICK_BLOCK.equals(event.getAction()))
            return;
        Block block = event.getClickedBlock();
        if (block == null)
            return;

        Location location = block.getLocation().add(event.getBlockFace().getDirection());
        if (!spawner.getLocation().equals(location))
            return;

        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);
        stopInspectReplaceBlock(Optional.ofNullable(event.getItem())
                .map(ItemStack::getType)
                .orElse(Material.AIR)
                .createBlockData());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!getPlayer().equals(event.getPlayer()))
            return;
        if (event.isSneaking()) {
            event.setCancelled(true);
            stopInspectReplaceBlock(Material.AIR.createBlockData());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDestroy(BlockBreakEvent event) {
        if (!spawner.getLocation().equals(event.getBlock().getLocation()))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (getPlayer().equals(event.getPlayer())) {
            stopInspectReplaceBlock(null);
        }
    }

}
