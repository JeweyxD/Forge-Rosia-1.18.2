package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.block_entity.LavaBasinBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class LavaBasinContainer extends BlockEntityContainer<LavaBasinBlockEntity>
{
    public final LavaBasinBlockEntity blockEntity;
    private FluidStack fluidStack;


    public static LavaBasinContainer create(LavaBasinBlockEntity boiler, Inventory playerInventory, int windowId)
    {
        return new LavaBasinContainer(boiler, windowId).init(playerInventory, 20);
    }

    private LavaBasinContainer(LavaBasinBlockEntity forge, int windowId)
    {
        super(ModContainerTypes.LAVA_BASIN.get(), windowId, forge);

        addDataSlots(forge.getSyncableData());

        blockEntity = forge;
        this.fluidStack = blockEntity.getFluidStack();
    }

    @Override
    protected void addContainerSlots()
    {
        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> {
            int index = 0;
            addSlot(new CallbackSlot(blockEntity, handler, index++, 98, 26)); //Fluid in
            addSlot(new CallbackSlot(blockEntity, handler, index, 98, 60)); //Output
        });
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex)
    {
        return switch (typeOf(slotIndex))
                {
                    case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, LavaBasinBlockEntity.SLOT_FLUID_CONTAINER_IN,
                            LavaBasinBlockEntity.SLOT_OUTPUT + 1, false);
                    case CONTAINER -> !moveItemStackTo(stack, containerSlots, slots.size(), false);
                };
    }

    public void setFluid(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }
    public FluidStack getFluidStack() {
        return fluidStack;
    }
}
