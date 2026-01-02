package com.gly091020.CreateTreadmill.item;

import com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent;
import com.gly091020.CreateTreadmill.block.MaidMotorBlockEntity;
import com.gly091020.CreateTreadmill.maid.MaidPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class MaidMotorItem extends BlockItem {
    public MaidMotorItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(@NotNull BlockPos pos, @NotNull Level level, @Nullable Player player, @NotNull ItemStack stack, @NotNull BlockState state) {
        super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (level.getBlockEntity(pos) instanceof MaidMotorBlockEntity blockEntity && stack.has(InitDataComponent.MAID_INFO)) {
            blockEntity.setMaid(MaidMotorBlockEntity.NBTToMaid(Objects.requireNonNull(
                            stack.get(InitDataComponent.MAID_INFO)).copyTag(),
                    level));
        }
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, components, tooltipFlag);
        if(stack.has(InitDataComponent.MAID_INFO)){
            var maid = MaidMotorBlockEntity.NBTToMaid(stack.getOrDefault(InitDataComponent.MAID_INFO, CustomData.EMPTY).copyTag(),
                    Objects.requireNonNull(Minecraft.getInstance().level));
            if(maid != null)
                components.add(Component.translatable("block.createtreadmill.maid_motor.maid_name", maid.getName()));
        }
        components.add(Component.translatable("block.createtreadmill.maid_motor.tip1"));
        components.add(Component.translatable("block.createtreadmill.maid_motor.tip2"));
    }
}
