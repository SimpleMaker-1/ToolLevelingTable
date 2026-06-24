package com.simplemaker.toolleveler.client.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Either;
import com.simplemaker.toolleveler.client.screen.ItemValueScreen;
import com.simplemaker.toolleveler.config.ItemValueConfig;
import com.simplemaker.toolleveler.utils.CustomItemStack;
import com.simplemaker.toolleveler.utils.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemValuesListWidget extends ObjectSelectionList<ItemValueEntry> {
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int ENTRIES_PER_ROW = 9;
    private final ItemValueScreen screen;

    public ItemValuesListWidget(ItemValueScreen screen, int x, int y, int height) {
        // 🔥 FIXED: Removed parentheses from 'height' variable
        super(screen.getMinecraft(), width(), height, y, 18);
        this.screen = screen;
        
        // Use standard Mojang setters to keep internal boundaries in sync
        this.setX(x);
        this.refreshList();
    }

    private void refreshList() {
        List<NonNullList<CustomItemStack>> values = ItemValueConfig.get().values().entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(ItemValuesListWidget::mapEntryToItemStack)
                .filter(list -> list != null && !list.isEmpty())
                .toList();

        List<List<NonNullList<CustomItemStack>>> partitions = new ArrayList<>();
        for (int i = 0; i < values.size(); i += ENTRIES_PER_ROW) {
            partitions.add(values.subList(i, Math.min(i + ENTRIES_PER_ROW, values.size())));
        }

        for (List<NonNullList<CustomItemStack>> list : partitions) {
            this.addEntry(new ItemValueEntry(screen, list));
        }
    }

    // -------------------------
    // RENDER (FIXED FOR 1.21.1)
    // -------------------------
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderScrollBar();
        this.renderList(guiGraphics, mouseX, mouseY, partialTicks);
    }

    protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // no background rendering
    }

    private void renderScrollBar() {
        int x0Bar = this.getScrollbarPosition();
        int x1Bar = x0Bar + SCROLLBAR_WIDTH;
        int maxScroll = this.getMaxScroll();
        if (maxScroll <= 0) return;

        // 🔥 FIXED FOR 1.21.1: Uses standard ScreenRectangle / bounding methods
        int topY = this.getRectangle().top();
        int bottomY = this.getRectangle().bottom();
        int totalHeight = bottomY - topY;
        
        int thumbHeight = (int) ((float) (totalHeight * totalHeight) / (float) this.getMaxPosition());
        thumbHeight = Mth.clamp(thumbHeight, 32, totalHeight - 8);
        int thumbY = (int) this.getScrollAmount() * (totalHeight - thumbHeight) / maxScroll + topY;
        thumbY = Math.max(topY, thumbY);

        drawQuad(x0Bar, topY, x1Bar, bottomY, 0xFF000000);
        drawQuad(x0Bar, thumbY, x1Bar, thumbY + thumbHeight, 0xFF808080);
    }

    private void drawQuad(int x1, int y1, int x2, int y2, int argb) {
        RenderSystem.enableBlend();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
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
    // LIST RENDERING
    // -------------------------
    
    protected void renderList(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int itemCount = this.getItemCount();
        int leftX = this.getRowLeft();
        int topY = this.getRectangle().top();
        int bottomY = this.getRectangle().bottom();
        
        for (int i = 0; i < itemCount; i++) {
            int rowTop = this.getRowTop(i);
            int rowBottom = rowTop + this.itemHeight - 2;
            if (rowBottom >= topY && rowTop <= bottomY) {
                ItemValueEntry entry = this.getEntry(i);
                boolean hover = mouseX >= leftX && mouseX <= (leftX + this.getRowWidth()) && mouseY >= topY && mouseY <= bottomY;
                entry.render(
                        guiGraphics,
                        i,
                        rowTop,
                        leftX,
                        this.getRowWidth(),
                        this.itemHeight,
                        mouseX,
                        mouseY,
                        hover,
                        partialTicks
                );
            }
        }
    }

    // -------------------------
    // SCROLL / LAYOUT
    // -------------------------
    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width - SCROLLBAR_WIDTH;
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    protected int getRowTop(int i) {
        return this.getRectangle().top() - (int) this.getScrollAmount() + i * this.itemHeight;
    }

    public int getRowLeft() {
        return this.getX();
    }

    public int getRowRight() {
        return this.getX() + this.width;
    }

    @Override
    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - this.getRectangle().height());
    }

    // -------------------------
    // DATA MAPPING
    // -------------------------
    private static NonNullList<CustomItemStack> mapEntryToItemStack(
            Map.Entry<Either<Item, TagKey<Item>>, Long> entry
    ) {
        Either<Item, TagKey<Item>> key = entry.getKey();
        if (key.left().isPresent()) {
            NonNullList<CustomItemStack> list = NonNullList.withSize(1, CustomItemStack.EMPTY);
            list.set(0, new CustomItemStack(
                    key.left().get(),
                    Utils.getItemWorth(key.left().get())
            ));
            return list;
        }
        if (key.right().isEmpty()) return null;
        List<Item> items = ItemValueConfig.getAllFromTag(key.right().get());
        if (items.isEmpty()) return null;
        NonNullList<CustomItemStack> list = NonNullList.withSize(items.size(), CustomItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            list.set(i, new CustomItemStack(
                    items.get(i),
                    Utils.getItemWorth(items.get(i)),
                    key.right().get().location()
            ));
        }
        return list;
    }

    public static int width() {
        return (ENTRIES_PER_ROW * 18) + SCROLLBAR_WIDTH;
    }
}
