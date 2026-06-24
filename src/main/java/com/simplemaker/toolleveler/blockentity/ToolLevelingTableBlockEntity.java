package com.simplemaker.toolleveler.blockentity;

import com.simplemaker.toolleveler.init.ModRegistry;
import com.simplemaker.toolleveler.menu.ToolLevelingTableMenu;
import com.simplemaker.toolleveler.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// 🔥 FIXED FOR 1.21.1: Explicitly added MenuProvider interface
public class ToolLevelingTableBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int NUMBER_OF_SLOTS = 16;
    private final NonNullList<ItemStack> items = NonNullList.withSize(NUMBER_OF_SLOTS, ItemStack.EMPTY);

    /** Leftover fractional fuel points carried between operations. */
    public long bonusPoints = 0L;

    private static final Component TITLE = Component.translatable("container.toolleveler.tool_leveling_table");

    /** Constructor called by newBlockEntity() in the block class. */
    public ToolLevelingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.TLT_BLOCK_ENTITY.get(), pos, state);
    }

    /* ----------------------------------- */
    /* NBT SAVE/LOAD                       */
    /* ----------------------------------- */
    
    // 🔥 FIXED FOR 1.21.1: Added HolderLookup.Provider parameters
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.bonusPoints = tag.getLong("BonusPoints");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putLong("BonusPoints", this.bonusPoints);
    }

    /* ----------------------------------- */
    /* CORE LOGIC                          */
    /* ----------------------------------- */

    /** Returns the item in slot 0 (the item to be enchanted). */
    public ItemStack getStackToEnchant() {
        return items.get(0);
    }

    /** @deprecated Use {@link #getStackToEnchant()} */
    @Deprecated
    public ItemStack getInputItem() {
        return getStackToEnchant();
    }

    /** Total value of all payment slots plus any stored bonus points. */
    public long getFuelValue() {
        long total = bonusPoints;
        for (int i = 1; i < NUMBER_OF_SLOTS; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                total += Utils.getStackWorth(stack);
            }
        }
        return total;
    }

    /**
     * Consume {@code cost} worth of items from the payment slots.
     * Any overpayment is stored in {@link #bonusPoints}.
     * Returns {@code true} on success, {@code false} if insufficient funds.
     */
    public boolean decreaseInventoryWorth(long cost) {
        if (getFuelValue() < cost) return false;

        // Drain stored bonus first
        if (bonusPoints >= cost) {
            bonusPoints -= cost;
            setChanged();
            return true;
        }

        cost -= bonusPoints;
        bonusPoints = 0;

        for (int i = 1; i < NUMBER_OF_SLOTS && cost > 0; i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;

            long stackValue = Utils.getStackWorth(stack);
            if (stackValue <= cost) {
                cost -= stackValue;
                items.set(i, ItemStack.EMPTY);
            } else {
                long singleValue = Utils.getItemWorth(stack);
                long needed = (long) Math.ceil((double) cost / singleValue);
                long usedValue = needed * singleValue;
                cost -= usedValue;
                int remaining = (int) (stack.getCount() - needed);
                if (remaining <= 0) {
                    items.set(i, ItemStack.EMPTY);
                } else {
                    stack.setCount(remaining);
                    items.set(i, stack);
                }
                // Store overpayment
                bonusPoints += (usedValue - cost);
                cost = 0;
            }
        }
        setChanged();
        return true;
    }

    /* ----------------------------------- */
    /* CONTAINER METHODS                   */
    /* ----------------------------------- */

    @Override
    public int getContainerSize() {
        return NUMBER_OF_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        setChanged();
        return ContainerHelper.removeItem(items, index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(items, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        items.set(index, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    /* ----------------------------------- */
    /* HOPPER SUPPORT                      */
    /* ----------------------------------- */

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] slots = new int[NUMBER_OF_SLOTS - 1];
        for (int i = 1; i < NUMBER_OF_SLOTS; i++) {
            slots[i - 1] = i;
        }
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction side) {
        return index > 0 && !stack.isEnchanted();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return index > 0 && !stack.isEnchanted();
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction side) {
        return index > 0;
    }

    /* ----------------------------------- */
    /* UI / MENU                           */
    /* ----------------------------------- */

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ToolLevelingTableMenu(id, inv, this, worldPosition);
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this && player.distanceToSqr(
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5
        ) <= 64.0;
    }

    /* ----------------------------------- */
    /* REDSTONE SIGNAL                     */
    /* ----------------------------------- */

    public int getSignalStrength() {
        int count = 0;
        for (int i = 1; i < NUMBER_OF_SLOTS; i++) {
            if (!items.get(i).isEmpty()) count++;
        }
        return Math.min(15, count);
    }
}