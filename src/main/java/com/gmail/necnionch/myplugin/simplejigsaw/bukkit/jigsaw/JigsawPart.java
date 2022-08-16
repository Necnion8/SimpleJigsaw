package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.ExtentIterator;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

public class JigsawPart {
    private final SimpleJigsawPlugin plugin;
    private final WorldEditBridge worldEdit;
    private final Clipboard clipboard;
    private final Set<JigsawConnector> connectors = Sets.newHashSet();
    private final Multimap<String, JigsawConnector> nameOfConnectors = ArrayListMultimap.create();
    private final Multimap<String, JigsawConnector> targetNameOfConnectors = ArrayListMultimap.create();
    private final BlockVector3 origin;
    private final BlockVector3 regionMinimumPoint;


    public JigsawPart(SimpleJigsawPlugin plugin, WorldEditBridge we, Clipboard clipboard) {
        this.plugin = plugin;
        this.worldEdit = we;
        this.clipboard = clipboard;
        this.origin = BlockVector3.at(clipboard.getOrigin().getBlockX(), clipboard.getOrigin().getBlockY(), clipboard.getOrigin().getBlockZ());
        this.regionMinimumPoint = clipboard.getRegion().getMinimumPoint();
    }

    public void loadConnectors() {
        BlockType blockType = BlockTypes.JIGSAW;
        if (blockType == null)
            return;

        connectors.clear();
        nameOfConnectors.clear();
        targetNameOfConnectors.clear();

        for (ExtentIterator it = worldEdit.iterateClipboard(clipboard); it.hasNext(); ) {
            ExtentIterator.Entry entry = it.next();
            if (!blockType.equals(entry.baseBlock().getBlockType()))
                continue;

            JigsawConnector connector = parseJigsawBlock(entry);
            if (connector == null)
                continue;

            this.connectors.add(connector);

            if (!connector.getName().isEmpty())
                nameOfConnectors.put(connector.getName(), connector);

            if (!connector.getTargetName().isEmpty())
                targetNameOfConnectors.put(connector.getTargetName(), connector);

        }

    }

    private Logger getLogger() {
        return plugin.getLogger();
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    public BlockVector3 getSize() {
        return clipboard.getRegion().getMaximumPoint().subtract(clipboard.getRegion().getMinimumPoint()).add(1, 1, 1);
    }

    public BlockVector3 getOrigin() {
        return origin;
    }

    public Set<JigsawConnector> getConnectors() {
        return Collections.unmodifiableSet(connectors);
    }

    public Collection<JigsawConnector> getJigsawsByName(String name) {
        return Collections.unmodifiableCollection(nameOfConnectors.get(name));
    }

    public Collection<JigsawConnector> getJigsawsByTargetName(String targetName) {
        return Collections.unmodifiableCollection(targetNameOfConnectors.get(targetName));
    }

    public BlockVector3 getRegionMinimumPoint() {
        return regionMinimumPoint;
    }

    public BlockVector3 toRelativeLocation(BlockVector3 location) {
//        return regionMinimumPoint.subtract(location);
        return location.subtract(origin);
    }

    public BlockVector3 setClipboardOriginToConnector(JigsawConnector connector) {
        if (!connectors.contains(connector))
            throw new IllegalArgumentException("no contains connector");

        clipboard.setOrigin(connector.getOriginalLocation());
        return connector.getOriginalLocation();
    }


    private @Nullable JigsawConnector parseJigsawBlock(ExtentIterator.Entry entry) {
        BaseBlock baseBlock = entry.baseBlock();

        CompoundTag nbt = baseBlock.getNbtData();
        if (nbt == null)
            return null;

        Property<Object> orientationProperty;
        try {
            orientationProperty = baseBlock.getBlockType().getProperty("orientation");
        } catch (IllegalArgumentException e) {
            getLogger().warning("Failed to get block orientation property");
            return null;
        }
        JigsawConnector.Orientation orientation;
        Object stateValue = baseBlock.getState(orientationProperty);
        try {
            if (stateValue == null) {
                getLogger().warning("Failed to get block orientation object");
                return null;
            }
            orientation = JigsawConnector.Orientation.valueOf(((String) stateValue).toUpperCase(Locale.ROOT));

        } catch (IllegalArgumentException e) {
            getLogger().warning("Unknown jigsaw orientation: " + stateValue);
            return null;
        }

        // mc1.18/1.19 keys
        String name = nbt.getString("name");
        String targetName = nbt.getString("target");
        String pool = nbt.getString("pool");
        String finalBlockState = nbt.getString("final_state");

        JigsawConnector.JointType jointType = JigsawConnector.JointType.UNKNOWN;
        String rawJoint = nbt.getString("joint");
        if (rawJoint != null && !rawJoint.isEmpty()) {
            try {
                jointType = JigsawConnector.JointType.valueOf(rawJoint.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                getLogger().warning("Unknown jigsaw joint type: " + rawJoint);
            }
        }

//         relative location
//        BlockVector3 location = BlockVector3.at(
//                getOrigin().getBlockX() - entry.location().getBlockX(),
//                getOrigin().getBlockY() - entry.location().getBlockY(),
//                getOrigin().getBlockZ() - entry.location().getBlockZ()
//        );
        return new JigsawConnector(this, entry.location(), pool, name, targetName, finalBlockState, jointType, orientation);
    }

}
