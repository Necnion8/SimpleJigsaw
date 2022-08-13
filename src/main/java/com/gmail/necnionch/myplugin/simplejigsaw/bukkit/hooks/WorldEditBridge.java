package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.ExtentIterator;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.JigsawJointType;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.JigsawParameters;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.InputExtent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.SessionOwner;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;


public class WorldEditBridge {

    private final SimpleJigsawPlugin plugin;

    public WorldEditBridge(SimpleJigsawPlugin plugin) {
        this.plugin = plugin;
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

    public JigsawParameters getJigsawParametersByNBT(CompoundTag nbt) {
        // mc1.18/1.19 keys
        String name = nbt.getString("name");
        String targetName = nbt.getString("target");
        String pool = nbt.getString("pool");
        String finalBlockState = nbt.getString("final_state");

        JigsawJointType jointType = JigsawJointType.UNKNOWN;
        String rawJoint = nbt.getString("joint");
        if (rawJoint != null && !rawJoint.isEmpty()) {
            try {
                jointType = JigsawJointType.valueOf(rawJoint.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                SimpleJigsawPlugin.getLog().warning("Unknown jigsaw joint type: " + rawJoint);
            }
        }
        return new JigsawParameters(pool, name, targetName, finalBlockState, jointType);
    }

    // utils

    public ExtentIterator extentIterator(Region region, InputExtent extent) {
        return new ExtentIterator(region, extent);
    }

    public ExtentIterator extentIterator(Clipboard clipboard) {
        return new ExtentIterator(clipboard.getRegion(), clipboard);
    }


}
