package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.commands;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfig;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.StructureBuilder;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.bUtils;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandBukkit;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandSender;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.RootCommand;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class MainCommand extends RootCommand {

    private final SimpleJigsawPlugin plugin;
    private final WorldEditBridge worldEdit;
    private boolean debugBuild;


    public MainCommand(SimpleJigsawPlugin plugin) {
        this.plugin = plugin;
        worldEdit = SimpleJigsawPlugin.getWorldEdit();

        addCommand("reload", null, this::cmdReload);
        addCommand("testbuild", null, this::cmdTestBuild, this::completeTestBuild);

        addCommand("setdebug", null, (sender, args) -> {
            debugBuild = !debugBuild;
            if (debugBuild) {
                sendTo(sender, "ジグソーブロックを残して生成します");
            } else {
                sendTo(sender, "ジグソーブロックを残さず生成します");
            }
        });

    }

    private void showParticle(BlockVector3 pos, World w, Color color) {
        Particle.DustOptions dust = new Particle.DustOptions(color, 1);
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            w.spawnParticle(Particle.REDSTONE, pos.getBlockX()+.5, pos.getBlockY()+.5, pos.getBlockZ()+.5, 1, 0, 0, 0, dust);
        }, 0, 1);
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

    private void cmdTestBuild(CommandSender sender, List<String> args) {
        Location location;
        if (sender.getSender() instanceof BlockCommandSender blockSender) {
            location = blockSender.getBlock().getLocation().add(0, 1, 0);
        } else if (sender.getSender() instanceof Player player) {
            location = player.getLocation();
        } else {
            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
            return;
        }

        String structureName;
        try {
            structureName = args.remove(0);
        } catch (IndexOutOfBoundsException e) {
            sendTo(sender, ChatColor.RED + "ストラクチャ名を指定してください");
            return;
        }

        StructureConfig.Structure structure = plugin.getStructureByName(structureName);
        if (structure == null) {
            sendTo(sender, ChatColor.RED + "ストラクチャ " + structureName + " はロードされていません");
            return;
        }

        int maxSize = 3;
        try {
            maxSize = Integer.parseInt(args.remove(0));
        } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
        }

        StructureBuilder builder = plugin.createStructureBuilder(structure, maxSize, !debugBuild);

        try (EditSession session = worldEdit.newEditSession(location.getWorld())) {
            long processTime = System.currentTimeMillis();
            int generatedParts = builder.build(session, bUtils.toBlockVector3(location), 0);
            getLogger().info("Generated " + structureName + " structure (" + generatedParts + " parts, " + (System.currentTimeMillis() - processTime) + " ms)");
            sendTo(sender, ChatColor.GOLD + "ストラクチャから " + generatedParts + " パーツを生成しました " + ChatColor.GRAY + "(" + (System.currentTimeMillis() - processTime) + " ms)");

        } catch (WorldEditException e) {
            e.printStackTrace();
        }

    }

    private @NotNull List<String> completeTestBuild(CommandSender sender, String label, List<String> args) {
        if (args.size() == 1) {
            return generateSuggests(args.get(0), plugin.getStructures().keySet().toArray(new String[0]));
        }
        return Collections.emptyList();
    }


    private void cmdReload(CommandSender sender, List<String> args) {
        plugin.reload();
        sendTo(sender, ChatColor.GOLD + "設定ファイルを再読み込みしました");
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
