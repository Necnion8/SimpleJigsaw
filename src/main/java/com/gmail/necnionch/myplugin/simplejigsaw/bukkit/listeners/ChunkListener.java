package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.listeners;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.generator.StructureGenerator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkListener implements Listener {

    private final StructureGenerator generator;

    public ChunkListener(SimpleJigsawPlugin plugin, StructureGenerator generator) {
        this.generator = generator;
    }

    @EventHandler
    public void onLoad(ChunkLoadEvent event) {
        generator.onEvent(event);
    }

}
