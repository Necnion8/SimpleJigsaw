package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.ExtentIterator;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class JigsawPart {
    private final WorldEditBridge worldEdit = SimpleJigsawPlugin.getWorldEdit();
    private final Clipboard clipboard;
    private final Set<Jigsaw> jigsaws = Sets.newHashSet();
    private final Multimap<String, Jigsaw> nameOfJigsaws = ArrayListMultimap.create();
    private final Multimap<String, Jigsaw> targetNameOfJigsaws = ArrayListMultimap.create();


    public JigsawPart(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    public void loadJigsaws() {
        BlockType blockType = BlockTypes.JIGSAW;
        if (blockType == null)
            return;

        jigsaws.clear();
        nameOfJigsaws.clear();
        targetNameOfJigsaws.clear();

        for (ExtentIterator it = worldEdit.extentIterator(clipboard); it.hasNext(); ) {
            ExtentIterator.Block block = it.next();
            if (!blockType.equals(block.baseBlock().getBlockType()))
                continue;

            JigsawParameters parameters = worldEdit.getJigsawParametersByBaseBlock(block.baseBlock());
            if (parameters == null)
                continue;

            Jigsaw jigsaw = new Jigsaw(block.location(), parameters);
            jigsaws.add(jigsaw);

            if (!parameters.getName().isEmpty())
                nameOfJigsaws.put(parameters.getName(), jigsaw);

            if (!parameters.getTargetName().isEmpty())
                nameOfJigsaws.put(parameters.getTargetName(), jigsaw);

        }

    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    public Set<Jigsaw> getJigsaws() {
        return Collections.unmodifiableSet(jigsaws);
    }

    public Collection<Jigsaw> getJigsawsByName(String name) {
        return Collections.unmodifiableCollection(nameOfJigsaws.get(name));
    }

}
