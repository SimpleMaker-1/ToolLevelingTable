package com.simplemaker.toolleveler.network;

import com.simplemaker.toolleveler.ToolLeveler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.enchantment.Enchantment;

public interface ServerBoundPacketHandler {

    ServerBoundPacketHandler INSTANCE = ToolLeveler.load(ServerBoundPacketHandler.class);

    void enchantAtToolLevelingTable(BlockPos pos, Enchantment enchantment, int level);

}