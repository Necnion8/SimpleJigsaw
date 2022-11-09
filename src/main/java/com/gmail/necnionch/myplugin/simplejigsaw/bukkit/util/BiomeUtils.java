package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.nms.NMSHandler;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BiomeUtils {

    public static @NotNull String getBiomeKeyByBlock(Block block) {
        if (NMSHandler.isAvailable()) {
            String biomeKey = NMSHandler.getNMS().getBiomeKeyByBlock(block);
            if (biomeKey != null) {
                return biomeKey;
            }
        }
        return block.getBiome().getKey().toString();
    }

    public static @Nullable String getBiomeKeyByPositionNMS(World world, int x, int y, int z) {
        if (NMSHandler.isAvailable()) {
            return NMSHandler.getNMS().getBiomeKeyByPosition(world, x, y, z);
        }
        return null;
    }

}
