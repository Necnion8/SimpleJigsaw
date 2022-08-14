package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util;

import com.sk89q.worldedit.math.BlockVector3;

public class bUtils {

    public static BlockVector3 toBlockVector3(org.bukkit.Location location) {
        return BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

}
