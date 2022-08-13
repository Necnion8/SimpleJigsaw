package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util;

import com.sk89q.worldedit.extent.InputExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ExtentIterator implements Iterator<ExtentIterator.Block> {

    private final InputExtent extent;
    private final Iterator<BlockVector3> blockVectorIterator;
    private final @Nullable BlockVector3 origin;
    private @Nullable Block nextElement;

    public ExtentIterator(Region region, InputExtent extent, @Nullable BlockVector3 origin) {
        blockVectorIterator = region.iterator();
        this.extent = extent;
        this.origin = origin;
        updateNext();
    }

    @Override
    public boolean hasNext() {
        return nextElement != null;
    }

    @Override
    public Block next() {
        if (this.nextElement == null)
            throw new NoSuchElementException();
        Block elem = this.nextElement;
        updateNext();
        return elem;
    }

    private void updateNext() {
        if (blockVectorIterator.hasNext()) {
            BlockVector3 originLocation, location = blockVectorIterator.next();

            if (origin != null) {
                originLocation = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()).subtract(origin);
            } else {
                originLocation = location;
            }
            nextElement = new Block(originLocation, extent.getFullBlock(location), extent.getBlock(location));
        } else {
            nextElement = null;
        }
    }


    public record Block(BlockVector3 location, BaseBlock baseBlock, BlockState blockState) {}

}
