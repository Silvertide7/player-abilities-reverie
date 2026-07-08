package net.silvertide.pa_reverie.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.pa_reverie.PAReverie;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record HunterHighlightPacket(
        List<Integer> entityIds,
        int durationTicks
) implements CustomPacketPayload {

    private static final int MAX_ENCODED_ENTITIES = 256;

    public HunterHighlightPacket {
        entityIds = List.copyOf(entityIds);
    }

    public static final CustomPacketPayload.Type<HunterHighlightPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(PAReverie.MOD_ID, "hunter_highlight")
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, HunterHighlightPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(MAX_ENCODED_ENTITIES)), HunterHighlightPacket::entityIds,
                    ByteBufCodecs.VAR_INT, HunterHighlightPacket::durationTicks,
                    HunterHighlightPacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
