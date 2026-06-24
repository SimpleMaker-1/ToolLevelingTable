package com.simplemaker.toolleveler.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.simplemaker.toolleveler.config.CommandConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Collection;
import java.util.Optional;

public final class SuperEnchantCommand {
    private static final DynamicCommandExceptionType NONLIVING = new DynamicCommandExceptionType(n -> Component.translatable("commands.enchant.failed.entity", n));
    private static final DynamicCommandExceptionType INCOMPATIBLE = new DynamicCommandExceptionType(n -> Component.translatable("commands.superenchant.failed.incompatible", n));
    private static final DynamicCommandExceptionType WRONG = new DynamicCommandExceptionType(n -> Component.translatable("commands.superenchant.failed.wrong", n));
    private static final DynamicCommandExceptionType ITEMLESS = new DynamicCommandExceptionType(n -> Component.translatable("commands.enchant.failed.itemless", n));
    private static final SimpleCommandExceptionType FAILED = new SimpleCommandExceptionType(Component.translatable("commands.enchant.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("superenchant")
                        .requires(src -> src.hasPermission(3))
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(Commands.argument("enchantment", StringArgumentType.string())
                                        .executes(ctx -> enchant(
                                                ctx.getSource(),
                                                EntityArgument.getEntities(ctx, "targets"),
                                                StringArgumentType.getString(ctx, "enchantment"),
                                                1
                                        ))
                                        .then(Commands.argument("level", IntegerArgumentType.integer(0, Short.MAX_VALUE))
                                                .executes(ctx -> enchant(
                                                        ctx.getSource(),
                                                        EntityArgument.getEntities(ctx, "targets"),
                                                        StringArgumentType.getString(ctx, "enchantment"),
                                                        IntegerArgumentType.getInteger(ctx, "level")
                                                ))
                                        )
                                )
                        )
        );
    }

    private static int enchant(
            CommandSourceStack source,
            Collection<? extends Entity> targets,
            String enchantmentId,
            int level
    ) throws CommandSyntaxException {
        
        // 🔥 FIXED FOR 1.21.1: Grab dynamic registry holder wrapper for Enchantments
        ResourceLocation resLoc = ResourceLocation.parse(enchantmentId);
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, resLoc);
        Optional<Holder.Reference<Enchantment>> enchantHolderOpt = source.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(key);

        if (enchantHolderOpt.isEmpty()) {
            throw new SimpleCommandExceptionType(
                    Component.literal("Unknown enchantment: " + enchantmentId)
            ).create();
        }

        Holder<Enchantment> enchantmentHolder = enchantHolderOpt.get();
        Enchantment enchantment = enchantmentHolder.value();
        int success = 0;
        
        boolean allowWrong = CommandConfig.get().superEnchantOptions().allowWrongEnchantments();
        boolean allowIncompatible = CommandConfig.get().superEnchantOptions().allowIncompatibleEnchantments();

        for (Entity entity : targets) {
            if (!(entity instanceof LivingEntity living)) {
                if (targets.size() == 1) throw NONLIVING.create(entity.getName().getString());
                continue;
            }

            ItemStack stack = living.getMainHandItem();
            if (stack.isEmpty()) {
                if (targets.size() == 1) throw ITEMLESS.create(living.getName().getString());
                continue;
            }

            // 🔥 FIXED FOR 1.21.1: canEnchant structure replaced by modern canEnchant helper
            if (!enchantment.canEnchant(stack) && !allowWrong) {
                if (targets.size() == 1) throw WRONG.create(stack.getHoverName().getString());
                continue;
            }

            // 🔥 FIXED FOR 1.21.1: Replaced raw Map with ItemEnchantments wrapper builder
            ItemEnchantments currentEnchants = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            
            if (!allowIncompatible && !EnchantmentHelper.isEnchantmentCompatible(currentEnchants.keySet(), enchantmentHolder)) {
                if (targets.size() == 1) throw INCOMPATIBLE.create(stack.getHoverName().getString());
                continue;
            }

            // Modify enchantments via modern builder
            ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(currentEnchants);
            if (level == 0) {
                builder.upgrade(enchantmentHolder, 0); // Setting level to 0 strips the component out
            } else {
                builder.set(enchantmentHolder, level);
            }

            // 🔥 FIXED FOR 1.21.1: Apply enchantments safely directly to the stack components
            EnchantmentHelper.setEnchantments(stack, builder.toImmutable());
            success++;
        }

        if (success == 0) throw FAILED.create();

        source.sendSuccess(() -> Component.translatable(
                targets.size() == 1 ? "commands.enchant.success.single" : "commands.enchant.success.multiple",
                enchantment.description(),
                targets.size()
        ), true);

        return success;
    }
}
