package com.simplemaker.toolleveler;

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // ✅ Ensure this is imported

import com.simplemaker.toolleveler.config.util.ConfigManager;
import com.simplemaker.toolleveler.init.ModCreativeTab;
import com.simplemaker.toolleveler.init.ModRegistry;
import com.simplemaker.toolleveler.network.ModPackets;


import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(ToolLeveler.MOD_ID)
public class ToolLeveler {
    public static final String MOD_ID = "toolleveler";
    public static final String MOD_NAME = "ToolLeveler++";
    public static final String TABLE = "tool_leveling_table";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public ToolLeveler(IEventBus modBus) {
        ModRegistry.register(modBus);
        ModPackets.register(modBus);
        
        modBus.addListener(this::commonSetup);
        modBus.addListener(ModCreativeTab::addToTabs);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ConfigManager.createConfigFolder();
            ConfigManager.loadAndVerifyConfigs();
        });
    }

    /** Service-loader helper used by platform interfaces. */
    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No implementation found for: " + clazz.getName()));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
