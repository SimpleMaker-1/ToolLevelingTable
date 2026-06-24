package com.simplemaker.toolleveler.network.packets;

import com.simplemaker.toolleveler.ToolLeveler;
import com.simplemaker.toolleveler.blockentity.ToolLevelingTableBlockEntity;
import com.simplemaker.toolleveler.utils.Utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetEnchantmentPacket(
    BlockPos pos,
    Holder<Enchantment> enchantment,
    int level
) implements CustomPacketPayload {

    public static final Type<SetEnchantmentPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(ToolLeveler.MOD_ID, "set_enchantment")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SetEnchantmentPacket> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SetEnchantmentPacket::pos,

            ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT),
            SetEnchantmentPacket::enchantment,

            ByteBufCodecs.VAR_INT,
            SetEnchantmentPacket::level,

            SetEnchantmentPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetEnchantmentPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            ServerLevel level = player.serverLevel();

            if (!level.hasChunkAt(msg.pos())) return;

            BlockEntity be = level.getBlockEntity(msg.pos());
            if (!(be instanceof ToolLevelingTableBlockEntity table)) return;

            ItemStack stack = table.getStackToEnchant();
            if (stack.isEmpty()) return;

            Holder<Enchantment> holder = msg.enchantment();

            ItemEnchantments enchants = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            
            int existing = enchants.getLevel(holder);
            if (existing <= 0) return;

            long cost = Utils.getEnchantmentUpgradeCost(holder, msg.level());

            boolean success = Utils.freeCreativeUpgrades(player) || table.decreaseInventoryWorth(cost);
            if (!success) return;

            ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(enchants);
            builder.set(holder, msg.level());

            EnchantmentHelper.setEnchantments(stack, builder.toImmutable());
            table.setItem(0, stack);
            table.setChanged();
        });
    }
}
