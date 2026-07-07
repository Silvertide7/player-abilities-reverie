package net.silvertide.pa_reverie.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.pa_reverie.PAReverie;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record TremorSenseHighlightPacket(
        BlockPos origin,
        List<BlockPos> positions,
        int durationTicks,
        boolean useTierColors
) implements CustomPacketPayload {

    private static final int MAX_ENCODED_POSITIONS = 512;

    public TremorSenseHighlightPacket {
        positions = List.copyOf(positions);
    }

    public static final CustomPacketPayload.Type<TremorSenseHighlightPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(PAReverie.MOD_ID, "tremor_sense_highlight")
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, TremorSenseHighlightPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, TremorSenseHighlightPacket::origin,
                    BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_ENCODED_POSITIONS)), TremorSenseHighlightPacket::positions,
                    ByteBufCodecs.VAR_INT, TremorSenseHighlightPacket::durationTicks,
                    ByteBufCodecs.BOOL, TremorSenseHighlightPacket::useTierColors,
                    TremorSenseHighlightPacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
