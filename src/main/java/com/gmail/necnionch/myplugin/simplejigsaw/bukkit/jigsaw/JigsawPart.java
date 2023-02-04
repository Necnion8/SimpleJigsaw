package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfig;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.SchematicPool;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.ExtentIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

public class JigsawPart {
    private final SimpleJigsawPlugin plugin;
    private final WorldEditBridge worldEdit;
    private final StructureConfig.Schematics schematics;
    private final SchematicPool.Entry schematic;
    private final Clipboard clipboard;
    private final List<JigsawConnector> connectors = Lists.newArrayList();
    private final BlockVector3 origin;
    private final Set<BlockVector3> filledBlockLocations = Sets.newHashSet();


    public JigsawPart(SimpleJigsawPlugin plugin, StructureConfig.Schematics schematics, SchematicPool.Entry schematic, WorldEditBridge we, Clipboard clipboard) {
        this.plugin = plugin;
        this.schematics = schematics;
        this.schematic = schematic;
        this.worldEdit = we;
        this.clipboard = clipboard;
        this.origin = clipboard.getOrigin();
    }

    public void loadBlocks(boolean replaceJigsaw) {
        BlockType blockType = BlockTypes.JIGSAW;
        if (blockType == null)
            return;

        BlockType voidType = Optional.ofNullable(BlockTypes.STRUCTURE_VOID).orElse(null);

        connectors.clear();
        filledBlockLocations.clear();

        for (ExtentIterator it = worldEdit.iterateClipboard(clipboard); it.hasNext(); ) {
            ExtentIterator.Entry entry = it.next();
            boolean isStructure = false;

            if (blockType.equals(entry.baseBlock().getBlockType())) {
                JigsawConnector connector = parseJigsawBlock(entry);

                if (connector != null) {
                    this.connectors.add(connector);

                    String finalBlockState = connector.getFinalBlockState();
                    NamespacedKey namespacedKey = NamespacedKey.fromString(finalBlockState);  // todo: blockStateも適用する
                    finalBlockState = (namespacedKey != null) ? namespacedKey.toString() : "minecraft:air";

                    BlockType finalBlockType;
                    if (!finalBlockState.equalsIgnoreCase("minecraft:structure_void")) {
                        finalBlockType = BlockTypes.get(finalBlockState);
                        if (finalBlockType == null)
                            finalBlockType = BlockTypes.AIR;
                    } else {
                        finalBlockType = BlockTypes.STRUCTURE_VOID;
                    }

                    if (replaceJigsaw && finalBlockType != null) {
                        try {
                            clipboard.setBlock(entry.location(), finalBlockType.getDefaultState());
                        } catch (WorldEditException e) {
                            e.printStackTrace();
                        }
                    }

                    isStructure = finalBlockType != null && finalBlockType.equals(BlockTypes.STRUCTURE_VOID);
                }

            } else if (entry.baseBlock().getBlockType().equals(voidType)) {
                isStructure = true;
            }

            if (!isStructure)
                filledBlockLocations.add(entry.location().subtract(origin));

        }

    }

    private Logger getLogger() {
        return plugin.getLogger();
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    public StructureConfig.Schematics getStructure() {
        return schematics;
    }

    public SchematicPool.Entry getPoolEntry() {
        return schematic;
    }

    public BlockVector3 getSize() {
        return clipboard.getRegion().getMaximumPoint().subtract(clipboard.getRegion().getMinimumPoint()).add(1, 1, 1);
    }

    public List<JigsawConnector> getConnectors() {
        return Collections.unmodifiableList(connectors);
    }

    public BlockVector3 toRelativeLocation(BlockVector3 location) {
//        return regionMinimumPoint.subtract(location);
        return location.subtract(origin);
    }

    public Clipboard setClipboardOriginToConnector(JigsawConnector connector) {
        if (!connectors.contains(connector))
            throw new IllegalArgumentException("no contains connector");

        clipboard.setOrigin(connector.getOriginalLocation());
        return clipboard;
    }

    public Set<BlockVector3> getFilledBlockLocations() {
        return Collections.unmodifiableSet(filledBlockLocations);
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
