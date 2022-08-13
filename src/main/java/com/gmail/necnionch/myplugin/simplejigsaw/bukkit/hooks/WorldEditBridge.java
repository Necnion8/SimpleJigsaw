package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawParameters;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.ExtentIterator;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.InputExtent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.SessionOwner;
import com.sk89q.worldedit.world.block.BaseBlock;
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

    public @Nullable JigsawParameters getJigsawParametersByBaseBlock(BaseBlock baseBlock) {
        CompoundTag nbt = baseBlock.getNbtData();
        if (nbt == null)
            return null;

        Property<Object> orientationProperty;
        try {
            orientationProperty = baseBlock.getBlockType().getProperty("orientation");
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Failed to get block orientation property");
            return null;
        }
        JigsawParameters.Orientation orientation;
        Object stateValue = baseBlock.getState(orientationProperty);
        try {
            if (stateValue == null) {
                plugin.getLogger().warning("Failed to get block orientation object");
                return null;
            }
            orientation = JigsawParameters.Orientation.valueOf(((String) stateValue).toUpperCase(Locale.ROOT));

        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown jigsaw orientation: " + stateValue);
            return null;
        }

        // mc1.18/1.19 keys
        String name = nbt.getString("name");
        String targetName = nbt.getString("target");
        String pool = nbt.getString("pool");
        String finalBlockState = nbt.getString("final_state");

        JigsawParameters.JointType jointType = JigsawParameters.JointType.UNKNOWN;
        String rawJoint = nbt.getString("joint");
        if (rawJoint != null && !rawJoint.isEmpty()) {
            try {
                jointType = JigsawParameters.JointType.valueOf(rawJoint.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown jigsaw joint type: " + rawJoint);
            }
        }
        return new JigsawParameters(pool, name, targetName, finalBlockState, jointType, orientation);
    }

    // utils

    public ExtentIterator extentIterator(Region region, InputExtent extent, @Nullable BlockVector3 origin) {
        return new ExtentIterator(region, extent, origin);
    }

    public ExtentIterator extentIterator(Clipboard clipboard) {
        return new ExtentIterator(clipboard.getRegion(), clipboard, clipboard.getOrigin());
    }


}
