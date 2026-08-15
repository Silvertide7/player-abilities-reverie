package net.silvertide.pa_reverie.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record HunterHighlightPacket(
        List<Integer> entityIds,
        int durationTicks
) {

    private static final int MAX_ENCODED_ENTITIES = 256;

    public HunterHighlightPacket {
        entityIds = List.copyOf(entityIds);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityIds.size());
        for (int entityId : entityIds) {
            buf.writeVarInt(entityId);
        }
        buf.writeVarInt(durationTicks);
    }

    public static HunterHighlightPacket decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > MAX_ENCODED_ENTITIES) {
            throw new DecoderException("Hunter highlight entity count out of range: " + count);
        }
        List<Integer> entityIds = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            entityIds.add(buf.readVarInt());
        }
        return new HunterHighlightPacket(entityIds, buf.readVarInt());
    }
}
