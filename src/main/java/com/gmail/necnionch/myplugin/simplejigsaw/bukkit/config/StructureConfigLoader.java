package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class StructureConfigLoader {
    private final Map<String, StructureConfig> configList = Maps.newHashMap();

    public StructureConfigLoader() {
    }

    public void loadAll(SimpleJigsawPlugin plugin) {
        configList.clear();

        File directory = new File(plugin.getDataFolder(), "structures");
        boolean copyExample = !directory.exists();
        //noinspection ResultOfMethodCallIgnored
        directory.mkdirs();

        if (copyExample) {
            StructureConfig example = new StructureConfig(plugin, "structures/example.yml", "example");
            example.load();  // load defaults and save
        }

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null)
            return;

        for (File file : files) {
            StructureConfig config = new StructureConfig(plugin, "structures/" + file.getName(), file.getName().substring(0, file.getName().length() - 4));
            if (!config.load())
                continue;

            configList.put(config.getName(), config);
        }
    }

    public Map<String, StructureConfig.Schematics> getStructures() {
        return configList
                .values()
                .stream()
                .map(StructureConfig::getSchematics)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(StructureConfig.Schematics::getName, c -> c));
    }

    public Map<String, StructureConfig> structures() {
        return configList;
    }


}
