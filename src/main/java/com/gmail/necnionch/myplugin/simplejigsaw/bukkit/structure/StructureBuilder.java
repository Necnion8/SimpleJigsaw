package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawConnector;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawPart;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.bUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
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
import org.bukkit.Color;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StructureBuilder {

    private final int maxSize;
    private final JigsawPart[] parts;
    private @Nullable JigsawPart firstPart;
    private @Nullable Map<String, List<JigsawConnector>> poolOfConnectors;  // caching

    public StructureBuilder(int maxSize, JigsawPart[] parts) {
        this.maxSize = maxSize;
        this.parts = parts;
    }

    private Logger getLogger() {
        return SimpleJigsawPlugin.getLog();
    }

    public Stream<JigsawConnector> filteredConnectors(String pool, String namePrefix) {
        if (poolOfConnectors == null) {
            poolOfConnectors = Maps.newHashMap();
            for (JigsawPart part : parts) {
                for (JigsawConnector connector : part.getConnectors()) {
                    if (!connector.getPool().isEmpty()) {
                        if (!poolOfConnectors.containsKey(connector.getPool())) {
                            poolOfConnectors.put(connector.getPool(), Lists.newArrayList(connector));
                        } else {
                            poolOfConnectors.get(connector.getPool()).add(connector);
                        }
                    }
                }
            }
        }

        if (!poolOfConnectors.containsKey(pool))
            return Stream.empty();

        return poolOfConnectors.get(pool)
                .stream()
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
                .maskSource(createNonReplaceMaskStructureVoid(clipboard))
                .build()
        );

        return 1 + buildJigsawConnectors(session, firstPart, position, angle, 1);
    }

    private int expandJigsawPart(ConnectInstance connect) throws WorldEditException {
        JigsawConnector from = connect.getConnector();  // 接続先
        JigsawConnector.Orientation orient = connect.getOppositeOrientation();  // 接続先に繋がる向き
        BlockVector3 position = connect.getPosition();  // 接続先に繋がる位置 (つまりここorigin)

        // 処理4
        List<JigsawConnector> targets = filteredConnectors(from.getPool(), from.getTargetName())
                .filter(conn -> conn.getOrientation().isHorizontal() == orient.isHorizontal())
                .collect(Collectors.toList());

        if (targets.isEmpty()) {
            getLogger().warning("target is not found (pool: " + from.getPool() + ", name: " + from.getName() + ")");
            return 0;
        }

        // TODO: ランダム選別から重さ値選別に変える
        // 処理5 - 被らないか調べた上で選択する
        // TODO: 最大サイズだった場合は、パーツが属するプールの中から優先的に末端パーツを引く
        JigsawConnector to = targets.get(new Random().nextInt(targets.size()));

        // 貼り付ける
        Clipboard clipboard = to.getJigsawPart().setClipboardOriginToConnector(to);
        ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);

        int newRotation = orient.getAngle();
        newRotation = newRotation - to.getOrientation().getAngle();

        if (newRotation != 0)
            clipboardHolder.setTransform(new AffineTransform().rotateY(-newRotation));

        Operations.complete(clipboardHolder
                .createPaste(connect.getSession())
                .to(position)
                .maskSource(createNonReplaceMaskStructureVoid(clipboard))
                .build()
        );

        // 最大サイズなら終了
        if (maxSize <= connect.getSize())
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

    private int buildJigsawConnectors(EditSession session, JigsawPart part, BlockVector3 position, int angle, int size) throws WorldEditException {
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
                ori = JigsawConnector.Orientation.ofFlatAngle(newAngle);
            }

            results += expandJigsawPart(new ConnectInstance(session, conn, pos, ori, size + 1));
//            break;
        }
        return results;

    }

    private Mask createNonReplaceMaskStructureVoid(Extent extent) {
        return new BlockTypeMask(extent, BlockType.REGISTRY.values().stream()
                .filter(bType -> !bType.equals(BlockTypes.STRUCTURE_VOID))
                .collect(Collectors.toSet()));
    }

    private void showParticle(BlockVector3 loc, EditSession session, Color color) {
//        World world = BukkitAdapter.adapt(session.getWorld());
////        getLogger().info(name + ": " + loc);
//        Particle.DustOptions dust = new Particle.DustOptions(color, 1);
//        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
//            world.spawnParticle(Particle.REDSTONE, loc.getBlockX() + .5, loc.getBlockY() + 1.5, loc.getBlockZ() + .5, 1, 0, 0, 0, dust);
//        }, 0, 1);
    }


    public static class ConnectInstance {
        private final EditSession session;
        private final int size;
        private final BlockVector3 position;
        private final JigsawConnector.Orientation oppositeOrientation;
        private final JigsawConnector connector;

        public EditSession getSession() {
            return session;
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
            this.session = connectInstance.session;
            this.connector = connector;
            this.position = position;
            this.oppositeOrientation = orientation;
        }

        public ConnectInstance(EditSession session, JigsawConnector connector, BlockVector3 position, JigsawConnector.Orientation orientation, int size) {
            this.session = session;
            this.connector = connector;
            this.position = position;
            this.oppositeOrientation = orientation;
            this.size = size;
        }

    }

}
