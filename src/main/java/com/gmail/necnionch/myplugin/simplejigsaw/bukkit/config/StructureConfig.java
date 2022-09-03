package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.SchematicPool;
import com.gmail.necnionch.myplugin.simplejigsaw.common.BukkitConfigDriver;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StructureConfig extends BukkitConfigDriver {
    private final String name;
    private @Nullable StructureConfig.Schematics schematics;
    private @Nullable Generator generator;

    public StructureConfig(JavaPlugin plugin, String fileName, String name) {
        super(plugin, fileName, "example_structure.yml");
        this.name = name;
    }

    public @Nullable StructureConfig.Schematics getSchematics() {
        return schematics;
    }

    public void setSchematics(@NotNull StructureConfig.Schematics schematics) {
        this.schematics = schematics;
    }

    public @Nullable Generator getGenerator() {
        return generator;
    }

    public void setGenerator(@Nullable Generator generator) {
        this.generator = generator;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean onLoaded(FileConfiguration config) {
        if (super.onLoaded(config)) {

            this.schematics = null;
            Optional.ofNullable(config.getConfigurationSection("schematics"))
                    .ifPresent(conf -> this.schematics = Schematics.deserialize(name, conf));

            this.generator = null;
            Optional.ofNullable(config.getConfigurationSection("generator"))
                    .ifPresent(conf -> this.generator = Generator.deserialize(conf));

            return true;
        }
        return false;
    }

    @Override
    public boolean save() {
        if (schematics != null)
            schematics.serialize(Optional.ofNullable(config.getConfigurationSection("schematics")).orElseGet(() ->
                    config.createSection("schematics")));

        if (generator != null)
            generator.serialize(Optional.ofNullable(config.getConfigurationSection("generator")).orElseGet(() ->
                    config.createSection("generator")));

        return super.save();
    }


    public static class Schematics {
        private final String name;
        private final Map<String, SchematicPool> pools;
        private @Nullable SchematicPool startPool;

        public Schematics(String name, Map<String, SchematicPool> pools, SchematicPool startPool) {
            this.name = name;
            this.pools = pools;
            setStartPool(startPool);
        }

        public String getName() {
            return name;
        }

        public Map<String, SchematicPool> getPools() {
            return pools;
        }

        public @Nullable SchematicPool getStartPool() {
            return startPool;
        }

        public void setStartPool(@Nullable SchematicPool startPool) {
            if (startPool != null && !pools.containsValue(startPool))
                throw new IllegalArgumentException("no contains start pool in pools");
            this.startPool = startPool;
        }


        public static Schematics deserialize(String name, ConfigurationSection config) {
            String startPoolName = config.getString("start_pool");
            SchematicPool startPool = null;
            Map<String, SchematicPool> pools = Maps.newHashMap();

            ConfigurationSection poolsSection = config.getConfigurationSection("pools");
            if (poolsSection != null) {
                for (String poolName : poolsSection.getKeys(false)) {
                    List<SchematicPool.Entry> schematicEntries = Lists.newArrayList();
                    poolsSection.getMapList(poolName).forEach(data -> {
                        SchematicPool.Entry entry = SchematicPool.Entry.deserialize(data);
                        if (entry != null)
                            schematicEntries.add(entry);
                    });
                    SchematicPool pool = new SchematicPool(poolName, schematicEntries);
                    pools.put(poolName, pool);
                    if (poolName.equalsIgnoreCase(startPoolName))
                        startPool = pool;
                }
            }

            return new Schematics(name, pools, startPool);

        }

        public void serialize(ConfigurationSection config) {
            if (startPool != null)
                config.set("start_pool", startPool.getName());

            for (Map.Entry<String, SchematicPool> e : pools.entrySet()) {
                String poolName = e.getKey();
                SchematicPool pool = e.getValue();
                config.set("pools." + poolName, pool.getSchematics()
                        .stream().map(SchematicPool.Entry::serialize).collect(Collectors.toList()));
            }
        }

    }

    public static class Generator {

        private boolean enable;
        private int chanceValue;
        private final List<String> worlds;
        private final Map<String, String> bottomFill;
        private final GroundCheck groundCheck;
        private int size;
        private boolean randomRotate;

        public Generator(boolean enable, int chanceValue, List<String> worlds, Map<String, String> bottomFill, GroundCheck groundCheck, int size, boolean randomRotate) {
            this.enable = enable;
            this.chanceValue = chanceValue;
            this.worlds = worlds;
            this.bottomFill = bottomFill;
            this.groundCheck = groundCheck;
            this.size = size;
            this.randomRotate = randomRotate;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public int getChanceValue() {
            return chanceValue;
        }

        public void setChanceValue(int chanceValue) {
            this.chanceValue = chanceValue;
        }

        public List<String> worlds() {
            return worlds;
        }

        public Map<String, String> bottomFill() {
            return bottomFill;
        }

        public GroundCheck getGroundCheck() {
            return groundCheck;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public boolean isRandomRotate() {
            return randomRotate;
        }

        public void setRandomRotate(boolean randomRotate) {
            this.randomRotate = randomRotate;
        }

        public void serialize(ConfigurationSection config) {
            config.set("enable", enable);
            config.set("chances.value", chanceValue);
            config.set("worlds", worlds);
            config.set("bottom_fill", bottomFill);
            config.set("size", size);
            config.set("random_rotate", randomRotate);
            config.set("ground_check.test.distance", groundCheck.getDistance());
            config.set("ground_check.test.count", groundCheck.getCount());
            config.set("ground_check.test.required_count", groundCheck.getRequiredCount());
            config.set("ground_check.test.y_avg", groundCheck.getYAvg());
            config.set("ground_check.blocks_type", groundCheck.type.name().toLowerCase(Locale.ROOT));
            config.set("ground_check.blocks", groundCheck.blocks);
            config.set("ground_check.biomes", groundCheck.biomes);
        }

        public static Generator deserialize(ConfigurationSection config) {
            return new Generator(
                    config.getBoolean("enable", false),
                    config.getInt("chances.value", 500),
                    config.getStringList("worlds"),
                    Optional.ofNullable(config.getConfigurationSection("bottom_fill"))
                            .map(sec -> {
                                Map<String, String> biomeFill = Maps.newHashMap();
                                sec.getKeys(false).forEach(k -> biomeFill.put(k, sec.getString(k)));
                                return biomeFill;
                            })
                            .orElseGet(Maps::newHashMap),
                    Optional.ofNullable(config.getConfigurationSection("ground_check"))
                            .map(sec -> new GroundCheck(
                                    sec.getInt("test.count", 8),
                                    sec.getInt("test.required_count", 4),
                                    sec.getInt("test.distance", 16),
                                    sec.getInt("test.y_avg", 8),
                                    Optional.ofNullable(sec.getString("blocks_type"))
                                            .map(s -> s.toUpperCase(Locale.ROOT))
                                            .map(s -> {
                                                try {
                                                    return GroundCheck.Type.valueOf(s);
                                                } catch (IllegalArgumentException e) {
                                                    return GroundCheck.Type.WHITELIST;
                                                }
                                            })
                                            .orElse(GroundCheck.Type.WHITELIST),
                                    sec.getStringList("blocks"),
                                    sec.getStringList("biomes")
                                    ))
                            .orElseGet(() -> new GroundCheck(8, 4, 16, 8, GroundCheck.Type.BLACKLIST, Lists.newArrayList(), Lists.newArrayList())),
                    config.getInt("size", 4),
                    config.getBoolean("random_rotate", false)
            );
        }


        public static class GroundCheck {

            public enum Type {
                WHITELIST, BLACKLIST;

            }

            private int count;
            private int requiredCount;
            private int distance;
            private int yAvg;
            private Type type;
            private final List<String> blocks;
            private final List<String> biomes;

            public GroundCheck(int count, int requiredCount, int distance, int yAvg, Type blocksType, List<String> blocks, List<String> biomes) {
                this.count = count;
                this.requiredCount = requiredCount;
                this.distance = distance;
                this.yAvg = yAvg;
                this.type = blocksType;
                this.blocks = blocks;
                this.biomes = biomes;
            }

            public int getCount() {
                return count;
            }

            public void setCount(int count) {
                this.count = count;
            }

            public int getRequiredCount() {
                return requiredCount;
            }

            public void setRequiredCount(int requiredCount) {
                this.requiredCount = requiredCount;
            }

            public int getDistance() {
                return distance;
            }

            public void setDistance(int distance) {
                this.distance = distance;
            }

            public int getYAvg() {
                return yAvg;
            }

            public void setYAvg(int y) {
                this.yAvg = y;
            }

            public Type getType() {
                return type;
            }

            public void setType(Type type) {
                this.type = type;
            }

            public List<String> blocks() {
                return blocks;
            }

            public List<String> biomes() {
                return biomes;
            }

            public boolean containsBlock(String blockKey) {
                return blocks
                        .stream()
                        .map(s -> s.contains(":") ? s : "minecraft:" + s)
                        .anyMatch(s -> s.equalsIgnoreCase(blockKey));
            }

            public boolean containsBiome(String biomeKey) {
                return biomes
                        .stream()
                        .map(s -> s.contains(":") ? s : "minecraft:" + s)
                        .anyMatch(s -> s.equalsIgnoreCase(biomeKey));
            }

        }
    }
}
