package com.simplemaker.toolleveler.init;

import com.simplemaker.toolleveler.ToolLeveler;
import com.simplemaker.toolleveler.block.ToolLevelingTableBlock;
import com.simplemaker.toolleveler.blockentity.ToolLevelingTableBlockEntity;
import com.simplemaker.toolleveler.menu.ToolLevelingTableMenu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRegistry {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, ToolLeveler.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, ToolLeveler.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, ToolLeveler.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ToolLeveler.MOD_ID);

    // Block
    // 🔥 FIXED FOR 1.21.1: Explicitly pass BlockBehaviour.Properties into the block constructor lambda
    public static final DeferredHolder<Block, ToolLevelingTableBlock> TLT_BLOCK = BLOCKS.register(ToolLeveler.TABLE, 
            () -> new ToolLevelingTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.5F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
            ));
    // Block item
    public static final DeferredHolder<Item, BlockItem> TLT_ITEM = ITEMS.register(ToolLeveler.TABLE, () -> new BlockItem(TLT_BLOCK.get(), new Item.Properties().stacksTo(64)));

    // Menu / container
    public static final DeferredHolder<MenuType<?>, MenuType<ToolLevelingTableMenu>> TLT_MENU = MENUS.register(ToolLeveler.TABLE, () -> IMenuTypeExtension.create(ToolLevelingTableMenu::new));

    // Block entity
    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ToolLevelingTableBlockEntity>> TLT_BLOCK_ENTITY = BLOCK_ENTITIES.register(ToolLeveler.TABLE, () -> BlockEntityType.Builder
            .of(ToolLevelingTableBlockEntity::new, TLT_BLOCK.get())
            .build(null));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        MENUS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
