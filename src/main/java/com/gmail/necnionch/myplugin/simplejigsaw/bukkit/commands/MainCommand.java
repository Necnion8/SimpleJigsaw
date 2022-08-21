package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.commands;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandBukkit;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandSender;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.RootCommand;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.PluginCommand;

import java.util.List;
import java.util.logging.Logger;

public class MainCommand extends RootCommand {

    private final SimpleJigsawPlugin plugin;
    private final WorldEditBridge worldEdit;

    private final List<String> schematicFiles = Lists.newArrayList();


    public MainCommand(SimpleJigsawPlugin plugin) {
        this.plugin = plugin;
        worldEdit = SimpleJigsawPlugin.getWorldEdit();

//        addCommand("testcb2", null, this::cmdTestCommandBlock2);
    }

    private Logger getLogger() {
        return plugin.getLogger();
    }

//    private void cmdTestCommandBlock2(CommandSender sender, List<String> args) {
//        Location location;
//        if (sender.getSender() instanceof BlockCommandSender blockSender) {
//            location = blockSender.getBlock().getLocation().add(0, 1, 0);
////            return;
//        } else if (sender.getSender() instanceof Player player) {
//            location = player.getLocation();
//        } else {
//            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
//            return;
//        }
//
//        int maxSize = 3;
//        try {
//            maxSize = Integer.parseInt(args.get(0));
//        } catch (IndexOutOfBoundsException | NumberFormatException ignored) {}
//
//
//        List<JigsawPart> parts = Lists.newArrayList();
//
//        List<String> entries = Lists.newArrayList();
//        entries.add("jigsaw.schem");  // CtoC
//        entries.add("jigsaw2.schem");  // cLc
//        entries.add("jigsaw3.schem");  // C4
//        entries.add("jigsaw_end.schem");  // C
//        entries.add("jigsaw_up.schem");
//        entries.add("jigsaw_down.schem");
//
//        entries.forEach(name -> {
//            File file = Paths.get("plugins", "WorldEdit", "schematics", name).toFile();
//            Clipboard clipboard = worldEdit.loadSchematic(file);
//            if (clipboard == null)
//                return;
//
//            JigsawPart part = worldEdit.createJigsawPartOf(clipboard, false);
//            getLogger().info("Loaded " + part.getConnectors().size() + " size, from " + name + " of " + part);
//
////            if (parts.isEmpty())
//                parts.add(part);
//        });
//
//        Bukkit.getScheduler().cancelTasks(plugin);
//        StructureBuilder structure = new StructureBuilder(maxSize, parts.toArray(new JigsawPart[0]));
//        structure.setFirstPart(parts.get(0));
//
//        try (EditSession session = worldEdit.newEditSession(location.getWorld())) {
//            long processTime = System.currentTimeMillis();
//            int generatedParts = structure.build(session, bUtils.toBlockVector3(location), 0);
//            getLogger().info("generated " + generatedParts + " parts " + (System.currentTimeMillis() - processTime) + " ms");
//
//        } catch (WorldEditException e) {
//            e.printStackTrace();
//        }
//
//    }


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

    private static void sendTo(CommandSender sender, String message) {
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }

}
