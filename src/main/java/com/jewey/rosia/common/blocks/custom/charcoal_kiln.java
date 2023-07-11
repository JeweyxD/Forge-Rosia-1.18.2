package com.jewey.rosia.common.blocks.custom;

import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.CharcoalKilnBlockEntity;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.dries007.tfc.common.blocks.devices.IBellowsConsumer;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.MultiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Random;
import java.util.function.BiPredicate;

public class charcoal_kiln extends DeviceBlock
{
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof CharcoalKilnBlockEntity) {
                ((CharcoalKilnBlockEntity) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    public charcoal_kiln(ExtendedProperties properties)
    {
        super(properties, InventoryRemoveBehavior.DROP);
        registerDefaultState(getStateDefinition().any().setValue(LIT, false));
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, Random rand)
    {
        if (!state.getValue(LIT)) return;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.875D;
        double z = pos.getZ() + 0.5D;

        if (rand.nextInt(10) == 0)
        {
            world.playLocalSound(x, y, z, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.6F, false);
        }
        for (int i = 0; i < 1 + rand.nextInt(2); i++)
        {
            world.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, x + Helpers.triangle(rand), y + rand.nextDouble(), z + Helpers.triangle(rand), 0, 0.07D, 0);
        }
        for (int i = 0; i < rand.nextInt(3); i++)
        {
            world.addParticle(ParticleTypes.SMOKE, x + Helpers.triangle(rand), y + rand.nextDouble(), z + Helpers.triangle(rand), 0, 0.005D, 0);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(LIT);
        builder.add(FACING);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
    {
        CharcoalKilnBlockEntity forge = level.getBlockEntity(pos, ModBlockEntities.CHARCOAL_KILN_BLOCK_ENTITY.get()).orElse(null);
        if (!state.getValue(LIT) && isValid(level, pos))
        {
            if (forge != null) {
                if (player instanceof ServerPlayer serverPlayer) {
                    Helpers.openScreen(serverPlayer, forge, pos);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }


    @Override
    @SuppressWarnings("deprecation")
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type)
    {
        return false;
    }

    //MULTIBLOCK

    private static final MultiBlock FORGE_MULTIBLOCK;

    static
    {
        BiPredicate<LevelAccessor, BlockPos> isValidSide = (level, pos) -> isForgeInsulationBlock(level.getBlockState(pos));
        BlockPos origin = BlockPos.ZERO;
        FORGE_MULTIBLOCK = new MultiBlock()
                // Center Air
                .match(origin.relative(Direction.SOUTH), BlockStateBase::isAir)
                // Around Center
                .matchEachDirection(origin.relative(Direction.SOUTH, 1), isValidSide, new Direction[] {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.UP}, 1);
    }

    public static boolean isValid(LevelAccessor world, BlockPos pos)
    {
        return FORGE_MULTIBLOCK.test(world, pos);
    }

    public static boolean isForgeInsulationBlock(BlockState state)
    {
        return Helpers.isBlock(state, Block.byItem(Items.BRICKS));
    }
}