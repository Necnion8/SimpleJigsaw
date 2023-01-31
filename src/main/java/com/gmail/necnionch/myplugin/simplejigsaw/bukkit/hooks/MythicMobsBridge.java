package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.mythicmobs.MSpawner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.config.file.YamlConfiguration;
import io.lumine.mythic.core.config.MythicConfigImpl;
import io.lumine.mythic.core.logging.MythicLogger;
import io.lumine.mythic.core.spawning.spawners.MythicSpawner;
import io.lumine.mythic.core.spawning.spawners.SpawnerManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MythicMobsBridge {

    private final SimpleJigsawPlugin plugin;
    private final File templateDirectory;
    private @Nullable Instance inst;

    public MythicMobsBridge(SimpleJigsawPlugin plugin) {
        this.plugin = plugin;
        templateDirectory = new File(plugin.getDataFolder(), "templates");
    }

    public void init() {
        if (available()) {
            inst = new Instance();
            inst.loadAll();
        }
    }

    public void cleanup() {
        inst = null;
    }

    public boolean available() {
        return plugin.getServer().getPluginManager().isPluginEnabled("MythicMobs");
    }

    public Instance get() {
        if (available() && inst != null)
            return inst;
        return null;
    }


    public class Instance {
        private final Map<String, MythicConfig> templates = Maps.newHashMap();

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

        public @Nullable
        List<MythicSpawner> replaceTemplateSpawnersToMythic(Location min, Location max, World world) {
            if (!available()) {
                String locationName = String.format("%s,min%d,%d,%d,max%d,%d,%d",
                        world.getName(), min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());

                plugin.getLogger().warning("Failed to replaceSpawners at (" + locationName + "). MythicMobs is not available!");
                return null;
            }

            SpawnerManager mgr = MythicBukkit.inst().getSpawnerManager();
            if (mgr == null) {
                String locationName = String.format("%s,min%d,%d,%d,max%d,%d,%d",
                        world.getName(), min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());

                plugin.getLogger().warning("Failed to replaceSpawners at (" + locationName + "). MythicMobs SpawnerManager is null!");
                return null;
            }

            List<MythicSpawner> created = Lists.newArrayList();

            for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                    for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {

                        Block block = world.getBlockAt(x, y, z);
                        if (!Material.SPAWNER.equals(block.getType()) || !(block.getState() instanceof CreatureSpawner blockSpawner))
                            continue;

                        boolean success = false;
                        try {
                            MSpawner spawnerSetting = MSpawner.fromPersistentData(blockSpawner.getPersistentDataContainer());
                            if (spawnerSetting == null) {
                                continue;
                            }

                            MythicConfig spawnerConfig = getTemplateByName(spawnerSetting.getName());
                            if (spawnerConfig == null) {
                                String locationName = String.format("%s,min%d,%d,%d,max%d,%d,%d",
                                        world.getName(), min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
                                plugin.getLogger().warning("Unknown spawner template: " + spawnerSetting.getName() + " (" + locationName + ")");

                            } else {
                                created.add(replaceSpawner(spawnerSetting, spawnerConfig, blockSpawner));
                                success = true;

                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            String locationName = String.format("%s,min%d,%d,%d,max%d,%d,%d",
                                    world.getName(), min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
                            plugin.getLogger().severe("Exception in replace spawner (" + locationName + ")");
                        }

                        if (!success) {
                            block.setType(Material.AIR);
                        }

                    }
                }
            }

            if (!created.isEmpty())
                mgr.saveSpawners();

            return created;
        }

        private MythicSpawner replaceSpawner(MSpawner setting, MythicConfig config, CreatureSpawner spawner) {
            SpawnerManager mgr = MythicBukkit.inst().getSpawnerManager();
            MythicSpawner ms = createSpawner(setting.getName(), config, spawner.getLocation());

            if (setting.getMobName() != null) {
                ms.setType(setting.getMobName());
                if (setting.getLevelString() != null)
                    ms.setMobLevel(setting.getLevel());
            }

            spawner.setSpawnedType(EntityType.BAT);
            spawner.setSpawnCount(0);
            spawner.update();

            mgr.listSpawners.add(ms);
            mgr.addSpawnerToChunkLookupTable(ms);

            mgr.mmSpawners.put(ms.getLocation(), ms);
            if (!mgr.mmSpawnerHashcodeLookup.containsKey(ms.hashCode())) {
                mgr.mmSpawnerHashcodeLookup.put(ms.hashCode(), ms);
            }

            return ms;
        }

    }

}
