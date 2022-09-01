package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.nms;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Logger;

public class NMSHandler {
    private static NMS nms;
    private final Logger logger;

    public static @NotNull NMS getNMS() {
        return Objects.requireNonNull(nms, "NMS Not Available");
    }

    public static boolean isAvailable() {
        return nms != null;
    }

    public NMSHandler(Logger logger) {
        this.logger = logger;
    }

    private Logger getLogger() {
        return logger;
    }

    public boolean init() {
        String nmsVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
        Class<?> nmsHandler;
        try {
            nmsHandler = Class.forName("com.gmail.necnionch.myplugin.simplejigsaw.bukkit.nms." + nmsVersion);
        } catch (ClassNotFoundException e) {
            getLogger().severe("Unsupported version: " + nmsVersion);
            return false;
        }
        try {
            nms = (NMS) nmsHandler.newInstance();
        } catch (Throwable e) {
            e.printStackTrace();
            getLogger().severe("Failed to initialize NMS handler");
            return false;
        }
        return true;
    }
}
