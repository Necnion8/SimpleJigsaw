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
        public void serialize(ConfigurationSection config) {
            config.set("enable", enable);
            config.set("chances.value", chanceValue);
            config.set("worlds", worlds);
            config.set("biomes", biomes);
            config.set("bottom_fill", bottomFill);
        }

        public static Generator deserialize(ConfigurationSection config) {
            return new Generator(
                    config.getBoolean("enable", false),
                    config.getInt("chances.value", 500),
                    config.getStringList("worlds"),
                    config.getStringList("biomes"),
                    Optional.ofNullable(config.getConfigurationSection("bottom_fill"))
                            .map(sec -> {
                                Map<String, String> biomeFill = Maps.newHashMap();
                                sec.getKeys(false).forEach(k -> biomeFill.put(k, sec.getString(k)));
                                return biomeFill;
                            })
                            .orElseGet(Maps::newHashMap)
            );
        }

        private final boolean enable;
        private final int chanceValue;
        private final List<String> worlds;
        private final List<String> biomes;
        private final Map<String, String> bottomFill;

        public Generator(boolean enable, int chanceValue, List<String> worlds, List<String> biomes, Map<String, String> bottomFill) {
            this.enable = enable;
            this.chanceValue = chanceValue;
            this.worlds = worlds;
            this.biomes = biomes;
            this.bottomFill = bottomFill;
        }

        public boolean isEnable() {
            return enable;
        }

        public int getChanceValue() {
            return chanceValue;
        }

        public List<String> worlds() {
            return worlds;
        }

        public List<String> biomes() {
            return biomes;
        }

        public Map<String, String> bottomFill() {
            return bottomFill;
        }

    }
}
