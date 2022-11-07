package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.listeners;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.generator.StructureGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class ChunkListener implements Listener {

    private final StructureGenerator generator;

    public ChunkListener(SimpleJigsawPlugin plugin, StructureGenerator generator) {
        this.generator = generator;
        List<BlockPopulator> populators = Bukkit.getWorld("test3").getPopulators();
        populators.removeIf(p -> p.getClass().getName().startsWith("com.gmail.necnionch.myplugin"));
        populators.add(createBlockPopulator());
    }

    @EventHandler
    public void onLoad(ChunkLoadEvent event) {
//        generator.onEvent(event);
    }

    @EventHandler
    public void onPopulate(WorldInitEvent event) {
        World world = event.getWorld();

        if (!world.getName().equalsIgnoreCase("test3"))
            return;

        world.getPopulators().add(createBlockPopulator());
        System.out.println("add pop");
    }

    public BlockPopulator createBlockPopulator() {
        return new BlockPopulator() {
            @Override
            public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
                try {
                    System.out.println("chunk " + chunkX + " : " + chunkZ + "  " + Thread.currentThread());
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 17; z++) {
                            if (!limitedRegion.isInRegion(chunkX * 16 + x, 0, chunkZ * 16 + z)) {
                                System.err.println("not contains: " + x + " ? " + z);
                                continue;
                            }
                            for (int y = 0; y < 256; y++) {
                                limitedRegion.setType(chunkX * 16 + x, 0 + y, chunkZ * 16 + z, Material.STONE);
//                                limitedRegion.setBlockData();
                            }
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };
    }

}
