package com.simplemaker.toolleveler.utils;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class CustomItemStack {
    public static final CustomItemStack EMPTY = new CustomItemStack(Items.AIR, 0);
    public final ItemStack stack;
    public final long count;
    private final ResourceLocation tag;

    public CustomItemStack(Item item, long count, ResourceLocation tag) {
        this.stack = item.getDefaultInstance();
        this.count = count;
        this.tag = tag;
    }

    public CustomItemStack(Item item, long count) {
        this(item, count, null);
    }

    public boolean isEmpty() {
        return this.stack.isEmpty();
    }

    public List<Component> getTooltipLines(TooltipFlag flag) {
        List<Component> lines = Lists.newArrayList();

        //coloured by rarity
        MutableComponent title = Component.empty()
                .append(stack.getHoverName())
                .withStyle(stack.getRarity().color());

        // Use Data Components to check for custom names
        if (stack.getComponents().has(DataComponents.CUSTOM_NAME)) {
            title = title.withStyle(ChatFormatting.ITALIC);
        }
        lines.add(title);

        // Advanced tooltip: show registry ID
        if (flag.isAdvanced()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(this.stack.getItem());
            lines.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
        }

        // Tag line
        if (tag != null) {
            lines.add(Component.literal("Item-Tag: #" + tag).withStyle(ChatFormatting.DARK_GRAY));
        }

        // Worth
        lines.add(Component.translatable(
                "screen.toolleveler.item_value_worth",
                String.format("%,d", count)
        ).withStyle(ChatFormatting.DARK_GRAY));

        return lines;
    }
}
