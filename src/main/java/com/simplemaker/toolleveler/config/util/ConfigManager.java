package com.simplemaker.toolleveler.config.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.simplemaker.toolleveler.ToolLeveler;
import com.simplemaker.toolleveler.utils.ProjectLinks;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ConfigManager {

    private static final File CONFIG_DIR =
            FMLPaths.CONFIGDIR.get()
                    .resolve(ToolLeveler.MOD_ID)
                    .toFile();

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    public static final Map<String, ConfigIdentifier<?>> CONFIGS = Map.of(
            "general",        ConfigIdentifier.GENERAL,
            "item_values",    ConfigIdentifier.ITEM_VALUES,
            "command_config", ConfigIdentifier.COMMANDS
    );

    public static void loadAndVerifyConfigs() {
        for (ConfigIdentifier<?> config : CONFIGS.values()) {
            File configFile = new File(CONFIG_DIR, config.getFileName());

            if (!configFile.exists()) {
                config.setToDefault();
                writeConfigToFile(config, configFile);
                ToolLeveler.LOGGER.warn("No config '{}' found — created default.", config.getFileName());
                continue;
            }

            try {
                loadConfigFromFile(config, configFile);
                ToolLeveler.LOGGER.info("Config '{}' loaded.", config.getFileName());
            } catch (Exception e) {
                ToolLeveler.LOGGER.error("Error loading '{}': {}", config.getFileName(), e.getMessage());
                ToolLeveler.LOGGER.error("Using default config for '{}'.", config.getFileName());
                config.setToDefault();
            }
        }
    }

    private static void writeConfigToFile(ConfigIdentifier<?> config, File file) {
        try {
            JsonElement json = config.serialize();
            JsonWriter writer = new JsonWriter(new FileWriter(file));
            writer.setIndent("\t");
            GSON.toJson(json, writer);
            writer.close();
        } catch (Exception e) {
            ToolLeveler.LOGGER.error("Error writing '{}': {}", config.getFileName(), e.getMessage());
        }
    }

    private static void loadConfigFromFile(ConfigIdentifier<?> config, File file)
            throws FileNotFoundException {
        JsonElement json = JsonParser.parseReader(new FileReader(file));
        config.deserialize(json);
    }

    public static void resetAllConfigs() {
        for (ConfigIdentifier<?> config : CONFIGS.values()) {
            config.setToDefault();
            File configFile = new File(CONFIG_DIR, config.getFileName());
            writeConfigToFile(config, configFile);
            ToolLeveler.LOGGER.info("Config '{}' reset to default.", config.getFileName());
        }
    }

    public static void createConfigFolder() {
        if (!CONFIG_DIR.exists()) {
            if (!CONFIG_DIR.mkdirs()) {
                throw new RuntimeException("Could not create config folder: " + CONFIG_DIR.getAbsolutePath());
            }
        }

        try {
            File readme = new File(CONFIG_DIR, "README.txt");
            if (!readme.exists()) {
                FileWriter writer = new FileWriter(readme);
                for (String line : getReadmeContent()) {
                    writer.write(line + "\n");
                }
                writer.close();
                ToolLeveler.LOGGER.info("Created README.txt in config folder.");
            }
        } catch (Exception e) {
            ToolLeveler.LOGGER.error("Could not create README.txt: {}", e.getMessage());
        }
    }

    private static List<String> getReadmeContent() {
        List<String> lines = new ArrayList<>();
        lines.add("============================================================");
        lines.add("                        IMPORTANT");
        lines.add("============================================================");
        lines.add("");
        lines.add("Before editing the config, please take a look at the wiki.");
        lines.add("You can find information about all configs and their options there.");
        lines.add("The wiki is located at: " + ProjectLinks.WIKI.url);
        lines.add("");
        return lines;
    }
}