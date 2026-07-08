package net.silvertide.pa_reverie.ability;

import net.minecraft.resources.ResourceLocation;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.player_abilities.api.ActiveAbility;

public abstract class HarvestAbility extends ActiveAbility {
    protected static final ResourceLocation HARVEST_CATEGORY = PAReverie.id("harvest");
    protected static final int TICKS_PER_SECOND = 20;

    protected static float spellPower(net.minecraft.world.entity.LivingEntity caster, int baseSpellPower,
                                      int spellPowerPerLevel, int level) {
        return (float) net.silvertide.pa_reverie.support.ReverieMagicAttributes.scaledByHarvestPower(caster,
                (double) (baseSpellPower + spellPowerPerLevel * (level - 1)));
    }

    @Override
    public ResourceLocation getCategory() {
        return HARVEST_CATEGORY;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
