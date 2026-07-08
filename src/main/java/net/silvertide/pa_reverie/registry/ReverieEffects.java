package net.silvertide.pa_reverie.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.silvertide.pa_reverie.PAReverie;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.silvertide.pa_reverie.effect.DeepSightEffect;
import net.silvertide.pa_reverie.effect.FathomsEyeEffect;
import net.silvertide.pa_reverie.effect.HunterEffect;
import net.silvertide.pa_reverie.effect.PeacefulReverieEffect;
import net.silvertide.pa_reverie.effect.RestfulMeditationEffect;
import net.silvertide.pa_reverie.effect.ShepherdsAuraEffect;

public final class ReverieEffects {
    private static final int DEEP_SIGHT_DISPLAY_COLOR = 0x2E8B57;
    private static final int FATHOMS_EYE_DISPLAY_COLOR = 0x4A9FE0;
    private static final int PEACEFUL_REVERIE_DISPLAY_COLOR = 0x6BA9C7;
    private static final int RESTFUL_MEDITATION_DISPLAY_COLOR = 0x7FB069;
    private static final int SHEPHERDS_AURA_DISPLAY_COLOR = 0x9CCC65;

    private static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, PAReverie.MOD_ID);

    public static final Holder<MobEffect> DEEP_SIGHT = MOB_EFFECTS.register("deep_sight",
            () -> new DeepSightEffect(MobEffectCategory.BENEFICIAL, DEEP_SIGHT_DISPLAY_COLOR));

    public static final Holder<MobEffect> FATHOMS_EYE = MOB_EFFECTS.register("fathoms_eye",
            () -> new FathomsEyeEffect(MobEffectCategory.BENEFICIAL, FATHOMS_EYE_DISPLAY_COLOR));

    public static final Holder<MobEffect> HUNTER_EFFECT = MOB_EFFECTS.register("hunter_effect",
            () -> new HunterEffect(MobEffectCategory.BENEFICIAL, 3124687));

    public static final Holder<MobEffect> PEACEFUL_REVERIE_EFFECT = MOB_EFFECTS.register("peaceful_reverie",
            () -> new PeacefulReverieEffect(MobEffectCategory.BENEFICIAL, PEACEFUL_REVERIE_DISPLAY_COLOR)
                    .addAttributeModifier(Attributes.LUCK,
                            ResourceLocation.fromNamespaceAndPath(PAReverie.MOD_ID, "peaceful_reverie_luck"),
                            1.0, AttributeModifier.Operation.ADD_VALUE));

    public static final Holder<MobEffect> RESTFUL_MEDITATION_EFFECT = MOB_EFFECTS.register("restful_meditation",
            () -> new RestfulMeditationEffect(MobEffectCategory.BENEFICIAL, RESTFUL_MEDITATION_DISPLAY_COLOR));

    public static final Holder<MobEffect> SHEPHERDS_AURA = MOB_EFFECTS.register("shepherds_aura",
            () -> new ShepherdsAuraEffect(MobEffectCategory.BENEFICIAL, SHEPHERDS_AURA_DISPLAY_COLOR));

    private ReverieEffects() {
    }

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}
