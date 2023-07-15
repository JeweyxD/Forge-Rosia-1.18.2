package com.jewey.rosia.screen;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.screen.renderer.EnergyInfoArea43Height;
import com.jewey.rosia.util.MouseUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.Optional;

public class CanningPressScreen extends AbstractContainerScreen<CanningPressMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID, "textures/gui/canning_press.png");

    public CanningPressScreen(CanningPressMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    private EnergyInfoArea43Height energyInfoArea;

    @Override
    protected void init() {
        super.init();
        assignEnergyInfoArea();
    }
    private void assignEnergyInfoArea() {
        energyInfoArea = new EnergyInfoArea43Height(leftPos  + 156, topPos + 20, menu.getBlockEntity().getEnergyStorage());
    }
    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        renderEnergyAreaTooltips(pPoseStack, pMouseX, pMouseY, leftPos, topPos);
        this.font.draw(pPoseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(pPoseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
    }
    private void renderEnergyAreaTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY, int leftPos, int topPos) {
        if(isMouseAboveArea(pMouseX, pMouseY, leftPos, topPos, 156, 20, 8, 43)) {
            renderTooltip(pPoseStack, energyInfoArea.getTooltips(),
                    Optional.empty(), pMouseX - leftPos, pMouseY - topPos);
        }
    }
    private boolean isMouseAboveArea(int pMouseX, int pMouseY, int leftPos, int topPos, int offsetX, int offsetY, int width, int height) {
        return MouseUtil.isMouseOver(pMouseX, pMouseY, leftPos + offsetX, topPos + offsetY, width, height);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        this.blit(pPoseStack, leftPos, topPos, 0, -2, imageWidth, imageHeight);

        if(menu.isCrafting()) {
            blit(pPoseStack, leftPos + 76, topPos + 50, 176, 0, menu.getScaledProgress(), 17);
        }
        energyInfoArea.draw(pPoseStack);

    }
    @Override
    public void render(PoseStack pPoseStack, int mouseX, int mouseY, float delta) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, mouseX, mouseY, delta);
        renderTooltip(pPoseStack, mouseX, mouseY);
    }
}
