package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config;

import com.gmail.necnionch.myplugin.simplejigsaw.common.BukkitConfigDriver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StructureConfig extends BukkitConfigDriver {
    private final String name;
    private @Nullable Structure structure;

    public StructureConfig(JavaPlugin plugin, String fileName, String name) {
        super(plugin, fileName, "example_structure.yml");
        this.name = name;
    }

    public @Nullable Structure getStructure() {
        return structure;
    }

    public void setStructure(@NotNull Structure structure) {
        this.structure = structure;
    }

    @Override
    public boolean onLoaded(FileConfiguration config) {
        if (super.onLoaded(config)) {
            structure = Structure.deserialize(name, config);
            return true;
        }
        return false;
    }

    @Override
    public boolean save() {
        if (structure != null)
            structure.serialize(config);
        return super.save();
    }

}
