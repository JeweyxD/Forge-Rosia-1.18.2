package com.jewey.rosia.screen.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;

/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */
public class FluidInfoArea50Height extends InfoArea {
    private final IFluidHandler fluid;

    public FluidInfoArea50Height(int xMin, int yMin)  {
        this(xMin, yMin, null,16,50);
    }

    public FluidInfoArea50Height(int xMin, int yMin, IFluidHandler fluid)  {
        this(xMin, yMin, fluid,16,50);
    }

    public FluidInfoArea50Height(int xMin, int yMin, IFluidHandler fluid, int width, int height)  {
        super(new Rect2i(xMin, yMin, width, height));
        this.fluid = fluid;
    }

    public List<Component> getTooltips() {
        return List.of(Component.nullToEmpty(fluid.getFluidInTank(0).getAmount()+"/"+fluid.getTankCapacity(0)+" mB of"+fluid.getFluidInTank(0).getFluid()));
    }

    @Override
    public void draw(PoseStack transform) {
        final int height = area.getHeight();
        int stored = (int)(height*(fluid.getFluidInTank(0).getAmount()/(float)fluid.getTankCapacity(0)));
        fillGradient(
                transform,
                area.getX(), area.getY()+(height-stored),
                area.getX() + area.getWidth(), area.getY() +area.getHeight(),
                0xffb51500, 0xff600b00
        );
    }
}