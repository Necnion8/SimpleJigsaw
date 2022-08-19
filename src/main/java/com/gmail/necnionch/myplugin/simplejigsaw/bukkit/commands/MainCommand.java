package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.commands;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.hooks.WorldEditBridge;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawConnector;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawPart;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.bUtils;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandBukkit;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.CommandSender;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.RootCommand;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainCommand extends RootCommand {

    private final SimpleJigsawPlugin plugin;
    private final WorldEditBridge worldEdit;

    private final List<String> schematicFiles = Lists.newArrayList();


    public MainCommand(SimpleJigsawPlugin plugin) {
        this.plugin = plugin;
        worldEdit = SimpleJigsawPlugin.getWorldEdit();

        addCommand("sample_set", null, (sender, args) -> {
            if (args.isEmpty()) {
                sendTo(sender, ChatColor.RED + "WorldEdit schematicsファイル名(拡張子なし)を指定してください (複数可)");
                return;
            }

            schematicFiles.addAll(args);
            sendTo(sender, ChatColor.GOLD + "設定しました。次回再起動まで有効です");
        });

        addCommand("sample_build", null, (sender, args) -> {
            String firstSchemName;
            int size;
            try {
                firstSchemName = args.get(0);
                size = Math.max(0, Integer.parseInt(args.get(1)));
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                sendTo(sender, ChatColor.RED + "開始schem名とサイズを指定してください");
                return;
            }

            Location loc;
            if (sender.getSender() instanceof Player player) {
                loc = player.getLocation();
            } else if (sender.getSender() instanceof BlockCommandSender block) {
                loc = block.getBlock().getLocation().add(0, 1, 0);
            } else {
                sendTo(sender, ChatColor.RED + "プレイヤーあるいはコマブロのみ実行できるコマンドです");
                return;
            }

            if (schematicFiles.isEmpty()) {
                sendTo(sender, ChatColor.RED + "schematicが1つも設定されていません");
                return;
            }

            JigsawPart firstPart = null;

            List<JigsawPart> parts = Lists.newArrayList();

            for (String name : schematicFiles) {
                File file = Paths.get("plugins", "WorldEdit", "schematics", name + ".schem").toFile();
                Clipboard clipboard = worldEdit.loadSchematic(file);
                if (clipboard == null)
                    continue;

                JigsawPart part = worldEdit.createJigsawPartOf(clipboard, true);
                getLogger().info("Loaded " + part.getConnectors().size() + " size, from " + name + " of " + part);

                if (name.equalsIgnoreCase(firstSchemName))
                    firstPart = part;

                parts.add(part);
            }

            if (firstPart == null) {
                sendTo(sender, ChatColor.RED + "指定されたschemが見つかりません");
                return;
            }

            BlockVector3 location = bUtils.toBlockVector3(loc);
            Bukkit.getScheduler().cancelTasks(plugin);
            StructureInstance structure = new StructureInstance(size, parts.toArray(new JigsawPart[0]));
            structure.setFirstPart(firstPart);

            try (EditSession session = worldEdit.newEditSession(loc.getWorld())) {
                int generatedParts = structure.build(session, location, 0);
                getLogger().info("generated " + generatedParts + " parts");

            } catch (WorldEditException e) {
                e.printStackTrace();
            }

        });

        addCommand("test", null, this::cmdTest);
        addCommand("test2", null, this::cmdTest2);
        addCommand("test3", null, this::cmdTest3);
        addCommand("testcb", null, this::cmdTestCommandBlock);
        addCommand("testcb2", null, this::cmdTestCommandBlock2);
        addCommand("testp", null, (sender, args) -> {
            Player p = (Player) sender.getSender();
            World w = p.getWorld();
            BlockVector3 loc = bUtils.toBlockVector3(p.getLocation());

            showParticle("p", loc, w, Color.AQUA);

            BlockVector3 a = BlockVector3.at(-2, 0 ,0);
            showParticle("", loc.add(bUtils.rotate90(0, a)), w, Color.YELLOW);
            showParticle("", loc.add(bUtils.rotate90(90, a)), w, Color.RED);
            showParticle("", loc.add(bUtils.rotate90(180, a)), w, Color.LIME);
            showParticle("", loc.add(bUtils.rotate90(-90, a)), w, Color.PURPLE);

//            BlockVector3 loc2 = loc.add(BlockVector3.at(3, 0, 0).transform2D(90, 0, 0, 0, 0));
//            showParticle("p", loc2, w, Color.GREEN);
//
//
//            BlockVector3 loc4 = BlockVector3.at(0, 0, 0).subtract(BlockVector3.at(2, 0, 0));
//            loc4 = loc4.transform2D(90, 0, 0, 0, 0);
//            BlockVector3 loc3 = loc.subtract(loc4);
//            showParticle("p", loc3, w, Color.RED);


        });
    }

    private Logger getLogger() {
        return plugin.getLogger();
    }

    private void cmdTest(CommandSender sender, List<String> args) {
        if (!(sender.getSender() instanceof Player player)) {
            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
            return;
        }

//        ClipboardHolder clipboardHolder = worldEdit.getClipboard(player);
//        if (clipboardHolder == null) {
//            sendTo(sender, ChatColor.RED + "Empty clipboard");
//            return;
//        }
//
//        Clipboard clipboard = clipboardHolder.getClipboard();
        File file = Paths.get("plugins", "WorldEdit", "schematics", "jigsaw.schem").toFile();
        Clipboard clipboard = worldEdit.loadSchematic(file);
        if (clipboard == null)
            return;

        JigsawPart jigsawPart = worldEdit.createJigsawPartOf(clipboard);
        if (jigsawPart != null) {
            sendTo(sender, "origin: " + jigsawPart.getOrigin());
            for (JigsawConnector connector : jigsawPart.getConnectors()) {
                sendTo(sender, "Found jigsaw : " + connector.getRelativeLocation() + " " + connector.getOriginalLocation());
                sendTo(sender, "  pool:   " + connector.getPool());
                sendTo(sender, "  name:   " + connector.getName());
                sendTo(sender, "  target: " + connector.getTargetName());
                sendTo(sender, "  final:  " + connector.getFinalBlockState());
                sendTo(sender, "  joint:  " + connector.getJointType());
                sendTo(sender, "  orient: " + connector.getOrientation().name());
//            break;
            }
        }

    }

    private void cmdTest2(CommandSender sender, List<String> args) {
        if (!(sender.getSender() instanceof Player player)) {
            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
            return;
        }

        ClipboardHolder clipboardHolder = worldEdit.getClipboard(player);
        if (clipboardHolder == null) {
            sendTo(sender, ChatColor.RED + "Empty clipboard");
            return;
        }

        World world = player.getWorld();
        Clipboard clipboard = clipboardHolder.getClipboard();

        Location orig = new Location(world, clipboard.getOrigin().getBlockX() + 0.5, clipboard.getOrigin().getBlockY(), clipboard.getOrigin().getBlockZ() + 0.5);
        player.teleport(orig);
//        plugin.getLogger().info("" + clipboard.getOrigin());

        JigsawPart jigsawPart = worldEdit.createJigsawPartOf(clipboard);

        sendTo(sender, jigsawPart.getConnectors().size() + " size");

        Location location = player.getLocation();
        for (JigsawConnector connector : jigsawPart.getConnectors()) {
            Location newLocation = location.clone();
            newLocation = new Location(world, 0, 0, 0);
            newLocation = newLocation.add(connector.getRelativeLocation().getBlockX(), connector.getRelativeLocation().getBlockY(), connector.getRelativeLocation().getBlockZ());
//            newLocation.add(jigsaw.getLocation().getBlockX(), jigsaw.getLocation().getBlockY(), jigsaw.getLocation().getBlockZ());
//            player.teleport(newLocation);
            plugin.getLogger().info("" + newLocation);
            world.setType(newLocation.add(0, 1, 0), Material.STONE);
        }

    }

    private void cmdTest3(CommandSender sender, List<String> args) {
        if (!(sender.getSender() instanceof Player player)) {
            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
            return;
        }

        ClipboardHolder clipboardHolder = worldEdit.getClipboard(player);
        if (clipboardHolder == null) {
            sendTo(sender, ChatColor.RED + "Empty clipboard");
            return;
        }

        World world = player.getWorld();
        Clipboard clipboard = clipboardHolder.getClipboard();

        JigsawPart jigsawPart = worldEdit.createJigsawPartOf(clipboard);

        sendTo(sender, jigsawPart.getConnectors().size() + " size");

        Location location = player.getLocation();
        for (JigsawConnector connector : jigsawPart.getConnectors()) {
            Location newLocation = location.clone();
//            newLocation = new Location(world, 0, 0, 0);
            newLocation = newLocation.add(connector.getRelativeLocation().getBlockX(), connector.getRelativeLocation().getBlockY(), connector.getRelativeLocation().getBlockZ());
//            newLocation.add(jigsaw.getLocation().getBlockX(), jigsaw.getLocation().getBlockY(), jigsaw.getLocation().getBlockZ());
//            player.teleport(newLocation);
            plugin.getLogger().info("" + newLocation);
            world.setType(newLocation.add(0, 1, 0), Material.STONE);
        }

    }

    private void cmdTestCommandBlock(CommandSender sender, List<String> args) {
        Location location;
        if (sender.getSender() instanceof BlockCommandSender blockSender) {
            location = blockSender.getBlock().getLocation().add(0, 1, 0);
//            return;
        } else if (sender.getSender() instanceof Player player) {
            location = player.getLocation();
        } else {
            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
            return;
        }


        List<JigsawConnector> jigsaws = Lists.newArrayList();
        List<JigsawConnector> targetJigsaws = Lists.newArrayList();

        Stream.of("jigsaw.schem", "jigsaw2.schem").forEachOrdered(name -> {
            File file = Paths.get("plugins", "WorldEdit", "schematics", name).toFile();
            Clipboard clipboard = worldEdit.loadSchematic(file);
            if (clipboard == null)
                return;

            JigsawPart part = worldEdit.createJigsawPartOf(clipboard);
            getLogger().info("Loaded " + part.getConnectors().size() + " size, from " + name + " of " + part);

            jigsaws.addAll(part.getJigsawsByName("minecraft:example1"));
            targetJigsaws.addAll(part.getJigsawsByTargetName("minecraft:example1")
                    .stream()
                .filter(conn -> conn.getOrientation().isHorizontal())
                .collect(Collectors.toList()));
        });

        if (targetJigsaws.isEmpty()) {
            getLogger().info("empty target jigsaws");
            return;
        }

        Random rand = new Random();

        BlockVector3 baseLoc = bUtils.toBlockVector3(location);

        Bukkit.getScheduler().cancelTasks(plugin);

        try (EditSession session = worldEdit.newEditSession(location.getWorld())) {
//        try (EditSession session = worldEdit.newEditSession(((Player) sender.getSender()))) {

            // select first part
            JigsawPart firstPart = jigsaws.get(0).getJigsawPart();
            Clipboard firstPartClipboard = firstPart.getClipboard();
            ClipboardHolder clipboardHolder = new ClipboardHolder(firstPartClipboard);
            Mask mask = createBlockTypeIgnoreMask(firstPartClipboard);
            Operations.complete(clipboardHolder
                    .createPaste(session)
                    .to(baseLoc)
//                    .maskSource(createBlockTypeIgnoreMask())
                    .maskSource(mask)
                    .build());

            for (JigsawConnector from : firstPart.getConnectors()) {
                JigsawConnector.Orientation orient = from.getOrientation();
                getLogger().info("= from ================>");

                BlockVector3 fromPos = baseLoc.add(from.getRelativeLocation());
                showParticle("p", fromPos, location.getWorld(), Color.RED);

                // 子を召喚
                JigsawConnector to = targetJigsaws.get(rand.nextInt(targetJigsaws.size()));
//                JigsawConnector to = targetJigsaws.get(targetJigsaws.size()-2);
                getLogger().info("To Part: " + to.getJigsawPart());

                BlockVector3 partSize = to.getJigsawPart().getSize();

                Clipboard fromClipboard = to.getJigsawPart().getClipboard();
                fromClipboard.setOrigin(to.getOriginalLocation());
                clipboardHolder = new ClipboardHolder(fromClipboard);

                int rotate = to.getOrientation().rotateAngleTo(orient.getOpposite());
                getLogger().info("rotate: " + rotate + ", in: " + orient.getAngle() + " <-> " + to.getOrientation().getAngle() + "?");

                if (rotate != 0)
                    clipboardHolder.setTransform(new AffineTransform().rotateY(rotate));

                BlockVector3 newPos = fromPos.add(orient.getX(), 0, orient.getZ());

                getLogger().info("size: " + partSize);

                getLogger().info("conn place to " + newPos + ", orientation " + orient);
                mask = createBlockTypeIgnoreMask(fromClipboard);
                Operations.complete(clipboardHolder
                        .createPaste(session)
                        .to(newPos)
                        .maskSource(mask)
                        .build()
                );

                showParticle("i", newPos, location.getWorld(), Color.YELLOW);

                break;
            }

        } catch (WorldEditException e) {
            e.printStackTrace();
        }

    }

    private void cmdTestCommandBlock2(CommandSender sender, List<String> args) {
        Location location;
        if (sender.getSender() instanceof BlockCommandSender blockSender) {
            location = blockSender.getBlock().getLocation().add(0, 1, 0);
//            return;
        } else if (sender.getSender() instanceof Player player) {
            location = player.getLocation();
        } else {
            sendTo(sender, ChatColor.RED + "プレイヤーのみ実行できるコマンドです");
            return;
        }


        List<JigsawPart> parts = Lists.newArrayList();

        List<String> entries = Lists.newArrayList();
        entries.add("jigsaw.schem");  // CtoC
        entries.add("jigsaw2.schem");  // cLc
        entries.add("jigsaw3.schem");  // C4
        entries.add("jigsaw_end.schem");  // C
        entries.add("jigsaw_up.schem");
//        entries.add("jigsaw_down.schem");

        entries.forEach(name -> {
            File file = Paths.get("plugins", "WorldEdit", "schematics", name).toFile();
            Clipboard clipboard = worldEdit.loadSchematic(file);
            if (clipboard == null)
                return;

            JigsawPart part = worldEdit.createJigsawPartOf(clipboard, false);
            getLogger().info("Loaded " + part.getConnectors().size() + " size, from " + name + " of " + part);

//            if (parts.isEmpty())
                parts.add(part);
        });

        Bukkit.getScheduler().cancelTasks(plugin);
        StructureInstance structure = new StructureInstance(7, parts.toArray(new JigsawPart[0]));
        structure.setFirstPart(parts.get(0));

        try (EditSession session = worldEdit.newEditSession(location.getWorld())) {
            int generatedParts = structure.build(session, bUtils.toBlockVector3(location), 0);
            getLogger().info("generated " + generatedParts + " parts");

        } catch (WorldEditException e) {
            e.printStackTrace();
        }

    }

    private void showParticle(String name, BlockVector3 loc, World world, Color color) {
        getLogger().info(name + ": " + loc);
        Particle.DustOptions dust = new Particle.DustOptions(color, 1);
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            world.spawnParticle(Particle.REDSTONE, loc.getBlockX() + .5, loc.getBlockY() + 1.5, loc.getBlockZ() + .5, 1, 0, 0, 0, dust);
        }, 0, 1);

    }

    private void showParticle(BlockVector3 loc, EditSession session, Color color) {
        World world = BukkitAdapter.adapt(session.getWorld());
//        getLogger().info(name + ": " + loc);
        Particle.DustOptions dust = new Particle.DustOptions(color, 1);
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            world.spawnParticle(Particle.REDSTONE, loc.getBlockX() + .5, loc.getBlockY() + 1.5, loc.getBlockZ() + .5, 1, 0, 0, 0, dust);
        }, 0, 1);

    }


    private Mask createBlockTypeIgnoreMask(Extent extent) {
//        return Masks.negate(new BlockTypeMask(new NullExtent(), BlockTypes.IRON_BLOCK));
        return new BlockTypeMask(extent, BlockType.REGISTRY.values().stream()
                .filter(bType -> !bType.equals(BlockTypes.STRUCTURE_VOID))
                .collect(Collectors.toSet()));
    }


    private int expandJigsawPart(ConnectInstance connect) throws WorldEditException {
//        Clipboard clipboard = connect.getClipboard();
        JigsawConnector from = connect.getConnector();  // 接続先
        JigsawConnector.Orientation orient = connect.getOppositeOrientation();  // 接続先に繋がる向き
        BlockVector3 position = connect.getPosition();  // 接続先に繋がる位置 (つまりここorigin)

        // 処理4
        List<JigsawConnector> targets = connect.getStructure().filteredConnectors(from.getPool(), from.getTargetName())
                .filter(conn -> conn.getOrientation().isHorizontal() == orient.isHorizontal())
                .collect(Collectors.toList());

        if (targets.isEmpty()) {
            plugin.getLogger().warning("target is not found (pool: " + from.getPool() + ", name: " + from.getName() + ")");
            return 0;
        }

        // TODO: ランダム選別から重さ値選別に変える
        // 処理5 - 被らないか調べた上で選択する
        // TODO: 最大サイズだった場合は、パーツが属するプールの中から優先的に末端パーツを引く
        JigsawConnector to = targets.get(new Random().nextInt(targets.size()));

        // 貼り付ける

        Clipboard clipboard = to.getJigsawPart().getClipboard();  // Clipboardを取得
        clipboard.setOrigin(to.getOriginalLocation());  // Clipboardのoriginを接続側のジグソー座標に設定
        ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);

        // connect.getRotation()とtoの向きから必要な回転角度を求める
