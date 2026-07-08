package net.silvertide.pa_reverie.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.pa_reverie.network.ReverieNetworking;
import net.silvertide.pa_reverie.registry.ReverieEntities;

import java.util.ArrayList;
import java.util.List;

public class EscapeShaftRiseEntity extends RiderControlEntity {

    private record ShaftBounds(BlockPos min, BlockPos max) {}

    private static final int PASSENGER_LATERAL_RADIUS_BLOCKS = 1;
    private static final int PASSENGER_VERTICAL_TOLERANCE_BLOCKS = 1;
    private static final int SHAFT_LATERAL_RADIUS_BLOCKS = 1;

    private double anchorX;
    private double anchorZ;
    private double targetY;
    private double blocksPerTick;
    private ShaftBounds shaft;

    public EscapeShaftRiseEntity(EntityType<? extends EscapeShaftRiseEntity> type, Level level) {
        super(type, level);
    }

    public static void startRise(ServerLevel level, List<ServerPlayer> participants, double blocksPerTick, int riseTargetOffsetAboveSurface) {
        ShaftBounds shaftBounds = computeUnifiedShaftBounds(level, participants, riseTargetOffsetAboveSurface);
        for (ServerPlayer participant : participants) {
            if (participant.getVehicle() instanceof EscapeShaftRiseEntity) {
                continue;
            }
            double participantTargetY = computeRiseTargetY(level,
                    (int) Math.floor(participant.getX()),
                    (int) Math.floor(participant.getZ()),
                    riseTargetOffsetAboveSurface);
            if (participantTargetY <= participant.getY()) {
                continue;
            }
            EscapeShaftRiseEntity vehicle = new EscapeShaftRiseEntity(ReverieEntities.ESCAPE_SHAFT_RISE.get(), level);
            vehicle.anchorX = participant.getX();
            vehicle.anchorZ = participant.getZ();
            vehicle.targetY = participantTargetY;
            vehicle.blocksPerTick = blocksPerTick;
            vehicle.setRiderUuid(participant.getUUID());
            vehicle.shaft = shaftBounds;
            vehicle.setPos(participant.getX(), participant.getY(), participant.getZ());
            vehicle.setDeltaMovement(0.0, blocksPerTick, 0.0);
            vehicle.hurtMarked = true;
            level.addFreshEntity(vehicle);
            participant.startRiding(vehicle, true);
            PacketDistributor.sendToPlayer(participant,
                    new ReverieNetworking.EscapeShaftSetupPayload(shaftBounds.min(), shaftBounds.max()));
        }
    }

    private static ShaftBounds computeUnifiedShaftBounds(ServerLevel level, List<ServerPlayer> participants, int riseTargetOffsetAboveSurface) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (ServerPlayer participant : participants) {
            int px = (int) Math.floor(participant.getX());
            int pz = (int) Math.floor(participant.getZ());
            int py = (int) Math.floor(participant.getY());
            int targetY = computeRiseTargetY(level, px, pz, riseTargetOffsetAboveSurface);
            minX = Math.min(minX, px - SHAFT_LATERAL_RADIUS_BLOCKS);
            maxX = Math.max(maxX, px + SHAFT_LATERAL_RADIUS_BLOCKS);
            minZ = Math.min(minZ, pz - SHAFT_LATERAL_RADIUS_BLOCKS);
            maxZ = Math.max(maxZ, pz + SHAFT_LATERAL_RADIUS_BLOCKS);
            minY = Math.min(minY, py);
            maxY = Math.max(maxY, targetY);
        }
        return new ShaftBounds(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }

    private static int computeRiseTargetY(ServerLevel level, int x, int z, int riseTargetOffsetAboveSurface) {
        int columnTop = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        int groundTop = findGroundTopBelowTrees(level, x, columnTop - 1, z);
        if (hasHeadroomForRider(level, x, groundTop, z)) {
            return groundTop + 1 + riseTargetOffsetAboveSurface;
        }
        return columnTop + riseTargetOffsetAboveSurface;
    }

    private static int findGroundTopBelowTrees(ServerLevel level, int x, int startScanY, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = startScanY; y >= level.getMinBuildHeight(); y--) {
            cursor.set(x, y, z);
            BlockState state = level.getBlockState(cursor);
            if (state.isAir() || state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                continue;
            }
            return y;
        }
        return startScanY;
    }

    private static boolean hasHeadroomForRider(ServerLevel level, int x, int groundTopY, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        return level.getBlockState(cursor.set(x, groundTopY + 1, z)).isAir()
                && level.getBlockState(cursor.set(x, groundTopY + 2, z)).isAir();
    }

    public static List<ServerPlayer> findCapturablePlayers(ServerLevel level, ServerPlayer caster) {
        BlockPos casterBlock = caster.blockPosition();
        AABB searchArea = new AABB(
                casterBlock.getX() - PASSENGER_LATERAL_RADIUS_BLOCKS,
                casterBlock.getY() - PASSENGER_VERTICAL_TOLERANCE_BLOCKS,
                casterBlock.getZ() - PASSENGER_LATERAL_RADIUS_BLOCKS,
                casterBlock.getX() + PASSENGER_LATERAL_RADIUS_BLOCKS + 1,
                casterBlock.getY() + PASSENGER_VERTICAL_TOLERANCE_BLOCKS + 1,
                casterBlock.getZ() + PASSENGER_LATERAL_RADIUS_BLOCKS + 1);
        List<ServerPlayer> result = new ArrayList<>();
        for (ServerPlayer candidate : level.getEntitiesOfClass(ServerPlayer.class, searchArea)) {
            if (candidate == caster || !candidate.isAlive() || candidate.hasDisconnected()) {
                continue;
            }
            BlockPos candidateBlock = candidate.blockPosition();
            int dx = candidateBlock.getX() - casterBlock.getX();
            int dy = candidateBlock.getY() - casterBlock.getY();
            int dz = candidateBlock.getZ() - casterBlock.getZ();
            if (Math.abs(dx) <= PASSENGER_LATERAL_RADIUS_BLOCKS
                    && Math.abs(dz) <= PASSENGER_LATERAL_RADIUS_BLOCKS
                    && Math.abs(dy) <= PASSENGER_VERTICAL_TOLERANCE_BLOCKS) {
                result.add(candidate);
            }
        }
        return result;
    }

    @Override
    protected void controlRiderTick() {
        double nextY = getY() + blocksPerTick;
        if (nextY >= targetY) {
            setPos(anchorX, targetY, anchorZ);
            setDeltaMovement(Vec3.ZERO);
            finishAndDiscard();
            return;
        }
        setPos(anchorX, nextY, anchorZ);
        setDeltaMovement(0.0, blocksPerTick, 0.0);
    }

    @Override
    protected void onControlEnded(ServerLevel level) {
        ServerPlayer rider = lookupRider(level);
        if (rider == null || shaft == null) {
            return;
        }
        int minChunkX = (shaft.min().getX() - 1) >> 4;
        int maxChunkX = (shaft.max().getX() + 1) >> 4;
        int minChunkZ = (shaft.min().getZ() - 1) >> 4;
        int maxChunkZ = (shaft.max().getZ() + 1) >> 4;
        LevelLightEngine lightEngine = level.getLightEngine();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                rider.connection.send(new ClientboundLevelChunkWithLightPacket(chunk, lightEngine, null, null));
            }
        }
    }

    @Override
    public void handleRiderDisconnect(ServerPlayer rider) {
        finished = true;
        riderUuid = null;
        rider.stopRiding();
        rider.setPos(anchorX, targetY, anchorZ);
        rider.resetFallDistance();
        discard();
    }
}
