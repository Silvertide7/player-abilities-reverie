package net.silvertide.pa_reverie.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.silvertide.pa_reverie.registry.ReverieEffects;

public class DeepSightEffect extends VisionEffect {

    public static final int REQUIRED_DEPTH_BELOW_SURFACE = 20;

    public DeepSightEffect(MobEffectCategory category, int displayColor) {
        super(category, displayColor);
    }

    @Override
    protected Holder<MobEffect> getEffectHolder() {
        return ReverieEffects.DEEP_SIGHT;
    }

    public static boolean isSufficientlyUnderground(Level level, LivingEntity entity) {
        if (Level.NETHER.equals(level.dimension())) {
            return true;
        }
        BlockPos pos = entity.blockPosition();
        if (level.canSeeSkyFromBelowWater(pos)) {
            return false;
        }
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        int depthBelowSurface = surfaceY - pos.getY();
        return depthBelowSurface >= REQUIRED_DEPTH_BELOW_SURFACE;
    }

    public static float getIntensity(Player player, float partialTicks) {
        return VisionEffect.getIntensity(player, ReverieEffects.DEEP_SIGHT, partialTicks);
    }
}
