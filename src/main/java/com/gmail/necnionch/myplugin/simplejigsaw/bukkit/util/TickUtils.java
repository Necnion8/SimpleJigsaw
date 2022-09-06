package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.google.common.collect.Lists;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Optional;

public class TickUtils {
    private final SimpleJigsawPlugin plugin;
    private static TickUtils instance;
    private BukkitTask timer;
    private List<long[]> ticks = Lists.newArrayList();
    private long lastTick;

    public TickUtils(SimpleJigsawPlugin plugin) {
        if (instance != null)
            throw new IllegalStateException("already instanced");
        this.plugin = plugin;
        instance = this;
    }

    public void start() {
        lastTick = System.currentTimeMillis();
        timer = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            ticks.removeIf(tim -> System.currentTimeMillis() - tim[0] > 1000 * 10);
            ticks.add(new long[] {System.currentTimeMillis(), System.currentTimeMillis() - lastTick});
            lastTick = System.currentTimeMillis();
        }, 0, 0);


    }

    public void stop() {
        Optional.ofNullable(timer).ifPresent(BukkitTask::cancel);
        timer = null;
    }

    public static long getLastTick() {
        return System.currentTimeMillis() - instance.lastTick;
    }

    public static long getAvgDelay() {
        if (instance.ticks.isEmpty())
            return System.currentTimeMillis() - instance.lastTick;
        long value = 0;
        for (long[] tick : instance.ticks) {
            value += tick[1];
        }
        return value / instance.ticks.size();
    }

}
