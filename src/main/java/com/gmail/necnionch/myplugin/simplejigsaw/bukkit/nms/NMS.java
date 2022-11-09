package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.nms;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public interface NMS {

    @Nullable String getBiomeKeyByBlock(Block block);

    @Nullable String getBiomeKeyByPosition(World world, int x, int y, int z);

}
