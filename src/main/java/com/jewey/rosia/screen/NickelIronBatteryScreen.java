package com.jewey.rosia.screen;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.entity.block_entity.NickelIronBatteryBlockEntity;
import com.jewey.rosia.common.container.NickelIronBatteryContainer;
import com.jewey.rosia.screen.button.nickel_iron_battery.BatteryEnergyOutputToggle;
import com.jewey.rosia.screen.renderer.EnergyInfoArea43Height;
import com.jewey.rosia.util.MouseUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.Optional;

public class NickelIronBatteryScreen extends BlockEntityScreen<NickelIronBatteryBlockEntity, NickelIronBatteryContainer>
{
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID, "textures/gui/nickel_iron_battery_gui.png");

    public NickelIronBatteryScreen(NickelIronBatteryContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, TEXTURE);
    }

    private EnergyInfoArea43Height energyInfoArea;

    @Override
    protected void init() {
        super.init();
        assignEnergyInfoArea();
        addRenderableWidget(new BatteryEnergyOutputToggle(blockEntity, getGuiLeft(), getGuiTop(),
                RenderHelpers.makeButtonTooltip(this, Component.nullToEmpty("Toggle Power Output"))));
    }
    private void assignEnergyInfoArea() {
        energyInfoArea = new EnergyInfoArea43Height(leftPos  + 84, topPos + 18, menu.getBlockEntity().getEnergyStorage());
    }
    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        renderEnergyAreaTooltips(pPoseStack, pMouseX, pMouseY, leftPos, topPos);
        this.font.draw(pPoseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(pPoseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
        //Button Label
        this.font.draw(pPoseStack, "Auto", 146, 72, 1447446);
    }
    private void renderEnergyAreaTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY, int leftPos, int topPos) {
        if(isMouseAboveArea(pMouseX, pMouseY, leftPos, topPos, 84, 18, 8, 43)) {
            renderTooltip(pPoseStack, energyInfoArea.getTooltips(),
                    Optional.empty(), pMouseX - leftPos, pMouseY - topPos);
        }
    }
    private boolean isMouseAboveArea(int pMouseX, int pMouseY, int leftPos, int topPos, int offsetX, int offsetY, int width, int height) {
        return MouseUtil.isMouseOver(pMouseX, pMouseY, leftPos + offsetX, topPos + offsetY, width, height);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        super.renderBg(pPoseStack, pPartialTick, pMouseX, pMouseY);

        energyInfoArea.draw(pPoseStack);

    }
    @Override
    public void render(PoseStack pPoseStack, int mouseX, int mouseY, float delta) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, mouseX, mouseY, delta);
        renderTooltip(pPoseStack, mouseX, mouseY);
    }
}
