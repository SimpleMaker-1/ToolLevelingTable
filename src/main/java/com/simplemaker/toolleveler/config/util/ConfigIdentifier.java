package com.simplemaker.toolleveler.config.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.simplemaker.toolleveler.ToolLeveler;
import com.simplemaker.toolleveler.config.CommandConfig;
import com.simplemaker.toolleveler.config.ItemValueConfig;
import com.simplemaker.toolleveler.config.ToolLevelingConfig;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ConfigIdentifier<T> {

    static final ConfigIdentifier<ToolLevelingConfig> GENERAL =
            new ConfigIdentifier<>(
                    "tool_leveling_table.json",
                    ToolLevelingConfig.CODEC,
                    ToolLevelingConfig::get,
                    ToolLevelingConfig::set,
                    ToolLevelingConfig::setToDefault
            );

    static final ConfigIdentifier<ItemValueConfig> ITEM_VALUES =
            new ConfigIdentifier<>(
                    "item_values.json",
                    ItemValueConfig.CODEC,
                    ItemValueConfig::get,
                    ItemValueConfig::set,
                    ItemValueConfig::setToDefault
            );

    static final ConfigIdentifier<CommandConfig> COMMANDS =
            new ConfigIdentifier<>(
                    "command_config.json",
                    CommandConfig.CODEC,
                    CommandConfig::get,
                    CommandConfig::set,
                    CommandConfig::setToDefault
            );

    private final String     fileName;
    private final Codec<T>   codec;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private final Runnable    resetter;

    ConfigIdentifier(String fileName, Codec<T> codec,
                     Supplier<T> getter, Consumer<T> setter, Runnable resetter) {
        this.fileName = fileName;
        this.codec    = codec;
        this.getter   = getter;
        this.setter   = setter;
        this.resetter = resetter;
    }

    public String getFileName() { return fileName; }

    public void setToDefault() { resetter.run(); }

    public JsonElement serialize() {
        DataResult<JsonElement> result =
                codec.encodeStart(JsonOps.INSTANCE, getter.get());
        result.error().ifPresent(p -> ToolLeveler.LOGGER.error(p.message()));
        return result.result().orElseThrow();
    }

    public void deserialize(JsonElement json) {
        DataResult<T> result = codec.parse(JsonOps.INSTANCE, json);
        result.error().ifPresent(p -> ToolLeveler.LOGGER.error(p.message()));
        setter.accept(result.result().orElseThrow());
    }
}