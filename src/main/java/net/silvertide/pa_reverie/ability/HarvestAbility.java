package net.silvertide.pa_reverie.ability;

import net.minecraft.resources.ResourceLocation;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.player_abilities.api.ActiveAbility;

public abstract class HarvestAbility extends ActiveAbility {
    protected static final ResourceLocation HARVEST_CATEGORY = PAReverie.id("harvest");
    protected static final int TICKS_PER_SECOND = 20;

    @Override
    public ResourceLocation getCategory() {
        return HARVEST_CATEGORY;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
