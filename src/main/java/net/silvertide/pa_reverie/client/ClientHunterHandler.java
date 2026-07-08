package net.silvertide.pa_reverie.client;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.pa_reverie.network.HunterHighlightPacket;

public final class ClientHunterHandler {

    private ClientHunterHandler() {}

    public static void handle(HunterHighlightPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> HunterRenderState.install(packet));
    }
}
