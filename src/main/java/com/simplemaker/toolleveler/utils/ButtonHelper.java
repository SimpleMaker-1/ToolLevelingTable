package com.simplemaker.toolleveler.utils;

import com.simplemaker.toolleveler.client.screen.widgets.ButtonEntry;
import com.simplemaker.toolleveler.client.screen.widgets.ButtonListWidget;
import com.simplemaker.toolleveler.config.ToolLevelingConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Holder;

public final class ButtonHelper {

    public static boolean shouldButtonBeActive(ButtonEntry entry) {
        ButtonStatus status = entry.getStatus();
        return switch (status) {
            case NORMAL -> true;
            case USELESS -> ToolLevelingConfig.get().generalOptions().allowLevelingUselessEnchantments();
            case BREAK -> ToolLevelingConfig.get().generalOptions().allowLevelingBreakingEnchantments();
            default -> false;
        };
    }

    public static ButtonEntry getButtonEntry(ButtonListWidget parent, Holder<Enchantment> enchantment, int level) {

        ButtonEntry entry = new ButtonEntry(parent, enchantment, level);

        var keyOpt = enchantment.unwrapKey();

        List<ResourceKey<Enchantment>> whitelist = ToolLevelingConfig.get().enchantmentOptions().whitelist();
        List<ResourceKey<Enchantment>> blacklist = ToolLevelingConfig.get().enchantmentOptions().blacklist();

        if (keyOpt.isPresent()) {
            var key = keyOpt.get();

            if (!whitelist.isEmpty() && !whitelist.contains(key)) {
                entry.setStatus(ButtonStatus.NOT_WHITELISTED);
                return entry;
            }
            if (whitelist.isEmpty() && blacklist.contains(key)) {
                entry.setStatus(ButtonStatus.BLACKLISTED);
                return entry;
            }
        }

        if (level >= Short.MAX_VALUE) {
            entry.setStatus(ButtonStatus.MAX_LEVEL);
            return entry;
        }
        if (Utils.isEnchantmentAtCap(enchantment, level)) {
            entry.setStatus(ButtonStatus.CAPPED);
            return entry;
        }
        if (!Utils.isEnchantmentOverMinimum(enchantment, level)) {
            entry.setStatus(ButtonStatus.MIN_LEVEL);
            return entry;
        }
        
        // Max level is stored under the dynamic .definition() property data block
        if (enchantment.value().definition().maxLevel() == 1) {
            entry.setStatus(ButtonStatus.USELESS);
            return entry;
        }
        if (Utils.willEnchantmentBreak(enchantment, level)) {
            entry.setStatus(ButtonStatus.BREAK);
            return entry;
        }

        return entry;
    }

    public static Component getButtonText(ButtonEntry entry) {
        ChatFormatting format = getButtonTextFormatting(entry);
        final int nextLvl = entry.currentLevel + 1;
        Component lvl;
        if (nextLvl >= 1 && nextLvl <= 100) {
            lvl = Component.translatable("enchantment.level." + nextLvl).withStyle(format);
        } else {
            lvl = Component.literal(String.format("%,d", nextLvl)).withStyle(format);
        }
        return Component.translatable(entry.name)
                .withStyle(format)
                .append(" ")
                .append(lvl);
    }

    public static List<Component> getButtonToolTips(ButtonEntry data) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable(data.name).withStyle(ChatFormatting.AQUA));
        final String base = "container.toolleveler.tool_leveling_table";
        
        if (shouldButtonBeActive(data) || Utils.freeCreativeUpgrades(Minecraft.getInstance().player)) {
            tooltip.add(Component.translatable(base + ".current_level", String.format("%,d", data.currentLevel)).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.translatable(base + ".next_level", String.format("%,d", data.currentLevel + 1)).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.translatable(base + ".cost", String.format("%,d", data.upgradeCost)).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (Utils.freeCreativeUpgrades(Minecraft.getInstance().player)) {
            tooltip.add(Component.translatable(base + ".free_creative").withStyle(ChatFormatting.GREEN));
        } else if (data.getStatus() != ButtonStatus.NORMAL) {
            tooltip.add(Component.translatable(base + ".error." + data.getStatus().toString().toLowerCase()).withStyle(getButtonTextFormatting(data)));
        }
        return tooltip;
    }

    public static ChatFormatting getButtonTextFormatting(ButtonEntry entry) {
        if (Utils.freeCreativeUpgrades(Minecraft.getInstance().player)) {
            return ChatFormatting.RESET;
        }
        if (entry.getStatus() == ButtonStatus.USELESS) {
            return ChatFormatting.YELLOW;
        }
        if (entry.getStatus() != ButtonStatus.NORMAL) {
            return ChatFormatting.DARK_RED;
        }
        return ChatFormatting.RESET;
    }

    public enum ButtonStatus {
        NORMAL, NOT_WHITELISTED, BLACKLISTED, USELESS, BREAK, MAX_LEVEL, CAPPED, MIN_LEVEL
    }
}
