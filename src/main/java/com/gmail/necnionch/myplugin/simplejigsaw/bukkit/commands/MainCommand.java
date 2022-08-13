package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.commands;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.ExtentIterator;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.JigsawParameters;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandBukkit;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandSender;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.RootCommand;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BaseBlock;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
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
            BaseBlock baseBlock = it.next();
            CompoundTag nbt = baseBlock.getNbtData();
            if (nbt == null)
                continue;

            JigsawParameters jigsawParameters = worldEdit.getJigsawParametersByNBT(nbt);
            if (jigsawParameters == null)
                continue;

            sendTo(sender, "Found jigsaw");
            sendTo(sender, "  pool:   " + jigsawParameters.pool());
            sendTo(sender, "  name:   " + jigsawParameters.name());
            sendTo(sender, "  target: " + jigsawParameters.targetName());
            sendTo(sender, "  final:  " + jigsawParameters.finalBlockState());
            sendTo(sender, "  joint:  " + jigsawParameters.jointType());
//            break;
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
