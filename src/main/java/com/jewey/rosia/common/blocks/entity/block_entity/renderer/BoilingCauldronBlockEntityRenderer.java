package com.jewey.rosia.common.blocks.entity.block_entity.renderer;

import com.jewey.rosia.common.blocks.entity.block_entity.BoilingCauldronBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class BoilingCauldronBlockEntityRenderer implements BlockEntityRenderer<BoilingCauldronBlockEntity>
{
    @Override
    public void render(BoilingCauldronBlockEntity boiler, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay)
    {
        //Render fluid in world
        boiler.getCapability(Capabilities.FLUID).ifPresent(fluidHandler -> {
            final float fillPercent = (float) fluidHandler.getFluidInTank(0).getAmount() / (float) fluidHandler.getTankCapacity(0);
            final float subtract = fillPercent > 0.03 ? 0f : (0.03f - fillPercent) * 7f;
            FluidStack fluidStack = fluidHandler.getFluidInTank(0);
            RenderHelpers.renderFluidFace(poseStack, fluidStack, buffer, 0.1875F + subtract, 0.1875F + subtract, 0.8125F - subtract, 0.8125F - subtract, 0.140625F + (0.75F - 0.015625F) * fillPercent, combinedOverlay, combinedLight);
        });

        //Render ingredient in world
        boiler.getCapability(Capabilities.ITEM).map(inv -> inv.getStackInSlot(BoilingCauldronBlockEntity.SLOT_INGREDIENT_IN)).filter(item -> !item.isEmpty()).ifPresent(itemStack -> {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.15625F, 0.5F);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(RenderHelpers.rotateDegreesX(90f));

            Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemTransforms.TransformType.FIXED, combinedLight, combinedOverlay, poseStack, buffer, 0);
            poseStack.popPose();
        });
    }
}
