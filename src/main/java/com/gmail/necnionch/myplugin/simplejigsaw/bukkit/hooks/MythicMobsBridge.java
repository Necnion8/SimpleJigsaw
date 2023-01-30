package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.google.common.collect.Maps;
import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.config.file.YamlConfiguration;
import io.lumine.mythic.core.config.MythicConfigImpl;
import io.lumine.mythic.core.logging.MythicLogger;
import io.lumine.mythic.core.spawning.spawners.MythicSpawner;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MythicMobsBridge {

    private final SimpleJigsawPlugin plugin;
    private final Map<String, MythicConfig> templates = Maps.newHashMap();
    private final File templateDirectory;

    public MythicMobsBridge(SimpleJigsawPlugin plugin) {
        this.plugin = plugin;
        templateDirectory = new File(plugin.getDataFolder(), "templates");
    }

    public void init() {
        if (available()) {
            loadAll();
        }
    }

    public void cleanup() {
    }

    public boolean available() {
        return plugin.getServer().getPluginManager().isPluginEnabled("MythicMobs");
    }


    public void addFromMythicSpawner(MythicSpawner ms, String templateName) {
        templateName = templateName.toLowerCase(Locale.ROOT);

//        if (templates.containsKey(templateName))
//            throw new IllegalArgumentException("already exists template name");

        File spawnerFile = new File(templateDirectory, templateName + ".yml");
        spawnerFile.getParentFile().mkdirs();

        YamlConfiguration spawner = new YamlConfiguration();
        spawner.set(ms.getName() + ".SpawnerGroup", ms.getGroup());
        spawner.set(ms.getName() + ".MobName", ms.getTypeName());
//        spawner.set(ms.getName() + ".World", ms.getWorldName());
//        spawner.set(ms.getName() + ".X", ms.getBlockX());
//        spawner.set(ms.getName() + ".Y", ms.getBlockY());
//        spawner.set(ms.getName() + ".Z", ms.getBlockZ());
//        spawner.set(ms.getName() + ".Yaw", ms.getYaw());
//        spawner.set(ms.getName() + ".Pitch", ms.getPitch());
        spawner.set(ms.getName() + ".Radius", ms.getSpawnRadius());
        spawner.set(ms.getName() + ".UseTimer", ms.getUseTimer());
        spawner.set(ms.getName() + ".MaxMobs", ms.getMaxMobs().serialize());
        spawner.set(ms.getName() + ".MobLevel", ms.getMobLevel().toString());
        spawner.set(ms.getName() + ".MobsPerSpawn", ms.getMobsPerSpawn());
        spawner.set(ms.getName() + ".Cooldown", ms.getCooldownSeconds());
        spawner.set(ms.getName() + ".CooldownTimer", ms.getRemainingCooldownSeconds());
        spawner.set(ms.getName() + ".Warmup", ms.getWarmupSeconds());
        spawner.set(ms.getName() + ".WarmupTimer", ms.getRemainingWarmupSeconds());
        spawner.set(ms.getName() + ".CheckForPlayers", ms.isCheckForPlayers());
        spawner.set(ms.getName() + ".ActivationRange", ms.getActivationRange());
        spawner.set(ms.getName() + ".ScalingRange", ms.getScalingRange());
        spawner.set(ms.getName() + ".LeashRange", ms.getLeashRange());
        spawner.set(ms.getName() + ".HealOnLeash", ms.isHealOnLeash());
        spawner.set(ms.getName() + ".ResetThreatOnLeash", ms.isLeashResetsThreat());
        spawner.set(ms.getName() + ".ShowFlames", ms.isShowFlames());
        spawner.set(ms.getName() + ".Breakable", ms.isBreakable());
        spawner.set(ms.getName() + ".Conditions", ms.getConditionList());
        spawner.set(ms.getName() + ".ActiveMobs", ms.getAssociatedMobs().size());

        try {
            spawner.save(spawnerFile);
        } catch (IOException e) {
            MythicLogger.error("Could not save configuration for spawner: " + ms.getName());
            e.printStackTrace();
        }
        MythicConfigImpl mythicConfig = new MythicConfigImpl(templateName, spawnerFile, spawner);
        templates.put(templateName, mythicConfig);
    }

    public void add(MythicConfig config, String name) {
        templates.put(name.toLowerCase(Locale.ROOT), config);
    }

    public void loadAll() {
        templates.clear();

        if (!templateDirectory.isDirectory())
            return;

        File[] files = templateDirectory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null)
            return;

        for (File file : files) {
            String name = file.getName().toLowerCase(Locale.ROOT).replaceFirst("\\.yml$", "");
            MythicConfig config = new MythicConfigImpl(name, file);
            config.load();
            templates.put(name, config);
        }

        plugin.getLogger().info("Loaded " + templates.size() + " spawner templates");
    }


    public Collection<MythicConfig> getTemplates() {
        return Collections.unmodifiableCollection(templates.values());
    }

    public MythicConfig getTemplateByName(String name) {
        name = name.toLowerCase(Locale.ROOT);
        return templates.get(name);
    }

    public Set<String> getTemplateNames() {
        return Collections.unmodifiableSet(templates.keySet());
    }


    public MythicSpawner createSpawner(String name, MythicConfig config, Location location) {
        World world = location.getWorld();
        name = String.format("%s_%s,%d,%d,%d", name, world.getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        MythicSpawner spawner = new MythicSpawner(MythicBukkit.inst().getSpawnerManager(), name, config);
        spawner.setLocation(BukkitAdapter.adapt(location));
        return spawner;
    }

}
