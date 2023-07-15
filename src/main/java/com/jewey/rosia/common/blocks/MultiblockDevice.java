package com.jewey.rosia.common.blocks;

import com.jewey.rosia.common.blocks.entity.MultiblockBlockEntity;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public abstract class MultiblockDevice extends DeviceBlock {

    public static final BooleanProperty DUMMY = BooleanProperty.create("dummy");

    public MultiblockDevice(ExtendedProperties properties, InventoryRemoveBehavior removeBehavior) {
        super(properties, removeBehavior);
    }

    public Supplier<BlockItem> blockItemSupplier(CreativeModeTab group) {
        return () -> new BlockItem(this, new Item.Properties().tab(group));
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MultiblockBlockEntity be) {
            if (player.isShiftKeyDown())
                return InteractionResult.PASS;
            be = be.master();
            if (be != null) {
                if (player instanceof ServerPlayer serverPlayer) {
                    Helpers.openScreen(serverPlayer, be, be.getBlockPos());
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    public abstract Direction getDummyOffsetDir();

    public BlockPos getDummyOffsetPos(BlockPos pos) {
        Direction dummyDir = getDummyOffsetDir();
        return pos.relative(dummyDir);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(DUMMY);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            level.setBlockAndUpdate(getDummyOffsetPos(pos), state.setValue(DUMMY, true));
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (state.getValue(DUMMY)) {
            level.destroyBlock(pos.relative(getDummyOffsetDir().getOpposite()), !player.isCreative());
        } else {
            level.destroyBlock(getDummyOffsetPos(pos), false);
        }

        super.playerWillDestroy(level, pos, state, player);
    }
}
