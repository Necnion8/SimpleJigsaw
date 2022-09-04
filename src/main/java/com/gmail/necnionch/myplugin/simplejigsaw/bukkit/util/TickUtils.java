package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.generator.StructureGenerator;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Optional;

public class TickUtils {
    private final SimpleJigsawPlugin plugin;
    private static TickUtils instance;
    private BukkitTask timer;
//    private List<Long> ticks = Lists.newArrayList();
    private List<long[]> ticks = Lists.newArrayList();
    private long lastTick;
    private int lastDelay;

    public TickUtils(SimpleJigsawPlugin plugin) {
        if (instance != null)
            throw new IllegalStateException("already instanced");
        this.plugin = plugin;
        instance = this;
    }

    private boolean a;
    public void start() {
        lastTick = System.currentTimeMillis();
        timer = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            ticks.removeIf(tim -> System.currentTimeMillis() - tim[0] > 1000 * 10);
//            if (ticks.size() >= 100)
//                ticks.remove(0);

            ticks.add(new long[] {System.currentTimeMillis(), System.currentTimeMillis() - lastTick});
            lastTick = System.currentTimeMillis();

            Optional.ofNullable(Bukkit.getPlayer("Necnion8")).ifPresent(p -> {
                long sum = ticks.stream().mapToLong(value -> value[1]).sum();
//                System.out.println("" + sum);
                float avg = sum / (float) ticks.size();
                lastDelay = (int) avg;

                StructureGenerator.clear();
                String s = String.format("%dms - %d", Math.round(avg), StructureGenerator.sampleSize);

                if (a) {
                    s = ChatColor.GOLD + "　     " + ChatColor.RESET + s + "     　";
                } else {
                    s = "　     " + s + ChatColor.AQUA + "     　";
                }
                a = !a;

                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(s));
            });


        }, 0, 0);


    }

    public static int getLastDelay() {
        return instance.lastDelay;
    }

    public static long getDelay() {
        return System.currentTimeMillis() - instance.lastTick;
    }

}
