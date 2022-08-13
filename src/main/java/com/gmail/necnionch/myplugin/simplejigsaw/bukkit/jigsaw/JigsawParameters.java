package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.jetbrains.annotations.NotNull;

public class JigsawParameters {

    private @NotNull final String pool;
    private @NotNull final String name;
    private @NotNull final String targetName;
    private @NotNull final String finalBlockState;
    private final @NotNull JointType jointType;
    private final @NotNull Orientation orientation;

    public JigsawParameters(
            @NotNull String pool,
            @NotNull String name,
            @NotNull String targetName,
            @NotNull String finalBlockState,
            @NotNull JointType jointType,
            @NotNull Orientation orientation) {
        this.pool = pool;
        this.name = name;
        this.targetName = targetName;
        this.finalBlockState = finalBlockState;
        this.jointType = jointType;
        this.orientation = orientation;
    }

    public @NotNull String getPool() {
        return pool;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getTargetName() {
        return targetName;
    }

    public @NotNull String getFinalBlockState() {
        return finalBlockState;
    }

    public @NotNull JointType getJointType() {
        return jointType;
    }

    public @NotNull Orientation getOrientation() {
        return orientation;
    }


    public static JigsawParameters fromBaseBlock(BaseBlock baseBlock) {
        return SimpleJigsawPlugin.getWorldEdit().getJigsawParametersByBaseBlock(baseBlock);
    }


    public enum JointType {
        ROLLABLE, ALIGNED,
        UNKNOWN,
    }

    public enum Orientation {
        DOWN_EAST, DOWN_NORTH, DOWN_SOUTH, DOWN_WEST,
        UP_EAST, UP_NORTH, UP_SOUTH, UP_WEST,
        WEST_UP, EAST_UP, NORTH_UP, SOUTH_UP,
    }


}
