package com.jewey.rosia.event;

import com.jewey.rosia.common.blocks.ModBlocks;
import com.jewey.rosia.common.blocks.entity.block_entity.CharcoalKilnBlockEntity;
import com.jewey.rosia.common.items.DynamicCanFood;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.util.events.StartFireEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class ForgeEventHandler {
    public static void init()
    {
        final IEventBus bus = MinecraftForge.EVENT_BUS;

        bus.addListener(ForgeEventHandler::onFireStart);
        bus.addListener(ForgeEventHandler::onItemUseFinish);
    }

    public static void onFireStart(StartFireEvent event)
    {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        Block block = state.getBlock();

        if (block == ModBlocks.CHARCOAL_KILN.get() && event.isStrong())
        {
            final BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof CharcoalKilnBlockEntity kiln && kiln.light(state, kiln))
            {
                event.setCanceled(true);
            }
        }
    }

    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event)
    {
        final IFood food = event.getItem().getCapability(FoodCapability.CAPABILITY).resolve().orElse(null);
        if (food instanceof DynamicCanFood.DynamicCanHandler)
        {
            event.setResultStack(DynamicCanFood.DynamicCanHandler.onItemUse(event.getItem(), event.getResultStack(), event.getEntityLiving()));
        }
    }
}
