package com.gmail.necnionch.myplugin.simplejigsaw.bukkit;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.commands.MainCommand;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfigLoader;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawPart;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.SchematicPool;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.Structure;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.StructureBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public final class SimpleJigsawPlugin extends JavaPlugin {
    private static SimpleJigsawPlugin instance;
    private final WorldEditBridge worldEditBridge = new WorldEditBridge(this);
    private final StructureConfigLoader structuresLoader = new StructureConfigLoader();

    @Override
    public void onEnable() {
        instance = this;
        MainCommand.registerCommand(this);
        reload();
    }

    @Override
    public void onDisable() {
    }

    public void reload() {
        structuresLoader.loadAll(this);

        Collection<Structure> structures = structuresLoader.getStructures().values();
        File schematicsDir = new File(getDataFolder(), "schematics");

        //noinspection ResultOfMethodCallIgnored
        schematicsDir.mkdirs();

        for (Structure structure : structures) {
            for (SchematicPool pool : structure.getPools().values()) {
                for (SchematicPool.Entry schematic : pool.getSchematics()) {
                    if (!new File(schematicsDir, schematic.getFileName()).isFile()) {
                        getLogger().warning("Not exists schematic file: " + schematic.getFileName() + " (in " + structure.getName() + ")");
                    }
                }
            }
            if (structure.getStartPool() == null) {
                getLogger().warning("Not exists start pool name (in " + structure.getName() + ")");
            }
        }
        getLogger().info("Loaded " + structures.size() + " structure settings");
    }

    public @Nullable Structure getStructureByName(String name) {
        return structuresLoader.getStructures().get(name);
    }

    public Map<String, Structure> getStructures() {
        return Collections.unmodifiableMap(structuresLoader.getStructures());
    }

    public StructureBuilder createStructureBuilder(Structure structure, int maxSize, boolean clearStructures) {
        Map<String, List<JigsawPart>> partsOfPool = Maps.newHashMap();

        structure.getPools().forEach((poolName, pool) -> {
            pool.getSchematics().forEach(schematic -> {
                String schematicFile = "schematics/" + schematic.getFileName();
                Clipboard clipboard = worldEditBridge.loadSchematic(new File(getDataFolder(), schematicFile));
                if (clipboard == null) {
                    getLogger().warning("Failed to load " + schematicFile + " file");
                    return;
                }
                JigsawPart part = worldEditBridge.createJigsawPartOf(structure, clipboard, clearStructures);
                if (partsOfPool.containsKey(poolName)) {
                    partsOfPool.get(poolName).add(part);
                } else {
                    partsOfPool.put(poolName, Lists.newArrayList(part));
                }
            });
        });

        return new StructureBuilder(structure, maxSize, partsOfPool);
    }


    public static Logger getLog() {
        return Objects.requireNonNull(instance, "Plugin is disabled").getLogger();
    }

    public static WorldEditBridge getWorldEdit() {
        return Objects.requireNonNull(instance, "Plugin is disabled").worldEditBridge;
    }

}
