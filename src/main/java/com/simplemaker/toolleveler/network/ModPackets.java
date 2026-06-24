package com.simplemaker.toolleveler.network;

import com.simplemaker.toolleveler.ToolLeveler;
import com.simplemaker.toolleveler.network.packets.OpenItemValueScreenPacket;
import com.simplemaker.toolleveler.network.packets.SetEnchantmentPacket;
import com.simplemaker.toolleveler.network.packets.SyncConfigPacket;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModPackets {

    public static void register(IEventBus modBus) {
        modBus.addListener(ModPackets::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        
        var registrar = event.registrar(ToolLeveler.MOD_ID);

        registrar.playToClient(
            SyncConfigPacket.TYPE,
            SyncConfigPacket.STREAM_CODEC,
            SyncConfigPacket::handle
        );
        registrar.playToServer(
            SetEnchantmentPacket.TYPE,
            SetEnchantmentPacket.STREAM_CODEC,
            SetEnchantmentPacket::handle
        );
        registrar.playToClient(
            OpenItemValueScreenPacket.TYPE,
            OpenItemValueScreenPacket.STREAM_CODEC,
            (packet, context) -> context.enqueueWork(() -> ClientPacketHandler.handleOpenItemValueScreen(packet, context))
        );
    }
}
