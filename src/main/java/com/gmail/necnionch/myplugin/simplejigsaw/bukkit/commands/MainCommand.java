package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.commands;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfig;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.MythicMobsBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.mythicmobs.MSpawner;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.StructureBuilder;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandBukkit;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandSender;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.RootCommand;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.numbers.RandomInt;
import io.lumine.mythic.core.spawning.spawners.MythicSpawner;
import io.lumine.mythic.core.spawning.spawners.SpawnerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

public class MainCommand extends RootCommand {

    private final SimpleJigsawPlugin plugin;
    private final WorldEditBridge worldEdit;


    public MainCommand(SimpleJigsawPlugin plugin) {
        this.plugin = plugin;
        worldEdit = SimpleJigsawPlugin.getWorldEdit();

        addCommand("reload", null, this::cmdReload);
        addCommand("testbuild", null, this::cmdTestBuild, this::completeTestBuild);
        addCommand("givespawner", null, this::onGiveSpawner, this::compGiveSpawner);
        addCommand("createtemplate", null, this::onCreateTemplate);

        addCommand("setdebug", null, (sender, args) -> {
            SimpleJigsawPlugin.DEBUG_MODE = !SimpleJigsawPlugin.DEBUG_MODE;
            if (SimpleJigsawPlugin.DEBUG_MODE) {
                sendTo(sender, "デバッグモードを有効にしました");
            } else {
                sendTo(sender, "デバッグモードを無効にしました");
            }
        });

    }

    private void showParticle(BlockVector3 pos, World w, Color color) {
        Particle.DustOptions dust = new Particle.DustOptions(color, 1);
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            w.spawnParticle(Particle.REDSTONE, pos.getBlockX()+.5, pos.getBlockY()+.5, pos.getBlockZ()+.5, 1, 0, 0, 0, dust);
        }, 0, 1);
    }

    private Logger getLogger() {
        return plugin.getLogger();
    }

