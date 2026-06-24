package com.simplemaker.toolleveler.network;

import com.google.gson.JsonElement;
import com.simplemaker.toolleveler.ToolLeveler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface ClientBoundPacketHandler {

    ClientBoundPacketHandler INSTANCE = ToolLeveler.load(ClientBoundPacketHandler.class);

    void syncOneConfigToOneClient(ServerPlayer player, String identifier, JsonElement json);

    void syncOneConfigToAllClients(ServerLevel level, String identifier, JsonElement json);

    void openItemValueScreen(ServerPlayer player);
}