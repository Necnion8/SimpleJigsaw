package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util;

import com.sk89q.worldedit.math.BlockVector3;

public class bUtils {

    public static BlockVector3 toBlockVector3(org.bukkit.Location location) {
        return BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static BlockVector3 rotate90(int angle, BlockVector3 vector) {
        angle = Math.round(angle / 90f) & 0x3;
        if (angle == 1) {  // 90
            return BlockVector3.at(-vector.getBlockZ(), vector.getBlockY(), vector.getBlockX());
        } else if (angle == 2) {  // 180
            return BlockVector3.at(-vector.getBlockX(), vector.getBlockY(), -vector.getBlockZ());
        } else if (angle == 3) {  // 270
            return BlockVector3.at(vector.getBlockZ(), vector.getBlockY(), -vector.getBlockX());
        }
        return vector;
    }

    public static BlockVector3 rotate90(int angle, BlockVector3 vector, BlockVector3 center) {
        BlockVector3 base = vector.subtract(center);

        return rotate90(angle, base); // .add(center);

//        angle = Math.round(angle / 90f) & 0x3;
//        if (angle == 1) {  // 90
//            return BlockVector3.at(-vector.getBlockZ(), vector.getBlockY(), vector.getBlockX());
//        } else if (angle == 2) {  // 180
//            return BlockVector3.at(-vector.getBlockX(), vector.getBlockY(), -vector.getBlockZ());
//        } else if (angle == 3) {  // 270
//            return BlockVector3.at(vector.getBlockZ(), vector.getBlockY(), -vector.getBlockX());
//        }
//        return vector;
    }

}