//    private void cmdTestCommandBlock2(CommandSender sender, List<String> args) {
//        Location location;
//        if (sender.getSender() instanceof BlockCommandSender blockSender) {
//            location = blockSender.getBlock().getLocation().add(0, 1, 0);
////            return;
//        } else if (sender.getSender() instanceof Player player) {
//            location = player.getLocation();
//        } else {
//            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
//            return;
//        }
//
//        int maxSize = 3;
//        try {
//            maxSize = Integer.parseInt(args.get(0));
//        } catch (IndexOutOfBoundsException | NumberFormatException ignored) {}
//
//
//        List<JigsawPart> parts = Lists.newArrayList();
//
//        List<String> entries = Lists.newArrayList();
//        entries.add("jigsaw.schem");  // CtoC
//        entries.add("jigsaw2.schem");  // cLc
//        entries.add("jigsaw3.schem");  // C4
//        entries.add("jigsaw_end.schem");  // C
//        entries.add("jigsaw_up.schem");
//        entries.add("jigsaw_down.schem");
//
//        entries.forEach(name -> {
//            File file = Paths.get("plugins", "WorldEdit", "schematics", name).toFile();
//            Clipboard clipboard = worldEdit.loadSchematic(file);
//            if (clipboard == null)
//                return;
//
//            JigsawPart part = worldEdit.createJigsawPartOf(clipboard, false);
//            getLogger().info("Loaded " + part.getConnectors().size() + " size, from " + name + " of " + part);
//
////            if (parts.isEmpty())
//                parts.add(part);
//        });
//
//        Bukkit.getScheduler().cancelTasks(plugin);
//        StructureBuilder structure = new StructureBuilder(maxSize, parts.toArray(new JigsawPart[0]));
//        structure.setFirstPart(parts.get(0));
//
//        try (EditSession session = worldEdit.newEditSession(location.getWorld())) {
//            long processTime = System.currentTimeMillis();
//            int generatedParts = structure.build(session, bUtils.toBlockVector3(location), 0);
//            getLogger().info("generated " + generatedParts + " parts " + (System.currentTimeMillis() - processTime) + " ms");
//
//        } catch (WorldEditException e) {
//            e.printStackTrace();
//        }
//
//    }

    private void cmdTestBuild(CommandSender sender, List<String> args) {
        Location location;
        if (sender.getSender() instanceof BlockCommandSender blockSender) {
            location = blockSender.getBlock().getLocation().add(0, 1, 0);
        } else if (sender.getSender() instanceof Player player) {
            location = player.getLocation();
        } else {
            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
            return;
        }

        String structureName;
        try {
            structureName = args.remove(0);
        } catch (IndexOutOfBoundsException e) {
            sendTo(sender, ChatColor.RED + "ストラクチャ名を指定してください");
            return;
        }

        StructureConfig structure = plugin.getStructureByName(structureName);
        if (structure == null || structure.getSchematics() == null) {
            sendTo(sender, ChatColor.RED + "ストラクチャ " + structureName + " はロードされていません");
            return;
        }

        int maxSize = 3;
        try {
            maxSize = Integer.parseInt(args.remove(0));
        } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
        }

        Map<String, String> fill = Optional.ofNullable(structure.getGenerator())
                .map(StructureConfig.Generator::bottomFill)
                .orElseGet(Collections::emptyMap);

        StructureBuilder builder = plugin.createStructureBuilder(structure.getSchematics(), maxSize, !SimpleJigsawPlugin.DEBUG_MODE);

        try {
            long processTime = System.currentTimeMillis();
            int generatedParts = builder.createBuild(location.getWorld(), new Random(), location, 0, fill).start();
            getLogger().info("Generated " + structureName + " structure (" + generatedParts + " parts, " + (System.currentTimeMillis() - processTime) + " ms)");
            sendTo(sender, ChatColor.GOLD + "ストラクチャから " + generatedParts + " パーツを生成しました " + ChatColor.GRAY + "(" + (System.currentTimeMillis() - processTime) + " ms)");

        } catch (WorldEditException e) {
            e.printStackTrace();
        }

    }

    private @NotNull List<String> completeTestBuild(CommandSender sender, String label, List<String> args) {
        if (args.size() == 1) {
            return generateSuggests(args.get(0), plugin.getSchematics().keySet().toArray(new String[0]));
        }
        return Collections.emptyList();
    }


    private void onGiveSpawner(CommandSender sender, List<String> args) {
        if (!(sender.getSender() instanceof Player player)) {
            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
            return;
        }

        MythicMobsBridge.Instance mgr = SimpleJigsawPlugin.getMythicMobsBridge().get();
        if (mgr == null) {
            sendTo(sender, ChatColor.RED + "MythicMobsを利用できません");
            return;
        }

        String name;
        try {
            name = args.get(0);
        } catch (IndexOutOfBoundsException e) {
            sendTo(sender, ChatColor.RED + "テンプレート名を指定してください");
            return;
        }
        MythicConfig config = mgr.getTemplateByName(name);
        if (config == null) {
            sendTo(sender, ChatColor.RED + "指定されたテンプレートがありません");
            return;
        }
        String mobName = config.getString("MobName");
        String level = config.getString("MobLevel");

        if (args.size() >= 2) {
            try {
                mobName = args.get(1);
            } catch (IndexOutOfBoundsException e) {
                sendTo(sender, ChatColor.RED + "対象のMob名を指定してください");
                return;
            }

            if (mobName.contains(":")) {
                String[] split = mobName.split(":");
                mobName = split[0];

                try {
                    level = split[1];
                    new RandomInt(level);  // test
                } catch (Exception e) {
                    sendTo(sender, ChatColor.RED + "Mobレベルの指定が無効です。整数または範囲(1, 2to5, etc)である必要があります");
                    return;
                }
            }
        }

        MSpawner setting = new MSpawner(name, mobName, level);
        ItemStack itemStack = setting.createBlockItem();

        player.getInventory().addItem(itemStack);
    }

    private void onCreateTemplate(CommandSender sender, List<String> args) {
        MythicMobsBridge.Instance mgr = SimpleJigsawPlugin.getMythicMobsBridge().get();
        if (mgr == null) {
            sendTo(sender, ChatColor.RED + "MythicMobsを利用できません");
            return;
        }

        if (args.isEmpty()) {
            sendTo(sender, ChatColor.RED + "MythicMobsスポナーを指定してください");
            return;
        }

        SpawnerManager spawners = MythicBukkit.inst().getSpawnerManager();
        MythicSpawner spawner = spawners.getSpawnerByName(args.get(0));

        if (spawner == null) {
            sendTo(sender, ChatColor.RED + "指定されたスポナーがありません");
            return;
        }

        mgr.addFromMythicSpawner(spawner, spawner.getName());
        sendTo(sender, ChatColor.GOLD + "テンプレート " + spawner.getName() + " として保存しました");

    }

    private List<String> compGiveSpawner(CommandSender sender, String label, List<String> args) {
        MythicMobsBridge.Instance mgr = SimpleJigsawPlugin.getMythicMobsBridge().get();
        if (mgr != null && args.size() == 1) {
            return generateSuggests(args.get(0), mgr.getTemplateNames().toArray(new String[0]));
        }
        return null;
    }


    private void cmdReload(CommandSender sender, List<String> args) {
        int success = 0;
        try {
            plugin.reload();
            success++;
        } catch (Throwable e) {
            e.printStackTrace();
            sendTo(sender, ChatColor.RED + "構造設定の読み込み中にエラーが発生しました");
        }

        MythicMobsBridge.Instance mgr = SimpleJigsawPlugin.getMythicMobsBridge().get();
        if (mgr != null) {
            try {
                mgr.loadAll();
                success++;
            } catch (Throwable e) {
                e.printStackTrace();
                sendTo(sender, ChatColor.RED + "スポナーテンプレート設定の読み込み中にエラーが発生しました");
            }
        } else {
            success++;
        }

        if (success >= 2)
            sendTo(sender, ChatColor.GOLD + "設定ファイルを再読み込みしました");

    }

    public static MainCommand registerCommand(SimpleJigsawPlugin plugin) {
        MainCommand mainCommand = new MainCommand(plugin);
        PluginCommand command = plugin.getCommand("simplejigsaw");
        if (command == null) {
            SimpleJigsawPlugin.getLog().warning("Command not registered in plugin.yml");
        } else {
            CommandBukkit.register(mainCommand, command);
        }
        return mainCommand;
    }

    private static void sendTo(CommandSender sender, String message) {
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }

}
