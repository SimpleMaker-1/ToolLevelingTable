package com.simplemaker.toolleveler.client.screen.widgets;

import com.simplemaker.toolleveler.network.packets.SetEnchantmentPacket;
import com.simplemaker.toolleveler.utils.ButtonHelper;
import com.simplemaker.toolleveler.utils.ButtonHelper.ButtonStatus;
import com.simplemaker.toolleveler.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class ButtonEntry extends ObjectSelectionList.Entry<ButtonEntry> {
    public String name;
    public int currentLevel;
    public long upgradeCost;
    private ButtonStatus status = ButtonStatus.NORMAL;
    private final ButtonListWidget parent;
    private final Holder<Enchantment> enchantment;
    private static Component NARRATION = null;
    private final Button button;

    public ButtonEntry(ButtonListWidget parent, Holder<Enchantment> enchantment, int level) {
        this.enchantment = enchantment;
        this.currentLevel = level;
        this.parent = parent;

        this.name = enchantment.value().description().getString();

        this.upgradeCost = Utils.getEnchantmentUpgradeCost(enchantment, level + 1);

        this.button = Button.builder(
                ButtonHelper.getButtonText(this),
                b -> PacketDistributor.sendToServer(
                        new SetEnchantmentPacket(
                            this.parent.screen.getMenu().getPos(),
                            this.enchantment,
                            this.currentLevel + 1
                        )
                )
        ).size(121, 20).build();

        updateButtonText();
    }

    @Override
    public void render(GuiGraphics graphics, int index, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean mouseOver, float partialTicks) {
        long worth = this.parent.screen.getMenu().getContainerWorth() + this.parent.screen.getMenu().getBonusPoints();
        boolean active = (this.upgradeCost <= worth && ButtonHelper.shouldButtonBeActive(this)) || Utils.freeCreativeUpgrades(Minecraft.getInstance().player);
        
        this.button.setX(left + 1);
        this.button.setY(top + 1);
        this.button.active = active;
        this.button.setWidth(entryWidth - 2);
        this.button.render(graphics, mouseX, mouseY, partialTicks);

        if (this.button.isHoveredOrFocused()) {
            List<Component> tooltip = ButtonHelper.getButtonToolTips(this);
            graphics.renderComponentTooltip(this.parent.screen.getMinecraft().font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.button.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void updateButtonText() {
        this.button.setMessage(ButtonHelper.getButtonText(this));
    }

    @Override
    public Component getNarration() {
        if (NARRATION == null) {
            NARRATION = Component.translatable("screen.toolleveler.tool_leveling_table");
        }
        return NARRATION;
    }

    public void setStatus(ButtonStatus status) {
        this.status = status;
        updateButtonText();
    }

    public ButtonStatus getStatus() {
        return status;
    }

    // Getter helper for utility classes
    public Holder<Enchantment> getEnchantment() {
        return this.enchantment;
    }
}
