package com.jewey.rosia.screen;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.entity.block_entity.SteamGeneratorBlockEntity;
import com.jewey.rosia.common.container.SteamGeneratorContainer;
import com.jewey.rosia.screen.button.steam_generator.SteamGeneratorEnergyOutputToggle;
import com.jewey.rosia.screen.renderer.EnergyInfoArea50Height;
import com.jewey.rosia.screen.renderer.FluidInfoArea50Height;
import com.jewey.rosia.util.MouseUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Tooltips;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.CallbackI;

import java.util.Optional;

public class SteamGeneratorScreen extends BlockEntityScreen<SteamGeneratorBlockEntity, SteamGeneratorContainer>
{
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID,"textures/gui/steam_generator_gui.png");

    private EnergyInfoArea50Height energyInfoArea;
    private FluidInfoArea50Height fluidInfoArea;

    public SteamGeneratorScreen(SteamGeneratorContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, TEXTURE);
        inventoryLabelY += 20;
        imageHeight += 20;
    }


    @Override
    protected void init() {
        super.init();
        assignEnergyInfoArea();
        assignFluidInfoArea();
        addRenderableWidget(new SteamGeneratorEnergyOutputToggle(blockEntity, getGuiLeft(), getGuiTop(),
                RenderHelpers.makeButtonTooltip(this, Component.nullToEmpty("Toggle Power Output"))));
    }

    private void assignEnergyInfoArea() {
        energyInfoArea = new EnergyInfoArea50Height(leftPos  + 156, topPos + 26, menu.getBlockEntity().getEnergyStorage());
    }
    private void assignFluidInfoArea() {
        fluidInfoArea = new FluidInfoArea50Height(leftPos  + 67, topPos + 26, menu.getBlockEntity().getFluidStorage());
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        renderEnergyAreaTooltips(pPoseStack, pMouseX, pMouseY, leftPos, topPos);
        this.font.draw(pPoseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(pPoseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
        //Button Label
        this.font.draw(pPoseStack, "Auto", 146, 92, 1447446);
    }

    private void renderEnergyAreaTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY, int leftPos, int topPos) {
        if(isMouseAboveArea(pMouseX, pMouseY, leftPos, topPos, 156, 26, 8, 50)) {
            renderTooltip(pPoseStack, energyInfoArea.getTooltips(),
                    Optional.empty(), pMouseX - leftPos, pMouseY - topPos);
        }
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
    {
        super.renderBg(poseStack, partialTicks, mouseX, mouseY);
        int temp = (int) (51 * blockEntity.getTemperature() / 2014);    // 2014 -> temp for max FE/tick
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

                RenderHelpers.fillAreaWithSprite(poseStack, sprite, leftPos + 67, topPos + 76 - fillHeight, 16, fillHeight, 16, 16);

                resetToBackgroundSprite();
            }
        });

        energyInfoArea.draw(poseStack);
    }

    private boolean isMouseAboveArea(int pMouseX, int pMouseY, int leftPos, int topPos, int offsetX, int offsetY, int width, int height) {
        return MouseUtil.isMouseOver(pMouseX, pMouseY, leftPos + offsetX, topPos + offsetY, width, height);
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY)
    {
        super.renderTooltip(poseStack, mouseX, mouseY);
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 8, topPos + 76 - 51, 15, 51))
        {
            final var text = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(blockEntity.getTemperature());
            if (text != null)
            {
                renderTooltip(poseStack, text, mouseX, mouseY);
            }
        }

        final int relX = mouseX - getGuiLeft();
        final int relY = mouseY - getGuiTop();

        if (relX >= 67 && relY >= 26 && relX < 83 && relY < 76)
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
