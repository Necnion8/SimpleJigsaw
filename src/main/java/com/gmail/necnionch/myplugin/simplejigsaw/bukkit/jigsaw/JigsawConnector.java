package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw;

import com.sk89q.worldedit.math.BlockVector3;
import org.jetbrains.annotations.NotNull;

public class JigsawConnector {
    private final @NotNull BlockVector3 location;
    private final @NotNull String pool;
    private final @NotNull String name;
    private final @NotNull String targetName;
    private final @NotNull String finalBlockState;
    private final @NotNull JointType jointType;
    private final @NotNull Orientation orientation;

    JigsawConnector(@NotNull BlockVector3 location,
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
        this.location = location;
    }

    public @NotNull BlockVector3 getLocation() {
        return location;
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


    public enum JointType {
        ROLLABLE, ALIGNED,
        UNKNOWN,
    }

    public enum Orientation {  // EAST(x) <=> WEST | NORTH <=> SOUTH(z)
        DOWN_EAST(0, 0), DOWN_NORTH(0, 0), DOWN_SOUTH(0, 0), DOWN_WEST(0, 0),
        UP_EAST(0, 0), UP_NORTH(0, 0), UP_SOUTH(0, 0), UP_WEST(0, 0),
        WEST_UP(-1, 0), EAST_UP(1, 0), NORTH_UP(0, -1), SOUTH_UP(0, 1);


        private final int x;
        private final int z;

        Orientation(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        public Orientation getOpposite() {
            switch (this) {
                case DOWN_EAST -> {
                    return UP_EAST;
                }
                case DOWN_NORTH -> {
                    return UP_NORTH;
                }
                case DOWN_SOUTH -> {
                    return UP_SOUTH;
                }
                case DOWN_WEST -> {
                    return UP_WEST;
                }
                case UP_EAST -> {
                    return DOWN_EAST;
                }
                case UP_NORTH -> {
                    return DOWN_NORTH;
                }
                case UP_SOUTH -> {
                    return DOWN_SOUTH;
                }
                case UP_WEST -> {
                    return DOWN_WEST;
                }
                case WEST_UP -> {
                    return EAST_UP;
                }
                case EAST_UP -> {
                    return WEST_UP;
                }
                case NORTH_UP -> {
                    return SOUTH_UP;
                }
                case SOUTH_UP -> {
                    return NORTH_UP;
                }
            }
            throw new IllegalArgumentException("program error");
        }

    }

}
