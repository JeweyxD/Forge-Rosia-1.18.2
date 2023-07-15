package com.jewey.rosia.common.blocks.custom;

import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.CanningPressBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class canning_press extends BaseEntityBlock {
    public canning_press(Properties pProperties) {
        super(pProperties);
        registerDefaultState(getStateDefinition().any().setValue(ON, false));
    }

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ON = BooleanProperty.create("on");

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(ON);
    }

    public static VoxelShape makeShapeNorth() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.25, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.25, 0.625, 1, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.6875, 0.25, 0.6875, 1, 0.625), BooleanOp.OR);
        return shape;
    }
    public static VoxelShape makeShapeSouth() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.25, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.25, 0, 1, 1, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.6875, 0.375, 0.6875, 1, 0.75), BooleanOp.OR);
        return shape;
    }
    public static VoxelShape makeShapeWest() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.25, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.25, 0, 1, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.6875, 0.3125, 0.625, 1, 0.6875), BooleanOp.OR);
        return shape;
    }
    public static VoxelShape makeShapeEast() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.25, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.25, 0, 0.375, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.6875, 0.3125, 0.75, 1, 0.6875), BooleanOp.OR);
        return shape;
    }
    private static final VoxelShape SHAPE_N = makeShapeNorth();
    private static final VoxelShape SHAPE_S = makeShapeSouth();
    private static final VoxelShape SHAPE_W = makeShapeWest();
    private static final VoxelShape SHAPE_E = makeShapeEast();
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            case SOUTH -> SHAPE_S;
            case WEST -> SHAPE_W;
            case EAST -> SHAPE_E;
            default -> SHAPE_N;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof CanningPressBlockEntity) {
                ((CanningPressBlockEntity) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos,
                                 Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if(entity instanceof CanningPressBlockEntity) {
                NetworkHooks.openGui(((ServerPlayer)pPlayer), (CanningPressBlockEntity)entity, pPos);
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CanningPressBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ModBlockEntities.CANNING_PRESS_BLOCK_ENTITY.get(),
                CanningPressBlockEntity::tick);
    }

}
