package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.sk89q.jnbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public record JigsawParameters(
        @NotNull String pool,
        @NotNull String name,
        @NotNull String targetName,
        @NotNull String finalBlockState,
        @NotNull JigsawJointType jointType
) {

//    public @NotNull String getPool() {
//        return pool;
//    }
//
//    public @NotNull String getName() {
//        return name;
//    }
//
//    public @NotNull String getTargetName() {
//        return targetName;
//    }
//
//    public @NotNull String getFinalBlockState() {
//        return finalBlockState;
//    }
//
//    public @NotNull JigsawJointType getJointType() {
//        return jointType;
//    }


    public static JigsawParameters fromNBT(CompoundTag nbt) {
        return SimpleJigsawPlugin.getWorldEdit().getJigsawParametersByNBT(nbt);
    }

}
