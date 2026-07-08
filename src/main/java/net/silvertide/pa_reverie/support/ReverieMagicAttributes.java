package net.silvertide.pa_reverie.support;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.silvertide.pa_reverie.PAReverie;

public final class ReverieMagicAttributes {

    private static final double POWER_BASELINE = 1.0;
    private static final double POWER_MIN = 0.0;
    private static final double POWER_MAX = 100.0;
    private static final double MAX_HARVEST_POWER_MULTIPLIER = 2.0;

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(
            BuiltInRegistries.ATTRIBUTE,
            PAReverie.MOD_ID
    );

    public static final Holder<Attribute> HARVEST_SPELL_POWER = ATTRIBUTES.register(
            "harvest_spell_power",
            () -> new RangedAttribute(
                    "attribute.pa_reverie.harvest_spell_power",
                    POWER_BASELINE,
                    POWER_MIN,
                    POWER_MAX
            ).setSyncable(true)
    );

    public static final Holder<Attribute> HARVEST_MAGIC_RESIST = ATTRIBUTES.register(
            "harvest_magic_resist",
            () -> new RangedAttribute(
                    "attribute.pa_reverie.harvest_magic_resist",
                    POWER_BASELINE,
                    POWER_MIN,
                    POWER_MAX
            ).setSyncable(true)
    );

    private ReverieMagicAttributes() {}

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
        modEventBus.addListener(ReverieMagicAttributes::attachHarvestAttributesToPlayer);
    }

    private static void attachHarvestAttributesToPlayer(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, HARVEST_SPELL_POWER);
        event.add(EntityType.PLAYER, HARVEST_MAGIC_RESIST);
    }

    public static int scaledByHarvestPower(LivingEntity caster, int base) {
        return (int) Math.round(base * harvestPowerMultiplier(caster));
    }

    public static double scaledByHarvestPower(LivingEntity caster, double base) {
        return base * harvestPowerMultiplier(caster);
    }

    private static double harvestPowerMultiplier(LivingEntity caster) {
        return Math.min(MAX_HARVEST_POWER_MULTIPLIER, caster.getAttributeValue(HARVEST_SPELL_POWER));
    }
}
