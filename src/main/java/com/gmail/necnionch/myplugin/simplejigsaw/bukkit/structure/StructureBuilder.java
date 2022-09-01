package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.SimpleJigsawPlugin;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawConnector;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawPart;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.WrapperPasteBuilder;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.bUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StructureBuilder {

    private final int maxSize;
    private final Map<String, List<JigsawPart>> partsOfPool;
    private final Structure structure;
    private final JigsawPart[] parts;
    private @Nullable JigsawPart firstPart;
    private @Nullable Map<String, List<JigsawConnector>> poolOfConnectors;  // caching
    private @Nullable Map<String, List<JigsawConnector>> poolOfEndConnectors;  // caching
    private final Set<String> structuredBlockLocations = Sets.newHashSet();

    private final Set<BlockType> whitelistBlockTypes = BlockType.REGISTRY.values().stream()
            .filter(bType -> !bType.equals(BlockTypes.STRUCTURE_VOID))
            .collect(Collectors.toSet());

    public StructureBuilder(Structure structure, int maxSize, Map<String, List<JigsawPart>> partsOfPool) {
        this.structure = structure;
        this.maxSize = maxSize;
        this.partsOfPool = partsOfPool;
        this.parts = partsOfPool.values().stream()
                .flatMap(Collection::stream)
                .toArray(JigsawPart[]::new);
    }

    private Logger getLogger() {
        return SimpleJigsawPlugin.getLog();
    }

    public Structure getStructure() {
        return structure;
    }

    public List<JigsawConnector> getConnectorsByPool(String pool) {
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
            return Collections.emptyList();

        return Collections.unmodifiableList(poolOfConnectors.get(pool));
    }

    public List<JigsawConnector> getEndConnectorsByPool(String pool) {
        if (poolOfEndConnectors == null) {
            poolOfEndConnectors = Maps.newHashMap();

            for (JigsawPart part : parts) {
                for (JigsawConnector connector : part.getConnectors()) {
                    if (!connector.getPool().isEmpty()) {
                        if (part.getConnectors().size() == 1) {
                            if (!poolOfEndConnectors.containsKey(connector.getPool())) {
                                poolOfEndConnectors.put(connector.getPool(), Lists.newArrayList(connector));
                            } else {
                                poolOfEndConnectors.get(connector.getPool()).add(connector);
                            }
                        }
                    }
                }
            }
        }

        if (!poolOfEndConnectors.containsKey(pool))
            return Collections.emptyList();

        return Collections.unmodifiableList(poolOfEndConnectors.get(pool));
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

    public @Nullable JigsawPart getRandomPartFromStartPool() {
        SchematicPool pool = structure.getStartPool();
        if (pool == null)
            return null;

        String poolName = pool.getName();
        List<JigsawPart> parts = partsOfPool.get(poolName);
        if (parts.isEmpty())
            return null;

        return parts.get(new Random().nextInt(parts.size()));
    }

    public int build(EditSession session, BlockVector3 position, int angle) throws WorldEditException {
       return build(session, new Random(), position, angle);
    }

    public int build(EditSession session, Random random, BlockVector3 position, int angle) throws WorldEditException {
        if (firstPart == null)
            firstPart = getRandomPartFromStartPool();
        if (firstPart == null)
            throw new IllegalArgumentException("no selected first part");

        if (maxSize <= 0)
            return 0;

        structuredBlockLocations.clear();
        Clipboard clipboard = firstPart.getClipboard();
        ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);

        if (angle != 0)
            clipboardHolder.setTransform(new AffineTransform().rotateY(-angle));

//        Operations.complete(clipboardHolder
//                .createPaste(session)
//                .to(position)
//                .maskSource(createNonReplaceMaskStructureVoid(clipboard))
//                .build()
//        );
        Operations.complete(new WrapperPasteBuilder(clipboardHolder, session, clipboard.getOrigin())
                .to(position)
                .maskSource(createNonReplaceMaskStructureVoid(clipboard))
                .build()
        );

        ConflictTestResult test = testConflictBlocks(firstPart, position, firstPart.toRelativeLocation(clipboard.getOrigin()), angle);
        structuredBlockLocations.addAll(test.locationNames);

        return 1 + buildJigsawConnectors(session, random, firstPart, position, angle, 1);
    }


    private int expandJigsawPart(ConnectInstance connect) throws WorldEditException {
        JigsawConnector.Orientation orient = connect.getOppositeOrientation();  // 接続先に繋がる向き
        BlockVector3 position = connect.getPosition();  // 接続先に繋がる位置 (つまりここorigin)

        JigsawConnector to = selectConnector(connect);
        if (to == null)
            return 0;

        // 貼り付ける
//        Clipboard clipboard = to.getJigsawPart().setClipboardOriginToConnector(to);
        Clipboard clipboard = to.getJigsawPart().getClipboard();
        ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
//
        int newRotation = orient.getAngle();
        newRotation = newRotation - to.getOrientation().getAngle();

        if (newRotation != 0)
            clipboardHolder.setTransform(new AffineTransform().rotateY(-newRotation));

//        Operations.complete(clipboardHolder
//                .createPaste(connect.getSession())
//                .to(position)
//                .maskSource(createNonReplaceMaskStructureVoid(clipboard))
//                .build()
//        );
        Operations.complete(new WrapperPasteBuilder(clipboardHolder, connect.getSession(), to.getOriginalLocation())
                .to(position)
                .maskSource(createNonReplaceMaskStructureVoid(clipboard))
                .build()
        );
//        markStructured(to.getJigsawPart(), position, to.getJigsawPart().toRelativeLocation(clipboard.getOrigin()), newRotation, Color.YELLOW);

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

            if (ori.getY() == 0) {
                ori = JigsawConnector.Orientation.ofFlatAngle(newAngle);

            } else {
                boolean aligned = JigsawConnector.JointType.ALIGNED.equals(conn.getJointType());
                if (ori.getY() > 0) {
                    if (aligned) {
                        ori = JigsawConnector.Orientation.ofUpAngle(newAngle);  // 角度を変える
                    } else {
                        ori = JigsawConnector.Orientation.UP_AXIS[connect.getRandom().nextInt(JigsawConnector.Orientation.UP_AXIS.length)];
                    }
                } else {
                    if (aligned) {
                        ori = JigsawConnector.Orientation.ofDownAngle(newAngle);
                    } else {
                        ori = JigsawConnector.Orientation.DOWN_AXIS[connect.getRandom().nextInt(JigsawConnector.Orientation.DOWN_AXIS.length)];
                    }
                }
            }

            showParticle(pos, connect.getSession(), Color.BLUE);
            results += 1 + expandJigsawPart(connect.createNextConnect(conn, pos, ori));
        }
        return results;
    }

    private int buildJigsawConnectors(EditSession session, Random random, JigsawPart part, BlockVector3 position, int angle, int size) throws WorldEditException {
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

            if (ori.getY() == 0) {
                ori = JigsawConnector.Orientation.ofFlatAngle(newAngle);

            } else {
                boolean aligned = JigsawConnector.JointType.ALIGNED.equals(conn.getJointType());
                if (ori.getY() > 0) {
                    if (aligned) {
                        ori = JigsawConnector.Orientation.ofUpAngle(newAngle);  // 角度を変える
                    } else {
                        ori = JigsawConnector.Orientation.UP_AXIS[random.nextInt(JigsawConnector.Orientation.UP_AXIS.length)];
                    }
                } else {
                    if (aligned) {
                        ori = JigsawConnector.Orientation.ofDownAngle(newAngle);
                    } else {
                        ori = JigsawConnector.Orientation.DOWN_AXIS[random.nextInt(JigsawConnector.Orientation.DOWN_AXIS.length)];
                    }
                }
            }

            results += expandJigsawPart(new ConnectInstance(session, random, conn, pos, ori, size + 1));
//            break;
        }
        return results;

    }

    private Mask createNonReplaceMaskStructureVoid(Extent extent) {
        return new BlockTypeMask(extent, whitelistBlockTypes);
    }

    private @Nullable JigsawConnector selectConnector(ConnectInstance connect) {
        getLogger().warning("selecting");
        JigsawConnector from = connect.getConnector();
        List<JigsawConnector> targets;
        Predicate<JigsawConnector> connectorsPredicate = conn ->
                conn.getOrientation().isHorizontal() == connect.getOppositeOrientation().isHorizontal()
                        && conn.getName().startsWith(from.getTargetName());
        getLogger().warning("pos: " + connect.getPosition());
        getLogger().warning("size: " + connect.getSize());
        getLogger().warning("rot: " + connect.getOppositeOrientation());

        if (maxSize <= connect.getSize()) {
            System.out.println("maxSize <= connectSize");
            // 最大サイズだった場合は末端パーツ
            targets = getEndConnectorsByPool(from.getPool())
                    .stream()
                    .filter(connectorsPredicate)
                    .collect(Collectors.toList());

            if (targets.isEmpty())
                targets = getConnectorsByPool(from.getPool())
                        .stream()
                        .filter(connectorsPredicate)
                        .collect(Collectors.toList());

        } else {
            targets = getConnectorsByPool(from.getPool())
                    .stream()
                    .filter(connectorsPredicate)
                    .collect(Collectors.toList());
        }

        if (targets.isEmpty()) {
            getLogger().warning("target is not found (pool: " + from.getPool() + ", name: " + from.getName() + ")");
            return null;
        }

        // 被らないか調べた上で選択する
//        System.out.println("checking");
        Map<JigsawConnector, ConflictTestResult> tests = Maps.newHashMap();
        while (!targets.isEmpty()) {
            // 重さ値選別に選別
            int totalWeight = targets.stream()
                    .mapToInt(conn -> conn.getJigsawPart().getPoolEntry().getWeight())
                    .sum();
            int selectWeight = connect.getRandom().nextInt(totalWeight);
            int tmpWeight = 0;
            JigsawConnector to = null;
            for (JigsawConnector target : targets) {
                tmpWeight += target.getJigsawPart().getPoolEntry().getWeight();
                if (selectWeight <= tmpWeight) {
                    to = target;
                    break;
                }
            }

            if (to == null)
                throw new IllegalStateException("program error");

            // 重なりをテストする
            int newRotation = connect.getOppositeOrientation().getAngle() - to.getOrientation().getAngle();
            ConflictTestResult conflict = testConflictBlocks(to.getJigsawPart(), connect.getPosition(), to.getRelativeLocation(), newRotation);

            System.out.println("conflict test: " + conflict.conflictCount + ", " + to.getJigsawPart().getPoolEntry().getFileName());
            if (conflict.conflictCount <= 0) {  // 被っていなかったら確定
                structuredBlockLocations.addAll(conflict.locationNames);
                getLogger().info("resolved");
                return to;
            }

            targets.remove(to);
            tests.put(to, conflict);
//            System.out.println("conflict: " + to.getStructure().getName() + ", count: " + conflict.conflictCount);
            System.out.println("conflict");
        }

        if (true)
            return null;
        // 全て重なる場合は、一番重なりが少ない物を選ぶ
        Map.Entry<JigsawConnector, ConflictTestResult> hit = tests.entrySet().stream()
                .min(Comparator.comparingInt(e -> e.getValue().conflictCount))
                .orElse(null);
        if (hit == null)
            return null;
        structuredBlockLocations.addAll(hit.getValue().locationNames);
//        System.out.println("conflict resolve: " + hit.getKey().getJigsawPart().getPoolEntry().getFileName());
        return hit.getKey();
    }


    private ConflictTestResult testConflictBlocks(JigsawPart part, BlockVector3 position, BlockVector3 center, int rotate) {
        Set<String> locations = Sets.newHashSet();
        int conflicts = 0;
        for (BlockVector3 pos : part.getFilledBlockLocations()) {
            pos = bUtils.rotate90(rotate, pos, center);
            BlockVector3 pos2 = position.add(pos);

            String locationName = String.format("%d,%d,%d", pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ());
            if (structuredBlockLocations.contains(locationName))
                conflicts++;

            locations.add(locationName);
        }
        return new ConflictTestResult(locations, conflicts);
    }

    private void showParticle(BlockVector3 loc, EditSession session, Color color) {
        org.bukkit.World world = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(session.getWorld());
        getLogger().info(color + ": " + loc);
        org.bukkit.Particle.DustOptions dust = new org.bukkit.Particle.DustOptions(color, 1);
        SimpleJigsawPlugin pl = JavaPlugin.getPlugin(SimpleJigsawPlugin.class);
        pl.getServer().getScheduler().scheduleSyncRepeatingTask(pl, () -> {
            world.spawnParticle(org.bukkit.Particle.REDSTONE, loc.getBlockX() + .5, loc.getBlockY() + 1.5, loc.getBlockZ() + .5, 1, 0, 0, 0, dust);
        }, 0, 1);
    }


    private static class ConnectInstance {
        private final EditSession session;
        private final int size;
        private final BlockVector3 position;
        private final JigsawConnector.Orientation oppositeOrientation;
        private final JigsawConnector connector;
        private final Random random;

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

        public Random getRandom() {
            return random;
        }

        public ConnectInstance createNextConnect(JigsawConnector connector, BlockVector3 position, JigsawConnector.Orientation orientation) {
            return new ConnectInstance(this, random, connector, position, orientation);
        }


        public ConnectInstance(ConnectInstance connectInstance, Random random, JigsawConnector connector, BlockVector3 position, JigsawConnector.Orientation orientation) {
            this.size = connectInstance.size + 1;
            this.session = connectInstance.session;
            this.random = random;
            this.connector = connector;
            this.position = position;
            this.oppositeOrientation = orientation;
        }

        public ConnectInstance(EditSession session, Random random, JigsawConnector connector, BlockVector3 position, JigsawConnector.Orientation orientation, int size) {
            this.session = session;
            this.random = random;
            this.connector = connector;
            this.position = position;
            this.oppositeOrientation = orientation;
            this.size = size;
        }

    }


    private static class ConflictTestResult {

        private final Set<String> locationNames;
        private final int conflictCount;

        public ConflictTestResult(Set<String> locationNames, int conflicts) {
            this.locationNames = locationNames;
            this.conflictCount = conflicts;
        }

    }

}
