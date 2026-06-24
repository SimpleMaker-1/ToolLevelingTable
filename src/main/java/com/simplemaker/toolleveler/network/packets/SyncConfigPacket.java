package com.simplemaker.toolleveler.network.packets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.simplemaker.toolleveler.ToolLeveler;
import com.simplemaker.toolleveler.config.util.ConfigSyncing;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncConfigPacket(String identifier, JsonElement json)
        implements CustomPacketPayload {

    public static final Type<SyncConfigPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    ToolLeveler.MOD_ID,
                    "sync_config"
            ));

    private static final Gson GSON = new Gson();

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncConfigPacket> STREAM_CODEC =
            StreamCodec.ofMember(
                    (msg, buf) -> {
                        buf.writeUtf(msg.identifier);
                        buf.writeUtf(GSON.toJson(msg.json));
                    },
                    buf -> {
                        String identifier = buf.readUtf();
                        JsonElement json = JsonParser.parseString(buf.readUtf());
                        return new SyncConfigPacket(identifier, json);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncConfigPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ToolLeveler.LOGGER.info("Received config from server: '{}'", msg.identifier);
            ConfigSyncing.deserializeConfig(msg.identifier, msg.json);
        });
    }
}