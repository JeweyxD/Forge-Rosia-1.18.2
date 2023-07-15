package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.util.RosiaTags;
import com.jewey.rosia.common.blocks.custom.fire_box;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.container.FireBoxContainer;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.items.Powder;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Fuel;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.IntArrayBuilder;
import net.dries007.tfc.util.calendar.ICalendarTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.jewey.rosia.Rosia.MOD_ID;

public class FireBoxBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> implements ICalendarTickable, MenuProvider
{
    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return switch (slot) {
            case 0, 1, 2 -> stack.is(RosiaTags.Items.FIRE_BOX_FUEL);
            case 3 -> stack.getItem() == TFCItems.POWDERS.get(Powder.WOOD_ASH).get();
            default -> super.isItemValid(slot, stack);
        };
    }

    public @NotNull Component getDisplayName() {
        return new TextComponent("Fire Box");
    }

    public static final int SLOT_FUEL_MIN = 0;
    public static final int SLOT_FUEL_MAX = 2;
    public static final int OUTPUT_SLOT = 3;

    public static final int MAX_FIRE_BOX_AIR_TICKS = 900;

    private static final TranslatableComponent NAME = Helpers.translatable(MOD_ID + ".block_entity.fire_box");


    public static void serverTick(Level level, BlockPos pos, BlockState state, FireBoxBlockEntity forge)
    {
        forge.checkForLastTickSync();
        forge.checkForCalendarUpdate();

        if (forge.needsRecipeUpdate)
        {
            forge.needsRecipeUpdate = false;
        }

        boolean isRaining = level.isRainingAt(pos);

        if (state.getValue(fire_box.HEAT) > 0)
        {
            if (isRaining && level.random.nextFloat() < 0.15F)
            {
                Helpers.playSound(level, pos, SoundEvents.LAVA_EXTINGUISH);
            }

            // Create wood ash when wooden fuel is done burning
            if(forge.burnTicks <= 3 && forge.ash) {   // burnTicks must be an odd number more than 1 due to bellows/rain ticking down at 2 -> an even number
                forge.inventory.setStackInSlot(OUTPUT_SLOT, new ItemStack(TFCItems.POWDERS.get(Powder.WOOD_ASH).get(),
                        forge.inventory.getStackInSlot(OUTPUT_SLOT).getCount() + 1));
                forge.ash = false;
            }

            // Update fuel
            if (forge.burnTicks < 0) {
                forge.burnTicks = 1;
            }
            if (forge.burnTicks > 0)
            {
                forge.burnTicks -= forge.airTicks > 0 || isRaining ? 2 : 1; // Fuel burns twice as fast using bellows, or in the rain
            }
            if (forge.burnTicks == 0 && forge.burnTemperature == 0 && forge.temperature == 0 && !forge.consumeFuel())
            {
                forge.extinguish();
                level.setBlockAndUpdate(pos, state.setValue(fire_box.HEAT, 0));
                forge.markForSync();
            }
            // No fuel -> extinguish
            if (forge.burnTemperature > 0 && forge.burnTicks <= 0 && !forge.consumeFuel())
            {
                forge.extinguish();
            }
        }
        else if (forge.burnTemperature > 0)
        {
            forge.extinguish();
        }
        if (forge.airTicks > 0)
        {
            forge.airTicks--;
        }

        // Always update temperature until the forge is not hot anymore
        if (forge.temperature > 0 || forge.burnTemperature > 0)
        {
            forge.temperature = HeatCapability.adjustDeviceTemp(forge.temperature, forge.burnTemperature, forge.airTicks, isRaining);

            HeatCapability.provideHeatTo(level, pos.above(), forge.temperature);

            forge.markForSync();
        }

        // Update heat level
        if (state.getValue(fire_box.HEAT) > 0 && forge.temperature > 0)
        {
            int heatLevel = Mth.clamp((int) (forge.temperature / Heat.maxVisibleTemperature() * 6) + 1, 1, 7); // scaled 1 through 7
            if (heatLevel != state.getValue(fire_box.HEAT))
            {
                level.setBlockAndUpdate(pos, state.setValue(fire_box.HEAT, heatLevel));
                forge.markForSync();
            }
        }

        // Light the forge automatically
        final ItemStack fuelStack = forge.inventory.getStackInSlot(SLOT_FUEL_MIN);
        if (state.getValue(fire_box.HEAT) == 0 && !fuelStack.isEmpty() && forge.burnTemperature == 0) {
            level.setBlock(pos, state.setValue(fire_box.HEAT, 1), Block.UPDATE_ALL);
            forge.markForSync();
        }
        // Re-light the forge if empty and new fuel is added
        if (state.getValue(fire_box.HEAT) != 0 && !fuelStack.isEmpty() && forge.burnTemperature == 0 && !forge.consumeFuel()) {
            forge.consumeFuel();
            forge.markForSync();
        }

        // This is here to avoid duplication glitches
        if (forge.needsSlotUpdate)
        {
            forge.cascadeFuelSlots();
        }
    }

    protected final ContainerData syncableData;
    private boolean needsSlotUpdate = false;
    private float temperature; // Current Temperature
    private int burnTicks; // Ticks remaining on the current item of fuel
    private float initBurnTicks; // Initial read of the burn ticks of consumed fuel
    private float burnTemperature; // Temperature provided from the current item of fuel
    private int airTicks; // Ticks of air provided by bellows
    private long lastPlayerTick; // Last player tick this forge was ticked (for purposes of catching up)
    private boolean needsRecipeUpdate; // Set to indicate on tick, the cached recipes need to be re-updated
    private boolean ash; // Whether ash is created or not

    public FireBoxBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.FIRE_BOX_BLOCK_ENTITY.get(), pos, state, defaultInventory(4), NAME);

        temperature = 0;
        burnTemperature = 0;
        burnTicks = 0;
        initBurnTicks = 0;
        airTicks = 0;
        ash = false;
        lastPlayerTick = Integer.MIN_VALUE;
        syncableData = new IntArrayBuilder().add(() -> (int) temperature, value -> temperature = value);

        sidedInventory.on(new PartialItemHandler(inventory)
                .insert(SLOT_FUEL_MIN, 1, SLOT_FUEL_MAX), Direction.Plane.HORIZONTAL);

    }

    public void intakeAir(int amount)
    {
        airTicks += amount;
        if (airTicks > MAX_FIRE_BOX_AIR_TICKS)
        {
            airTicks = MAX_FIRE_BOX_AIR_TICKS;
        }
    }

    @Override
    public void onCalendarUpdate(long ticks)
    {
        assert level != null;
        final BlockState state = level.getBlockState(worldPosition);
        if (state.getValue(fire_box.HEAT) != 0)
        {
            HeatCapability.Remainder remainder =
                    HeatCapability.consumeFuelForTicks(ticks, inventory, burnTicks, burnTemperature, SLOT_FUEL_MIN, SLOT_FUEL_MAX);

            burnTicks = remainder.burnTicks();
            burnTemperature = remainder.burnTemperature();
            needsSlotUpdate = true;

            if (remainder.ticks() > 0)
            {
                // Consumed all fuel, so extinguish and cool instantly
                extinguish();
            }
        }
    }

    @Override
    @Deprecated
    public long getLastUpdateTick()
    {
        return lastPlayerTick;
    }

    @Override
    @Deprecated
    public void setLastUpdateTick(long tick)
    {
        lastPlayerTick = tick;
    }

    public ContainerData getSyncableData()
    {
        return syncableData;
    }

    public float getTemperature()
    {
        return temperature;
    }
    public float getBurnTicks()
    {
        return burnTicks;
    }
    public float getBurnTicksInit()
    {
        return  initBurnTicks;
    }

    public int getAirTicks()
    {
        return airTicks;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory playerInv, Player player)
    {
        return FireBoxContainer.create(this, playerInv, windowID);
    }

    @Override
    public void loadAdditional(CompoundTag nbt)
    {
        temperature = nbt.getFloat("temperature");
        burnTicks = nbt.getInt("burnTicks");
        initBurnTicks = nbt.getInt("initBurnTicks");
        airTicks = nbt.getInt("airTicks");
        burnTemperature = nbt.getFloat("burnTemperature");
        lastPlayerTick = nbt.getLong("lastPlayerTick");
        ash = nbt.getBoolean("ash");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        nbt.putFloat("temperature", temperature);
        nbt.putInt("burnTicks", burnTicks);
        nbt.putFloat("initBurnTicks", initBurnTicks);
        nbt.putInt("airTicks", airTicks);
        nbt.putFloat("burnTemperature", burnTemperature);
        nbt.putLong("lastPlayerTick", lastPlayerTick);
        nbt.putBoolean("ash", ash);
        super.saveAdditional(nbt);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public void setAndUpdateSlots(int slot)
    {
        super.setAndUpdateSlots(slot);
        needsSlotUpdate = true;
    }

    @Override
    public int getSlotStackLimit(int slot) {
        return switch (slot) {
            case 0, 1, 2 -> 4;
            default -> 64;
        };
    }


    private boolean consumeFuel()
    {
        final ItemStack fuelStack = inventory.getStackInSlot(SLOT_FUEL_MIN);
        if (!fuelStack.isEmpty())
        {
            //check for wood -> create wood ash when the fuel has been used
            if(inventory.getStackInSlot(SLOT_FUEL_MIN).is(TFCTags.Items.LOG_PILE_LOGS))
            {
                ash = true;
            }
            // Try and consume a piece of fuel
            inventory.extractItem(SLOT_FUEL_MIN, 1, false);
            needsSlotUpdate = true;
            Fuel fuel = Fuel.get(fuelStack);
            if (fuel != null)
            {
                initBurnTicks = (float) (fuel.getDuration() * 1.1);
                burnTicks += fuel.getDuration() * 1.1;      // 10% more efficient
                burnTemperature = fuel.getTemperature();
            }
            markForSync();
        }
        return burnTicks > 0;
    }

    private void extinguish()
    {
        assert level != null;
        burnTicks = 0;
        burnTemperature = 0;
        markForSync();
    }

    private void cascadeFuelSlots() {
        // move stack from slot 1 to 0 if empty
        if(inventory.getStackInSlot(0) == ItemStack.EMPTY
                && inventory.getStackInSlot(1) != ItemStack.EMPTY ) {
            inventory.setStackInSlot(0, inventory.getStackInSlot(1).copy());
            inventory.setStackInSlot(1, ItemStack.EMPTY);
        }
        // move stack from slot 2 to 1 if empty
        else if(inventory.getStackInSlot(1) == ItemStack.EMPTY
                && inventory.getStackInSlot(2) != ItemStack.EMPTY ) {
            inventory.setStackInSlot(1, inventory.getStackInSlot(2).copy());
            inventory.setStackInSlot(2, ItemStack.EMPTY);
        }
    }
}