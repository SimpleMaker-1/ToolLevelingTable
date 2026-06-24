package com.simplemaker.toolleveler.client;

import com.simplemaker.toolleveler.ToolLeveler;
import com.simplemaker.toolleveler.client.screen.ToolLevelingTableScreen;
import com.simplemaker.toolleveler.init.ModRegistry;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ToolLeveler.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(
                ModRegistry.TLT_MENU.get(),
                ToolLevelingTableScreen::new
        );
    }
    
}