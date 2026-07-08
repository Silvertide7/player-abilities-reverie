package net.silvertide.pa_reverie.support;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ExcavateManager {

    private static final int PROCESS_INTERVAL_TICKS = 4;
    private static final int TARGET_DURATION_TICKS = 40;
    private static final int TARGET_WAVE_COUNT = TARGET_DURATION_TICKS / PROCESS_INTERVAL_TICKS;
    private static final int MAX_BLOCKS_PER_WAVE = 16;

    private static int ticksSinceLastProcess = 0;

    private static final Map<UUID, ExcavateJob> ACTIVE_JOBS = new ConcurrentHashMap<>();

    private ExcavateManager() {}

    public static void register() {
        NeoForge.EVENT_BUS.register(ExcavateManager.class);
    }

    public static void start(ServerPlayer player, ServerLevel level, List<BlockPos> positions, boolean teleportDropsToPlayer) {
        if (positions.isEmpty()) {
            return;
        }
        ACTIVE_JOBS.putIfAbsent(player.getUUID(), new ExcavateJob(player, level, positions, teleportDropsToPlayer));
    }

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        if (ACTIVE_JOBS.isEmpty()) {
            return;
        }
        if (++ticksSinceLastProcess < PROCESS_INTERVAL_TICKS) {
            return;
        }
        ticksSinceLastProcess = 0;
        Iterator<Map.Entry<UUID, ExcavateJob>> iterator = ACTIVE_JOBS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ExcavateJob> entry = iterator.next();
            ExcavateJob job = entry.getValue();
            if (!job.isPlayerStillValid()) {
                iterator.remove();
                continue;
            }
            int blocksThisWave = Math.min(MAX_BLOCKS_PER_WAVE,
                    Math.max(1, (job.totalCount() + TARGET_WAVE_COUNT - 1) / TARGET_WAVE_COUNT));
            for (int i = 0; i < blocksThisWave && !job.isDone(); i++) {
                job.processOne();
            }
            if (job.isDone()) {
                iterator.remove();
            }
        }
    }
}
