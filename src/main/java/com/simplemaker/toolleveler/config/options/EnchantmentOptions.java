package com.simplemaker.toolleveler.config.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simplemaker.toolleveler.config.util.CodecHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments; // 🔥 FIXED: Replaced EnchantmentKeys with Enchantments

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record EnchantmentOptions(
        List<ResourceKey<Enchantment>> whitelist,
        List<ResourceKey<Enchantment>> blacklist,
        short globalEnchantmentCap,
        Map<ResourceKey<Enchantment>, Short> enchantmentCaps,
        double globalUpgradeCostMultiplier,
        Map<ResourceKey<Enchantment>, Double> enchantmentUpgradeCostModifier,
        short globalMinimumEnchantmentLevel,
        Map<ResourceKey<Enchantment>, Short> minimumEnchantmentLevels
) {
    public static final Codec<List<ResourceKey<Enchantment>>> ENCHANTMENT_LIST = 
            ResourceKey.codec(Registries.ENCHANTMENT).listOf();

    public static final Codec<Map<ResourceKey<Enchantment>, Short>> ENCHANTMENT_TO_SHORT = 
            Codec.unboundedMap(ResourceKey.codec(Registries.ENCHANTMENT), CodecHelper.POSITIVE_SHORT);

    public static final Codec<Map<ResourceKey<Enchantment>, Double>> ENCHANTMENT_TO_DOUBLE = 
            Codec.unboundedMap(ResourceKey.codec(Registries.ENCHANTMENT), CodecHelper.PERCENTAGE);

    public static final Codec<EnchantmentOptions> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ENCHANTMENT_LIST.fieldOf("enchantment_whitelist")
                            .forGetter(EnchantmentOptions::whitelist),
                    ENCHANTMENT_LIST.fieldOf("enchantment_blacklist")
                            .forGetter(EnchantmentOptions::blacklist),
                    CodecHelper.NON_NEGATIVE_SHORT.fieldOf("global_enchantment_cap")
                            .forGetter(EnchantmentOptions::globalEnchantmentCap),
                    ENCHANTMENT_TO_SHORT.fieldOf("enchantment_caps")
                            .forGetter(EnchantmentOptions::enchantmentCaps),
                    CodecHelper.PERCENTAGE.fieldOf("global_upgrade_cost_multiplier")
                            .forGetter(EnchantmentOptions::globalUpgradeCostMultiplier),
                    ENCHANTMENT_TO_DOUBLE.fieldOf("enchantment_upgrade_cost_modifier")
                            .forGetter(EnchantmentOptions::enchantmentUpgradeCostModifier),
                    CodecHelper.NON_NEGATIVE_SHORT.fieldOf("global_minimum_enchantment_level")
                            .forGetter(EnchantmentOptions::globalMinimumEnchantmentLevel),
                    ENCHANTMENT_TO_SHORT.fieldOf("minimum_enchantment_levels")
                            .forGetter(EnchantmentOptions::minimumEnchantmentLevels)
            ).apply(instance, EnchantmentOptions::new)
    );

    public static final EnchantmentOptions DEFAULT = new EnchantmentOptions(
            List.of(),
            getDefaultBlacklist(),
            (short) 0,
            getDefaultCaps(),
            1.0D,
            getDefaultModifiers(),
            (short) 0,
            Map.of()
    );

    private static List<ResourceKey<Enchantment>> getDefaultBlacklist() {
        List<ResourceKey<Enchantment>> list = new ArrayList<>();
        // 🔥 FIXED: Restored to standard Enchantments keys
        list.add(Enchantments.MENDING);
        list.add(Enchantments.AQUA_AFFINITY);
        list.add(Enchantments.CHANNELING);
        list.add(Enchantments.BINDING_CURSE);
        list.add(Enchantments.VANISHING_CURSE);
        list.add(Enchantments.FLAME);
        list.add(Enchantments.INFINITY);
        list.add(Enchantments.MULTISHOT);
        list.add(Enchantments.SILK_TOUCH);
        return list;
    }

    private static Map<ResourceKey<Enchantment>, Short> getDefaultCaps() {
        Map<ResourceKey<Enchantment>, Short> caps = new HashMap<>();
        caps.put(Enchantments.FIRE_PROTECTION, (short) 100);
        return caps;
    }

    private static Map<ResourceKey<Enchantment>, Double> getDefaultModifiers() {
        Map<ResourceKey<Enchantment>, Double> mod = new HashMap<>();
        mod.put(Enchantments.LOOTING, 1.5D);
        return mod;
    }
}
