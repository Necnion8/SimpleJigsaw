package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.listeners;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfig;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.StructureBuilder;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.bUtils;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

public class ChunkListener implements Listener {

    private final SimpleJigsawPlugin plugin;
    private final Random random = new Random();
    private long lastComplete = System.currentTimeMillis();

    public ChunkListener(SimpleJigsawPlugin plugin) {
        this.plugin = plugin;
    }

    private Logger getLogger() {
        return plugin.getLogger();
    }


    @EventHandler
    public void onLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk())
            return;

        World world = event.getWorld();
        Chunk chunk = event.getChunk();

        if (!world.getName().equalsIgnoreCase("test3"))
            return;

        int rnd = random.nextInt(50);
//        getLogger().info("" + rnd);
        if (rnd != 0) {
//            getLogger().warning("skip chunk: " + chunk);
            return;
        }

        queueExecute(() -> {
            if (!chunk.isLoaded()) {
                getLogger().severe("unloaded!");
                return;
            }
            Location loc = test(chunk, world);
            if (loc == null) {
                getLogger().severe("NOT HIT: " + chunk);
                return;
            }

            getLogger().info("new chunk loaded: " + world.getName() + " " + chunk + ", " + loc);

            StructureConfig.Schematics schematics = plugin.getStructureByName("test");
            if (schematics == null) {
                for (int i = 0; i < 255; i++) {
                    world.setType(loc.getBlockX(), i, loc.getBlockZ(), Material.STONE);
                }

            } else {
                StructureBuilder builder = plugin.createStructureBuilder(schematics, 7, true);

                try (EditSession session = SimpleJigsawPlugin.getWorldEdit().newEditSession(world)) {
                    long processTime = System.currentTimeMillis();
                    int generatedParts = builder.build(session, bUtils.toBlockVector3(loc), 0);
                    getLogger().info("Generated " + schematics.getName() + " structure (" + generatedParts + " parts, " + (System.currentTimeMillis() - processTime) + " ms)");

                } catch (WorldEditException e) {
                    e.printStackTrace();
                }
            }
        });

//        Player onion = Bukkit.getPlayer("Necnion8");
//        onion.teleport(loc.add(0, 128, 0));


    }


    public @Nullable Location test(Chunk chunk, World world) {
        Random random = new Random();
        Block block = null;
        for (int tryCount = 0; tryCount <= 4; tryCount++) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);

            block = world.getHighestBlockAt(chunk.getX() * 16 + x, chunk.getZ() * 16 + z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
            if (Material.GRASS_BLOCK.equals(block.getType()) || Tag.SAND.isTagged(block.getType())) {
                return block.getLocation();
            }
        }
        getLogger().warning(block.getType().name());
        return null;
    }



    private final ArrayList<Runnable> queue = Lists.newArrayList();
    private boolean processing;

    public void queueExecute(Runnable task) {
        synchronized (this) {
            this.queue.add(task);
            if (!this.processing) {
                getLogger().info("now processing");
                execute(this.queue.remove(0));
            } else {
                getLogger().info("queueing size: " + (queue.size() + 1));
            }
        }
    }

    public void execute(Runnable task) {
        synchronized (this) {
            this.processing = true;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            synchronized (this) {
                this.processing = true;
                if (System.currentTimeMillis() - lastComplete < 250) {
                    execute(task);
                    return;
                }
                try {
                    task.run();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                lastComplete = System.currentTimeMillis();

                if (!this.queue.isEmpty()) {
                    getLogger().info("queueing size: " + queue.size());
                    execute(this.queue.remove(0));

                } else {
                    this.processing = false;
                }


            }
        }, 5);
    }


}
