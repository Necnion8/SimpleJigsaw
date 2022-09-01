package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.session.ClipboardHolder;

public class WrapperPasteBuilder {
    private final Clipboard clipboard;
    private final Transform transform;
    private final Extent targetExtent;
    private final BlockVector3 clipboardOrigin;
    private Mask sourceMask = Masks.alwaysTrue();
    private BlockVector3 to;
    private boolean ignoreAirBlocks;
    private boolean copyEntities;
    private boolean copyBiomes;

    public WrapperPasteBuilder(ClipboardHolder holder, Extent targetExtent, BlockVector3 clipboardOrigin) {
        this.to = BlockVector3.ZERO;
        this.copyEntities = true;
        this.clipboardOrigin = clipboardOrigin;
        Preconditions.checkNotNull(holder);
        Preconditions.checkNotNull(targetExtent);
        this.clipboard = holder.getClipboard();
        this.transform = holder.getTransform();
        this.targetExtent = targetExtent;
    }

    public WrapperPasteBuilder to(BlockVector3 to) {
        this.to = to;
        return this;
    }

    public WrapperPasteBuilder maskSource(Mask sourceMask) {
        if (sourceMask == null) {
            this.sourceMask = Masks.alwaysTrue();
            return this;
        } else {
            this.sourceMask = sourceMask;
            return this;
        }
    }

    public WrapperPasteBuilder ignoreAirBlocks(boolean ignoreAirBlocks) {
        this.ignoreAirBlocks = ignoreAirBlocks;
        return this;
    }

    public WrapperPasteBuilder copyEntities(boolean copyEntities) {
        this.copyEntities = copyEntities;
        return this;
    }

    public WrapperPasteBuilder copyBiomes(boolean copyBiomes) {
        this.copyBiomes = copyBiomes;
        return this;
    }

    public Operation build() {
        BlockTransformExtent extent = new BlockTransformExtent(this.clipboard, this.transform);
        ForwardExtentCopy copy = new ForwardExtentCopy(extent, this.clipboard.getRegion(), this.clipboardOrigin, this.targetExtent, this.to);
        copy.setTransform(this.transform);
        if (this.ignoreAirBlocks) {
            copy.setSourceMask(this.sourceMask == Masks.alwaysTrue() ? new ExistingBlockMask(this.clipboard) : new MaskIntersection(this.sourceMask, new ExistingBlockMask(this.clipboard)));
        } else {
            copy.setSourceMask(this.sourceMask);
        }

        copy.setCopyingEntities(this.copyEntities);
        copy.setCopyingBiomes(this.copyBiomes && this.clipboard.hasBiomes());
        return copy;
    }
}
