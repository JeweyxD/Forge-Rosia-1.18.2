package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.custom.FireBoxBlockEntity;
import com.jewey.rosia.screen.ModMenuTypes;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.dries007.tfc.common.container.Container;
import net.dries007.tfc.util.Fuel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import static com.jewey.rosia.common.blocks.entity.custom.FireBoxBlockEntity.SLOT_FUEL_MIN;

public class FireBoxContainer extends BlockEntityContainer<FireBoxBlockEntity>
{
    public static FireBoxContainer create(FireBoxBlockEntity forge, Inventory playerInventory, int windowId)
    {
        return new FireBoxContainer(forge, windowId).init(playerInventory, 20);
    }

    private FireBoxContainer(FireBoxBlockEntity forge, int windowId)
    {
        super(ModContainerTypes.FIRE_BOX.get(), windowId, forge);

        addDataSlots(forge.getSyncableData());
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex)
    {
        return switch (typeOf(slotIndex))
                {
                    case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, SLOT_FUEL_MIN,
                            FireBoxBlockEntity.SLOT_FUEL_MAX + 1, false);
                    case CONTAINER -> !moveItemStackTo(stack, containerSlots, slots.size(), false);
                };
    }

    @Override
    protected void addContainerSlots()
    {
        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> {
            // Fuel slots
            // Note: the order of these statements is important
            int index = SLOT_FUEL_MIN;
            addSlot(new CallbackSlot(blockEntity, handler, index++, 80, 60));
            addSlot(new CallbackSlot(blockEntity, handler, index++, 62, 57));
            addSlot(new CallbackSlot(blockEntity, handler, index, 98, 57));

        });
    }

}