package com.gmail.necnionch.myplugin.simplejigsaw.bukkit.builder;

import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.config.StructureConfig;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawConnector;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.jigsaw.JigsawPart;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.structure.SchematicPool;
import com.gmail.necnionch.myplugin.simplejigsaw.bukkit.util.bUtils;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StructureBuilder {
    private static final Set<BlockType> whitelistBlockTypes = BlockType.REGISTRY.values().stream()
            .filter(bType -> !bType.equals(BlockTypes.STRUCTURE_VOID))
            .collect(Collectors.toSet());
    final Logger log;
    private final StructureConfig.Schematics schematics;
    private final int maxSize;
    private final ImmutableMultimap<String, JigsawPart> partsOfPool;
    private final Random random;
    private final List<StructurePart> buildParts = Lists.newArrayList();

    public StructureBuilder(Logger logger, StructureConfig.Schematics schematics, @Range(from = 1, to = Integer.MAX_VALUE) int maxSize, ImmutableMultimap<String, JigsawPart> partsOfPool, Random random) {
        log = logger;
        this.schematics = schematics;
        this.maxSize = maxSize;
        this.partsOfPool = partsOfPool;
        this.random = random;
    }

    public List<Operation> buildStructure(Extent target, BlockVector3 position) {
        if (buildParts.isEmpty())
            throw new IllegalArgumentException("No building parts");

        List<Operation> operations = Lists.newArrayList();
        buildStructure(target, position, buildParts.get(0), operations);
        return operations;
    }

    private void buildStructure(Extent target, BlockVector3 position, StructurePart part, List<Operation> operations) {
        operations.addAll(part.buildOperation(target, position));
        for (StructurePart child : part.children()) {
            buildStructure(target, position, child, operations);
        }
    }

    public int buildPartsTree(int angle) throws IllegalArgumentException {
        JigsawPart first = Optional.ofNullable(getRandomPartFromStartPool())
                .orElseThrow(() -> new IllegalArgumentException("Part not found in start pool"));

        buildParts.clear();
        BlockVector3 pos = BlockVector3.ZERO;
        StructurePart sPart = new StructurePart(this, first, null, pos, angle, null);
        buildParts.add(sPart);
        // このパーツに含まれるJigsawに繋がるパーツを探す
        buildPartsTree(sPart, 0);
        return buildParts.size();
    }

    private void buildPartsTree(StructurePart part, int size) {
        JigsawConnector fromPartConnector = part.getFromConnector();

        // パーツの派生元Jigsaw(fromConnector)をチェックし、派生先の配置すべき位置を計算する
        for (JigsawConnector fromConnector : part.getJigsawPart().getConnectors()) {
            // 派生元を除く
            if (fromConnector.equals(fromPartConnector))
                continue;

            BlockVector3 pos = part.getPosition();
            JigsawConnector.Orientation ori = fromConnector.getOrientation();

            // connの相対座標+1
            BlockVector3 jigsawRel = fromConnector.getRelativeLocation();

            if (fromPartConnector != null)
                jigsawRel = jigsawRel.subtract(fromPartConnector.getRelativeLocation());

            jigsawRel = jigsawRel.add(fromConnector.getOrientation().toVector());
            // 回転を適用
            int angle = part.getAngle(); // - fromConnector.getOrientation().getAngle();
            jigsawRel = bUtils.rotate90(angle, jigsawRel);

//            showParticle(pos.add(bUtils.rotate90(angle, conn.getRelativeLocation())), world, Color.RED);

            // 現在の位置に足す
            pos = pos.add(jigsawRel);  // .transform2D(angle, 0, 0, 0, 0));
//            showParticle(pos, world, Color.LIME);

            // ジグソーの向きから現在の角度より回転角度を計算
            int newAngle = angle + ori.getOpposite().getAngle();

            if (ori.getY() == 0) {
                ori = JigsawConnector.Orientation.ofFlatAngle(newAngle);

            } else {
                boolean aligned = JigsawConnector.JointType.ALIGNED.equals(fromConnector.getJointType());
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

            // 派生先Jigsawに接続できるパーツを探して、構造パーツを作成する
            StructurePart sPart = createConnectablePart(fromConnector, size, pos, ori.getAngle(), part);
            if (sPart == null)
                continue;

            // 見つかった派生先パーツを配置
            buildParts.add(sPart);
            part.children().add(sPart);

            if (maxSize > size) {
                // このパーツに含まれるJigsawに繋がるパーツを探す
                buildPartsTree(sPart, size + 1);
            }
        }
    }



    private @Nullable JigsawPart getRandomPartFromStartPool() {
        SchematicPool pool = schematics.getStartPool();
        if (pool == null)
            return null;

        String poolName = pool.getName();
        if (!poolName.contains(":"))
            poolName = "minecraft:" + poolName;
        Collection<JigsawPart> parts = partsOfPool.get(poolName);
        if (parts.isEmpty())
            return null;

        return Lists.newArrayList(parts).get(new Random().nextInt(parts.size()));
    }

    private @Nullable StructurePart createConnectablePart(JigsawConnector fromConnector, int size, BlockVector3 position, int angle, StructurePart parentPart) {
        List<JigsawConnector> targets;


        if (maxSize <= size) {
            // 最大サイズだった場合は末端パーツ
            // TODO: fallbackの動作が必要？
            targets = partsOfPool.get(fromConnector.getPool()).stream()
                    .filter(part -> part.getConnectors().size() == 1)
                    .flatMap(part -> part.getConnectors().stream())
                    .filter(fromConnector::isConnectableTo)
                    .collect(Collectors.toList());

            if (targets.isEmpty()) {
                targets = partsOfPool.get(fromConnector.getPool()).stream()
                        .flatMap(part -> part.getConnectors().stream())
                        .filter(fromConnector::isConnectableTo)
                        .collect(Collectors.toList());
            }

        } else {
            targets = partsOfPool.get(fromConnector.getPool()).stream()
                    .flatMap(part -> part.getConnectors().stream())
                    .filter(fromConnector::isConnectableTo)
                    .collect(Collectors.toList());
        }

        if (targets.isEmpty()) {
            log.warning("target is not found (pool: " + fromConnector.getPool() + ", targetName: " + fromConnector.getTargetName() + ")");
            return null;
        }

        // 被らないか調べた上で選択する
        while (!targets.isEmpty()) {
            // 重さ値選別に選別
            int totalWeight = targets.stream()
                    .mapToInt(conn -> conn.getJigsawPart().getPoolEntry().getWeight())
                    .sum();
            int selectWeight = random.nextInt(totalWeight);
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
                throw new IllegalStateException("program error: weight parse");

            StructurePart sPart = new StructurePart(this, to.getJigsawPart(), to, position, angle, parentPart);
            Set<String> sPartCollisions = sPart.testBlockPositionCollisions();

            // 重なりをテストする
            boolean hitBlocks = buildParts.stream()
                    .flatMap(part -> part.testBlockPositionCollisions().stream())
                    .anyMatch(sPartCollisions::contains);

            if (!hitBlocks)
                return sPart;

            targets.remove(to);
        }

        // 全て重なる場合は、一番重なりが少ない物を選ぶ？
        log.warning("Failed collision test : size=" + size);
        return null;
    }

    Mask createNonReplaceMaskStructureVoid(Extent extent) {
        return new BlockTypeMask(extent, whitelistBlockTypes);
    }

}
