package net.silvertide.pa_reverie.client;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.pa_reverie.network.TremorSenseHighlightPacket;

public final class ClientTremorSenseHandler {

    private ClientTremorSenseHandler() {}

    public static void handle(TremorSenseHighlightPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> TremorSenseRenderState.install(packet));
    }
}
