package com.simplemaker.toolleveler.client.blockentityrenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simplemaker.toolleveler.blockentity.ToolLevelingTableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ToolLevelingTableRenderer implements BlockEntityRenderer<ToolLevelingTableBlockEntity> {

    public ToolLevelingTableRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(ToolLevelingTableBlockEntity blockEntity,
                       float partialTick,
                       PoseStack poseStack,
                       MultiBufferSource buffer,
                       int light,
                       int overlay) {

        ItemStack stack = blockEntity.getStackToEnchant();
        if (stack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.83, 0.5);
        poseStack.scale(0.6F, 0.6F, 0.6F);
        poseStack.mulPose(Axis.XP.rotation(1.5707F));

        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(stack, ItemDisplayContext.FIXED,
                        light, overlay, poseStack, buffer,
                        blockEntity.getLevel(), 0);

        poseStack.popPose();
    }
}