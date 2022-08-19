package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.commands;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawPart;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.StructureBuilder;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.bUtils;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandBukkit;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandSender;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.RootCommand;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

public class MainCommand extends RootCommand {

    private final SimpleJigsawPlugin plugin;
    private final WorldEditBridge worldEdit;

    private final List<String> schematicFiles = Lists.newArrayList();


    public MainCommand(SimpleJigsawPlugin plugin) {
        this.plugin = plugin;
        worldEdit = SimpleJigsawPlugin.getWorldEdit();

        addCommand("sample_set", null, (sender, args) -> {
            if (args.isEmpty()) {
                sendTo(sender, ChatColor.RED + "WorldEdit schematicsファイル名(拡張子なし)を指定してください (複数可)");
                return;
            }

            schematicFiles.addAll(args);
            sendTo(sender, ChatColor.GOLD + "設定しました。次回再起動まで有効です");
        });

        addCommand("sample_build", null, (sender, args) -> {
            String firstSchemName;
            int size;
            try {
                firstSchemName = args.get(0);
                size = Math.max(0, Integer.parseInt(args.get(1)));
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                sendTo(sender, ChatColor.RED + "開始schem名とサイズを指定してください");
                return;
            }

            Location loc;
            if (sender.getSender() instanceof Player player) {
                loc = player.getLocation();
            } else if (sender.getSender() instanceof BlockCommandSender block) {
                loc = block.getBlock().getLocation().add(0, 1, 0);
            } else {
                sendTo(sender, ChatColor.RED + "プレイヤーあるいはコマブロのみ実行できるコマンドです");
                return;
            }

            if (schematicFiles.isEmpty()) {
                sendTo(sender, ChatColor.RED + "schematicが1つも設定されていません");
                return;
            }

            JigsawPart firstPart = null;

            List<JigsawPart> parts = Lists.newArrayList();

            for (String name : schematicFiles) {
                File file = Paths.get("plugins", "WorldEdit", "schematics", name + ".schem").toFile();
                Clipboard clipboard = worldEdit.loadSchematic(file);
                if (clipboard == null)
                    continue;

                JigsawPart part = worldEdit.createJigsawPartOf(clipboard, true);
                getLogger().info("Loaded " + part.getConnectors().size() + " size, from " + name + " of " + part);

                if (name.equalsIgnoreCase(firstSchemName))
                    firstPart = part;

                parts.add(part);
            }

            if (firstPart == null) {
                sendTo(sender, ChatColor.RED + "指定されたschemが見つかりません");
                return;
            }

            BlockVector3 location = bUtils.toBlockVector3(loc);
            Bukkit.getScheduler().cancelTasks(plugin);
            StructureBuilder builder = new StructureBuilder(size, parts.toArray(new JigsawPart[0]));
            builder.setFirstPart(firstPart);

            try (EditSession session = worldEdit.newEditSession(loc.getWorld())) {
                int generatedParts = builder.build(session, location, 0);
                getLogger().info("generated " + generatedParts + " parts");

            } catch (WorldEditException e) {
                e.printStackTrace();
            }

        });

        addCommand("testcb2", null, this::cmdTestCommandBlock2);
    }

    private Logger getLogger() {
        return plugin.getLogger();
    }

    private void cmdTestCommandBlock2(CommandSender sender, List<String> args) {
        Location location;
        if (sender.getSender() instanceof BlockCommandSender blockSender) {
            location = blockSender.getBlock().getLocation().add(0, 1, 0);
//            return;
        } else if (sender.getSender() instanceof Player player) {
            location = player.getLocation();
        } else {
            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
            return;
        }

        int maxSize = 3;
        try {
            maxSize = Integer.parseInt(args.get(0));
        } catch (IndexOutOfBoundsException | NumberFormatException ignored) {}


        List<JigsawPart> parts = Lists.newArrayList();

        List<String> entries = Lists.newArrayList();
        entries.add("jigsaw.schem");  // CtoC
        entries.add("jigsaw2.schem");  // cLc
        entries.add("jigsaw3.schem");  // C4
        entries.add("jigsaw_end.schem");  // C
        entries.add("jigsaw_up.schem");
        entries.add("jigsaw_down.schem");

        entries.forEach(name -> {
            File file = Paths.get("plugins", "WorldEdit", "schematics", name).toFile();
            Clipboard clipboard = worldEdit.loadSchematic(file);
            if (clipboard == null)
                return;

            JigsawPart part = worldEdit.createJigsawPartOf(clipboard, false);
            getLogger().info("Loaded " + part.getConnectors().size() + " size, from " + name + " of " + part);

//            if (parts.isEmpty())
                parts.add(part);
        });

        Bukkit.getScheduler().cancelTasks(plugin);
        StructureBuilder structure = new StructureBuilder(maxSize, parts.toArray(new JigsawPart[0]));
        structure.setFirstPart(parts.get(0));

        try (EditSession session = worldEdit.newEditSession(location.getWorld())) {
            long processTime = System.currentTimeMillis();
            int generatedParts = structure.build(session, bUtils.toBlockVector3(location), 0);
            getLogger().info("generated " + generatedParts + " parts " + (System.currentTimeMillis() - processTime) + " ms");

        } catch (WorldEditException e) {
            e.printStackTrace();
        }

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

    private static void sendTo(CommandSender sender, String message) {
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }

}
