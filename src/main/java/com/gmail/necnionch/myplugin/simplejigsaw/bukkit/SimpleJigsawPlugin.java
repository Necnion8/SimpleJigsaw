package com.gmail.necnionch.myplugin.simplejigsaw.bukkit;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.commands.MainCommand;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfig;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfigLoader;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawPart;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.listeners.ChunkListener;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.nms.NMSHandler;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.SchematicPool;
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
        if (!new NMSHandler(getLogger()).init())
            getLogger().warning("NMS disabled! (ignored it)");

        MainCommand.registerCommand(this);
        getServer().getPluginManager().registerEvents(new ChunkListener(this), this);
        reload();
    }

    @Override
    public void onDisable() {
    }

    public void reload() {
        structuresLoader.loadAll(this);

        Collection<StructureConfig.Schematics> schematics = structuresLoader.getStructures().values();
        File schematicsDir = new File(getDataFolder(), "schematics");

        //noinspection ResultOfMethodCallIgnored
        schematicsDir.mkdirs();

        for (StructureConfig.Schematics entry : schematics) {
            for (SchematicPool pool : entry.getPools().values()) {
                for (SchematicPool.Entry schematic : pool.getSchematics()) {
                    if (!new File(schematicsDir, schematic.getFileName()).isFile()) {
                        getLogger().warning("Not exists schematic file: " + schematic.getFileName() + " (in " + entry.getName() + ")");
                    }
                }
            }
            if (entry.getStartPool() == null) {
                getLogger().warning("Not exists start pool name (in " + entry.getName() + ")");
            }
        }
        getLogger().info("Loaded " + schematics.size() + " structure settings");
    }

    public @Nullable StructureConfig.Schematics getStructureByName(String name) {
        return structuresLoader.getStructures().get(name);
    }

    public Map<String, StructureConfig.Schematics> getStructures() {
        return Collections.unmodifiableMap(structuresLoader.getStructures());
    }

    public StructureBuilder createStructureBuilder(StructureConfig.Schematics schematics, int maxSize, boolean clearStructures) {
        Map<String, List<JigsawPart>> partsOfPool = Maps.newHashMap();

        schematics.getPools().forEach((poolName, pool) -> {
            pool.getSchematics().forEach(schematic -> {
                String schematicFile = "schematics/" + schematic.getFileName();
                Clipboard clipboard = worldEditBridge.loadSchematic(new File(getDataFolder(), schematicFile));
                if (clipboard == null) {
                    getLogger().warning("Failed to load " + schematicFile + " file");
                    return;
                }
                JigsawPart part = worldEditBridge.createJigsawPartOf(schematics, schematic, clipboard, clearStructures);
                if (partsOfPool.containsKey(poolName)) {
                    partsOfPool.get(poolName).add(part);
                } else {
                    partsOfPool.put(poolName, Lists.newArrayList(part));
                }
            });
        });

        return new StructureBuilder(schematics, maxSize, partsOfPool);
    }


    public static Logger getLog() {
        return Objects.requireNonNull(instance, "Plugin is disabled").getLogger();
    }

    public static WorldEditBridge getWorldEdit() {
        return Objects.requireNonNull(instance, "Plugin is disabled").worldEditBridge;
    }

}
