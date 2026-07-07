package net.silvertide.pa_reverie.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.ability.CaissonAbility;
import net.silvertide.pa_reverie.ability.CanopyLeapAbility;
import net.silvertide.pa_reverie.ability.ConjureFoodAbility;
import net.silvertide.pa_reverie.ability.DeepsightAbility;
import net.silvertide.pa_reverie.ability.EscapeShaftAbility;
import net.silvertide.pa_reverie.ability.ExcavateAbility;
import net.silvertide.pa_reverie.ability.FathomsEyeAbility;
import net.silvertide.pa_reverie.ability.FeastOfLifeAbility;
import net.silvertide.pa_reverie.ability.HuntersMarkAbility;
import net.silvertide.pa_reverie.ability.MendAbility;
import net.silvertide.pa_reverie.ability.PeacefulReverieAbility;
import net.silvertide.pa_reverie.ability.RainDanceAbility;
import net.silvertide.pa_reverie.ability.RestfulMeditationAbility;
import net.silvertide.pa_reverie.ability.ShepherdsAuraAbility;
import net.silvertide.pa_reverie.ability.TransmuteAbility;
import net.silvertide.pa_reverie.ability.TremorSenseAbility;
import net.silvertide.pa_reverie.ability.VerdantCascadeAbility;
import net.silvertide.pa_reverie.ability.WoodsongAbility;
import net.silvertide.player_abilities.api.Ability;
import net.silvertide.player_abilities.api.AbilityRegistry;

public final class ReverieAbilities {
    private static final DeferredRegister<Ability> ABILITIES =
            DeferredRegister.create(AbilityRegistry.ABILITY_REGISTRY_KEY, PAReverie.MOD_ID);

    public static final DeferredHolder<Ability, ConjureFoodAbility> CONJURE_FOOD =
            ABILITIES.register("conjure_food", ConjureFoodAbility::new);
    public static final DeferredHolder<Ability, DeepsightAbility> DEEPSIGHT =
            ABILITIES.register("deepsight", DeepsightAbility::new);
    public static final DeferredHolder<Ability, FathomsEyeAbility> FATHOMS_EYE =
            ABILITIES.register("fathoms_eye", FathomsEyeAbility::new);
    public static final DeferredHolder<Ability, FeastOfLifeAbility> FEAST_OF_LIFE =
            ABILITIES.register("feast_of_life", FeastOfLifeAbility::new);
    public static final DeferredHolder<Ability, MendAbility> MEND =
            ABILITIES.register("mend", MendAbility::new);
    public static final DeferredHolder<Ability, CaissonAbility> CAISSON =
            ABILITIES.register("caisson", CaissonAbility::new);
    public static final DeferredHolder<Ability, CanopyLeapAbility> CANOPY_LEAP =
            ABILITIES.register("canopy_leap", CanopyLeapAbility::new);
    public static final DeferredHolder<Ability, EscapeShaftAbility> ESCAPE_SHAFT =
            ABILITIES.register("escape_shaft", EscapeShaftAbility::new);
    public static final DeferredHolder<Ability, ExcavateAbility> EXCAVATE =
            ABILITIES.register("excavate", ExcavateAbility::new);
    public static final DeferredHolder<Ability, HuntersMarkAbility> HUNTERS_MARK =
            ABILITIES.register("hunters_mark", HuntersMarkAbility::new);
    public static final DeferredHolder<Ability, PeacefulReverieAbility> PEACEFUL_REVERIE =
            ABILITIES.register("peaceful_reverie", PeacefulReverieAbility::new);
    public static final DeferredHolder<Ability, RainDanceAbility> RAIN_DANCE =
            ABILITIES.register("rain_dance", RainDanceAbility::new);
    public static final DeferredHolder<Ability, RestfulMeditationAbility> RESTFUL_MEDITATION =
            ABILITIES.register("restful_meditation", RestfulMeditationAbility::new);
    public static final DeferredHolder<Ability, ShepherdsAuraAbility> SHEPHERDS_AURA =
            ABILITIES.register("shepherds_aura", ShepherdsAuraAbility::new);
    public static final DeferredHolder<Ability, TransmuteAbility> TRANSMUTE =
            ABILITIES.register("transmute", TransmuteAbility::new);
    public static final DeferredHolder<Ability, TremorSenseAbility> TREMOR_SENSE =
            ABILITIES.register("tremor_sense", TremorSenseAbility::new);
    public static final DeferredHolder<Ability, VerdantCascadeAbility> VERDANT_CASCADE =
            ABILITIES.register("verdant_cascade", VerdantCascadeAbility::new);
    public static final DeferredHolder<Ability, WoodsongAbility> WOODSONG =
            ABILITIES.register("woodsong", WoodsongAbility::new);

    private ReverieAbilities() {
    }

    public static void register(IEventBus modEventBus) {
        ABILITIES.register(modEventBus);
    }
}
