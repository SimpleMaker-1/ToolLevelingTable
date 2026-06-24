package com.simplemaker.toolleveler.menu;

import com.simplemaker.toolleveler.blockentity.ToolLevelingTableBlockEntity;
import com.simplemaker.toolleveler.init.ModRegistry;
import com.simplemaker.toolleveler.menu.slot.EquipmentSlots;
import com.simplemaker.toolleveler.menu.slot.OffhandSlot;
import com.simplemaker.toolleveler.menu.slot.PaymentSlot;
import com.simplemaker.toolleveler.menu.slot.UpgradeSlot;
import com.simplemaker.toolleveler.utils.Utils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ToolLevelingTableMenu extends AbstractContainerMenu {

    private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS =
            new EquipmentSlot[]
            { 
            EquipmentSlot.FEET, 
            EquipmentSlot.LEGS, 
            EquipmentSlot.CHEST, 
            EquipmentSlot.HEAD 
            };

    private static final int[][] EQUIPMENT_SLOT_POINTS = new int[][]{
        {215, 154}, // Feet coordinates
        {215, 136}, // Legs coordinates
        {197, 154}, // Chest coordinates
        {197, 136}  // Head coordinates
    };
    private final ContainerLevelAccess worldPos;
    private final Container container;

    /** Client-side constructor (called via IMenuTypeExtension). */
    public ToolLevelingTableMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv,
                new SimpleContainer(ToolLevelingTableBlockEntity.NUMBER_OF_SLOTS),
                buf.readBlockPos());
    }

    /** Server-side constructor. */
    public ToolLevelingTableMenu(int id, Inventory playerInv, Container container, BlockPos pos) {
        super(ModRegistry.TLT_MENU.get(), id);

        this.worldPos  = ContainerLevelAccess.create(playerInv.player.level(), pos);
        this.container = container;

        final int slotSize = 18;

        // Slot 0 — item to enchant
        this.addSlot(new UpgradeSlot(container, 0, 44, 22));

        // Slots 1-15 — payment items (3 rows × 5 cols)
        int startX = 8;
        int startY = 68;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                this.addSlot(new PaymentSlot(container,
                        1 + (row * 5) + col,
                        startX + col * slotSize,
                        startY + row * slotSize));
            }
        }

        // Player inventory (3 rows)
        startY = 136;
        startX = 17;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv,
                        9 + (row * 9) + col,
                        startX + col * slotSize,
                        startY + row * slotSize));
            }
        }

        // Hotbar
        startY = 194;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, startX + col * slotSize, startY));
        }

        // Armor slots
        for (int i = 0; i < 4; i++) {
            int[] p = EQUIPMENT_SLOT_POINTS[i];
            this.addSlot(new EquipmentSlots(playerInv, p[0], p[1], VALID_EQUIPMENT_SLOTS[i]));
        }

        // Offhand
        this.addSlot(new OffhandSlot(playerInv, 206, 172));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(worldPos, player, ModRegistry.TLT_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        int tileSlots = ToolLevelingTableBlockEntity.NUMBER_OF_SLOTS;

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index >= 0 && index < tileSlots) {
                // From tile → player
                if (!this.moveItemStackTo(itemstack1, tileSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.slots.get(1).mayPlace(itemstack1)) {
                // From player → payment slots
                if (!this.moveItemStackTo(itemstack1, 1, tileSlots, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Try upgrade slot
                if (this.slots.get(0).hasItem() || !this.slots.get(0).mayPlace(itemstack1)) {
                    return ItemStack.EMPTY;
                }
                ItemStack single = itemstack1.copy();
                single.setCount(1);
                itemstack1.shrink(1);
                this.slots.get(0).set(single);
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    public long getContainerWorth() {
        long worth = 0;
        for (int i = 1; i < ToolLevelingTableBlockEntity.NUMBER_OF_SLOTS; i++) {
            worth += Utils.getStackWorth(this.container.getItem(i));
        }
        return worth;
    }

    public BlockPos getPos() {
        return this.worldPos.evaluate((level, pos) -> pos, BlockPos.ZERO);
    }

    public long getBonusPoints() {
        return this.worldPos.evaluate((level, pos) -> {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof ToolLevelingTableBlockEntity table) {
                return table.bonusPoints;
            }
            return 0L;
        }, 0L);
    }
}