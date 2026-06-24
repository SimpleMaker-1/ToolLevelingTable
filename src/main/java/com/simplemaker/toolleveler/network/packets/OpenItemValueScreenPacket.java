package com.simplemaker.toolleveler.network.packets;

import com.simplemaker.toolleveler.ToolLeveler;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenItemValueScreenPacket() implements CustomPacketPayload {

    public static final Type<OpenItemValueScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    ToolLeveler.MOD_ID,
                    "open_item_value_screen"
            ));

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, OpenItemValueScreenPacket> STREAM_CODEC =
            StreamCodec.ofMember(
                    (msg, buf) -> {},
                    buf -> new OpenItemValueScreenPacket()
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}