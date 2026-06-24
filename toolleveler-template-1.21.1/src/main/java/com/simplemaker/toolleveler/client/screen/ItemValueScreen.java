package com.simplemaker.toolleveler.client.screen;

import com.simplemaker.toolleveler.client.screen.widgets.ItemValuesListWidget;
import com.simplemaker.toolleveler.config.ItemValueConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ItemValueScreen extends Screen {
    private static final Component TITLE = Component.translatable("block.toolleveler.item_values");
    private ItemValuesListWidget itemValues;
    private Component defaultItemValueText;

    private static final int TITLE_X = 10;
    private static final int TITLE_Y = 10;

    public ItemValueScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        super.init();
        int widgetWidth = ItemValuesListWidget.width();
        int leftPos = (this.width - widgetWidth) / 2;
        this.itemValues = new ItemValuesListWidget(this, leftPos, 25, this.height - 16);
        this.addWidget(itemValues);
        this.defaultItemValueText = Component.translatable(
                "screen.toolleveler.default_item_value_worth",
                ItemValueConfig.get().defaultItemWorth()
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // 🔥 FIXED FOR 1.21.1: Added mouse coordinates and partial ticks parameters
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        this.itemValues.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawString(this.font, defaultItemValueText, TITLE_X, TITLE_Y, 0xFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonID) {
        this.itemValues.mouseClicked(mouseX, mouseY, buttonID);
        return super.mouseClicked(mouseX, mouseY, buttonID);
    }

    // Note: In 1.21.1 mouseScrolled also includes a scrollX parameter in some mappings (double mouseX, double mouseY, double scrollX, double scrollY). 
    // If your compiler gets angry at this method next, adjust its signature accordingly!
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.itemValues.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }
}
