package com.jewey.rosia.screen.button.electric_forge;

import com.jewey.rosia.common.blocks.entity.block_entity.ElectricForgeBlockEntity;
import com.jewey.rosia.screen.ElectricForgeScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;


public class ElectricForgeButtonVeryHot extends Button {
    private final ElectricForgeBlockEntity forge;

    public ElectricForgeButtonVeryHot(ElectricForgeBlockEntity forge, int guiLeft, int guiTop, OnTooltip tooltip)
    {
        super(guiLeft + 48, guiTop + 69, 6, 7, Component.nullToEmpty("Temperature"), button -> {
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(8, null));
        }, tooltip);

        this.forge = forge;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ElectricForgeScreen.TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        blit(poseStack, x, y, 48, 188, width, height, 256, 256);

        if (isHoveredOrFocused())
        {
            renderToolTip(poseStack, mouseX, mouseY);
        }
    }
}
