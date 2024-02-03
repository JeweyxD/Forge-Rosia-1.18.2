package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.custom.boiling_cauldron;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.container.BoilingCauldronContainer;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.FluidSyncS2CPacket;
import com.jewey.rosia.recipe.BoilingCauldronRecipe;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.DelegateItemHandler;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;
import net.dries007.tfc.common.capabilities.SidedHandler;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeatBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.IntArrayBuilder;
import net.dries007.tfc.util.calendar.ICalendarTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import java.util.Optional;

import static com.jewey.rosia.Rosia.MOD_ID;

public class BoilingCauldronBlockEntity extends TickableInventoryBlockEntity<BoilingCauldronBlockEntity.BoilingCauldronInventory> implements ICalendarTickable
{

    public @NotNull Component getDisplayName() {
        return new TextComponent("Boiling Cauldron");
    }
    public static final int SLOT_FLUID_CONTAINER_IN = 0;
    public static final int SLOT_INGREDIENT_IN = 1;
    public static final int SLOTS = 2;

    private static final TranslatableComponent NAME = Helpers.translatable(MOD_ID + ".block_entity.boiling_cauldron");
    private static final int TARGET_TEMPERATURE_STABILITY_TICKS = 5;

    public static void serverTick(Level level, BlockPos pos, BlockState state, BoilingCauldronBlockEntity boiler) {
        boiler.checkForLastTickSync();
        boiler.checkForCalendarUpdate();

        if (boiler.needsRecipeUpdate) {
            boiler.needsRecipeUpdate = false;
        }

        //Temperature control
        if (boiler.temperature != boiler.targetTemperature) {
            boiler.temperature = HeatCapability.adjustTempTowards(boiler.temperature, boiler.targetTemperature);
        }
        if (boiler.targetTemperatureStabilityTicks > 0) {
            boiler.targetTemperatureStabilityTicks--;
        }
        if (boiler.targetTemperature > 0 && boiler.targetTemperatureStabilityTicks == 0) {
            // boiler target temperature decays constantly, since it is set externally. As long as we don't consider ourselves 'stable' (received an external setTemperature() call within the last 5 ticks)
            boiler.targetTemperature = HeatCapability.adjustTempTowards(boiler.targetTemperature, 0);
        }

        //Boiling
        if(hasRecipe(boiler))
        {
            if (boiler.progress > 0)
            {
                boiler.progress--;
            }
            level.setBlock(pos, state.setValue(boiling_cauldron.LIT, true), 3);
            boiler.markForSync();
            if (boiler.progress <= 1)
            {
                finishBoiling(boiler);
                boiler.markForSync();
            }
        }
        else
        {
            boiler.progress = 0;
            level.setBlock(pos, state.setValue(boiling_cauldron.LIT, false), 3);
            boiler.markForSync();
        }

        //Transfer fluid from item in slot 0 to FLUID_TANK
        if(hasFluidItemInSourceSlot(boiler)) {
            transferItemFluidToFluidTank(boiler);
            boiler.markForSync();
        }
    }

    private int progress;

    public int getProgress()
    {
        return progress;
    }

    private static boolean hasRecipe(BoilingCauldronBlockEntity entity)
    {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.inventory.getSlots());
        for (int i = 0; i < entity.inventory.getSlots(); i++)
        {
            inventory.setItem(i, entity.inventory.getStackInSlot(i));
        }

        Optional<BoilingCauldronRecipe> match = level.getRecipeManager()
                .getRecipeFor(BoilingCauldronRecipe.Type.INSTANCE, inventory, level);

        //Check all parts of the recipe are present at their minimum values
        boolean hasCount = false;
        boolean hasTemp = false;
        boolean hasFluid = false;
        boolean hasFluidAmount = false;
        if (match.isPresent())
        {
            //Check recipe for its input item; if no input item, no item allowed in slot
            int count = match.get().getInputCount();
            int slotCount = entity.inventory.getStackInSlot(1).isEmpty() ? 0 : entity.inventory.getStackInSlot(1).getCount();
            hasCount = count != 0 ? slotCount >= count : slotCount == 0;
            //Check recipe for min temperature
            float temp = match.get().getMinTemp();
            hasTemp = entity.temperature >= temp;
            //Check recipe for its input fluid, match with fluid in tank; if no input fluid, no fluid allowed in tank
            FluidStack fluidStack = match.get().getFluidStackInput();
            hasFluid = match.get().getFluidStackInput().getAmount() == 0 || entity.FLUID_TANK.getFluid().containsFluid(fluidStack);
            //Check recipe for its input fluid; if no input fluid, no fluid allowed in tank
            var tank = entity.FLUID_TANK.getFluidInTank(0).getAmount();
            hasFluidAmount = fluidStack.getAmount() != 0 ? tank >= fluidStack.getAmount() : tank == 0;

            //If all true and progress hasn't been set, set progress as recipe duration
            if (hasCount && hasTemp && hasFluid && hasFluidAmount && entity.progress == 0) {
                entity.progress = match.get().getDuration();
            }
        }

