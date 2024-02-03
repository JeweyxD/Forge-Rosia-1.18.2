package com.jewey.rosia.screen;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.entity.block_entity.LavaBasinBlockEntity;
import com.jewey.rosia.common.container.LavaBasinContainer;
import com.jewey.rosia.screen.renderer.FluidInfoArea50Height;
import com.jewey.rosia.util.MouseUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Tooltips;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class LavaBasinScreen extends BlockEntityScreen<LavaBasinBlockEntity, LavaBasinContainer>
{
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID,"textures/gui/lava_basin_gui.png");

    private FluidInfoArea50Height fluidInfoArea;

    public LavaBasinScreen(LavaBasinContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, TEXTURE);
        inventoryLabelY += 20;
        imageHeight += 20;
    }

    @Override
    protected void init() {
        super.init();
        assignFluidInfoArea();
    }

    private void assignFluidInfoArea() {
        fluidInfoArea = new FluidInfoArea50Height(leftPos  + 68, topPos + 26, menu.getBlockEntity().getFluidStorage());
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        this.font.draw(pPoseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(pPoseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
    {
        super.renderBg(poseStack, partialTicks, mouseX, mouseY);
        int temp = (int) (51 * blockEntity.getTemperature() / Heat.maxVisibleTemperature());
        if (temp > 0)
        {
            blit(poseStack, leftPos + 8, topPos + 76 - Math.min(51, temp), 176, 0, 15, 5);
        }

        blockEntity.getCapability(Capabilities.FLUID).ifPresent(fluidHandler -> {
            FluidStack fluidStack = fluidHandler.getFluidInTank(0);
            if (!fluidStack.isEmpty())
            {
                final TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(fluidStack);
                final int fillHeight = (int) Math.ceil((float) 50 * fluidStack.getAmount() / (float) fluidHandler.getTankCapacity(0));

                RenderHelpers.fillAreaWithSprite(poseStack, sprite, leftPos + 68, topPos + 76 - fillHeight, 16, fillHeight, 16, 16);

                resetToBackgroundSprite();
            }
        });
    }

    private boolean isMouseAboveArea(int pMouseX, int pMouseY, int leftPos, int topPos, int offsetX, int offsetY, int width, int height) {
        return MouseUtil.isMouseOver(pMouseX, pMouseY, leftPos + offsetX, topPos + offsetY, width, height);
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY)
    {
        super.renderTooltip(poseStack, mouseX, mouseY);
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 8, topPos + 25, 15, 51))
        {
            final var text = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(blockEntity.getTemperature());
            if (text != null)
            {
                renderTooltip(poseStack, text, mouseX, mouseY);
            }
        }

        final int relX = mouseX - getGuiLeft();
        final int relY = mouseY - getGuiTop();

        if (relX >= 68 && relY >= 26 && relX < 84 && relY < 76)
        {
            blockEntity.getCapability(Capabilities.FLUID).ifPresent(fluidHandler -> {
                FluidStack fluid = fluidHandler.getFluidInTank(0);
                if (!fluid.isEmpty())
                {
                    renderTooltip(poseStack, Tooltips.fluidUnitsOf(fluid), mouseX, mouseY);
                }
            });
        }
    }
}
