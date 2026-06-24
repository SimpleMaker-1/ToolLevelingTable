package com.simplemaker.toolleveler.config.util;

import com.google.gson.JsonElement;
import com.simplemaker.toolleveler.ToolLeveler;
import com.simplemaker.toolleveler.network.packets.SyncConfigPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;

public final class ConfigSyncing {

    public static void syncAllConfigsToAllClients(ServerLevel level) {
        for (Map.Entry<String, ConfigIdentifier<?>> entry : ConfigManager.CONFIGS.entrySet()) {
            JsonElement json = entry.getValue().serialize();
            ToolLeveler.LOGGER.info("Syncing config '{}' to all clients.", entry.getKey());
            
            PacketDistributor.sendToAllPlayers(new SyncConfigPacket(entry.getKey(), json));
        }
    }

    public static void syncAllConfigsToOneClient(ServerPlayer player) {
        for (Map.Entry<String, ConfigIdentifier<?>> entry : ConfigManager.CONFIGS.entrySet()) {
            JsonElement json = entry.getValue().serialize();
            ToolLeveler.LOGGER.info("Syncing config '{}' to {}.", entry.getKey(), player.getName().getString());
            
            PacketDistributor.sendToPlayer(player, new SyncConfigPacket(entry.getKey(), json));
        }
    }

    public static void deserializeConfig(String identifier, JsonElement json) {
        ConfigIdentifier<?> config = ConfigManager.CONFIGS.get(identifier);
        if (config != null) {
            config.deserialize(json);
        } else {
            ToolLeveler.LOGGER.warn("Received unknown config identifier: '{}'", identifier);
        }
    }
}
