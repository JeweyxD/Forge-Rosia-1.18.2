package com.jewey.rosia.common.blocks.entity.block_entity;

import com.jewey.rosia.common.blocks.custom.canning_press;
import com.jewey.rosia.common.blocks.custom.electric_lantern;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.WrappedHandler;
import com.jewey.rosia.common.blocks.entity.WrappedHandlerEnergy;
import com.jewey.rosia.common.capabilities.food.RosiaFoodTraits;
import com.jewey.rosia.common.items.ModItems;
import com.jewey.rosia.networking.ModMessages;
import com.jewey.rosia.networking.packet.EnergySyncS2CPacket;
import com.jewey.rosia.networking.packet.ItemStackSyncS2CPacket;
import com.jewey.rosia.screen.CanningPressMenu;
import com.jewey.rosia.util.ModEnergyStorage;
import com.jewey.rosia.util.RosiaTags;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Random;


public class CanningPressBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                ModMessages.sendToClients(new ItemStackSyncS2CPacket(this, worldPosition));
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.is(RosiaTags.Items.DYNAMIC_CAN);
                case 1 -> false;
                default -> super.isItemValid(slot, stack);
            };
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private final Map<Direction, LazyOptional<WrappedHandler>> directionWrappedHandlerMap =
            Map.of(Direction.DOWN, LazyOptional.of(() -> new WrappedHandler(itemHandler, (i) -> i == 1,
                            (index, stack) -> false)),

                    Direction.UP, LazyOptional.of(() -> new WrappedHandler(itemHandler, (i) -> i == 0,
                            (index, stack) -> itemHandler.isItemValid(0, stack))),

                    Direction.NORTH, LazyOptional.of(() -> new WrappedHandler(itemHandler, (i) -> i == 1,
                            (index, stack) -> itemHandler.isItemValid(0, stack))),

                    Direction.SOUTH, LazyOptional.of(() -> new WrappedHandler(itemHandler, (i) -> i == 1,
                            (index, stack) -> itemHandler.isItemValid(0, stack))),

                    Direction.EAST, LazyOptional.of(() -> new WrappedHandler(itemHandler, (i) -> i == 1,
                            (index, stack) -> itemHandler.isItemValid(0, stack))),

                    Direction.WEST, LazyOptional.of(() -> new WrappedHandler(itemHandler, (i) -> i == 1,
                            (index, stack) -> itemHandler.isItemValid(0, stack))));

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress= 50;


    public CanningPressBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.CANNING_PRESS_BLOCK_ENTITY.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> CanningPressBlockEntity.this.progress;
                    case 1 -> CanningPressBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> CanningPressBlockEntity.this.progress = value;
                    case 1 -> CanningPressBlockEntity.this.maxProgress = value;
                }
            }

            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    public @NotNull Component getDisplayName() {
        return new TextComponent("Canning Press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        ModMessages.sendToClients(new EnergySyncS2CPacket(this.ENERGY_STORAGE.getEnergyStored(), getBlockPos()));
        return new CanningPressMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityEnergy.ENERGY) {
            if(side == null) {
                return lazyEnergyHandler.cast();
            }

            if(directionWrappedHandlerMapEnergy.containsKey(side)) {
                Direction localDir = this.getBlockState().getValue(electric_lantern.FACING);

                if(side == Direction.UP || side == Direction.DOWN) {
                    return directionWrappedHandlerMapEnergy.get(side).cast();
                }

                return switch (localDir) {
                    default -> directionWrappedHandlerMapEnergy.get(side.getOpposite()).cast();
                    case EAST -> directionWrappedHandlerMapEnergy.get(side.getClockWise()).cast();
                    case SOUTH -> directionWrappedHandlerMapEnergy.get(side).cast();
                    case WEST -> directionWrappedHandlerMapEnergy.get(side.getCounterClockWise()).cast();
                };
            }
        }

        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if(side == null) {
                return lazyItemHandler.cast();
            }

            if(directionWrappedHandlerMap.containsKey(side)) {
                Direction localDir = this.getBlockState().getValue(canning_press.FACING);

                if(side == Direction.UP || side == Direction.DOWN) {
                    return directionWrappedHandlerMap.get(side).cast();
                }

                return switch (localDir) {
                    default -> directionWrappedHandlerMap.get(side.getOpposite()).cast();
                    case EAST -> directionWrappedHandlerMap.get(side.getClockWise()).cast();
                    case SOUTH -> directionWrappedHandlerMap.get(side).cast();
                    case WEST -> directionWrappedHandlerMap.get(side.getCounterClockWise()).cast();
                };
            }
        }

        return super.getCapability(cap, side);
    }

    private final ModEnergyStorage ENERGY_STORAGE = new ModEnergyStorage(100, 10) {
        @Override
        public void onEnergyChanged() {
            setChanged();
            ModMessages.sendToClients(new EnergySyncS2CPacket(this.energy, getBlockPos()));
        }
    };
    private static final int ENERGY_REQ = 10; // Energy cost to craft item

    private final Map<Direction, LazyOptional<WrappedHandlerEnergy>> directionWrappedHandlerMapEnergy =
            //Handler for sided energy: extract, receive, canExtract, canReceive
            //Determines what sides can perform actions
            Map.of(Direction.DOWN, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> true, (i) -> true, true, true)),
                    Direction.UP, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE,(i) -> false, (i) -> false, false, false)),
                    Direction.NORTH, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> true, (i) -> true, true, true)),
                    Direction.SOUTH, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> false, (i) -> false, false, false)),
                    Direction.EAST, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> false, (i) -> false, false, false)),
                    Direction.WEST, LazyOptional.of(() -> new WrappedHandlerEnergy(ENERGY_STORAGE, (i) -> false, (i) -> false, false, false)));

    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();


    public IEnergyStorage getEnergyStorage() {
        return ENERGY_STORAGE;
    }

    public void setEnergyLevel(int energy) {
        this.ENERGY_STORAGE.setEnergy(energy);
    }



    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
    }

    @Override
    public void invalidateCaps()  {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyEnergyHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("canning_press.progress", progress);
        tag.putInt("energy", ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("canning_press.progress");
        ENERGY_STORAGE.setEnergy(nbt.getInt("energy"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, CanningPressBlockEntity pBlockEntity) {
        if(!pBlockEntity.itemHandler.getStackInSlot(0).isEmpty() && hasEnoughEnergy(pBlockEntity) && canOutput(pBlockEntity)) {
            pBlockEntity.progress++;
            pLevel.setBlock(pPos, pState.setValue(canning_press.ON, true), 3);
            setChanged(pLevel, pPos, pState);
            if(pBlockEntity.progress > pBlockEntity.maxProgress) {
                craftItem(pBlockEntity);
                extractEnergy(pBlockEntity);
            }

            if(pBlockEntity.progress == pBlockEntity.maxProgress) {
                pLevel.playSound(null, pPos, SoundEvents.NETHERITE_BLOCK_PLACE,
                        SoundSource.BLOCKS, 1F, 1);
            }
        } else {
            pBlockEntity.resetProgress();
            setChanged(pLevel, pPos, pState);
            pLevel.setBlock(pPos, pState.setValue(canning_press.ON, false), 3);
        }
    }

    private static void extractEnergy(CanningPressBlockEntity pBlockEntity) {
        pBlockEntity.ENERGY_STORAGE.extractEnergy(ENERGY_REQ, false);
    }

    private static boolean hasEnoughEnergy(CanningPressBlockEntity pBlockEntity) {
        return pBlockEntity.ENERGY_STORAGE.getEnergyStored() >= ENERGY_REQ;
    }

    private static void craftItem(CanningPressBlockEntity entity) {
        ItemStack inputStack = entity.itemHandler.getStackInSlot(0).copy();  // Keep as an itemStack to avoid losing nutrient/nbt data
        inputStack.setCount(entity.itemHandler.getStackInSlot(1).getCount() + 1);
        entity.itemHandler.setStackInSlot(1, inputStack);

        ItemStack outputStack = entity.itemHandler.getStackInSlot(1);
        FoodCapability.applyTrait(outputStack, RosiaFoodTraits.CANNED);

        entity.itemHandler.extractItem(0,1, false);

        entity.resetProgress();
    }

    private void resetProgress() {
        this.progress = 0;
    }

    private static boolean canInsertItemIntoOutputSlot(SimpleContainer inventory) {
        return inventory.getItem(1).getItem() == inventory.getItem(0).getItem() || inventory.getItem(1).isEmpty();
    }

    private static boolean canInsertAmountIntoOutputSlot(SimpleContainer inventory) {
        return inventory.getItem(1).getMaxStackSize() > inventory.getItem(1).getCount();
    }

    private static boolean canOutput(CanningPressBlockEntity entity) {
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        return canInsertAmountIntoOutputSlot(inventory) && canInsertItemIntoOutputSlot(inventory);
    }

    public ItemStack getRenderStack() {
        ItemStack stack;
        if(!itemHandler.getStackInSlot(0).isEmpty()) {
            stack = itemHandler.getStackInSlot(0);
        } else {
            stack = itemHandler.getStackInSlot(1);
        }

        return stack;
    }

    public void setHandler(ItemStackHandler itemStackHandler) {
        for (int i = 0; i < itemStackHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, itemStackHandler.getStackInSlot(i));
        }
    }
}
