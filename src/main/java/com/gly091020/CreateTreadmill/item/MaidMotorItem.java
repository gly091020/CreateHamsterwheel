package com.gly091020.CreateTreadmill.item;

import com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent;
import com.gly091020.CreateTreadmill.block.MaidMotorBlockEntity;
import com.gly091020.CreateTreadmill.maid.MaidPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class MaidMotorItem extends BlockItem {
    public MaidMotorItem(Properties properties) {
        super(MaidPlugin.MAID_MOTOR_BLOCK.get(), properties);
    }

    @Override
    public @NotNull InteractionResult place(@NotNull BlockPlaceContext context) {
        var out = super.place(context);
        if(out.consumesAction() && !context.getLevel().isClientSide &&
                context.getItemInHand().has(InitDataComponent.MAID_INFO) &&
                context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MaidMotorBlockEntity entity){
            entity.setMaid(MaidMotorBlockEntity.NBTToMaid(Objects.requireNonNull(
                    context.getItemInHand().get(InitDataComponent.MAID_INFO)).copyTag(),
                    context.getLevel()));
        }
        return out;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, components, tooltipFlag);
        if(stack.has(InitDataComponent.MAID_INFO)){
            var maid = MaidMotorBlockEntity.NBTToMaid(stack.getOrDefault(InitDataComponent.MAID_INFO, CustomData.EMPTY).copyTag(),
                    Objects.requireNonNull(Minecraft.getInstance().level));
            if(maid != null)
                components.add(maid.getName());
        }
    }
}
