package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw;

import com.sk89q.worldedit.math.BlockVector3;
import org.jetbrains.annotations.NotNull;

public class JigsawConnector {
    private final @NotNull JigsawPart jigsawPart;
    private final @NotNull BlockVector3 location;
    private final @NotNull String pool;
    private final @NotNull String name;
    private final @NotNull String targetName;
    private final @NotNull String finalBlockState;
    private final @NotNull JointType jointType;
    private final @NotNull Orientation orientation;

    JigsawConnector(@NotNull JigsawPart jigsawPart,
                    @NotNull BlockVector3 location,
                    @NotNull String pool,
                    @NotNull String name,
                    @NotNull String targetName,
                    @NotNull String finalBlockState,
                    @NotNull JointType jointType,
                    @NotNull Orientation orientation) {
        this.jigsawPart = jigsawPart;
        this.pool = pool;
        this.name = name;
        this.targetName = targetName;
        this.finalBlockState = finalBlockState;
        this.jointType = jointType;
        this.orientation = orientation;
        this.location = location;
    }

    public @NotNull JigsawPart getJigsawPart() {
        return jigsawPart;
    }

    public @NotNull BlockVector3 getRelativeLocation() {
        return jigsawPart.toRelativeLocation(location);
    }

    public @NotNull BlockVector3 getOriginalLocation() {
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

    public enum Orientation {  // EAST(x)-90 <=> WEST90 | NORTH180 <=> SOUTH(z)0
        DOWN_EAST(0, 0, -90), DOWN_NORTH(0, 0, 180), DOWN_SOUTH(0, 0, 0), DOWN_WEST(0, 0, 90),
        UP_EAST(0, 0, -90), UP_NORTH(0, 0, 180), UP_SOUTH(0, 0, 0), UP_WEST(0, 0, 90),
        WEST_UP(-1, 0, 90), EAST_UP(1, 0, -90), NORTH_UP(0, -1, 180), SOUTH_UP(0, 1, 0);


        private final int x;
        private final int z;
        private final int angle;

        Orientation(int x, int z, int angle) {
            this.x = x;
            this.z = z;
            this.angle = angle;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        public int getAngle() {
            return angle;
        }

        public int rotateAngleTo(Orientation targetOrientation) {
            if (!isHorizontal() || !targetOrientation.isHorizontal() || targetOrientation.angle == angle)
                return 0;

            float yaw = (angle - targetOrientation.angle) % 360F;
            int dir = Math.round(yaw / 90F) % 4;
            return dir * 90;
        }// TODO

        public boolean isHorizontal() {
            return x != 0 || z != 0;
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

        public String toString() {
            return name() + "(" + x + "," + z + ")";
        }

    }

}
