package net.silvertide.pa_reverie.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.silvertide.pa_reverie.client.ClientHunterHandler;
import net.silvertide.pa_reverie.client.ClientTremorSenseHandler;

public final class ReverieSpellNetwork {

    private static final String PROTOCOL_VERSION = "1";

    private ReverieSpellNetwork() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ReverieSpellNetwork::onRegisterPayloadHandlers);
    }

    private static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(
                TremorSenseHighlightPacket.TYPE,
                TremorSenseHighlightPacket.STREAM_CODEC,
                ClientTremorSenseHandler::handle
        );
        registrar.playToClient(
                HunterHighlightPacket.TYPE,
                HunterHighlightPacket.STREAM_CODEC,
                ClientHunterHandler::handle
        );
    }
}
