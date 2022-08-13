package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw;

import com.sk89q.worldedit.math.BlockVector3;

public class Jigsaw {
    private final BlockVector3 location;
    private final JigsawParameters parameters;

    public Jigsaw(BlockVector3 pos, JigsawParameters parameters) {
        location = pos;
        this.parameters = parameters;
    }

    public BlockVector3 getLocation() {
        return location;
    }

    public JigsawParameters getParameters() {
        return parameters;
    }

}
