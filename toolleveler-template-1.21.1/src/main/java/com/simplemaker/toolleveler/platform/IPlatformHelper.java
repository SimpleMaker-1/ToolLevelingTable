package com.simplemaker.toolleveler.platform;

import com.simplemaker.toolleveler.ToolLeveler;
import com.simplemaker.toolleveler.blockentity.ToolLevelingTableBlockEntity;
import com.simplemaker.toolleveler.menu.ToolLevelingTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.function.Supplier;

public interface IPlatformHelper {

    IPlatformHelper INSTANCE = ToolLeveler.load(IPlatformHelper.class);

    boolean isModLoaded(String modId);

    Path getConfigDirectory();

    Supplier<MenuType<ToolLevelingTableMenu>> buildContainer();

    Supplier<BlockEntityType<? extends ToolLevelingTableBlockEntity>> buildBlockEntityType();

    ToolLevelingTableBlockEntity newBlockEntity(BlockPos pos, BlockState state);

    void openMenu(BlockState state, Level level, BlockPos pos, Player player);

}