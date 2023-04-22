package com.jewey.rosia.common.items;

import com.jewey.rosia.common.capabilities.food.RosiaFoodTraits;
import com.jewey.rosia.common.container.ModContainerProviders;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.capabilities.*;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.HeatHandler;
import net.dries007.tfc.common.capabilities.heat.IHeat;
import net.dries007.tfc.common.capabilities.size.ItemSizeManager;
import net.dries007.tfc.common.container.ItemStackContainerProvider;
import net.dries007.tfc.common.container.TFCContainerProviders;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Alloy;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Metal;
import net.dries007.tfc.util.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class SatchelItem extends Item {
    public static final int SLOTS = 5;

    public SatchelItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack carried, Slot slot, ClickAction action, Player player, SlotAccess carriedSlot)
    {
        final VesselLike vessel = VesselLike.get(stack);
        if (vessel != null && TFCConfig.SERVER.enableSmallVesselInventoryInteraction.get() && vessel.mode() == VesselLike.Mode.INVENTORY && vessel.getTemperature() == 0f && !player.isCreative() && action == ClickAction.SECONDARY)
        {
            if (!carried.isEmpty())
            {
                for (int i = 0; i < SLOTS; i++)
                {
                    final ItemStack current = vessel.getStackInSlot(i);
                    if (current.isEmpty())
                    {
                        carriedSlot.set(vessel.insertItem(i, carried, false));
                        return true;
                    }
                }
            }
            else
            {
                for (int i = SLOTS - 1; i >= 0; i--)
                {
                    final ItemStack current = vessel.getStackInSlot(i);
                    if (!current.isEmpty())
                    {
                        carriedSlot.set(vessel.extractItem(i, 64, false));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        final ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() && !level.isClientSide() && player instanceof ServerPlayer serverPlayer)
        {
            final VesselLike vessel = VesselLike.get(stack);
            if (vessel != null)
            {
                if (vessel.mode() == VesselLike.Mode.INVENTORY)
                {
                    if (vessel.getTemperature() > 0)
                    {
                        player.displayClientMessage(Helpers.translatable("tfc.tooltip.small_vessel.inventory_too_hot"), true);
                    }
                    else
                    {
                        Helpers.openScreen(serverPlayer, ModContainerProviders.LEATHER_SATCHEL.of(stack, hand), ItemStackContainerProvider.write(hand));
                    }
                }
                else if (vessel.mode() == VesselLike.Mode.MOLTEN_ALLOY)
                {
                    Helpers.openScreen(serverPlayer, TFCContainerProviders.MOLD_LIKE_ALLOY.of(stack, hand), ItemStackContainerProvider.write(hand));
                }
                else
                {
                    player.displayClientMessage(Helpers.translatable("tfc.tooltip.small_vessel.alloy_solid"), true);
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
    {
        return new VesselCapability(stack);
    }

    static class VesselCapability implements VesselLike, ICapabilityProvider, INBTSerializable<CompoundTag>, DelegateItemHandler, DelegateHeatHandler, SimpleFluidHandler
    {
        private final ItemStack stack;
        private final LazyOptional<VesselCapability> capability;

        private final ItemStackHandler inventory;
        private final Alloy alloy;
        private final HeatHandler heat; // Since we cannot heat individual items (no tick() method), we only use a heat value for the container
        private final int capacity;

        private final HeatingRecipe[] cachedRecipes; // Recipes for each of the four slots in the inventory

        VesselCapability(ItemStack stack)
        {
            this.stack = stack;
            this.capability = LazyOptional.of(() -> this);

            this.inventory = new InventoryItemHandler(this, SLOTS);
            this.capacity = TFCConfig.SERVER.smallVesselCapacity.get();
            this.alloy = new Alloy(capacity);
            this.heat = new HeatHandler(0, 0, 0);

            this.cachedRecipes = new HeatingRecipe[SLOTS];

            load();
        }

        @Override
        public IHeat getHeatHandler()
        {
            return heat;
        }

        @Override
        public int getSlotStackLimit(int slot)
        {
            return 16;
        }

        @Override
        public void setAndUpdateSlots(int slot)
        {
            final ItemStack stack = inventory.getStackInSlot(slot);
            cachedRecipes[slot] = stack.isEmpty() ? null : HeatingRecipe.getRecipe(stack); // Update cached recipe for slot
            updateAndSave(); // Update heat capacity as average of inventory slots
        }

        @Override
        public void onSlotTake(Player player, int slot, ItemStack stack)
        {
            FoodCapability.removeTrait(stack, RosiaFoodTraits.BOUND);
        }

        @Override
        public void onCarried(ItemStack stack)
        {
            FoodCapability.removeTrait(stack, RosiaFoodTraits.BOUND);
        }

        @Override
        public Mode mode()
        {
            if (alloy.isEmpty())
            {
                return Mode.INVENTORY;
            }
            else
            {
                // Since the temperature here is not cached, we cannot cache the mode, and instead have to calculate it on demand
                // The alloy result here is cached internally, and the temperature should be quick (since it queries the alloy heat handler)
                final Metal result = alloy.getResult();
                return getTemperature() >= result.getMeltTemperature() ? Mode.MOLTEN_ALLOY : Mode.SOLID_ALLOY;
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        {
            return ItemSizeManager.get(stack).getSize(stack).isEqualOrSmallerThan(TFCConfig.SERVER.smallVesselMaximumItemSize.get());
        }

        @Override
        public void setTemperature(float temperature)
        {
            heat.setTemperature(0);
            updateInventoryMelting();
        }

        @Override
        public void addTooltipInfo(ItemStack stack, List<Component> text)
        {
            heat.addTooltipInfo(stack, text);
            if (!Helpers.isEmpty(inventory) || !alloy.isEmpty()) // Only show the 'contents' label if we actually have contents
            {
                text.add(Helpers.translatable("tfc.tooltip.small_vessel.contents").withStyle(ChatFormatting.DARK_GREEN));

                final Mode mode = mode();
                switch (mode)
                {
                    case INVENTORY -> Helpers.addInventoryTooltipInfo(inventory, text);
                    case MOLTEN_ALLOY, SOLID_ALLOY -> {
                        text.add(Tooltips.fluidUnitsAndCapacityOf(alloy.getResult().getDisplayName(), alloy.getAmount(), capacity)
                                .append(Tooltips.moltenOrSolid(isMolten())));
                        if (!Helpers.isEmpty(inventory))
                        {
                            text.add(Helpers.translatable("tfc.tooltip.small_vessel.still_has_unmelted_items").withStyle(ChatFormatting.RED));
                        }
                    }
                }
            }
        }

        @NotNull
        @Override
        public ItemStack getContainer()
        {
            return stack;
        }

        @Override
        public IItemHandlerModifiable getItemHandler()
        {
            return inventory;
        }

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
        {
            if (cap == HeatCapability.CAPABILITY || cap == Capabilities.ITEM || cap == Capabilities.FLUID || cap == Capabilities.FLUID_ITEM)
            {
                return capability.cast();
            }
            return LazyOptional.empty();
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank)
        {
            return alloy.getResultAsFluidStack();
        }

        @Override
        public int getTankCapacity(int tank)
        {
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack)
        {
            return Metal.get(stack.getFluid()) != null && Helpers.isFluid(stack.getFluid(), TFCTags.Fluids.USABLE_IN_VESSEL);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action)
        {
            final Metal metal = Metal.get(resource.getFluid());
            if (metal != null)
            {
                final int result = alloy.add(metal, resource.getAmount(), action.simulate());
                if (action.execute())
                {
                    updateAndSave();
                }
                return result;
            }
            return 0;
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action)
        {
            return mode() == Mode.MOLTEN_ALLOY ? drainIgnoringTemperature(maxDrain, action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drainIgnoringTemperature(int maxDrain, FluidAction action)
        {
            final Mode mode = mode();
            if (mode == Mode.MOLTEN_ALLOY || mode == Mode.SOLID_ALLOY)
            {
                final Metal result = alloy.getResult();
                final int amount = alloy.removeAlloy(maxDrain, action.simulate());
                if (action.execute())
                {
                    updateAndSave();
                }
                return new FluidStack(result.getFluid(), amount);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack)
        {
            FoodCapability.applyTrait(stack, RosiaFoodTraits.BOUND);
            inventory.setStackInSlot(slot, stack);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            FoodCapability.applyTrait(stack, RosiaFoodTraits.BOUND);
            final ItemStack result = inventory.insertItem(slot, stack, simulate);
            if (simulate)
            {
                FoodCapability.removeTrait(result, RosiaFoodTraits.BOUND); // Un-do preservation for simulated actions
            }
            return result;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            final ItemStack result = inventory.extractItem(slot, amount, simulate);
            FoodCapability.removeTrait(result, RosiaFoodTraits.BOUND);
            return result;
        }

        @Override
        public CompoundTag serializeNBT()
        {
            return heat.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt)
        {
            heat.deserializeNBT(nbt);
        }

        /**
         * Called when a change to the inventory, or heat, indicates that the recipes should be re-checked for completion
         * Note: this does not update the recipes themselves.
         */
        private void updateInventoryMelting()
        {
            boolean updatedAlloy = false;
            final ItemStackInventory inventory = new ItemStackInventory();
            for (int i = 0; i < SLOTS; i++)
            {
                final ItemStack stack = this.inventory.getStackInSlot(i);
                inventory.setStack(stack);
                if (cachedRecipes[i] != null)
                {
                    final HeatingRecipe recipe = cachedRecipes[i];
                    if (recipe.isValidTemperature(heat.getTemperature()))
                    {
                        // Melt item, add the contents to the alloy. Excess solids are placed into the inventory, more than can fit is voided.
                        final ItemStack outputStack = recipe.assemble(inventory);
                        final FluidStack outputFluid = recipe.assembleFluid(inventory);

                        if (!outputStack.isEmpty())
                        {
                            // Multiply the contents by the inventory count, since heat recipes only apply to single stack sizes
                            outputStack.setCount(Math.min(
                                    outputStack.getCount() * stack.getCount(),
                                    Math.min(outputStack.getMaxStackSize(), getSlotStackLimit(i))
                            ));
                        }

                        if (!outputFluid.isEmpty())
                        {
                            outputFluid.setAmount(outputFluid.getAmount() * stack.getCount());
                        }

                        // Apply item output
                        this.inventory.setStackInSlot(i, outputStack);

                        // Apply fluid output
                        Metal metal = Metal.get(outputFluid.getFluid());
                        if (metal != null)
                        {
                            alloy.add(metal, outputFluid.getAmount(), false);
                            updatedAlloy = true;
                        }
                    }
                }
            }
            if (updatedAlloy)
            {
                updateAndSave();
            }
        }

        private void load()
        {
            final CompoundTag tag = stack.getOrCreateTag();
            inventory.deserializeNBT(tag.getCompound("inventory"));
            alloy.deserializeNBT(tag.getCompound("alloy"));

            // Additionally, we need to update the contents of our cached recipes. Since we can experience modification (copy) which will invalidate our cache, that would not trigger setAndUpdateSlots
            for (int i = 0; i < inventory.getSlots(); i++)
            {
                final ItemStack stack = inventory.getStackInSlot(i);
                cachedRecipes[i] = stack.isEmpty() ? null : HeatingRecipe.getRecipe(stack);
            }

            updateHeatCapacity();
        }

        private void updateHeatCapacity()
        {
            float value = 0;
            if (mode() == Mode.INVENTORY)
            {
                int count = 0;
                for (ItemStack stack : Helpers.iterate(inventory))
                {
                    final IHeat heat = Helpers.getCapability(stack, HeatCapability.CAPABILITY);
                    if (heat != null)
                    {
                        count = 0;
                        value = 0;
                    }
                }
                if (count > 0)
                {
                    // Vessel has contents
                    // Instead of an ideal mixture, we weight slightly so that heating items in a vessel is more efficient than heating individually.
                    value = HeatCapability.POTTERY_HEAT_CAPACITY + value * 0.7f + (value / count) * 0.3f;
                }
                else
                {
                    // Vessel has no contents, so the value is just the heat capacity of the vessel alone.
                    value = HeatCapability.POTTERY_HEAT_CAPACITY;
                }
            }
            else
            {
                // Bias so that larger quantities of liquid cool faster (relative to a perfect mixture)
                value = HeatCapability.POTTERY_HEAT_CAPACITY + alloy.getHeatCapacity(0.7f);
            }

            heat.setHeatCapacity(0);
        }

        private void updateAndSave()
        {
            updateHeatCapacity();

            final CompoundTag tag = stack.getOrCreateTag();

            tag.put("inventory", inventory.serializeNBT());
            tag.put("alloy", alloy.serializeNBT());
        }
    }
}