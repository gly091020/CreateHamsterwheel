package com.gly091020.CreateTreadmill.block;

import com.gly091020.CreateTreadmill.CreateTreadmillMod;
import com.gly091020.CreateTreadmill.Part;
import com.gly091020.CreateTreadmill.maid.MaidHelper;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TreadmillBlock extends HorizontalKineticBlock implements IBE<TreadmillBlockEntity> {
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public TreadmillBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return Objects.requireNonNull(super.getStateForPlacement(context))
                .setValue(PART, Part.BOTTOM_BACK).setValue(HORIZONTAL_FACING, context.getHorizontalDirection());
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        if (worldIn.isClientSide) {return;}
        if(state.getValue(PART) == Part.BOTTOM_BACK){
            var p1 = BlockPos.ZERO;
            switch (state.getValue(HORIZONTAL_FACING)){
                case NORTH -> p1 = pos.north(1);
                case SOUTH -> p1 = pos.south(1);
                case WEST -> p1 = pos.west(1);
                case EAST -> p1 = pos.east(1);
            }
            var p2 = pos.above(1);
            var p3 = p1.above(1);
            worldIn.setBlock(p1, state.setValue(PART, Part.BOTTOM_FRONT), Block.UPDATE_ALL);
            worldIn.setBlock(p2, state.setValue(PART, Part.TOP_BACK), Block.UPDATE_ALL);
            worldIn.setBlock(p3, state.setValue(PART, Part.TOP_FRONT), Block.UPDATE_ALL);
            worldIn.blockUpdated(pos, Blocks.AIR);
            worldIn.blockUpdated(p1, Blocks.AIR);
            worldIn.blockUpdated(p2, Blocks.AIR);
            worldIn.blockUpdated(p3, Blocks.AIR);
        }
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder params) {
        return Collections.singletonList(new ItemStack(CreateTreadmillMod.TREADMILL_ITEM.get(), 1));
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis() == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z;
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        withBlockEntityDo(level, pos, TreadmillBlockEntity::lazyTick);
    }

    @Override
    public Class<TreadmillBlockEntity> getBlockEntityClass() {
        return TreadmillBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TreadmillBlockEntity> getBlockEntityType() {
        return CreateTreadmillMod.TREADMILL_ENTITY.get();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        if(state.getValue(PART) != Part.BOTTOM_FRONT){return false;}
        switch (state.getValue(HORIZONTAL_FACING)){
            case EAST, WEST -> {return face == Direction.NORTH || face == Direction.SOUTH;}
            case NORTH, SOUTH -> {return face == Direction.EAST || face == Direction.WEST;}
        }
        return false;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult result) {
        var entity = level.getBlockEntity(findPart(level, state, pos, Part.BOTTOM_FRONT));
        if(entity instanceof TreadmillBlockEntity blockEntity && !hasShaftTowards(level, pos, state, result.getDirection())){
            if(blockEntity.getOnTreadmillEntity() != null){
                blockEntity.setOnTreadmillEntity(null);
                return ItemInteractionResult.SUCCESS;
            }
            for(Entity entity1: level.getEntities(null, new AABB(pos).inflate(10))){
                if(entity1 instanceof LivingEntity livingEntity && entity1 instanceof Leashable leashable && leashable.getLeashHolder() != null && leashable.getLeashHolder().is(player)){
                    blockEntity.setOnTreadmillEntity(livingEntity);
                    leashable.dropLeash(true, true);
                    blockEntity.setEntityTimer(20 * 60);
                    if (entity1 instanceof OwnableEntity ownableEntity && ownableEntity.getOwner() != null){
                        blockEntity.setEntityTimer(20 * 60 * 10);
                    }
                    if(CreateTreadmillMod.hasMaid() && MaidHelper.isMaid(entity1)){
                        blockEntity.setEntityTimer(Integer.MAX_VALUE);
                    }
                    return ItemInteractionResult.SUCCESS;
                }
            }
            blockEntity.setOnTreadmillEntity(player);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        switch (state.getValue(PART)){
            case TOP_BACK, TOP_FRONT -> {return Shapes.create(new AABB(0, 0, 0, 1, 5.5 / 16, 1));}
        }
        return Shapes.block();
    }

    public static BlockPos findPart(Level level, BlockState state, BlockPos pos, Part part){
        var p1 = BlockPos.ZERO;
        var a = part == Part.BOTTOM_BACK || part == Part.TOP_BACK ? 1 : -1;
        switch (state.getValue(HORIZONTAL_FACING)){
            case NORTH -> p1 = pos.south(a);
            case SOUTH -> p1 = pos.north(a);
            case WEST -> p1 = pos.east(a);
            case EAST -> p1 = pos.west(a);
        }
        var p2 = pos.below(1);
        var p3 = p1.below(1);
        if(part == Part.TOP_BACK || part == Part.TOP_FRONT){
            p2 = pos.below(-1);
            p3 = p1.below(-1);
        }
        if(level.getBlockState(p1).is(CreateTreadmillMod.TREADMILL_BLOCK) && level.getBlockState(p1).getValue(PART) == part){
            return p1;
        }else if(level.getBlockState(p2).is(CreateTreadmillMod.TREADMILL_BLOCK) && level.getBlockState(p2).getValue(PART) == part){
            return p2;
        }else if(level.getBlockState(p3).is(CreateTreadmillMod.TREADMILL_BLOCK) && level.getBlockState(p3).getValue(PART) == part){
            return p3;
        }
        return pos;
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if(oldState.getValue(PART) == Part.BOTTOM_FRONT && level.getBlockEntity(pos) instanceof TreadmillBlockEntity entity){
            entity.setOnTreadmillEntity(null);
        }
        super.onRemove(oldState, level, pos, newState, isMoving);
        if(!oldState.hasProperty(PART)){return;}
        if(oldState.getValue(PART) == Part.BOTTOM_BACK){
            var p1 = BlockPos.ZERO;
            switch (oldState.getValue(HORIZONTAL_FACING)){
                case NORTH -> p1 = pos.north(1);
                case SOUTH -> p1 = pos.south(1);
                case WEST -> p1 = pos.west(1);
                case EAST -> p1 = pos.east(1);
            }
            var p2 = pos.above(1);
            var p3 = p1.above(1);
            level.setBlock(p1, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(p2, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(p3, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }else{
            var p = findPart(level, oldState, pos, Part.BOTTOM_BACK);
            if(level.getBlockState(p).is(CreateTreadmillMod.TREADMILL_BLOCK) && level.getBlockState(p).getValue(HORIZONTAL_FACING) != oldState.getValue(HORIZONTAL_FACING))
                return;
            destroy(level, p, level.getBlockState(p));
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
