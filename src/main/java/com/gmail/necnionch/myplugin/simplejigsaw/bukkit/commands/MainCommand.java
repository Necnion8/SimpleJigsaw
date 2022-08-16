package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.commands;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawConnector;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawPart;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.bUtils;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandBukkit;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandSender;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.RootCommand;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

//        ClipboardHolder clipboardHolder = worldEdit.getClipboard(player);
//        if (clipboardHolder == null) {
//            sendTo(sender, ChatColor.RED + "Empty clipboard");
//            return;
//        }
//
//        Clipboard clipboard = clipboardHolder.getClipboard();
        File file = Paths.get("plugins", "WorldEdit", "schematics", "jigsaw.schem").toFile();
        Clipboard clipboard = worldEdit.loadSchematic(file);
        if (clipboard == null)
            return;

        JigsawPart jigsawPart = worldEdit.loadJigsawPart(clipboard);
        if (jigsawPart != null) {
            sendTo(sender, "origin: " + jigsawPart.getOrigin());
            for (JigsawConnector connector : jigsawPart.getConnectors()) {
                sendTo(sender, "Found jigsaw : " + connector.getRelativeLocation() + " " + connector.getOriginalLocation());
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
            newLocation = newLocation.add(connector.getRelativeLocation().getBlockX(), connector.getRelativeLocation().getBlockY(), connector.getRelativeLocation().getBlockZ());
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
            newLocation = newLocation.add(connector.getRelativeLocation().getBlockX(), connector.getRelativeLocation().getBlockY(), connector.getRelativeLocation().getBlockZ());
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
//            return;
        } else if (sender.getSender() instanceof Player player) {
            location = player.getLocation();
        } else {
            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
            return;
        }


        List<JigsawConnector> jigsaws = Lists.newArrayList();
        List<JigsawConnector> targetJigsaws = Lists.newArrayList();

        Stream.of("jigsaw.schem", "jigsaw2.schem").forEachOrdered(name -> {
            File file = Paths.get("plugins", "WorldEdit", "schematics", name).toFile();
            Clipboard clipboard = worldEdit.loadSchematic(file);
            if (clipboard == null)
                return;

            JigsawPart part = worldEdit.loadJigsawPart(clipboard);
            getLogger().info("Loaded " + part.getConnectors().size() + " size, from " + name + " of " + part);

            jigsaws.addAll(part.getJigsawsByName("minecraft:example1"));
            targetJigsaws.addAll(part.getJigsawsByTargetName("minecraft:example1")
                    .stream()
                .filter(conn -> conn.getOrientation().isHorizontal())
                .collect(Collectors.toList()));
        });

        if (targetJigsaws.isEmpty()) {
            getLogger().info("empty target jigsaws");
            return;
        }

        Random rand = new Random();

        BlockVector3 baseLoc = bUtils.toBlockVector3(location);

        Bukkit.getScheduler().cancelTasks(plugin);

        try (EditSession session = worldEdit.newEditSession(location.getWorld())) {
//        try (EditSession session = worldEdit.newEditSession(((Player) sender.getSender()))) {

            // select first part
            JigsawPart firstPart = jigsaws.get(0).getJigsawPart();
            Clipboard firstPartClipboard = firstPart.getClipboard();
            ClipboardHolder clipboardHolder = new ClipboardHolder(firstPartClipboard);
            Operations.complete(clipboardHolder
                    .createPaste(session)
                    .to(baseLoc)
                    .build());

            for (JigsawConnector from : firstPart.getConnectors()) {
                JigsawConnector.Orientation orient = from.getOrientation();
                getLogger().info("= from ================>");

                BlockVector3 fromPos = baseLoc.add(from.getRelativeLocation());
                showParticle("p", fromPos, location.getWorld(), Color.RED);

                // 子を召喚
                JigsawConnector to = targetJigsaws.get(rand.nextInt(targetJigsaws.size()));
//                JigsawConnector to = targetJigsaws.get(targetJigsaws.size()-2);
                getLogger().info("To Part: " + to.getJigsawPart());

                BlockVector3 partSize = to.getJigsawPart().getSize();

                Clipboard fromClipboard = to.getJigsawPart().getClipboard();
                fromClipboard.setOrigin(to.getOriginalLocation());
                clipboardHolder = new ClipboardHolder(fromClipboard);
//                int rotate = to.getOrientation().rotateAngleTo(orient);
                int rotate = to.getOrientation().rotateAngleTo(orient.getOpposite());
                getLogger().info("rotate: " + rotate + ", in: " + orient.getAngle() + " <-> " + to.getOrientation().getAngle() + "?");
//                if (to.getOrientation().getAngle() == 0 || to.getOrientation().getAngle() == -90) {
//                    getLogger().info("skip transform " + rotate);
//                } else {
                    clipboardHolder.setTransform(new AffineTransform().rotateY(rotate));
//                }

                BlockVector3 newPos = fromPos;  // = fromPos.add(orient.getX(), 0, orient.getZ());
//                newPos = loc.add(orient.getX(), 0, orient.getZ());
//                if (rotate == 0) {
//                    newPos = fromPos.add(orient.getX(), 0, orient.getZ());
//                } else {
//                    newPos = fromPos.add(orient.getX() * partSize.getBlockX(), 0, orient.getZ() * partSize.getBlockZ());
//                }

//                if (rotate == 180 || rotate == -180) {
                    newPos = newPos.add(
                            orient.getX(),  // * partSize.getBlockX(),
                            0,
                            orient.getZ()  // * partSize.getBlockZ()
                    );
//                }

//                newPos = fromPos.add(
//                        orient.getX() == -1 ? orient.getX() * partSize.getBlockX() : orient.getX(),
//                        0,
//                        orient.getZ() == -1 ? orient.getZ() * partSize.getBlockZ() : orient.getZ()
//                );

                getLogger().info("size: " + partSize);

                getLogger().info("conn place to " + newPos + ", orientation " + orient);
                Operations.complete(clipboardHolder
                        .createPaste(session)
                        .to(newPos)
                        .build()
                );

                showParticle("i", newPos, location.getWorld(), Color.YELLOW);

//                break;
            }

        } catch (WorldEditException e) {
            e.printStackTrace();
        }

    }

    private void showParticle(String name, BlockVector3 loc, World world, Color color) {
        getLogger().info(name + ": " + loc);
        Particle.DustOptions dust = new Particle.DustOptions(color, 1);
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            world.spawnParticle(Particle.REDSTONE, loc.getBlockX() + .5, loc.getBlockY() + 1.5, loc.getBlockZ() + .5, 1, 0, 0, 0, dust);
        }, 0, 1);

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
