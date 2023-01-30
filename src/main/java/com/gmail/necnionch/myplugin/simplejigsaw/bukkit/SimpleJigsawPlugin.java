package com.gmail.necnionch.myplugin.simplejigsaw.bukkit;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.commands.MainCommand;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfig;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfigLoader;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.generator.StructureGenerator;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.MythicMobsBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawPart;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.listeners.BlockListener;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.listeners.ChunkListener;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.nms.NMSHandler;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.SchematicPool;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.StructureBuilder;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.TickUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class SimpleJigsawPlugin extends JavaPlugin {
    public static boolean DEBUG_MODE = false;
    public static final Permission DEBUG_PERM = new Permission("simplejigsaw.debug");
    private static SimpleJigsawPlugin instance;
    private final WorldEditBridge worldEditBridge = new WorldEditBridge(this);
    private final MythicMobsBridge mythicMobsBridge = new MythicMobsBridge(this);
    private final StructureConfigLoader structuresLoader = new StructureConfigLoader();
    private final StructureGenerator structureGenerator = new StructureGenerator(this, structuresLoader);
    private final TickUtils tickUtils = new TickUtils(this);
    private final Map<String, Map<String, List<JigsawPart>>> cachedPartsOfPool = Maps.newHashMap();

    @Override
    public void onEnable() {
        instance = this;
        if (!new NMSHandler(getLogger()).init())
            getLogger().warning("NMS disabled! (ignored it)");

        MainCommand.registerCommand(this);
        getServer().getPluginManager().registerEvents(new ChunkListener(this, structureGenerator), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        reload();

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPrecommand(PlayerCommandPreprocessEvent event) {
                if (event.getMessage().equalsIgnoreCase("/checkstruc")) {
                    event.setCancelled(true);

                    Player p = event.getPlayer();
                    List<Location> entries = structureGenerator.buildLocations.stream()
                            .sorted(Comparator.comparingDouble(l -> l.distance(p.getLocation())))
                            .collect(Collectors.toList());
                    p.sendMessage(ChatColor.GOLD + "structure " + entries.size() + " generated");
                    for (int i = 0; i < entries.size(); i++) {
                        Location loc = entries.get(i);
                        p.sendMessage(String.format("- %d %d %d (%dm)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), (int) p.getLocation().distance(loc)));
                        if (i >= 9)
                            break;
                    }

                }
            }
        }, this);

        Optional.ofNullable(Bukkit.getPlayer("Necnion8")).ifPresent(p -> {
//            AtomicLong delay = new AtomicLong(System.currentTimeMillis());
//            Bukkit.getScheduler().runTaskTimer(this, task -> {
//                if (!p.isOnline()) {
//                    task.cancel();
//                    return;
//                }
//                long d = System.currentTimeMillis() - delay.get();
//                delay.set(System.currentTimeMillis());
//
//
//                long value = Math.abs(50 - d);
//
//                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("" + d + " ms" + " / " + value + " ms"));
//
//            }, 0, 0);
            DEBUG_MODE = true;
        });

        tickUtils.start();
        structureGenerator.task.runTaskTimer(this, 0, 0);

        mythicMobsBridge.init();
    }

    @Override
    public void onDisable() {
        tickUtils.stop();
        mythicMobsBridge.cleanup();
    }

    public void reload() {
        cachedPartsOfPool.clear();
        structuresLoader.loadAll(this);

        Collection<StructureConfig.Schematics> schematics = structuresLoader.getSchematics().values();
        File schematicsDir = new File(getDataFolder(), "schematics");

        //noinspection ResultOfMethodCallIgnored
        schematicsDir.mkdirs();

        schematics.forEach(entry -> {
            for (SchematicPool pool : entry.getPools().values()) {
                for (SchematicPool.Entry schematic : pool.getSchematics()) {
                    if (!new File(schematicsDir, schematic.getFileName()).isFile()) {
                        getLogger().warning("Not exists schematic file: " + schematic.getFileName() + " (in " + entry.getName() + ")");
                    }
                }
            }
            if (entry.getStartPool() == null) {
                getLogger().warning("Not exists start pool name (in " + entry.getName() + ")");
            }
        });
        getLogger().info("Loaded " + schematics.size() + " structure settings");
    }

    public @Nullable StructureConfig.Schematics getSchematicsByName(String name) {
        return structuresLoader.getSchematics().get(name);
    }

    public Map<String, StructureConfig.Schematics> getSchematics() {
        return Collections.unmodifiableMap(structuresLoader.getSchematics());
    }

    public @Nullable StructureConfig getStructureByName(String name) {
        return structuresLoader.structures().get(name);
    }

    public Map<String, StructureConfig> getStructures() {
        return Collections.unmodifiableMap(structuresLoader.structures());
    }


    public StructureBuilder createStructureBuilder(StructureConfig.Schematics schematics, int maxSize, boolean replaceJigsaw) {
        Map<String, List<JigsawPart>> partsOfPool = Maps.newHashMap();

        if (cachedPartsOfPool.containsKey(schematics.getName()))
            return new StructureBuilder(schematics, maxSize, cachedPartsOfPool.get(schematics.getName()));

        schematics.getPools().forEach((poolName, pool) -> {
            pool.getSchematics().forEach(schematic -> {
                String schematicFile = "schematics/" + schematic.getFileName();
                Clipboard clipboard = worldEditBridge.loadSchematic(new File(getDataFolder(), schematicFile));
                if (clipboard == null) {
                    getLogger().warning("Failed to load " + schematicFile + " file");
                    return;
                }
                JigsawPart part = worldEditBridge.createJigsawPartOf(schematics, schematic, clipboard, replaceJigsaw);
                if (partsOfPool.containsKey(poolName)) {
                    partsOfPool.get(poolName).add(part);
                } else {
                    partsOfPool.put(poolName, Lists.newArrayList(part));
                }
            });
        });

        cachedPartsOfPool.put(schematics.getName(), partsOfPool);
        return new StructureBuilder(schematics, maxSize, partsOfPool);
    }


    public static Logger getLog() {
        return Objects.requireNonNull(instance, "Plugin is disabled").getLogger();
    }

    public static WorldEditBridge getWorldEdit() {
        return Objects.requireNonNull(instance, "Plugin is disabled").worldEditBridge;
    }

    public static MythicMobsBridge getMythicMobsBridge() {
        return Objects.requireNonNull(instance, "Plugin is disabled").mythicMobsBridge;
    }

}
