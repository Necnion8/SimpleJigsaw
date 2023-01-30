package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.listeners;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.mythicmobs.MSpawner;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

public class BlockListener implements Listener {

    private final SimpleJigsawPlugin plugin;

    public BlockListener(SimpleJigsawPlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        ItemStack itemStack = event.getItemInHand();

        if (!(block.getState() instanceof CreatureSpawner blockSpawner))
            return;

        if (!Material.SPAWNER.equals(itemStack.getType()))
            return;

        MSpawner spawner = MSpawner.fromBlockItem(itemStack);
        if (spawner == null)
            return;

        PersistentDataContainer data = blockSpawner.getPersistentDataContainer();
        spawner.setPersistentData(data);

        blockSpawner.setSpawnedType(EntityType.BAT);
        blockSpawner.setSpawnCount(0);
        blockSpawner.update();

    }

}
