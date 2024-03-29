package com.jewey.rosia.common.items;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodHandler;
import net.dries007.tfc.common.items.DecayingItem;
import net.dries007.tfc.util.Helpers;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

// Just a copy of DynamicBowlFood
// Same functionality of returning a bowl -> return a can

public class DynamicCanFood extends DecayingItem {
    public DynamicCanFood(Properties properties)
    {
        super(properties);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
    {
        return new DynamicCanHandler(stack);
    }

    public static class DynamicCanHandler extends FoodHandler.Dynamic
    {
        private final ItemStack stack;
        private ItemStack can;

        public DynamicCanHandler(ItemStack stack)
        {
            this.stack = stack;
            final CompoundTag tag = stack.getOrCreateTag();
            can = new ItemStack(ModItems.TIN_CAN.get());  // Item to be returned on consume
        }

        public ItemStack getCan()
        {
            return can.copy();
        }

        public void setCan(ItemStack can)
        {
            this.can = Helpers.copyWithSize(can, 1);
            save();
        }

        private void save()
        {
            final CompoundTag tag = stack.getOrCreateTag();
            if (!can.isEmpty())
            {
                tag.put("can", can.save(new CompoundTag()));
            }
        }

        public static ItemStack onItemUse(ItemStack original, ItemStack result, LivingEntity entity)
        {
            // This is a rare stackable-with-remainder-after-finished-using item
            // See: vanilla honey bottles
            if (entity instanceof ServerPlayer player)
            {
                CriteriaTriggers.CONSUME_ITEM.trigger(player, original);
                player.awardStat(Stats.ITEM_USED.get(original.getItem()));
            }

            // Pull the can out first, before we shrink the stack in super.finishUsingItem()
            final ItemStack can = original.getCapability(FoodCapability.CAPABILITY)
                    .map(cap -> cap instanceof DynamicCanFood.DynamicCanHandler handler ? handler.getCan() : ItemStack.EMPTY)
                    .orElse(ItemStack.EMPTY);

            if (result.isEmpty())
            {
                return can;
            }
            else if (entity instanceof Player player && !player.getAbilities().instabuild)
            {
                // In non-creative, we still need to give the player an empty can, but we must also return the result here, as it is non-empty
                // The super() call to finishUsingItem will handle decrementing the stack - only in non-creative - for us already.
                ItemHandlerHelper.giveItemToPlayer(player, can);
            }
            return result;
        }
    }
}
