package net.silvertide.pa_reverie.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.client.ClientHunterHandler;
import net.silvertide.pa_reverie.client.ClientTremorSenseHandler;
import net.silvertide.pa_reverie.client.EscapeShaftClientGhostShaft;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = PAReverie.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ReverieNetworking {

    private static final String NETWORK_VERSION = "1";

    private ReverieNetworking() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);
        registrar.playToClient(
                EscapeShaftSetupPayload.TYPE,
                EscapeShaftSetupPayload.STREAM_CODEC,
                ReverieNetworking::handleEscapeShaftSetup
        );
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

    private static void handleEscapeShaftSetup(EscapeShaftSetupPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> EscapeShaftClientGhostShaft.applyShaft(payload.min(), payload.max()));
    }

    public record EscapeShaftSetupPayload(BlockPos min, BlockPos max) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<EscapeShaftSetupPayload> TYPE =
                new CustomPacketPayload.Type<>(
                        ResourceLocation.fromNamespaceAndPath(PAReverie.MOD_ID, "escape_shaft_setup")
                );

        public static final StreamCodec<ByteBuf, EscapeShaftSetupPayload> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC, EscapeShaftSetupPayload::min,
                        BlockPos.STREAM_CODEC, EscapeShaftSetupPayload::max,
                        EscapeShaftSetupPayload::new
                );

        @Override
        public CustomPacketPayload.@NotNull Type<EscapeShaftSetupPayload> type() {
            return TYPE;
        }
    }
}
