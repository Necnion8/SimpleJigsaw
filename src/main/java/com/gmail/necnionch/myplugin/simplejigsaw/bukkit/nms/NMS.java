package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.nms;

import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public interface NMS {

    @Nullable String getBiomeKeyByBlock(Block block);

}
