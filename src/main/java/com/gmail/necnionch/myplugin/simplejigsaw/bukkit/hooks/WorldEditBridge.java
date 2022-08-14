package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawPart;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.ExtentIterator;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.InputExtent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.SessionOwner;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class WorldEditBridge {

    private final SimpleJigsawPlugin plugin;

    public WorldEditBridge(SimpleJigsawPlugin plugin) {
        this.plugin = plugin;
    }

    public EditSession newEditSession(World world) {
        return WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world));
    }

    // clipboard

    public @Nullable ClipboardHolder getClipboard(Player player) {
        return getClipboard(BukkitAdapter.adapt(player));
    }

    public @Nullable ClipboardHolder getClipboard(SessionOwner owner) {
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(owner);
        try {
            return session.getClipboard();
        } catch (EmptyClipboardException e) {
            return null;
        }
    }

    // jigsaw

    public JigsawPart loadJigsawPart(Clipboard clipboard) {
        JigsawPart part = new JigsawPart(plugin, this, clipboard);
        part.loadConnectors();
        return part;
    }

    // utils

    public ExtentIterator extentIterator(Region region, InputExtent extent, @Nullable BlockVector3 origin) {
        return new ExtentIterator(region, extent, origin);
    }

    public ExtentIterator extentIterator(Clipboard clipboard) {
        return new ExtentIterator(clipboard.getRegion(), clipboard, clipboard.getOrigin());
    }

    public @Nullable Clipboard loadSchematic(File schemFile) {
        ClipboardFormat format = ClipboardFormats.findByFile(schemFile);
        if (format == null)
            return null;

        try (FileInputStream is = new FileInputStream(schemFile);
             ClipboardReader reader = format.getReader(is)) {
            return reader.read();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
