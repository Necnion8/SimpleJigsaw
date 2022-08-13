package com.gmail.necnionch.myplugin.simplejigsaw.common.command;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class CommandBukkit {
    public static org.bukkit.command.Command build(RootCommand command, String name, String description, String usageMessage, List<String> aliases) {
        return new org.bukkit.command.Command(name, description, usageMessage, aliases) {
            @Override
            public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
                command.execute(conv(sender), listFrom(args));
                return true;
            }

            @Override
            public List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
                return command.tabComplete(conv(sender), alias, listFrom(args));
            }
        };
    }

    public static void register(RootCommand command, PluginCommand pluginCommand) {
        pluginCommand.setExecutor((s, c, l, a) -> {command.execute(conv(s), listFrom(a)); return true;});
        pluginCommand.setTabCompleter((sender, cmd, label, args) -> command.tabComplete(conv(sender), args[0], listFrom(args)));
    }


    public static Sender conv(org.bukkit.command.CommandSender sender) {
        return new Sender(sender);
    }

    public static ArrayList<String> listFrom(String[] args) {
        return new ArrayList<>(Arrays.asList(args));
    }


    public static class Sender implements CommandSender {
        private static Method LOCALE_METHOD;
        private static boolean LOCALE_IS_SPIGOT;

        static {
            try {
                LOCALE_IS_SPIGOT = false;
                try {  // Spigot 1.12, 1.13, 1.14, 1.15, 1.16, 1.17
                    LOCALE_METHOD = Player.class.getDeclaredMethod("getLocale");

                } catch (NoSuchMethodException e) {
                    try {  // Spigot 1.8 ~ 1.11
                        LOCALE_METHOD = Player.Spigot.class.getDeclaredMethod("getLocale");
                        LOCALE_IS_SPIGOT = true;
                    } catch (NoSuchMethodException ignored) {
                    }
                }
            } catch (Throwable ignored) {

            }

        }

        private final org.bukkit.command.CommandSender sender;

        public Sender(org.bukkit.command.CommandSender sender) {
            this.sender = sender;
        }

        @Override
        public org.bukkit.command.CommandSender getSender() {
            return sender;
        }

        @Override
        public void sendMessage(BaseComponent[] components) {
            if (sender instanceof Player) {
                ((Player) sender).spigot().sendMessage(components);
            } else {
                StringBuilder builder = new StringBuilder();
                for (BaseComponent component : components) {
                    builder.append(component.toLegacyText());
                }
                sender.sendMessage(builder.toString());
            }
        }

        @Override
        public boolean hasPermission(String permission) {
            return sender.hasPermission(permission);
        }

        @Override
        public boolean hasPermission(Command command) {
            return command.getPermission() == null || sender.hasPermission(command.getPermission());
        }

        @Override
        public String getLocale() {
            if (sender instanceof Player && LOCALE_METHOD != null) {
                try {
                    Object p = (LOCALE_IS_SPIGOT) ? ((Player) sender).spigot() : (Player) sender;
                    return ((String) LOCALE_METHOD.invoke(p)).toLowerCase(Locale.ROOT);
                } catch (ClassCastException | InvocationTargetException | IllegalAccessException ignored) {

                }
            }
            return null;
        }

    }
}
