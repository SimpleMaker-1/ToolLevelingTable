package com.simplemaker.toolleveler.client.screen;

import com.simplemaker.toolleveler.ToolLeveler;
import com.simplemaker.toolleveler.client.screen.widgets.ButtonListWidget;
import com.simplemaker.toolleveler.menu.ToolLevelingTableMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ToolLevelingTableScreen extends AbstractContainerScreen<ToolLevelingTableMenu> {
    // 🔥 FIXED FOR 1.21.1: Use static helper method instead of the private constructor
    public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(ToolLeveler.MOD_ID, "textures/gui/tool_leveling_table.png");
    
    public ButtonListWidget buttonList;
    private byte ticksSinceUpdate = 0;

    public ToolLevelingTableScreen(ToolLevelingTableMenu container, Inventory inv, Component name) {
        super(container, inv, name);
        this.imageWidth = 248;
        this.imageHeight = 220;
        this.inventoryLabelY += 52;
        this.titleLabelX -= 1;
    }

    @Override
    protected void init() {
        super.init();
        this.buttonList = new ButtonListWidget(this, this.leftPos + 104, this.topPos + 22, 136, 98);
        this.addWidget(this.buttonList);
    }

    @Override
    public void containerTick() {
        if (++this.ticksSinceUpdate % 5 == 0) {
            this.ticksSinceUpdate = 0;
            this.buttonList.refreshList();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        this.buttonList.render(graphics, mouseX, mouseY, partialTicks);
        
        graphics.pose().pushPose();
        graphics.pose().translate(0.0, 0.0, 10.0);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderPointsSummary(graphics);
        graphics.pose().popPose();
        
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderPointsSummary(GuiGraphics graphics) {
        final String key = "container.toolleveler.tool_leveling_table.worth.";
        Component bonusPoints = Component.translatable(key + "bonus_points", String.format("%,d", this.menu.getBonusPoints()));
        Component invWorth = Component.translatable(key + "inv", String.format("%,d", this.menu.getContainerWorth()));
        
        int left = this.leftPos + 8;
        graphics.drawString(this.font, bonusPoints, left, this.topPos + 45, 0x404040, false);
        graphics.drawString(this.font, invWorth, left, this.topPos + 56, 0x404040, false);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        this.buttonList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.buttonList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(GUI_TEXTURE, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }
}
