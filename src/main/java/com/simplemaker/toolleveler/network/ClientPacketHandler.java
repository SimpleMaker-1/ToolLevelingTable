package com.simplemaker.toolleveler.network;

import com.simplemaker.toolleveler.network.packets.OpenItemValueScreenPacket;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPacketHandler {

    public static void handleOpenItemValueScreen(OpenItemValueScreenPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            net.minecraft.client.Minecraft mc
                    = net.minecraft.client.Minecraft.getInstance();

            mc.setScreen(
                    new com.simplemaker.toolleveler.client.screen.ItemValueScreen()
            );
        });
    }
    
}