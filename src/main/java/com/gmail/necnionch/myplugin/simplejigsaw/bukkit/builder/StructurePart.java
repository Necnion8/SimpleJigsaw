package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.builder;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawConnector;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawPart;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.WrapperPasteBuilder;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.bUtils;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StructurePart {

    private final StructureBuilder builder;
    private final JigsawPart jigsawPart;
    private final @Nullable JigsawConnector fromConnector;
    private final BlockVector3 position;
    private final int angle;
    private final @Nullable StructurePart parent;
    private final List<StructurePart> children = Lists.newArrayList();
    private final Clipboard jigsawPartClipboard;
    private @Nullable Set<String> posCollisions;

    public StructurePart(StructureBuilder builder, JigsawPart jigsawPart, @Nullable JigsawConnector fromConnector, BlockVector3 pos, int angle, @Nullable StructurePart parent) {
        this.builder = builder;
        this.jigsawPart = jigsawPart;
        this.fromConnector = fromConnector;
        this.position = pos;
        this.angle = angle;
        this.parent = parent;
        this.jigsawPartClipboard = jigsawPart.getClipboard();

        if (fromConnector != null && !jigsawPart.getConnectors().contains(fromConnector))
            throw new IllegalArgumentException("fromConnector is not contained in JigsawPart");
    }

    public JigsawPart getJigsawPart() {
        return jigsawPart;
    }

    public @Nullable JigsawConnector getFromConnector() {
        return fromConnector;
    }

    public BlockVector3 getPosition() {
        return position;
    }

    public int getAngle() {
        return angle;
    }

    public @Nullable StructurePart getParent() {
        return parent;
    }

    public List<StructurePart> children() {
        return children;
    }


    public Set<String> testBlockPositionCollisions() {
        if (posCollisions != null)
            return posCollisions;

        BlockVector3 origin = getPasteOrigin();
        int angle = getPasteAngle();

        posCollisions = jigsawPart.getFilledBlockLocations().stream()
                .map(pos -> bUtils.rotate90(angle, pos, jigsawPart.toRelativeLocation(origin)))
                .map(position::add)
                .map(pos -> String.format("%d,%d,%d", pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()))
                .collect(Collectors.toSet());
        return posCollisions;
    }


    public List<Operation> buildOperation(Extent target, BlockVector3 position) {
        ClipboardHolder clipboardHolder = new ClipboardHolder(jigsawPartClipboard);

        BlockVector3 origin = getPasteOrigin();
        int angle = getPasteAngle();

        if (angle != 0)
            clipboardHolder.setTransform(new AffineTransform().rotateY(-angle));

//        builder.log.warning("Paste Pos:       " + position.add(this.position));
//        builder.log.warning("  Base Position: " + position);
//        builder.log.warning("  Build Pos:     " + this.position);

        return Collections.singletonList(
                new WrapperPasteBuilder(clipboardHolder, target, origin)
                        .to(position.add(this.position))
                        .maskSource(builder.createNonReplaceMaskStructureVoid(jigsawPartClipboard))
                        .build());
    }

    private BlockVector3 getPasteOrigin() {
        return (fromConnector == null) ? jigsawPartClipboard.getOrigin() : fromConnector.getOriginalLocation();
    }

    private int getPasteAngle() {
        return (fromConnector == null) ? this.angle : this.angle - fromConnector.getOrientation().getAngle();
    }


}
