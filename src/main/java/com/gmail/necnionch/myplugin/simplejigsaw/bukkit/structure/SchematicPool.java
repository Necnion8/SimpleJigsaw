package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class SchematicPool {
    private final String name;
    private final List<Entry> schematics;

    public SchematicPool(String name, List<Entry> schematics) {
        this.name = name;
        this.schematics = schematics;
    }

    public String getName() {
        return name;
    }

    public List<Entry> getSchematics() {
        return schematics;
    }


    public static class Entry {
        private final String fileName;
        private final int weight;
        private final String poolName;

        public Entry(String fileName, int weight, String poolName) {
            this.fileName = fileName;
            this.weight = weight;
            this.poolName = poolName;
        }

        public String getFileName() {
            return fileName;
        }

        public int getWeight() {
            return weight;
        }

        public String getPoolName() {
            return poolName;
        }

        public static @Nullable Entry deserialize(Map<?, ?> data, String poolName) {
            Object file = data.get("file");
            Object weight = data.get("weight");
            try {
                if (weight == null)
                    weight = 1;

                return new Entry((String) file, (int) weight, poolName);

            } catch (NullPointerException | ClassCastException ignored) {
                return null;
            }
        }

        public Map<String, Object> serialize() {
            Map<String, Object> data = Maps.newHashMap();
            data.put("file", fileName);
            data.put("weight", weight);
            return data;
        }

    }

}
