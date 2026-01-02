package com.gly091020.CreateTreadmill.block;

import com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.item.AbstractStoreMaidItem;
import com.gly091020.CreateTreadmill.CreateTreadmillMod;
import com.gly091020.CreateTreadmill.maid.MaidPlugin;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class MaidMotorBlock extends DirectionalKineticBlock implements IBE<MaidMotorBlockEntity> {
    public static final BooleanProperty GLASS = BooleanProperty.create("glass");
    public static final TagKey<Item> WRENCH = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"));
    public MaidMotorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(GLASS, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(GLASS);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return AllShapes.MOTOR_BLOCK.get(state.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        if ((context.getPlayer() != null && context.getPlayer()
                .isShiftKeyDown()) || preferred == null)
            return super.getStateForPlacement(context);
        return defaultBlockState().setValue(FACING, preferred);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING)
                .getAxis();
    }

    @Override
    public boolean hideStressImpact() {
        return true;
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public Class<MaidMotorBlockEntity> getBlockEntityClass() {
        return MaidMotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MaidMotorBlockEntity> getBlockEntityType() {
        return MaidPlugin.MAID_MOTOR_ENTITY.get();
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if(level.isClientSide)return ItemInteractionResult.SUCCESS;
        if(stack.is(WRENCH) && hitResult.getDirection() == state.getValue(DirectionalKineticBlock.FACING).getOpposite()){
            level.setBlock(pos, state.setValue(GLASS, !state.getValue(GLASS)), Block.UPDATE_ALL);
            return ItemInteractionResult.SUCCESS;
        }
        if(stack.is(InitItems.SMART_SLAB_HAS_MAID) || stack.is(InitItems.PHOTO) && stack.has(InitDataComponent.MAID_INFO)){
            var maid = MaidMotorBlockEntity.NBTToMaid(stack.getOrDefault(InitDataComponent.MAID_INFO, CustomData.EMPTY).copyTag(), level);
            if(maid != null && maid.getOwner() != null && maid.getOwner().is(player) && level.getBlockEntity(pos) instanceof MaidMotorBlockEntity blockEntity && blockEntity.getMaid() == null){
                blockEntity.setMaid(maid);
                blockEntity.setChanged();
                if(stack.is(InitItems.SMART_SLAB_HAS_MAID))
                    player.setItemInHand(hand, InitItems.SMART_SLAB_EMPTY.get().getDefaultInstance());
                else
                    player.setItemInHand(hand, ItemStack.EMPTY);
                return ItemInteractionResult.SUCCESS;
            }
        }
        if(stack.is(InitItems.SMART_SLAB_EMPTY)) {
            if (level.getBlockEntity(pos) instanceof MaidMotorBlockEntity blockEntity) {
                var maid = blockEntity.getMaid();
                if(maid != null){
                    var stack1 = InitItems.SMART_SLAB_HAS_MAID.get().getDefaultInstance();
                    if(maid.getOwnerUUID() == null){
                        CreateTreadmillMod.LOGGER.warn("女仆{}出现没有主人的情况", maid.getName().getString());
                        maid.setOwnerUUID(new UUID(0, 0));
                    }
                    AbstractStoreMaidItem.storeMaidData(stack1, maid);
                    blockEntity.setMaid(null);
                    blockEntity.setChanged();
                    player.setItemInHand(hand, stack1);
                }
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder params) {
        try {
            var blockEntity = params.getParameter(LootContextParams.BLOCK_ENTITY);
            var stack = MaidPlugin.MAID_MOTOR_BLOCK.asStack().copyWithCount(1);
            if (blockEntity instanceof MaidMotorBlockEntity entity && entity.getMaid() != null)
                stack.set(InitDataComponent.MAID_INFO, CustomData.of(MaidMotorBlockEntity.maidToNBT(entity.getMaid())));
            return List.of(stack);
        }catch (NoSuchElementException e){
            return List.of(MaidPlugin.MAID_MOTOR_BLOCK.asStack().copyWithCount(1));
        }
    }
}