        return match.isPresent() && hasCount && hasTemp && hasFluid && hasFluidAmount;
    }

    private static void finishBoiling(BoilingCauldronBlockEntity boiler)
    {
        Level level = boiler.level;
        SimpleContainer inventory = new SimpleContainer(boiler.inventory.getSlots());
        for (int i = 0; i < boiler.inventory.getSlots(); i++)
        {
            inventory.setItem(i, boiler.inventory.getStackInSlot(i));
        }

        Optional<BoilingCauldronRecipe> match = level.getRecipeManager()
                .getRecipeFor(BoilingCauldronRecipe.Type.INSTANCE, inventory, level);

        if (match.isPresent())
        {
            //Prevent stack overflow and limit consumption of inputs to prevent item/fluid consumption on overflow
            int bulkCount = getBulkCount(boiler);
            int newBulkCount;
            double maxItemBulk = 0;
            double maxFluidBulk = 0;
            //Check if item output would cause stack overflow, return the max bulk recipe count that would not overflow
            if ((bulkCount * match.get().getResultItem().getCount()) > match.get().getResultItem().getMaxStackSize())
            {
                int a = (bulkCount * match.get().getResultItem().getCount()) - match.get().getResultItem().getMaxStackSize();
                int b = a / match.get().getInputCount();
                int c = (int) Math.ceil(b);
                maxItemBulk = bulkCount - c;
            }
            //Check if fluid output would cause stack overflow, return the max bulk recipe count that would not overflow
            if ((bulkCount * match.get().getFluidStackOutput().getAmount()) > boiler.FLUID_TANK.getTankCapacity(0))
            {
                int a = (bulkCount * match.get().getFluidStackOutput().getAmount()) - boiler.FLUID_TANK.getTankCapacity(0);
                int b = a / match.get().getFluidStackOutput().getAmount();
                int c = (int) Math.ceil(b);
                maxFluidBulk = bulkCount - c;
            }
            //Check if either item or fluid output would overflow with default bulk count, return new count to prevent overflow
            if (maxItemBulk != 0 && maxFluidBulk != 0)
            {
                newBulkCount = (int) Math.min(maxItemBulk, maxFluidBulk);
            }
            else if (maxItemBulk != 0 || maxFluidBulk != 0)
            {
                newBulkCount = (int) Math.max(maxItemBulk, maxFluidBulk);
            }
            else newBulkCount = bulkCount;


            //Extract only what was used in the recipe
            boiler.inventory.extractItem(1, match.get().getInputCount() * newBulkCount, false);

            //Will overwrite any excess input items if output item exists
            if (match.get().getResultItem().getCount() != 0)
            {
                boiler.inventory.setStackInSlot(1, new ItemStack(match.get().getResultItem().getItem(),
                        Math.min(match.get().getResultItem().getCount() * newBulkCount, match.get().getResultItem().getMaxStackSize())));
            }

            //If recipe has output fluid, empty tank first, then set new fluidStack
            if (match.get().getFluidStackOutput().getAmount() != 0)
            {
                boiler.FLUID_TANK.setFluid(FluidStack.EMPTY);
                FluidStack fluidStack = new FluidStack(match.get().getFluidStackOutput().getFluid(),
                        Math.min(match.get().getFluidStackOutput().getAmount() * newBulkCount, boiler.FLUID_TANK.getCapacity()));
                boiler.FLUID_TANK.setFluid(fluidStack);
            }
            //This is for a recipe with a fluid input, item output, but no fluid output
            else boiler.FLUID_TANK.drain(match.get().getFluidStackInput().getAmount() * newBulkCount, IFluidHandler.FluidAction.EXECUTE);
        }
        //Reset progress
        boiler.progress = 0;
    }

    private static int getBulkCount(BoilingCauldronBlockEntity boiler)
    {
        Level level = boiler.level;
        SimpleContainer inventory = new SimpleContainer(boiler.inventory.getSlots());
        for (int i = 0; i < boiler.inventory.getSlots(); i++)
        {
            inventory.setItem(i, boiler.inventory.getStackInSlot(i));
        }

        Optional<BoilingCauldronRecipe> match = level.getRecipeManager()
                .getRecipeFor(BoilingCauldronRecipe.Type.INSTANCE, inventory, level);

        if (match.isPresent())
        {
            //Determine the raw max number of recipes the current inventory can process

            //Return arbitrary large number to prevent a false 0 output if recipe does not require input fluid
            int a = boiler.FLUID_TANK.getFluidInTank(0).isEmpty() ? 100 : boiler.FLUID_TANK.getFluidInTank(0).getAmount();
            //The recipe may not have an input fluid, prevent divide by 0
            int b = match.get().getFluidStackInput().getAmount() != 0 ? match.get().getFluidStackInput().getAmount() : 1;
            double maxFluidRecipe = Math.floor(a / b);
            //Return arbitrary large number to prevent a false 0 output if recipe does not require input item
            int c = boiler.inventory.getStackInSlot(1).isEmpty() ? 100 : boiler.inventory.getStackInSlot(1).getCount();
            //The recipe may not have an input item, prevent divide by 0
            int d = match.get().getInputCount() != 0 ? match.get().getInputCount() : 1;
            double maxItemRecipe = Math.floor(c / d);

            //If the recipe has neither input item nor input fluid, 100 is returned which would break the output
            //However, if neither inputs exist, the recipe would not be able to be found in the first place so who cares
            //Basically don't be an idiot and make a recipe that converts nothing into something because straight to jail
            int minRecipe = (int) Math.min(maxFluidRecipe, maxItemRecipe);
            //Should only be called when a recipe is valid, therefor always returns minimum of 1 when called
            return Math.max(minRecipe, 1);
        }
        else return 1;
    }


    private static void transferItemFluidToFluidTank(BoilingCauldronBlockEntity boiler) {
        boiler.inventory.getStackInSlot(0).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(handler -> {
            int drainAmount = Math.min(boiler.FLUID_TANK.getSpace(), 1000);

            FluidStack stack = handler.drain(drainAmount, IFluidHandler.FluidAction.SIMULATE);
            //Check if the fluid is valid and is the same as any current fluid in tank
            if(boiler.FLUID_TANK.isFluidValid(stack)
                    && (boiler.FLUID_TANK.getFluid().containsFluid(stack) || boiler.FLUID_TANK.getFluid().containsFluid(FluidStack.EMPTY))) {
                stack = handler.drain(drainAmount, IFluidHandler.FluidAction.EXECUTE);
                fillTankWithFluid(boiler, stack, handler.getContainer());
            }
        });
    }

    private static void fillTankWithFluid(BoilingCauldronBlockEntity boiler, FluidStack stack, ItemStack container) {
        boiler.FLUID_TANK.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        boiler.inventory.extractItem(0, 1, false);
        boiler.inventory.insertItem(0, container, false);
    }

    private static boolean hasFluidItemInSourceSlot(BoilingCauldronBlockEntity boiler) {
        return boiler.inventory.getStackInSlot(0).getCount() > 0;
    }


    private final SidedHandler.Noop<IHeatBlock> sidedHeat;
    private final IntArrayBuilder syncableData;
    private float temperature;
    private float targetTemperature;
    private boolean needsRecipeUpdate;

    /**
     * Prevent the target temperature from "hovering" around a particular value.
     * Effectively means that setTemperature() sets for the next 5 ticks, before it starts to decay naturally.
     */
    private int targetTemperatureStabilityTicks;
    private int lastFillTicks;
    private long lastUpdateTick; // for ICalendarTickable

    public BoilingCauldronBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.BOILING_CAULDRON_BLOCK_ENTITY.get(), pos, state, BoilingCauldronInventory::new, NAME);

        needsRecipeUpdate = true;
        temperature = targetTemperature = 0;
        lastFillTicks = 0;
        lastUpdateTick = Integer.MIN_VALUE;


        // Heat can be accessed from all sides
        sidedHeat = new SidedHandler.Noop<>(inventory);

        syncableData = new IntArrayBuilder()
                .add(() -> (int) temperature, value -> temperature = value);
    }

    public float getTemperature()
    {
        return temperature;
    }

    public ContainerData getSyncableData()
    {
        return syncableData;
    }


    @Override
    public void onCalendarUpdate(long ticks)
    {
        assert level != null;

        targetTemperature = HeatCapability.adjustTempTowards(targetTemperature, 0, ticks);
        temperature = HeatCapability.adjustTempTowards(temperature, targetTemperature, ticks);
    }

    @Override
    public int getSlotStackLimit(int slot)
    {
        return switch (slot) {
            case 0 -> 1;
            default -> super.getSlotStackLimit(slot);
        };
    }

    @Override
    @Deprecated
    public long getLastUpdateTick()
    {
        return lastUpdateTick;
    }

    @Override
    @Deprecated
    public void setLastUpdateTick(long tick)
    {
        lastUpdateTick = tick;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
    {
        ModMessages.sendToClients(new FluidSyncS2CPacket(this.getFluidStack(), worldPosition));
        return BoilingCauldronContainer.create(this, player.getInventory(), containerId);
    }


    @Override
    public void onLoad() {
        super.onLoad();
        lazyFluidHandler = LazyOptional.of(() -> FLUID_TANK);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyFluidHandler.invalidate();
    }

    @Override
    public void loadAdditional(CompoundTag nbt)
    {
        temperature = nbt.getFloat("temperature");
        targetTemperature = nbt.getFloat("targetTemperature");
        targetTemperatureStabilityTicks = nbt.getInt("targetTemperatureStabilityTicks");
        lastUpdateTick = nbt.getLong("lastUpdateTick");
        needsRecipeUpdate = true;
        FLUID_TANK.readFromNBT(nbt);
        progress = nbt.getInt("progress");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        nbt.putFloat("temperature", temperature);
        nbt.putFloat("targetTemperature", targetTemperature);
        nbt.putInt("targetTemperatureStabilityTicks", targetTemperatureStabilityTicks);
        nbt.putLong("lastUpdateTick", lastUpdateTick);
        nbt = FLUID_TANK.writeToNBT(nbt);
        nbt.putInt("progress", progress);
        super.saveAdditional(nbt);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return lazyFluidHandler.cast();
        }
        if (cap == HeatCapability.BLOCK_CAPABILITY)
        {
            return sidedHeat.getSidedHandler(side).cast();
        }
        return super.getCapability(cap, side);
    }


    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {  //Use for hopper interaction
            return false;
        }
    };

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {  //Use for player interaction
        return switch (slot) {
            case 0 -> stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent() || stack.getItem() instanceof BucketItem;
            default -> super.isItemValid(slot, stack);
        };
    }

    public static class BoilingCauldronInventory implements DelegateItemHandler, INBTSerializable<CompoundTag>, IHeatBlock
    {
        private final BoilingCauldronBlockEntity boiler;

        private final InventoryItemHandler inventory;

        BoilingCauldronInventory(InventoryBlockEntity<?> entity)
        {
            boiler = (BoilingCauldronBlockEntity) entity;
            inventory = new InventoryItemHandler(entity, SLOTS);
        }

        @Override
        public IItemHandlerModifiable getItemHandler()
        {
            return inventory;
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag nbt = new CompoundTag();
            nbt.put("inventory", inventory.serializeNBT());
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt)
        {
            inventory.deserializeNBT(nbt.getCompound("inventory"));
        }

        @Override
        public float getTemperature()
        {
            return boiler.temperature;
        }

        @Override
        public void setTemperature(float temperature)
        {
            boiler.targetTemperature = temperature;
            boiler.targetTemperatureStabilityTicks = TARGET_TEMPERATURE_STABILITY_TICKS;
            boiler.markForSync();
        }

        @Override
        public void setTemperatureIfWarmer(float temperature)
        {
            // Override to still cause an update to the stability ticks
            if (temperature >= boiler.temperature)
            {
                boiler.temperature = temperature;
                boiler.targetTemperatureStabilityTicks = TARGET_TEMPERATURE_STABILITY_TICKS;
                boiler.markForSync();
            }
        }

    }

    /**
     * Fluid stuff
     */


    private final FluidTank FLUID_TANK = new FluidTank(5000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if(!level.isClientSide()) {
                ModMessages.sendToClients(new FluidSyncS2CPacket(this.fluid, worldPosition));
            }
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().is(TFCTags.Fluids.USABLE_IN_POT);
        }
    };

    public void setFluid(FluidStack stack){
        this.FLUID_TANK.setFluid(stack);
    }

    public FluidStack getFluidStack() {
        return this.FLUID_TANK.getFluid();
    }

    public IFluidHandler getFluidStorage() {
        return FLUID_TANK;
    }

    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    public float render() {
        return (float) FLUID_TANK.getFluidAmount() / (float) FLUID_TANK.getCapacity();
    }

}


