package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawPart;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.SchematicPool;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.Structure;
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

    public EditSession newEditSession(Player player) {
        return WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player)).createEditSession(BukkitAdapter.adapt(player));
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

    public JigsawPart createJigsawPartOf(Structure structure, SchematicPool.Entry schematic, Clipboard clipboard, boolean clearStructures) {
        JigsawPart part = new JigsawPart(plugin, structure, schematic, this, clipboard);
        part.loadBlocks(clearStructures);
        return part;
    }

    public JigsawPart createJigsawPartOf(Structure structure, SchematicPool.Entry schematic, Clipboard clipboard) {
        JigsawPart part = new JigsawPart(plugin, structure, schematic, this, clipboard);
        part.loadBlocks(true);
        return part;
    }

    // utils

    public ExtentIterator iterateExtent(Region region, InputExtent extent) {
        return new ExtentIterator(region, extent);
    }

    public ExtentIterator iterateClipboard(Clipboard clipboard) {
        return new ExtentIterator(clipboard.getRegion(), clipboard);
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
