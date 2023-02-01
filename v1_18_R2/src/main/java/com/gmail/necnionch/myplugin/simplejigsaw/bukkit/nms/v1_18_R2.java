package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.nms;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.item.ItemWorldMap;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
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

    @Nullable
    @Override
    public ItemStack createExplorerMap(World world, Location point, StructureType icon) {
        WorldServer worldServer = ((CraftWorld)world).getHandle();
        BlockPosition structurePosition = new BlockPosition(point.getBlockX(), point.getBlockY(), point.getBlockZ());
        net.minecraft.world.item.ItemStack stack = ItemWorldMap.a(worldServer, structurePosition.u(), structurePosition.w(), MapView.Scale.NORMAL.getValue(), true, true);
        ItemWorldMap.a(worldServer, stack);
        ItemWorldMap.a(stack, worldServer);
        WorldMap.a(stack, structurePosition, "+", MapIcon.Type.a(icon.getMapIcon().getValue()));
        return CraftItemStack.asBukkitCopy(stack);
    }

}
