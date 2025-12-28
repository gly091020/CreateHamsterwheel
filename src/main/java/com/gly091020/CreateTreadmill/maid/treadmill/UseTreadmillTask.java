package com.gly091020.CreateTreadmill.maid.treadmill;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CreateTreadmill.CreateTreadmillMod;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UseTreadmillTask implements IMaidTask {
    public static final ResourceLocation UID = new ResourceLocation(CreateTreadmillMod.MOD_ID, "use_treadmill");

    @Override
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return CreateTreadmillMod.TREADMILL_ITEM.asStack();
    }

    @Override
    public @Nullable SoundEvent getAmbientSound(@NotNull EntityMaid entityMaid) {
        return null;
    }

    @Override
    public @NotNull List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(@NotNull EntityMaid entityMaid) {
        return Lists.newArrayList(new Pair<>(0, new UseTreadmillBehavior()));
    }

    @Override
    public boolean enableLookAndRandomWalk(@NotNull EntityMaid maid) {
        return false;
    }

    @Override
    public boolean isHidden(@NotNull EntityMaid maid) {
        return true;
    }
}
