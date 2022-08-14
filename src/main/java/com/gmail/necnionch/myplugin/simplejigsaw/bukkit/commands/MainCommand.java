package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.commands;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawConnector;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawPart;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.bUtils;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandBukkit;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandSender;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.RootCommand;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class MainCommand extends RootCommand {

    private final SimpleJigsawPlugin plugin;
    private final WorldEditBridge worldEdit;

    public MainCommand(SimpleJigsawPlugin plugin) {
        this.plugin = plugin;
        worldEdit = SimpleJigsawPlugin.getWorldEdit();

        addCommand("test", null, this::cmdTest);
        addCommand("test2", null, this::cmdTest2);
        addCommand("test3", null, this::cmdTest3);
        addCommand("testcb", null, this::cmdTestCommandBlock);
    }

    private Logger getLogger() {
        return plugin.getLogger();
    }

    private void cmdTest(CommandSender sender, List<String> args) {
        if (!(sender.getSender() instanceof Player player)) {
            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
            return;
        }

        ClipboardHolder clipboardHolder = worldEdit.getClipboard(player);
        if (clipboardHolder == null) {
            sendTo(sender, ChatColor.RED + "Empty clipboard");
            return;
        }

        Clipboard clipboard = clipboardHolder.getClipboard();

        JigsawPart jigsawPart = worldEdit.loadJigsawPart(clipboard);
        if (jigsawPart != null) {
            for (JigsawConnector connector : jigsawPart.getConnectors()) {
                sendTo(sender, "Found jigsaw : " + connector.getLocation());
                sendTo(sender, "  pool:   " + connector.getPool());
                sendTo(sender, "  name:   " + connector.getName());
                sendTo(sender, "  target: " + connector.getTargetName());
                sendTo(sender, "  final:  " + connector.getFinalBlockState());
                sendTo(sender, "  joint:  " + connector.getJointType());
                sendTo(sender, "  orient: " + connector.getOrientation().name());
//            break;
            }
        }

    }

    private void cmdTest2(CommandSender sender, List<String> args) {
        if (!(sender.getSender() instanceof Player player)) {
            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
            return;
        }

        ClipboardHolder clipboardHolder = worldEdit.getClipboard(player);
        if (clipboardHolder == null) {
            sendTo(sender, ChatColor.RED + "Empty clipboard");
            return;
        }

        World world = player.getWorld();
        Clipboard clipboard = clipboardHolder.getClipboard();

        Location orig = new Location(world, clipboard.getOrigin().getBlockX() + 0.5, clipboard.getOrigin().getBlockY(), clipboard.getOrigin().getBlockZ() + 0.5);
        player.teleport(orig);
//        plugin.getLogger().info("" + clipboard.getOrigin());

        JigsawPart jigsawPart = worldEdit.loadJigsawPart(clipboard);

        sendTo(sender, jigsawPart.getConnectors().size() + " size");

        Location location = player.getLocation();
        for (JigsawConnector connector : jigsawPart.getConnectors()) {
            Location newLocation = location.clone();
            newLocation = new Location(world, 0, 0, 0);
            newLocation = newLocation.add(connector.getLocation().getBlockX(), connector.getLocation().getBlockY(), connector.getLocation().getBlockZ());
//            newLocation.add(jigsaw.getLocation().getBlockX(), jigsaw.getLocation().getBlockY(), jigsaw.getLocation().getBlockZ());
//            player.teleport(newLocation);
            plugin.getLogger().info("" + newLocation);
            world.setType(newLocation.add(0, 1, 0), Material.STONE);
        }

    }

    private void cmdTest3(CommandSender sender, List<String> args) {
        if (!(sender.getSender() instanceof Player player)) {
            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
            return;
        }

        ClipboardHolder clipboardHolder = worldEdit.getClipboard(player);
        if (clipboardHolder == null) {
            sendTo(sender, ChatColor.RED + "Empty clipboard");
            return;
        }

        World world = player.getWorld();
        Clipboard clipboard = clipboardHolder.getClipboard();

        JigsawPart jigsawPart = worldEdit.loadJigsawPart(clipboard);

        sendTo(sender, jigsawPart.getConnectors().size() + " size");

        Location location = player.getLocation();
        for (JigsawConnector connector : jigsawPart.getConnectors()) {
            Location newLocation = location.clone();
//            newLocation = new Location(world, 0, 0, 0);
            newLocation = newLocation.add(connector.getLocation().getBlockX(), connector.getLocation().getBlockY(), connector.getLocation().getBlockZ());
//            newLocation.add(jigsaw.getLocation().getBlockX(), jigsaw.getLocation().getBlockY(), jigsaw.getLocation().getBlockZ());
//            player.teleport(newLocation);
            plugin.getLogger().info("" + newLocation);
            world.setType(newLocation.add(0, 1, 0), Material.STONE);
        }

    }

    private void cmdTestCommandBlock(CommandSender sender, List<String> args) {
        Location location;
        if (sender.getSender() instanceof BlockCommandSender blockSender) {
            location = blockSender.getBlock().getLocation().add(0, 1, 0);
        } else if (!(sender.getSender() instanceof Player player)) {
            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
            return;
        } else {
            location = player.getLocation();
        }

        File file = Paths.get("plugins", "WorldEdit", "schematics", "jigsaw.schem").toFile();
        Clipboard clipboard = worldEdit.loadSchematic(file);
        if (clipboard == null)
            return;

        JigsawPart part = worldEdit.loadJigsawPart(clipboard);
        getLogger().info("Loaded " + part.getConnectors().size() + " size");

        Collection<JigsawConnector> jigsaws = part.getJigsawsByName("minecraft:example1");
        List<JigsawConnector> targetJigsaws = Lists.newArrayList(part.getJigsawsByTargetName("minecraft:example1"));


        if (targetJigsaws.isEmpty()) {
            getLogger().info("empty target jigsaws");
            return;
        }


        ImmutableListMultimap<JigsawConnector.Orientation, JigsawConnector> orientOfTargetJigsaws = Multimaps.index(targetJigsaws, JigsawConnector::getOrientation);
        Random rand = new Random();

        try (EditSession session = worldEdit.newEditSession(location.getWorld())) {
//            Operations.complete(new ClipboardHolder(clipboard)
//                    .createPaste(session)
//                    .to(bUtils.toBlockVector3(location))
//                    .build());


            for (JigsawConnector from : jigsaws) {
                JigsawConnector.Orientation targetOrient = from.getOrientation().getOpposite();

                ArrayList<JigsawConnector> orients = Lists.newArrayList(orientOfTargetJigsaws.get(targetOrient));
                orients.remove(from);
                if (orients.isEmpty())
                    continue;

                getLogger().info("hit");

                JigsawConnector to = orients.get(rand.nextInt(orients.size()));

                clipboard.setOrigin(from.getLocation());
                ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
                clipboardHolder.setTransform(new AffineTransform().rotateY(180));  // jigsawの向きが変わらない
                Operations.complete(clipboardHolder
                        .createPaste(session)
                        .to(bUtils.toBlockVector3(location.add(to.getOrientation().getX(), 0, to.getOrientation().getZ()))) // TODO: edit
                        .build()
                );
            }

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
