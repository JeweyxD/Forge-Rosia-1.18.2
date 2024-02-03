package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.block_entity.ScrapingMachineBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;


public class ScrapingMachineContainer extends BlockEntityContainer<ScrapingMachineBlockEntity>
{
    public static ScrapingMachineContainer create(ScrapingMachineBlockEntity entity, Inventory playerInventory, int windowId)
    {
        return new ScrapingMachineContainer(entity, windowId).init(playerInventory, 0);
    }


    private ScrapingMachineContainer(ScrapingMachineBlockEntity entity, int windowId)
    {
        super(ModContainerTypes.SCRAPING_MACHINE.get(), windowId, entity);
        addDataSlots(entity.getSyncableData());
    }

    public boolean isCrafting() {
        return blockEntity.getSyncableData().get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = this.blockEntity.getSyncableData().get(0);
        int maxProgress = this.blockEntity.getSyncableData().get(1);
        int progressArrowSize = 24; // This is the height or width in pixels of your arrow

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex)
    {
        return switch (typeOf(slotIndex))
                {
                    case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, ScrapingMachineBlockEntity.SLOT_MIN,
                            ScrapingMachineBlockEntity.SLOT_MAX + 1, false);
                    case CONTAINER -> !moveItemStackTo(stack, containerSlots, slots.size(), false);
                };
    }

    @Override
    protected void addContainerSlots()
    {
        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> {
            addSlot(new CallbackSlot(blockEntity, handler, 0, 26, 47));  //Tool
            addSlot(new CallbackSlot(blockEntity, handler, 1, 62, 47));  //Input
            addSlot(new CallbackSlot(blockEntity, handler, 2, 116, 47)); //Output
        });
    }

}