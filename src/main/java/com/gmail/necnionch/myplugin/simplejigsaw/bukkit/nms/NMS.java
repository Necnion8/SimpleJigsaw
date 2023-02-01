package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.nms;

import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface NMS {

    @Nullable String getBiomeKeyByBlock(Block block);

    @Nullable ItemStack createExplorerMap(World world, Location point, StructureType icon);

}