//        int newRotation = to.getOrientation().rotateAngleTo(orient);
        int newRotation = orient.getAngle();

        newRotation = newRotation - to.getOrientation().getAngle();

        if (newRotation != 0)
            clipboardHolder.setTransform(new AffineTransform().rotateY(-newRotation));

//        if (orient.isVertical()) {
            // TODO: 上下方向だった場合、JointTypeを考慮する。配置位置も変わる
//            throw new IllegalArgumentException("not Implemented");
//        }

        Operations.complete(clipboardHolder
                .createPaste(connect.getSession())
                .to(position)
                .maskSource(createBlockTypeIgnoreMask(clipboard))
                .build()
        );


        // 最大サイズなら終了
        if (connect.getStructure().getMaxSize() <= connect.getSize())
            return 1;

        // 含まれるConnectorを探し、次のパーツのために座標をもとめる
        int results = 0;
        for (JigsawConnector conn : to.getJigsawPart().getConnectors()) {
            if (to.equals(conn))  // 自らは除く
                continue;

            BlockVector3 pos = position;
            JigsawConnector.Orientation ori = conn.getOrientation();

            // connの相対座標+1
            BlockVector3 jigsawRel = conn.getRelativeLocation().subtract(to.getRelativeLocation());

            showParticle(pos.add(bUtils.rotate90(newRotation, jigsawRel)), connect.getSession(), Color.AQUA);

            //                BlockVector3 jigsawRel = BlockVector3.ZERO;
            jigsawRel = jigsawRel.add(conn.getOrientation().toVector());
            // 回転を適用
            jigsawRel = bUtils.rotate90(newRotation, jigsawRel);

            // 現在の位置に足す
            pos = pos.add(jigsawRel);

            int newAngle = newRotation + ori.getOpposite().getAngle();

            if (ori.getY() >= 1) {  // TODO: JointTypeを考慮する
                ori = JigsawConnector.Orientation.ofUpAngle(newAngle);  // 角度を変える
            } else if (ori.getY() <= -1) {
                ori = JigsawConnector.Orientation.ofDownAngle(newAngle);
            } else {
                ori = JigsawConnector.Orientation.ofFlatAngle(newAngle);
            }

            showParticle(pos, connect.getSession(), Color.BLUE);
            results += 1 + expandJigsawPart(connect.createNextConnect(conn, pos, ori));
        }
        return results;
    }

    private int buildJigsawConnectors(StructureInstance structure, EditSession session, JigsawPart part, BlockVector3 position, int angle, int size) throws WorldEditException {
        // 含まれるConnectorを探し、次のパーツのために座標をもとめる
        int results = 0;

        for (JigsawConnector conn : part.getConnectors()) {

            BlockVector3 pos = position;
            JigsawConnector.Orientation ori = conn.getOrientation();

            // connの相対座標+1
            BlockVector3 jigsawRel = conn.getRelativeLocation();
            jigsawRel = jigsawRel.add(conn.getOrientation().toVector());
            // 回転を適用
            jigsawRel = bUtils.rotate90(angle, jigsawRel);

            showParticle(pos.add(bUtils.rotate90(angle, conn.getRelativeLocation())), session, Color.RED);

            // 現在の位置に足す
            pos = pos.add(jigsawRel);  // .transform2D(angle, 0, 0, 0, 0));
            showParticle(pos, session, Color.ORANGE);

            // ジグソーの向きから現在の角度より回転角度を計算
            int newAngle = angle + ori.getOpposite().getAngle();

            if (ori.getY() >= 1) {  // TODO: JointType
                ori = JigsawConnector.Orientation.ofUpAngle(newAngle);  // 角度を変える
            } else if (ori.getY() <= -1) {
                ori = JigsawConnector.Orientation.ofDownAngle(newAngle);
            } else {
                ori = JigsawConnector.Orientation.ofFlatAngle(newAngle);  // .getOpposite();
            }

//            results += expandJigsawPart(connect.createNextConnect(conn, pos, ori));
            results += expandJigsawPart(new ConnectInstance(structure, session, conn, pos, ori, size + 1));
//            break;
        }
        return results;

    }

    public class StructureInstance {

        private final int maxSize;
        private final JigsawPart[] parts;
        private @Nullable JigsawPart firstPart;

        public StructureInstance(int maxSize, JigsawPart[] parts) {
            this.maxSize = maxSize;
            this.parts = parts;
        }

//        public Collection<Object> getPools() { return null; }
        public Stream<JigsawConnector> filteredConnectors(String pool, String namePrefix) {
            // TODO: poolを比較する

            return Stream.of(parts)
                    .flatMap(jigsawPart -> jigsawPart.getConnectors().stream())
                    .filter(conn -> conn.getPool().equals(pool))
                    .filter(conn -> conn.getName().startsWith(namePrefix));
        }
        public int getMaxSize() {
            return maxSize;
        }

        public void setFirstPart(JigsawPart part) {
            if (!Arrays.asList(parts).contains(part))
                throw new IllegalArgumentException("not contains part");
            firstPart = part;
        }

        public @Nullable JigsawPart getFirstPart() {
            return firstPart;
        }

        public int build(EditSession session, BlockVector3 position, int angle) throws WorldEditException {
            if (firstPart == null)
                throw new IllegalArgumentException("no selected first part");

            if (maxSize <= 0)
                return 0;

            Clipboard clipboard = firstPart.getClipboard();
            ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);

            if (angle != 0)
                clipboardHolder.setTransform(new AffineTransform().rotateY(-angle));

            Operations.complete(clipboardHolder
                    .createPaste(session)
                    .to(position)
                    .maskSource(createBlockTypeIgnoreMask(clipboard))
                    .build()
            );

            return 1 + buildJigsawConnectors(this, session, firstPart, position, angle, 1);
        }

    }

    public static class ConnectInstance {
        private final StructureInstance structure;
        private final EditSession session;
        private final int size;
        private final BlockVector3 position;
        private final JigsawConnector.Orientation oppositeOrientation;
        private final JigsawConnector connector;

        public StructureInstance getStructure() {
            return structure;
        }
        public EditSession getSession() {
            return session;
        }
        public JigsawPart getPart() {
            return connector.getJigsawPart();
        }

        public JigsawConnector.Orientation getOppositeOrientation() {  // 接続元が向いてる向き (schem上ではなく実際の)
            return oppositeOrientation;
        }
        public BlockVector3 getPosition() {  // 接続元が向いてる向き (schem上ではなく実際の)
            return position;
        }

        public JigsawConnector getConnector() {
            return connector;
        }

        public int getSize() {
            return size;
        }

        public ConnectInstance createNextConnect(JigsawConnector connector, BlockVector3 position, JigsawConnector.Orientation orientation) {
            return new ConnectInstance(this, connector, position, orientation);
        }


        public ConnectInstance(ConnectInstance connectInstance, JigsawConnector connector, BlockVector3 position, JigsawConnector.Orientation orientation) {
            this.size = connectInstance.size + 1;
            this.structure = connectInstance.structure;
            this.session = connectInstance.session;
            this.connector = connector;
            this.position = position;
            this.oppositeOrientation = orientation;
        }

        public ConnectInstance(StructureInstance structureInstance, EditSession session, JigsawConnector connector, BlockVector3 position, JigsawConnector.Orientation orientation, int size) {
            this.structure = structureInstance;
            this.session = session;
            this.connector = connector;
            this.position = position;
            this.oppositeOrientation = orientation;
            this.size = size;
        }

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
