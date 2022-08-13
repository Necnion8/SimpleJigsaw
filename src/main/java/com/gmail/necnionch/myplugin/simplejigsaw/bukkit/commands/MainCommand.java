package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.commands;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandBukkit;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.RootCommand;
import org.bukkit.command.PluginCommand;

public class MainCommand extends RootCommand {

    private final SimpleJigsawPlugin plugin;

    public MainCommand(SimpleJigsawPlugin plugin) {
        this.plugin = plugin;
    }

    public static MainCommand registerCommand(SimpleJigsawPlugin plugin) {
        MainCommand mainCommand = new MainCommand(plugin);
        PluginCommand command = plugin.getCommand("simplejigsaw");
        if (command == null) {
            SimpleJigsawPlugin.getLog().warning("Command not registered in plugin.yml");
        } else {
            CommandBukkit.register(mainCommand, command);
        }
        return mainCommand;
    }

}
