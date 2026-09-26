package com.gly091020.CreateTreadmill.item;

import com.gly091020.CreateTreadmill.block.MaidMotorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
        if (level.getBlockEntity(pos) instanceof MaidMotorBlockEntity blockEntity && hasMaidData(stack)) {
            blockEntity.setMaid(MaidMotorBlockEntity.NBTToMaid(Objects.requireNonNull(
                            getMaidData(stack)),
                    level));
        }
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, components, tooltipFlag);
        if(hasMaidData(stack)){
            var maid = MaidMotorBlockEntity.NBTToMaid(getMaidData(stack),
                    Objects.requireNonNull(Minecraft.getInstance().level));
            if(maid != null)
                components.add(Component.translatable("block.createtreadmill.maid_motor.maid_name", maid.getName()));
        }
//        components.add(Component.translatable("block.createtreadmill.maid_motor.tip1"));
//        components.add(Component.translatable("block.createtreadmill.maid_motor.tip2"));
    }

    public static boolean hasMaidData(ItemStack stack) {
        return stack.hasTag() && !Objects.requireNonNull(stack.getTag()).getCompound("MaidInfo").isEmpty();
    }

    public static CompoundTag getMaidData(ItemStack stack) {
        return hasMaidData(stack) ? Objects.requireNonNull(stack.getTag()).getCompound("MaidInfo") : new CompoundTag();
    }

    public static void setMaidData(ItemStack stack, CompoundTag tag){
        stack.getOrCreateTag().put("MaidInfo", tag);
    }
}
