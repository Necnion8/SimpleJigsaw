package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util;

import com.sk89q.worldedit.extent.InputExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ExtentIterator implements Iterator<BaseBlock> {

    private final InputExtent extent;
    private final Iterator<BlockVector3> blockVectorIterator;
    private @Nullable BaseBlock nextElement;

    public ExtentIterator(Region region, InputExtent extent) {
        blockVectorIterator = region.iterator();
        this.extent = extent;
        nextElement = blockVectorIterator.hasNext() ? extent.getFullBlock(blockVectorIterator.next()) : null;
    }

    @Override
    public boolean hasNext() {
        return nextElement != null;
    }

    @Override
    public BaseBlock next() {
        if (this.nextElement == null)
            throw new NoSuchElementException();
        BaseBlock elem = this.nextElement;
        this.nextElement = blockVectorIterator.hasNext() ? extent.getFullBlock(blockVectorIterator.next()) : null;
        return elem;
    }

}
