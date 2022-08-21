package com.gmail.necnionch.myplugin.simplejigsaw.bukkit;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.commands.MainCommand;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfigLoader;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.SchematicPool;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.Structure;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Objects;
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


    public static Logger getLog() {
        return Objects.requireNonNull(instance, "Plugin is disabled").getLogger();
    }

    public static WorldEditBridge getWorldEdit() {
        return Objects.requireNonNull(instance, "Plugin is disabled").worldEditBridge;
    }

}
