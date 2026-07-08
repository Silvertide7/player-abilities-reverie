package net.silvertide.pa_reverie.compat;

import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class QualityFoodCompat {
    private static final boolean QUALITY_FOOD_LOADED = ModList.get().isLoaded("quality_food");

    private QualityFoodCompat() {
    }

    public static int qualityLevel(ItemStack stack) {
        return QUALITY_FOOD_LOADED ? Bridge.qualityLevel(stack) : 0;
    }

    private static final class Bridge {
        private static int qualityLevel(ItemStack stack) {
            var quality = QualityUtils.getQuality(stack);
            return QualityUtils.isValidQuality(quality) ? quality.level() : 0;
        }
    }
}
