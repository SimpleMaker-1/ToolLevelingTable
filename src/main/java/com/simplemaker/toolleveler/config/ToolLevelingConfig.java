package com.simplemaker.toolleveler.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simplemaker.toolleveler.config.options.EnchantmentOptions;
import com.simplemaker.toolleveler.config.options.GeneralOptions;

public record ToolLevelingConfig(
        GeneralOptions generalOptions,
        EnchantmentOptions enchantmentOptions
) {
    public static final Codec<ToolLevelingConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    GeneralOptions.CODEC.fieldOf("general_options")
                            .forGetter(ToolLevelingConfig::generalOptions),
                    EnchantmentOptions.CODEC.fieldOf("enchantment_options")
                            .forGetter(ToolLevelingConfig::enchantmentOptions)
            ).apply(instance, ToolLevelingConfig::new)
    );

    public static final ToolLevelingConfig DEFAULT =
            new ToolLevelingConfig(GeneralOptions.DEFAULT, EnchantmentOptions.DEFAULT);

    private static ToolLevelingConfig INSTANCE = DEFAULT;

    public static ToolLevelingConfig get()                { return INSTANCE; }
    public static void set(ToolLevelingConfig config)     { INSTANCE = config; }
    public static void setToDefault()                     { INSTANCE = DEFAULT; }
}