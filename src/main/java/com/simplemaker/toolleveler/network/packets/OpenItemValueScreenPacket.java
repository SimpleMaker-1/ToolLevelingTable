package com.simplemaker.toolleveler.network.packets;

import com.simplemaker.toolleveler.ToolLeveler;
import com.simplemaker.toolleveler.client.screen.ItemValueScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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

    public static void handle(OpenItemValueScreenPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() ->
                Minecraft.getInstance().setScreen(new ItemValueScreen())
        );
    }
}