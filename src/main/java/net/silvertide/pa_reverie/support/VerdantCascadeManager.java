package net.silvertide.pa_reverie.support;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VerdantCascadeManager {

    private static final int PROCESS_INTERVAL_TICKS = 4;
    private static final int TARGET_DURATION_TICKS = 200;
    private static final int TARGET_WAVE_COUNT = TARGET_DURATION_TICKS / PROCESS_INTERVAL_TICKS;

    private static int ticksSinceLastProcess = 0;

    private static final Map<UUID, CascadeJob> ACTIVE_JOBS = new ConcurrentHashMap<>();

    private VerdantCascadeManager() {}

    public static void register() {
        NeoForge.EVENT_BUS.register(VerdantCascadeManager.class);
    }

    public static void start(ServerPlayer player, ServerLevel level, BlockPos start, int chainMax, int spellLevel) {
        ACTIVE_JOBS.putIfAbsent(player.getUUID(), new CascadeJob(player, level, start, chainMax, spellLevel));
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
        Iterator<Map.Entry<UUID, CascadeJob>> iterator = ACTIVE_JOBS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, CascadeJob> entry = iterator.next();
            CascadeJob job = entry.getValue();
            if (!job.isPlayerStillValid()) {
                iterator.remove();
                continue;
            }
            int cropsThisWave = Math.max(1, (job.getChainMax() + TARGET_WAVE_COUNT - 1) / TARGET_WAVE_COUNT);
            for (int i = 0; i < cropsThisWave && !job.isDone(); i++) {
                job.processOne();
            }
            if (job.isDone()) {
                iterator.remove();
            }
        }
    }
}
