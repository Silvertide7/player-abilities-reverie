package net.silvertide.pa_reverie.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.entity.RiderControlEntity;

@EventBusSubscriber(modid = PAReverie.MOD_ID)
public final class RiderControlEvents {

    private RiderControlEvents() {}

    @SubscribeEvent
    public static void preventDismountWhileControlled(EntityMountEvent event) {
        if (!event.isDismounting()) {
            return;
        }
        if (event.getEntity().level().isClientSide) {
            return;
        }
        if (event.getEntityBeingMounted() instanceof RiderControlEntity controlEntity && !controlEntity.isFinished()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void releaseRiderOnLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.getVehicle() instanceof RiderControlEntity controlEntity && !controlEntity.isFinished()) {
            controlEntity.handleRiderDisconnect(player);
        }
    }
}
