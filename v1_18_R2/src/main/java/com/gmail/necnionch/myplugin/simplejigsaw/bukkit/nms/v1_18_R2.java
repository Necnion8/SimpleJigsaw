package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.nms;

import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.biome.BiomeBase;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.jetbrains.annotations.Nullable;

public class v1_18_R2 implements NMS {
    private final IRegistry<BiomeBase> biomeRegistry;

    public v1_18_R2() {
        CraftServer server = (CraftServer) Bukkit.getServer();
        biomeRegistry = server.getServer().Q.a(IRegistry.aP).orElse(null);
    }

    @Override
    public @Nullable String getBiomeKeyByBlock(Block block) {
        World world = block.getWorld();

        BiomeBase biomeBase = ((CraftWorld) world).getHandle().v(((CraftBlock) block).getPosition()).a();
        MinecraftKey biomeKey = biomeRegistry.b(biomeBase);

        return biomeKey != null ? biomeKey.toString() : null;
    }

}
