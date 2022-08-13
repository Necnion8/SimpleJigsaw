package com.gmail.necnionch.myplugin.simplejigsaw.bukkit;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class SimpleJigsawPlugin extends JavaPlugin {
    private static SimpleJigsawPlugin instance;
    private final WorldEditBridge worldEditBridge = new WorldEditBridge(this);

    @Override
    public void onEnable() {
        instance = this;

    }

    @Override
    public void onDisable() {
    }

    public static Logger getLog() {
        return Objects.requireNonNull(instance, "Plugin is disabled").getLogger();
    }

    public static WorldEditBridge getWorldEdit() {
        return Objects.requireNonNull(instance, "Plugin is disabled").worldEditBridge;
    }

}
