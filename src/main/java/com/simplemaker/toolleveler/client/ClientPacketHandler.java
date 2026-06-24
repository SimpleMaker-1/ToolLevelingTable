package com.simplemaker.toolleveler.client;

import com.simplemaker.toolleveler.client.screen.ItemValueScreen;
import com.simplemaker.toolleveler.network.packets.OpenItemValueScreenPacket;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPacketHandler {

    public static void handleOpenItemValueScreen(
            OpenItemValueScreenPacket msg,
            IPayloadContext ctx
    ) {
        ctx.enqueueWork(() ->
                Minecraft.getInstance().setScreen(new ItemValueScreen())
        );
    }
}