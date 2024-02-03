package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.block_entity.BoilingCauldronBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class BoilingCauldronContainer extends BlockEntityContainer<BoilingCauldronBlockEntity>
{
    public final BoilingCauldronBlockEntity blockEntity;
    private FluidStack fluidStack;


    public static BoilingCauldronContainer create(BoilingCauldronBlockEntity boiler, Inventory playerInventory, int windowId)
    {
        return new BoilingCauldronContainer(boiler, windowId).init(playerInventory, 20);
    }

    private BoilingCauldronContainer(BoilingCauldronBlockEntity forge, int windowId)
    {
        super(ModContainerTypes.BOILING_CAULDRON.get(), windowId, forge);

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
            addSlot(new CallbackSlot(blockEntity, handler, index, 98, 48)); //Ingredient
        });
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex)
    {
        return switch (typeOf(slotIndex))
                {
                    case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, BoilingCauldronBlockEntity.SLOT_FLUID_CONTAINER_IN,
                            BoilingCauldronBlockEntity.SLOT_INGREDIENT_IN + 1, false);
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
