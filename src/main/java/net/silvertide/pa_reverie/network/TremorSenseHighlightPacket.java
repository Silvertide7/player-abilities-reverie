package net.silvertide.pa_reverie.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record TremorSenseHighlightPacket(
        BlockPos origin,
        List<BlockPos> positions,
        int durationTicks,
        boolean useTierColors
) {

    private static final int MAX_ENCODED_POSITIONS = 512;

    public TremorSenseHighlightPacket {
        positions = List.copyOf(positions);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(origin);
        buf.writeVarInt(positions.size());
        for (BlockPos position : positions) {
            buf.writeBlockPos(position);
        }
        buf.writeVarInt(durationTicks);
        buf.writeBoolean(useTierColors);
    }

    public static TremorSenseHighlightPacket decode(FriendlyByteBuf buf) {
        BlockPos origin = buf.readBlockPos();
        int count = buf.readVarInt();
        if (count < 0 || count > MAX_ENCODED_POSITIONS) {
            throw new DecoderException("Tremor sense position count out of range: " + count);
        }
        List<BlockPos> positions = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            positions.add(buf.readBlockPos());
        }
        return new TremorSenseHighlightPacket(origin, positions, buf.readVarInt(), buf.readBoolean());
    }
}
