package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Structure {
    private final String name;
    private final Map<String, SchematicPool> pools;
    private @Nullable SchematicPool startPool;

    public Structure(String name, Map<String, SchematicPool> pools, SchematicPool startPool) {
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


    public static @Nullable Structure deserialize(String name, Configuration config) {
        ConfigurationSection schematics = config.getConfigurationSection("schematics");
        if (schematics == null)
            return null;

        String startPoolName = schematics.getString("start_pool");
        SchematicPool startPool = null;
        Map<String, SchematicPool> pools = Maps.newHashMap();

        for (String poolName : schematics.getKeys(false)) {
            List<SchematicPool.Entry> schematicEntries = Lists.newArrayList();
            schematics.getMapList("pools." + poolName).forEach(data -> {
                SchematicPool.Entry entry = SchematicPool.Entry.deserialize(data);
                if (entry != null)
                    schematicEntries.add(entry);
            });
            SchematicPool pool = new SchematicPool(poolName, schematicEntries);
            pools.put(poolName, pool);
            if (poolName.equalsIgnoreCase(startPoolName))
                startPool = pool;
        }

        return new Structure(name, pools, startPool);

    }

    public void serialize(Configuration config) {
        ConfigurationSection schematics = config.getConfigurationSection("schematics");
        if (schematics == null)
            schematics = config.createSection("schematics");

        if (startPool != null)
            schematics.set("start_pool", startPool.getName());

        for (Map.Entry<String, SchematicPool> e : pools.entrySet()) {
            String poolName = e.getKey();
            SchematicPool pool = e.getValue();
            schematics.set("pools." + poolName, pool.getSchematics()
                    .stream().map(SchematicPool.Entry::serialize).collect(Collectors.toList()));
        }
    }

}
