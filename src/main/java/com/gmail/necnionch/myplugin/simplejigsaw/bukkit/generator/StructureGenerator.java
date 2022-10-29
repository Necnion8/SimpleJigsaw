package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.generator;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfig;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfigLoader;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.StructureBuilder;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.BiomeUtils;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.operation.Operations;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StructureGenerator {

    private final SimpleJigsawPlugin plugin;
    private final StructureConfigLoader loader;

    public StructureGenerator(SimpleJigsawPlugin plugin, StructureConfigLoader loader) {
        this.plugin = plugin;
        this.loader = loader;
    }

    private Logger getLogger() {
        return plugin.getLogger();
    }

    public void onEvent(ChunkLoadEvent event) {
        if (!event.isNewChunk())
            return;

        List<StructureConfig> entries = loader.structures().values().stream()
                .filter(conf -> Optional.ofNullable(conf.getGenerator()).map(StructureConfig.Generator::isEnable).orElse(false))
                .collect(Collectors.toList());

        Random rand = new Random();
        while (!entries.isEmpty()) {
            StructureConfig config = entries.remove(entries.size() == 1 ? 0 : rand.nextInt(entries.size()));
            StructureConfig.Schematics schematics = config.getSchematics();
            if (schematics == null)
                continue;
            StructureConfig.Generator generator = Objects.requireNonNull(config.getGenerator());

            try {
                long startAt = System.currentTimeMillis();
                Location center = checkSpawn(generator, event.getChunk());
//                getLogger().info("" + (System.currentTimeMillis() - startAt) + " ms");
                if (center != null) {
                    build(generator, schematics, event.getWorld(), center);
                }

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

    public @Nullable Location checkSpawn(StructureConfig.Generator generator, Chunk chunk) {
        // world check
        if (!generator.worlds().contains(chunk.getWorld().getName()))
            return null;

        // chances
        int chance = generator.getChanceValue();
        if (chance <= 0 || new Random().nextInt(chance) != 0) {
            getLogger().info("test failed : chance");
            return null;
        }

        // select block
        StructureConfig.Generator.GroundCheck ground = generator.getGroundCheck();
        Random random = new Random();
        Block block;

        int testCount = Math.max(1, ground.getCount());
        int testDistance = Math.min(16, Math.abs(ground.getDistance()));
        int success = 0;
        int xSum = 0;
        int zSum = 0;
        int ySum = 0;
        int yMin = 0;
        int yMax = 0;

        for (int idx = 0; idx < testCount && success < ground.getRequiredCount(); idx++) {
            int x = (testDistance - testDistance / 2) - random.nextInt(testDistance / 2);
            int z = (testDistance - testDistance / 2) - random.nextInt(testDistance / 2);
            x = chunk.getX() * 16 + x;
            z = chunk.getZ() * 16 + z;

            block = chunk.getWorld().getHighestBlockAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);

            // type check
            NamespacedKey blockKey = block.getType().getKey();
            if (!ground.blocks().isEmpty()) {
                if (ground.containsBlock(blockKey.toString())) {
                    if (StructureConfig.Generator.GroundCheck.Type.BLACKLIST.equals(ground.getType())) {
                        continue;
                    }
                } else if (StructureConfig.Generator.GroundCheck.Type.WHITELIST.equals(ground.getType())) {
                    continue;
                }
            }

            // biome check
            String biomeName = BiomeUtils.getBiomeKeyByBlock(block);  // TODO: バイオーム指定がなければ調べない
            if (!ground.biomes().isEmpty() && !ground.containsBiome(biomeName))
                continue;

            xSum += block.getX();
            ySum += block.getY();
            zSum += block.getZ();
            yMin = Math.min(yMin, block.getY());
            yMax = Math.max(yMax, block.getY());
            ++success;
        }

        if (success < ground.getRequiredCount()) {
            getLogger().info("test failed : ground");
            return null;
        }

        int yAvg = Math.abs(yMax - yMin - (ySum / success));
        if (yAvg > ground.getYAvg()) {
            getLogger().info("test failed : avg y = " + yAvg);
            return null;
        }

        //noinspection IntegerDivisionInFloatingPointContext
        return new Location(chunk.getWorld(), xSum / success, ySum / success, zSum / success);
    }

    public void build(StructureConfig.Generator generator, StructureConfig.Schematics schematics, World world, Location center) {
        StructureBuilder builder = plugin.createStructureBuilder(schematics, generator.getSize(), true);

        int angle = 0;
        if (generator.isRandomRotate())
            angle = new Random().nextInt(4) * 90;

        long processTime = System.currentTimeMillis();

        buildLocations.add(center);
        StructureBuilder.WorldEditBuild build = builder.createBuild(world, new Random(), center, angle, generator.bottomFill());

        builds.add(build);
        getLogger().info("Pre-generated " + schematics.getName() + " structure (" + build.getParts() + " parts, " + (System.currentTimeMillis() - processTime) + " ms)");

        queue();

    }

    public final List<Location> buildLocations = Lists.newArrayList();
    private final List<StructureBuilder.WorldEditBuild> builds = Lists.newArrayList();
    private boolean building;
    private StructureBuilder.WorldEditBuild selectedBuild;

    public void queue() {
        if (!building)
            doBuild();
    }

    private void doBuild() {
        StructureBuilder.WorldEditBuild b;

        if (selectedBuild != null) {
            b = selectedBuild;

        } else {
            b = selectNearestBuild();
            if (b == null) {
                building = false;
                return;
            }
        }

        building = true;
//        builds.remove(b);

        int distance = b.getWorld().getEntitiesByClass(Player.class).stream()
                .mapToInt(p -> (int) b.getLocation().distance(p.getLocation()))
                .min().orElse(Integer.MAX_VALUE);

        int totalParts = builds.stream().mapToInt(b2 -> b2.operations().size()).sum();

        getLogger().warning("starting           build (waited " + builds.size() + " builds, total " + totalParts + " parts, nearest " + distance + "m)");
        long startAt = System.currentTimeMillis();
        try {
            Operations.complete(b.operations().remove(0));
//            b.start();
        } catch (WorldEditException ex) {
            ex.printStackTrace();
        }
        if (b.operations().isEmpty()) {
            builds.remove(b);
            selectedBuild = null;
        }
        getLogger().info("completed build " + (System.currentTimeMillis() - startAt));

//        System.out.println(System.currentTimeMillis() - lastTime);
//        if (System.currentTimeMillis() - lastTime > 25) {
//            lastTime = System.currentTimeMillis() - 50;
//            plugin.getServer().getScheduler().runTaskLater(plugin, this::doBuild, 0);
//            System.out.println("      delay tick 0");
//        } else {
//            lastTime = System.currentTimeMillis();
//            plugin.getServer().getScheduler().runTaskLater(plugin, this::doBuild, 1);
//            System.out.println("      delay tick 1");
//        }

//        long avg = TickUtils.getAvgDelay();  // todo: 古い遅延処理をロールバックする
//        long tick = Math.max(0, (avg - 30) / 25);
//        getLogger().severe("               delay tick: " + tick + " | " + avg + " ms");
//        plugin.getServer().getScheduler().runTaskLater(plugin, this::doBuild, tick);
    }


    long lastTime = 0;
    public BukkitRunnable task = new BukkitRunnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - lastTime < 10) {  // Skip tick を考慮する (0:無し,0<:有り,10ms推奨)
                getLogger().warning("Skipping 1 tick (delayed " + (System.currentTimeMillis() - lastTime) + "ms)");
                lastTime = System.currentTimeMillis();
                return;
            }

            int count = 0;
            while (System.currentTimeMillis() - lastTime - 50 < 50 * 2 && building) {  // 1tickで処理していい時間 (2tick=100ms)
                doBuild();
                count++;
            }
            if (0 < count)
                System.out.println("loooooooooooooooooooooooooooop tick process: " + count);
            if (n != null)
                n.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("process: " + count));
            lastTime = System.currentTimeMillis();
        }
    };

    Player n = Bukkit.getPlayer("Necnion8");

    private @Nullable StructureBuilder.WorldEditBuild selectNearestBuild() {
        return builds.stream().min(Comparator.comparingDouble(op -> op.getWorld().getEntitiesByClass(Player.class).stream()
                .mapToInt(e -> (int) op.getLocation().distance(e.getLocation()))
                .min().orElse(Integer.MAX_VALUE))).orElse(null);
    }

}
