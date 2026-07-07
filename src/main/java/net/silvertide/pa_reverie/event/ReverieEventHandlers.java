package net.silvertide.pa_reverie.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.item.EphemeralFoodItem;
import net.silvertide.pa_reverie.ability.PeacefulReverieAbility;
import net.silvertide.pa_reverie.registry.ReverieAbilities;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityEffect;

@EventBusSubscriber(modid = PAReverie.MOD_ID)
public final class ReverieEventHandlers {
    private static final int EPHEMERAL_SWEEP_INTERVAL_TICKS = 20;

    private ReverieEventHandlers() {
    }

    @SubscribeEvent
    public static void onAnimalChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Animal
                && event.getNewAboutToBeSetTarget() instanceof ServerPlayer target
                && AbilityAPI.getActiveEffects(target).containsKey(ReverieAbilities.SHEPHERDS_AURA.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer watched)) {
            return;
        }
        AbilityEffect reverie = AbilityAPI.getActiveEffects(watched).get(ReverieAbilities.PEACEFUL_REVERIE.get());
        if (reverie != null) {
            event.modifyVisibility(PeacefulReverieAbility.visibilityMultiplierForLevel(reverie.getLevel()));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || player.tickCount % EPHEMERAL_SWEEP_INTERVAL_TICKS != 0) {
            return;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof EphemeralFoodItem && EphemeralFoodItem.isExpired(stack, player.level())) {
                player.getInventory().setItem(slot, ItemStack.EMPTY);
            }
        }
    }
}
