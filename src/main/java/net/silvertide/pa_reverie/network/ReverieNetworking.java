package net.silvertide.pa_reverie.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.client.EscapeShaftClientGhostShaft;
import net.silvertide.pa_reverie.client.HunterRenderState;
import net.silvertide.pa_reverie.client.TremorSenseRenderState;

public final class ReverieNetworking {

    private static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PAReverie.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private ReverieNetworking() {}

    public static void register() {
        int id = 0;

        CHANNEL.messageBuilder(EscapeShaftSetupPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(EscapeShaftSetupPayload::encode).decoder(EscapeShaftSetupPayload::decode)
                .consumerMainThread((msg, ctx) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> EscapeShaftClientGhostShaft.applyShaft(msg.min(), msg.max()))).add();
        CHANNEL.messageBuilder(TremorSenseHighlightPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TremorSenseHighlightPacket::encode).decoder(TremorSenseHighlightPacket::decode)
                .consumerMainThread((msg, ctx) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> TremorSenseRenderState.install(msg))).add();
        CHANNEL.messageBuilder(HunterHighlightPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(HunterHighlightPacket::encode).decoder(HunterHighlightPacket::decode)
                .consumerMainThread((msg, ctx) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> HunterRenderState.install(msg))).add();
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public record EscapeShaftSetupPayload(BlockPos min, BlockPos max) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(min);
            buf.writeBlockPos(max);
        }

        public static EscapeShaftSetupPayload decode(FriendlyByteBuf buf) {
            return new EscapeShaftSetupPayload(buf.readBlockPos(), buf.readBlockPos());
        }
    }
}
