package com.simplemaker.toolleveler.client.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simplemaker.toolleveler.ToolLeveler;
import com.simplemaker.toolleveler.client.screen.ItemValueScreen;
import com.simplemaker.toolleveler.utils.CustomItemStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ItemValueEntry extends ObjectSelectionList.Entry<ItemValueEntry> {
    private final ItemValueScreen screen;
    private final List<NonNullList<CustomItemStack>> list;
    private static Component NARRATION = null;
    private int counter = 0;

    public ItemValueEntry(ItemValueScreen screen, List<NonNullList<CustomItemStack>> list) {
        this.screen = screen;
        this.list = list;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseInBounds, float partialTick) {
        for (int i = 0; i < list.size(); i++) {
            int x = left + (i * 18);
            CustomItemStack customStack = getNextItemStack(list.get(i));
            
            guiGraphics.renderItem(
                    customStack.stack,
                    x + 1,
                    top + 1
            );

            if (isMouseOverItem(x, top, mouseX, mouseY) && isMouseInBounds) {
                renderSlotHighlight(guiGraphics, x, top, 0x33FFFFFF);
                renderItemTooltip(guiGraphics, customStack, mouseX, mouseY);
            }
        }
        counter++;
    }

    private void renderItemTooltip(GuiGraphics guiGraphics, CustomItemStack stack, int mouseX, int mouseY) {
        // 🔥 FIXED FOR 1.21.1: TooltipFlag properties migrated to static inner Default instances
        TooltipFlag flag = screen.getMinecraft().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        List<Component> tooltips = stack.getTooltipLines(flag);
        
        // 🔥 FIXED FOR 1.21.1: Method renamed to renderComponentTooltip to natively pass a list of Components
        guiGraphics.renderComponentTooltip(
                screen.getMinecraft().font,
                tooltips,
                mouseX,
                mouseY
            );
    }

    private boolean isMouseOverItem(int left, int top, int mouseX, int mouseY) {
        return mouseX > left && mouseX <= (left + 18) && mouseY > top && mouseY <= (top + 18);
    }

    @Override
    public Component getNarration() {
        if (NARRATION == null) {
            NARRATION = Component.translatable(
                    "screen." + ToolLeveler.MOD_ID + ".item_values"
            );
        }
        return NARRATION;
    }

    private CustomItemStack getNextItemStack(NonNullList<CustomItemStack> list) {
        if (list.size() == 1) {
            return list.get(0);
        }
        int rendersPerItem = 60;
        return list.get((counter / rendersPerItem) % list.size());
    }

    private static void renderSlotHighlight(GuiGraphics guiGraphics, int x, int y, int color) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        guiGraphics.fill(x, y, x + 18, y + 18, color);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }
}
