package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.nms.NMSHandler;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

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

}
