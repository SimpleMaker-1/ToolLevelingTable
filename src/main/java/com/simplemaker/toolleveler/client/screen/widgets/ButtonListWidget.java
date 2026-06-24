package com.simplemaker.toolleveler.client.screen.widgets;

import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.simplemaker.toolleveler.client.screen.ToolLevelingTableScreen;
import com.simplemaker.toolleveler.utils.ButtonHelper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class ButtonListWidget extends ObjectSelectionList<ButtonEntry> {
    private static final int SCROLLBAR_BUFFER_RIGHT = 1;
    private static final int SCROLLBAR_WIDTH = 6;
    final ToolLevelingTableScreen screen;
    
    private final int rowLeftOffset;
    private final int entryWidth;

    public ButtonListWidget(ToolLevelingTableScreen screen, int x, int y, int width, int height) {
        // 🔥 FIXED FOR 1.21.1: 5 parameters (minecraft, width, height, y0, itemHeight)
        super(screen.getMinecraft(), width, height, y, 22);
        this.screen = screen;
        
        // Use standard Mojang widget setter for X position
        this.setX(x);
        
        this.rowLeftOffset = 1;
        int rowRightOffset = width - SCROLLBAR_WIDTH - 1 - SCROLLBAR_BUFFER_RIGHT;
        this.entryWidth = rowRightOffset - rowLeftOffset;
    }

    // -------------------------
    // DATA (FIXED FOR 1.21.1 ENCHANTMENTS)
    // -------------------------
    public void refreshList() {
        this.clearEntries();
        ItemStack stack = this.screen.getMenu().getSlot(0).getItem();
        if (!stack.is(Items.AIR)) {
            // 🔥 FIXED FOR 1.21.1: EnchantmentHelper now returns ItemEnchantments wrapper
            ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            
            // Loop over Holder<Enchantment> registry entries
            for (Map.Entry<Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
                // Pass the raw Enchantment instance or Holder depending on your ButtonHelper signature
                ButtonEntry buttonEntry = ButtonHelper.getButtonEntry(this, entry.getKey(), entry.getValue());
                this.addEntry(buttonEntry);
            }
        }
    }

    // -------------------------
    // RENDER (FIXED FOR 1.21.1)
    // -------------------------
    
    // 🔥 FIXED: Overriding renderWidget because render() is final
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.renderScrollBar();
        this.renderList(guiGraphics, mouseX, mouseY, partialTick);
    }

    // 🔥 FIXED: Added correct parameters and removed @Override since it's custom behavior here
    protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.fill(this.getX(), this.getRectangle().top(), this.getX() + this.width, this.getRectangle().bottom(), 0xFF8B8B8B);
    }

    // 🔥 FIXED: Matching exact signature for 1.21.1 list rendering
    
    protected void renderList(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int itemCount = this.getItemCount();
        int topY = this.getRectangle().top();
        int bottomY = this.getRectangle().bottom();
        
        for (int i = 0; i < itemCount; i++) {
            int rowTop = this.getRowTop(i);
            int rowBottom = rowTop + this.itemHeight;
            if (rowBottom >= topY && rowTop <= bottomY) {
                ButtonEntry entry = this.getEntry(i);
                boolean hover = this.isMouseOver(mouseX, mouseY);
                entry.render(
                        guiGraphics,
                        i,
                        rowTop,
                        this.getRowLeft(),
                        this.getRowWidth(),
                        22,
                        mouseX,
                        mouseY,
                        hover,
                        partialTicks
                );
            }
        }
    }

    // -------------------------
    // CLICK HANDLING
    // -------------------------
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int key) {
        this.updateScrollingState(mouseX, mouseY, key);
        if (!this.isMouseOver(mouseX, mouseY)) return false;
        ButtonEntry entry = this.getEntryAtClick(mouseX, mouseY);
        if (entry != null) {
            this.setFocused(entry);
            this.setSelected(entry);
            return entry.mouseClicked(mouseX, mouseY, key);
        }
        return false;
    }

    protected final ButtonEntry getEntryAtClick(double mouseX, double mouseY) {
        int left = this.getRowLeft() + 1;
        int right = this.getRowRight() - 1;
        int topY = this.getRectangle().top();
        
        double adjustedMouseY = mouseY - topY + 1 + this.getScrollAmount() - 2F;
        int index = (int) (adjustedMouseY / this.itemHeight);
        double relativeY = adjustedMouseY % this.itemHeight;
        boolean padding = relativeY < 1 || relativeY > this.itemHeight - 1;
        
        return mouseX >= left && mouseX <= right && index >= 0 && adjustedMouseY >= 0 && index < this.getItemCount() && !padding ? this.children().get(index) : null;
    }

    // -------------------------
    // SCROLLBAR (UPDATED RENDERING)
    // -------------------------
    private void renderScrollBar() {
        int x0Bar = this.getScrollbarPosition();
        int x1Bar = x0Bar + SCROLLBAR_WIDTH;
        int topY = this.getRectangle().top() + 1;
        int bottomY = this.getRectangle().bottom() - 1;
        
        int maxScroll = this.getMaxScroll();
        if (maxScroll <= 0) return;
        
        int totalHeight = bottomY - topY;
        int thumbHeight = (int) ((float) (totalHeight * totalHeight) / (float) this.getMaxPosition());
        thumbHeight = Mth.clamp(thumbHeight, 32, totalHeight - 8);
        int thumbY = (int) this.getScrollAmount() * (totalHeight - thumbHeight) / maxScroll + topY;
        thumbY = Math.max(topY, thumbY);

        // background
        drawRect(x0Bar, topY, x1Bar, bottomY, 0xFF000000);
        // thumb
        drawRect(x0Bar, thumbY, x1Bar, thumbY + thumbHeight, 0xFF808080);
        // highlight strip
        drawRect(x0Bar, thumbY, x1Bar - 1, thumbY + thumbHeight - 1, 0xFFC0C0C0);
    }

    private void drawRect(int x1, int y1, int x2, int y2, int argb) {
        RenderSystem.enableBlend();
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buffer = t.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        float a = ((argb >> 24) & 255) / 255f;
        float r = ((argb >> 16) & 255) / 255f;
        float g = ((argb >> 8) & 255) / 255f;
        float b = (argb & 255) / 255f;
        
        buffer.addVertex(x1, y2, 0).setColor(r, g, b, a);
        buffer.addVertex(x2, y2, 0).setColor(r, g, b, a);
        buffer.addVertex(x2, y1, 0).setColor(r, g, b, a);
        buffer.addVertex(x1, y1, 0).setColor(r, g, b, a);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    // -------------------------
    // LAYOUT
    // -------------------------
    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width - SCROLLBAR_WIDTH - SCROLLBAR_BUFFER_RIGHT;
    }

    @Override
    public int getRowWidth() {
        return this.entryWidth;
    }

    @Override
    protected int getRowTop(int i) {
        return this.getRectangle().top() + 1 - (int) this.getScrollAmount() + i * this.itemHeight;
    }

    @Override
    public int getRowLeft() {
        return this.getX() + this.rowLeftOffset;
    }

    @Override
    public int getRowRight() {
        return this.getRowLeft() + this.entryWidth;
    }

    @Override
    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight;
    }

    @Override
    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - this.getRectangle().height() + 2);
    }
}
