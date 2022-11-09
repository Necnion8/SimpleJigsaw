package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.generator;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfig;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfigLoader;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.StructureBuilder2;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.BiomeUtils;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.BukkitRegionOutputExtent;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.operation.Operations;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StructureGenerator2 extends BlockPopulator implements Listener {

    private final SimpleJigsawPlugin plugin;
    private final StructureConfigLoader loader;

    public StructureGenerator2(SimpleJigsawPlugin plugin, StructureConfigLoader loader) {
        this.plugin = plugin;
        this.loader = loader;

        Bukkit.getWorlds().forEach(w -> {
            w.getPopulators().removeIf(p -> p.getClass().getName().startsWith("com.gmail.necnionch.myplugin"));
            w.getPopulators().add(this);
        });

    }

    private Logger getLogger() {
        return plugin.getLogger();
    }

    public void onEnable() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<BukkitRegionOutputExtent.CachedBlockEntry<?>> it = BukkitRegionOutputExtent.OUT_RANGE_BLOCKS.values().iterator();
            if (it.hasNext()) {
                BukkitRegionOutputExtent.CachedBlockEntry<?> e = it.next();
                Optional.ofNullable(Bukkit.getWorld(e.getWorld())).ifPresent(w -> {
                    BukkitRegionOutputExtent.applyChunkBlocks(w, null);
                });
            }

        }, 0, 20);
    }


    @EventHandler
    public void onInitWorld(WorldInitEvent event) {
        event.getWorld().getPopulators().add(this);
    }

    @Override
    public void populate(@NotNull WorldInfo info, @NotNull Random rand, int chunkX, int chunkZ, @NotNull LimitedRegion region) {
        try {
            BukkitRegionOutputExtent.applyChunkBlocks(region, chunkX, chunkZ, info);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            List<StructureConfig> entries = loader.structures().values().stream()
                    .filter(conf -> Optional.ofNullable(conf.getGenerator()).map(StructureConfig.Generator::isEnable).orElse(false))
                    .collect(Collectors.toList());

            while (!entries.isEmpty()) {
                StructureConfig config = entries.remove(entries.size() == 1 ? 0 : rand.nextInt(entries.size()));
                StructureConfig.Schematics schematics = config.getSchematics();
                if (schematics == null)
                    continue;
                StructureConfig.Generator generator = Objects.requireNonNull(config.getGenerator());

                // world check
                if (!generator.worlds().contains(info.getName()))
                    continue;
//                System.out.println("w: " + info.getName());

                // chances
                int chance = generator.getChanceValue();
                if (chance <= 0 || new Random().nextInt(chance) != 0) {
                    continue;  // failed: chance
                }

                // select block
                StructureConfig.Generator.GroundCheck ground = generator.getGroundCheck();

                int testCount = Math.max(1, ground.getCount());
                int testDistance = Math.min(16, Math.abs(ground.getDistance()));
                int success = 0;
                int xSum = 0;
                int zSum = 0;
                int ySum = 0;
                int yMin = 0;
                int yMax = 0;

                for (int idx = 0; idx < testCount && success < ground.getRequiredCount(); idx++) {
                    int x = (testDistance - testDistance / 2) - rand.nextInt(testDistance / 2);
                    int z = (testDistance - testDistance / 2) - rand.nextInt(testDistance / 2);
                    x = chunkX * 16 + x;
                    z = chunkZ * 16 + z;

                    int y = info.getMinHeight();
                    Material groundType = region.getType(x, y, z);
                    for (int i = info.getMaxHeight() - 1; i >= info.getMinHeight(); i--) {
                        Material type = region.getType(x, i, z);
//                        System.out.println("test " + x + "," + i + "," + z + " type " + type);
                        if (!Tag.LEAVES.isTagged(type) && !Material.WATER.equals(type) && !Material.LAVA.equals(type) && !type.isAir()) {
                            y = i;
                            groundType = type;
                            break;
                        }
                    }

                    // type check
                    NamespacedKey blockKey = groundType.getKey();
//                    System.out.println(groundType);
                    if (!ground.blocks().isEmpty()) {
                        if (ground.containsBlock(blockKey.toString())) {
                            if (StructureConfig.Generator.GroundCheck.Type.BLACKLIST.equals(ground.getType())) {
//                                System.out.println("fail ground a");
                                continue;
                            }
                        } else if (StructureConfig.Generator.GroundCheck.Type.WHITELIST.equals(ground.getType())) {
//                            System.out.println("fail ground b");
                            continue;
                        }
                    }

                    // biome check
                    World world = region.getBlockState(x, y, z).getWorld();
                    String biomeName = BiomeUtils.getBiomeKeyByPositionNMS(world, x, y, z);  // TODO: バイオーム指定がなければ調べない
                    if (biomeName == null)
                        biomeName = region.getBiome(x, y, z).getKey().toString();

//                    System.out.println("biome: " + biomeName);
                    if (!ground.biomes().isEmpty() && !ground.containsBiome(biomeName))
                        continue;

                    xSum += x;
                    ySum += y;
                    zSum += z;
                    yMin = Math.min(yMin, y);
                    yMax = Math.max(yMax, y);
                    ++success;
                }

                if (success < ground.getRequiredCount()) {
//                    System.out.println("fail ground");
                    continue;  // failed ground
                }

                int yAvg = Math.abs(yMax - yMin - (ySum / success));
                if (yAvg > ground.getYAvg()) {
//                    System.out.println("fail avg");
                    continue;  // failed avg y
                }

//                System.out.println("building");
//                long startAt = System.currentTimeMillis();
                build(generator, schematics, xSum / success, ySum / success, zSum / success, info, rand, chunkX, chunkZ, region);
//                getLogger().info("" + (System.currentTimeMillis() - startAt) + " ms");

            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private void build(StructureConfig.Generator generator, StructureConfig.Schematics schematics, int x, int y, int z, @NotNull WorldInfo info, @NotNull Random rand, int chunkX, int chunkZ, @NotNull LimitedRegion region) {
        StructureBuilder2 builder = plugin.createStructureBuilder2(schematics, generator.getSize(), true);

        int angle = 0;
        if (generator.isRandomRotate())
            angle = rand.nextInt(4) * 90;

        long processTime = System.currentTimeMillis();

        BukkitRegionOutputExtent extent = new BukkitRegionOutputExtent(region, chunkX, chunkZ, info);
        StructureBuilder2.WorldEditBuild build = builder.createBuild(rand, extent, x, y, z, angle, generator.bottomFill());
        getLogger().info("Pre-generated " + schematics.getName() + " structure (" + build.getParts() + " parts, " + (System.currentTimeMillis() - processTime) + " ms)");

        build.operations().forEach(op -> {
            try {
                Operations.complete(op);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        });

        getLogger().info("Generated " + schematics.getName() + " in " + x + ", " + y + ", " + z + " (" + build.getParts() + " parts, total " + (System.currentTimeMillis() - processTime) + " ms)");
    }


    private void sendDebugMessage(Supplier<String> print) {
        if (SimpleJigsawPlugin.DEBUG_MODE && !Bukkit.getOnlinePlayers().isEmpty()) {
            TextComponent message = new TextComponent(print.get());
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission(SimpleJigsawPlugin.DEBUG_PERM))
                    .forEach(p -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, message));
        }
    }

}
