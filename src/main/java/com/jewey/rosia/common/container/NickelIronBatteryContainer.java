package com.jewey.rosia.common.container;

import com.jewey.rosia.common.blocks.entity.block_entity.NickelIronBatteryBlockEntity;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

public class NickelIronBatteryContainer extends BlockEntityContainer<NickelIronBatteryBlockEntity> implements ButtonHandlerContainer {

    public static NickelIronBatteryContainer create(NickelIronBatteryBlockEntity forge, Inventory playerInventory, int windowId)
    {
        return new NickelIronBatteryContainer(forge, windowId).init(playerInventory, 0);
    }

    private NickelIronBatteryContainer(NickelIronBatteryBlockEntity forge, int windowId)
    {
        super(ModContainerTypes.NICKEL_IRON_BATTERY.get(), windowId, forge);

        addDataSlots(forge.getSyncableData());
    }

    @Override
    public void onButtonPress(int buttonID, @Nullable CompoundTag compoundTag) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (buttonID == 0) { blockEntity.togglePush(); }
        }
    }
}
