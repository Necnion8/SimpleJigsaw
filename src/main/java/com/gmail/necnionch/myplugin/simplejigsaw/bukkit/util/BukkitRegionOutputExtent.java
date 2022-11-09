package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class BukkitRegionOutputExtent implements Extent {
    public static class CachedBlockEntry<T extends BlockStateHolder<T>> {

        private final BlockVector3 position;
        private final T block;
        private final UUID world;
        private boolean generated;

        public CachedBlockEntry(UUID world, BlockVector3 position, T block) {
            this.world = world;
            this.position = position;
            this.block = block;
        }

        public BlockVector3 getPosition() {
            return position;
        }

        public T getBlock() {
            return block;
        }

        public UUID getWorld() {
            return world;
        }

        public boolean generated() {
            return generated;
        }

        public void setGenerated() {
            this.generated = true;
        }
    }

    private final LimitedRegion region;
    private final int chunkX;
    private final int chunkZ;
    private final WorldInfo worldInfo;

    public static final Multimap<String, CachedBlockEntry<?>> OUT_RANGE_BLOCKS = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    public static <B extends BlockStateHolder<B>> void applyChunkBlocks(LimitedRegion region, int chunkX, int chunkZ, WorldInfo worldInfo) {
        String key = worldInfo.getName();
        if (!OUT_RANGE_BLOCKS.containsKey(key)) {
            System.out.println("" + OUT_RANGE_BLOCKS.size());

            return;
        }

        OUT_RANGE_BLOCKS.get(key).removeIf(e -> {
            if (e.generated())
                return true;

            if (region.isInRegion(e.position.getX(), e.position.getY(), e.position.getZ())) {
                System.out.println("resolved");
                region.setBlockData(e.position.getX(), e.position.getY(), e.position.getZ(), BukkitAdapter.adapt((B) e.block));
                e.setGenerated();
                return true;
            }
            return false;
        });
        System.out.println("" + OUT_RANGE_BLOCKS.size());

    }

    public static <B extends BlockStateHolder<B>> void applyChunkBlocks(World world, Chunk chunk) {
//        String key = world.getName();
//        if (!OUT_RANGE_BLOCKS.containsKey(key)) {
//            System.out.println("" + OUT_RANGE_BLOCKS.size());
//
//            return;
//        }
//
//        OUT_RANGE_BLOCKS.get(key).removeIf(e -> {
//            if (e.generated())
//                return true;
//
//            System.out.println("resolved");
//            world.setBlockData(e.position.getX(), e.position.getY(), e.position.getZ(), BukkitAdapter.adapt((B) e.block));
//            e.setGenerated();
//            return true;
//        });
//        System.out.println("" + OUT_RANGE_BLOCKS.size());
//
    }


    public BukkitRegionOutputExtent(LimitedRegion region, int chunkX, int chunkZ, WorldInfo worldInfo) {
        this.region = region;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.worldInfo = worldInfo;
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) {
        if (region.isInRegion(position.getX(), position.getY(), position.getZ())) {
            region.setBlockData(position.getX(), position.getY(), position.getZ(), BukkitAdapter.adapt(block));
            return true;
        }
//        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(SimpleJigsawPlugin.class), () -> {
//            Chunk chunk = Objects.requireNonNull(Bukkit.getWorld(worldInfo.getUID())).getBlockAt(position.getX(), position.getY(), position.getZ()).getChunk();
//            if (!chunk.isLoaded()) {
//                System.out.println("loading request");
//                System.out.println("result: " + chunk.load(true));
//            } else {
//                System.out.println("loaded ok");
//            }
//        }, 1);
        System.err.println("queued " + position);
        OUT_RANGE_BLOCKS.put(worldInfo.getName(), new CachedBlockEntry<>(worldInfo.getUID(), position, block));
        return false;
    }

    @Override
    public boolean fullySupports3DBiomes() {
        return true;
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        if (region.isInRegion(position.getX(), position.getY(), position.getZ())) {
            region.setBiome(position.getX(), position.getY(), position.getZ(), BukkitAdapter.adapt(biome));
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Operation commit() {
        return null;
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        return BlockVector3.at(
                chunkX * 16 - region.getBuffer(),
                worldInfo.getMinHeight(),
                chunkZ * 16 - region.getBuffer()
        );
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return BlockVector3.at(
                (chunkX + 1) * 16 + region.getBuffer(),
                worldInfo.getMaxHeight(),
                (chunkZ + 1) * 16 + region.getBuffer()
        );
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return null;
    }

    @Override
    public List<? extends Entity> getEntities() {
        return null;
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        return null;
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        if (region.isInRegion(position.getX(), position.getY(), position.getZ())) {
            return BukkitAdapter.adapt(region.getBlockState(position.getX(), position.getY(), position.getZ()).getBlockData());
        }
        return null;
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        if (region.isInRegion(position.getX(), position.getY(), position.getZ())) {
            return BukkitAdapter.adapt(region.getBlockState(position.getX(), position.getY(), position.getZ()).getBlockData()).toBaseBlock();
        }
        return null;
    }
}
