package net.silvertide.pa_reverie.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ServerConfigs {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue FEAST_OF_LIFE_HEALTH_PER_NUTRITION;
    public static final ForgeConfigSpec.DoubleValue FEAST_OF_LIFE_ABSORPTION_PER_SATURATION;
    public static final ForgeConfigSpec.DoubleValue FEAST_OF_LIFE_QUALITY_BONUS_PER_LEVEL;

    static {
        BUILDER.push("Player Abilities Reverie - Feast of Life");

        BUILDER.comment("Health restored per point of the eaten food's nutrition. Final heal = nutrition x this x level potency x quality bonus, then scaled by ability power.");
        FEAST_OF_LIFE_HEALTH_PER_NUTRITION = BUILDER.defineInRange("feastOfLifeHealthPerNutrition", 1.0, 0.0, 100.0);

        BUILDER.comment("Absorption granted per point of the eaten food's saturation (nutrition x saturation modifier x 2). Final absorption = saturation x this x level potency x quality bonus, then scaled by ability power.");
        FEAST_OF_LIFE_ABSORPTION_PER_SATURATION = BUILDER.defineInRange("feastOfLifeAbsorptionPerSaturation", 3.0, 0.0, 100.0);

        BUILDER.comment("Per-quality-level bonus from Quality Food. Multiplier = 1 + (quality level x this), applied to both heal and absorption.");
        FEAST_OF_LIFE_QUALITY_BONUS_PER_LEVEL = BUILDER.defineInRange("feastOfLifeQualityBonusPerLevel", 0.25, 0.0, 10.0);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private ServerConfigs() {
    }
}
