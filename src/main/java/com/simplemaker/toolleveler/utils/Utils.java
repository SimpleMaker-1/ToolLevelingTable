package com.simplemaker.toolleveler.utils;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.simplemaker.toolleveler.config.ItemValueConfig;
import com.simplemaker.toolleveler.config.ToolLevelingConfig;
import com.simplemaker.toolleveler.config.options.EnchantmentOptions;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public final class Utils {

    // 🔥 FIXED FOR 1.21.1: Map keys changed to ResourceKey<Enchantment>
    // Renamed FISHING_LUCK to LUCK_OF_THE_SEA and FISHING_SPEED to LURE
    public static final Map<ResourceKey<Enchantment>, Integer> BREAKING_ENCHANTMENTS = ImmutableMap.of(
            Enchantments.LUCK_OF_THE_SEA, 84,
            Enchantments.QUICK_CHARGE, 5,
            Enchantments.THORNS, 7,
            Enchantments.LURE, 5
    );


    public static long getEnchantmentUpgradeCost(Holder<Enchantment> enchantment, int level) {
        EnchantmentOptions options = ToolLevelingConfig.get().enchantmentOptions();
        double globalModifier = options.globalUpgradeCostMultiplier();
        
        // 🔥 FIXED FOR 1.21.1: Read using the extracted ResourceKey pointer
        ResourceKey<Enchantment> key = enchantment.unwrapKey().orElse(null);
        double specificModifier = key != null ? options.enchantmentUpgradeCostModifier().getOrDefault(key, 1.0D) : 1.0D;
        
        long minCost = ToolLevelingConfig.get().generalOptions().minimumUpgradeCost();
        double raw = (0.87 * level * level) + (300 * level);
        long cost = (long) (raw * specificModifier * globalModifier);
        return Math.max(minCost, cost);
    }

    public static long getItemWorth(Item item) {
        ItemValueConfig cfg = ItemValueConfig.get();
        return cfg.itemValues().getOrDefault(item, cfg.defaultItemWorth());
    }

    public static long getItemWorth(ItemStack stack) {
        return getItemWorth(stack.getItem());
    }

    public static long getStackWorth(ItemStack stack) {
        if (stack.isEmpty()) return 0L;
        return stack.getCount() * getItemWorth(stack);
    }

    public static boolean isEnchantmentAtCap(Holder<Enchantment> enchantment, int level) {
        EnchantmentOptions options = ToolLevelingConfig.get().enchantmentOptions();
        short globalCap = options.globalEnchantmentCap();
        ResourceKey<Enchantment> key = enchantment.unwrapKey().orElse(null);
        if (key == null) return false;

        if (globalCap > 0) {
            short specific = options.enchantmentCaps().getOrDefault(key, globalCap);
            return level >= Math.min(globalCap, specific);
        }
        Short cap = options.enchantmentCaps().get(key);
        return cap != null && level >= cap;
    }

    public static boolean isEnchantmentOverMinimum(Holder<Enchantment> enchantment, int level) {
        EnchantmentOptions options = ToolLevelingConfig.get().enchantmentOptions();
        short globalMin = options.globalMinimumEnchantmentLevel();
        ResourceKey<Enchantment> key = enchantment.unwrapKey().orElse(null);
        if (key == null) return true;

        if (globalMin > 0) {
            short specific = options.minimumEnchantmentLevels().getOrDefault(key, globalMin);
            return level >= Math.max(globalMin, specific);
        }
        Short min = options.minimumEnchantmentLevels().get(key);
        return min == null || level >= min;
    }

    public static boolean willEnchantmentBreak(Holder<Enchantment> enchantment, int level) {
        ResourceKey<Enchantment> key = enchantment.unwrapKey().orElse(null);
        if (key == null) return false;
        
        Integer cap = BREAKING_ENCHANTMENTS.get(key);
        return cap != null && level >= cap;
    }

    public static boolean freeCreativeUpgrades(Player player) {
        return ToolLevelingConfig.get().generalOptions().freeUpgradesForCreativePlayers() && player != null && player.isCreative();
    }
}
