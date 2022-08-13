package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.commands;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.Jigsaw;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawParameters;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawPart;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.ExtentIterator;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandBukkit;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandSender;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.RootCommand;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BaseBlock;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.List;

public class MainCommand extends RootCommand {

    private final SimpleJigsawPlugin plugin;
    private final WorldEditBridge worldEdit;

    public MainCommand(SimpleJigsawPlugin plugin) {
        this.plugin = plugin;
        worldEdit = SimpleJigsawPlugin.getWorldEdit();

        addCommand("test", null, this::cmdTest);
        addCommand("test2", null, this::cmdTest2);
        addCommand("test3", null, this::cmdTest3);
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

        for (ExtentIterator it = worldEdit.extentIterator(clipboard); it.hasNext(); ) {
            BaseBlock baseBlock = it.next().baseBlock();
            CompoundTag nbt = baseBlock.getNbtData();
            if (nbt == null)
                continue;

            JigsawParameters jigsawParameters = worldEdit.getJigsawParametersByBaseBlock(baseBlock);
            if (jigsawParameters == null)
                continue;

            sendTo(sender, "Found jigsaw");
            sendTo(sender, "  pool:   " + jigsawParameters.getPool());
            sendTo(sender, "  name:   " + jigsawParameters.getName());
            sendTo(sender, "  target: " + jigsawParameters.getTargetName());
            sendTo(sender, "  final:  " + jigsawParameters.getFinalBlockState());
            sendTo(sender, "  joint:  " + jigsawParameters.getJointType());
            sendTo(sender, "  orient: " + jigsawParameters.getOrientation().name());
//            break;
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

        JigsawPart jigsawPart = new JigsawPart(clipboard);
        jigsawPart.loadJigsaws();

        sendTo(sender, jigsawPart.getJigsaws().size() + " size");

        Location location = player.getLocation();
        for (Jigsaw jigsaw : jigsawPart.getJigsaws()) {
            Location newLocation = location.clone();
//            newLocation = new Location(world, 0, 0, 0);
            newLocation = newLocation.add(jigsaw.getLocation().getBlockX(), jigsaw.getLocation().getBlockY(), jigsaw.getLocation().getBlockZ());
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

        JigsawPart jigsawPart = new JigsawPart(clipboard);
        jigsawPart.loadJigsaws();

        sendTo(sender, jigsawPart.getJigsaws().size() + " size");

        Location location = player.getLocation();
        for (Jigsaw jigsaw : jigsawPart.getJigsaws()) {
            Location newLocation = location.clone();
//            newLocation = new Location(world, 0, 0, 0);
            newLocation = newLocation.add(jigsaw.getLocation().getBlockX(), jigsaw.getLocation().getBlockY(), jigsaw.getLocation().getBlockZ());
//            newLocation.add(jigsaw.getLocation().getBlockX(), jigsaw.getLocation().getBlockY(), jigsaw.getLocation().getBlockZ());
//            player.teleport(newLocation);
            plugin.getLogger().info("" + newLocation);
            world.setType(newLocation.add(0, 1, 0), Material.STONE);
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
