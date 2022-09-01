package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfig;
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

    public @NotNull StructureConfig.Structure getStructure() {
        return jigsawPart.getStructure();
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
        DOWN_EAST(0, -1, 0, -90), DOWN_NORTH(0, -1, 0, 180), DOWN_SOUTH(0, -1, 0, 0), DOWN_WEST(0, -1, 0, 90),
        UP_EAST(0, 1, 0, -90), UP_NORTH(0, 1, 0, 180), UP_SOUTH(0, 1, 0, 0), UP_WEST(0, 1, 0, 90),
        WEST_UP(-1, 0, 0, 90), EAST_UP(1, 0, 0, -90), NORTH_UP(0, 0, -1, 180), SOUTH_UP(0, 0, 1, 0);

        public final static Orientation[] DOWN_AXIS = { DOWN_SOUTH, DOWN_WEST, DOWN_NORTH, DOWN_EAST };
        public final static Orientation[] UP_AXIS = { UP_SOUTH, UP_WEST, UP_NORTH, UP_EAST };
        public final static Orientation[] FLAT_AXIS = { SOUTH_UP, WEST_UP, NORTH_UP, EAST_UP };

        private final int x;
        private final int z;
        private final int y;
        private final int angle;

        Orientation(int x, int y, int z, int angle) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.angle = angle;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public int getAngle() {
            return angle;
        }

        public BlockVector3 toVector() {
            return BlockVector3.at(x, y, z);
        }

        public boolean isHorizontal() {
            return x != 0 || z != 0;
        }

        public boolean isVertical() {
            return !isHorizontal();
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

        public static Orientation ofUpAngle(int angle) {
            return UP_AXIS[Math.round(angle / 90f) & 0x3];
        }

        public static Orientation ofDownAngle(int angle) {
            return DOWN_AXIS[Math.round(angle / 90f) & 0x3];
        }

        public static Orientation ofFlatAngle(int angle) {
            return FLAT_AXIS[Math.round(angle / 90f) & 0x3];
        }

        public String toString() {
            return name() + "(" + x + "," + y + "," + z + ")";
        }

    }

}
