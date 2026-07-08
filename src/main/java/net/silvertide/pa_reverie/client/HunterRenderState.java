package net.silvertide.pa_reverie.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.silvertide.pa_reverie.network.HunterHighlightPacket;

import java.util.HashSet;
import java.util.Set;

public final class HunterRenderState {

    private static final int ANIMAL_COLOR = 0xF7E7CE;
    private static final int HOSTILE_COLOR = 0xFF5555;
    private static final int PLAYER_COLOR = 0xFFD24D;

    private static Set<Integer> highlightedEntityIds = Set.of();
    private static long endGameTick;

    private HunterRenderState() {}

    public static void install(HunterHighlightPacket packet) {
        long now = currentGameTick();
        if (now < 0) {
            return;
        }
        highlightedEntityIds = new HashSet<>(packet.entityIds());
        endGameTick = now + packet.durationTicks();
    }

    public static boolean isHighlighted(Entity entity) {
        if (highlightedEntityIds.isEmpty()) {
            return false;
        }
        if (currentGameTick() >= endGameTick) {
            highlightedEntityIds = Set.of();
            return false;
        }
        return highlightedEntityIds.contains(entity.getId());
    }

    public static int highlightColor(Entity entity) {
        return entity instanceof LivingEntity living ? colorFor(living) : ANIMAL_COLOR;
    }

    public static void clear() {
        highlightedEntityIds = Set.of();
        endGameTick = 0;
    }

    private static int colorFor(LivingEntity entity) {
        if (entity instanceof Player) {
            return PLAYER_COLOR;
        }
        if (entity instanceof Enemy) {
            return HOSTILE_COLOR;
        }
        return ANIMAL_COLOR;
    }

    private static long currentGameTick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return -1L;
        }
        return minecraft.level.getGameTime();
    }
}
