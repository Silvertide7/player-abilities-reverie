package net.silvertide.pa_reverie.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.client.ClientTremorSenseHandler;

@EventBusSubscriber(modid = PAReverie.MOD_ID)
public final class ReverieNetworking {
    private ReverieNetworking() {
    }

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(TremorSenseHighlightPacket.TYPE, TremorSenseHighlightPacket.STREAM_CODEC,
                ClientTremorSenseHandler::handle);
    }
}
