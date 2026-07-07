package net.silvertide.pa_reverie.support;

import net.minecraft.core.Vec3i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SphereOffsets {

    private static final Map<Integer, List<Vec3i>> SPHERE_BY_RADIUS = new ConcurrentHashMap<>();
    private static final Map<Integer, List<Vec3i>> UPPER_DOME_BY_RADIUS = new ConcurrentHashMap<>();

    private SphereOffsets() {}

    public static List<Vec3i> sortedOffsetsForRadius(int radius) {
        return SPHERE_BY_RADIUS.computeIfAbsent(radius, r -> buildSortedOffsets(r, -r));
    }

    public static List<Vec3i> sortedDomeOffsetsForRadius(int radius) {
        return UPPER_DOME_BY_RADIUS.computeIfAbsent(radius, r -> buildSortedOffsets(r, 0));
    }

    private static List<Vec3i> buildSortedOffsets(int radius, int minVerticalOffset) {
        int radiusSquared = radius * radius;
        List<Vec3i> offsets = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = minVerticalOffset; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= radiusSquared) {
                        offsets.add(new Vec3i(dx, dy, dz));
                    }
                }
            }
        }
        offsets.sort(Comparator.comparingInt(offset ->
                offset.getX() * offset.getX() + offset.getY() * offset.getY() + offset.getZ() * offset.getZ()));
        return offsets;
    }
}
