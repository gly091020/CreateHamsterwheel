package com.gly091020.CreateTreadmill.block;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CreateTreadmill.CreateTreadmillMod;
import com.gly091020.CreateTreadmill.maid.MaidHelper;
import com.gly091020.CreateTreadmill.maid.MaidUseHandCrankUtil;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MaidMotorBlockEntity extends CreativeMotorBlockEntity {
    @Nullable
    private EntityMaid maid;

    @Nullable
    private CompoundTag maidTag;

    public MaidMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static CompoundTag maidToNBT(@NotNull EntityMaid maid){
        var data = new CompoundTag();
        maid.saveWithoutId(data);
        return data;
    }

    public static CompoundTag maidToNBTOnlyRender(@NotNull EntityMaid maid){
        var compound = new CompoundTag();
        compound.putString("model_id", maid.getModelId());
        compound.putBoolean("IsYsmModel", maid.isYsmModel());
        compound.putString("YsmModelId", maid.getYsmModelId());
        compound.putString("YsmModelTexture", maid.getYsmModelTexture());
        compound.putString("YsmModelName", Component.Serializer.toJson(maid.getYsmModelName(), maid.registryAccess()));
        compound.putString("YsmRouletteAnim", maid.rouletteAnim);
        compound.putInt("YsmRoamingUpdateFlag", maid.roamingVarsUpdateFlag);
        CompoundTag roamingVarsTag = new CompoundTag();
        Object2FloatOpenHashMap<String> vars = maid.roamingVars;
        Objects.requireNonNull(roamingVarsTag);
        vars.forEach(roamingVarsTag::putFloat);
        compound.put("YsmRoamingVars", roamingVarsTag);
        return compound;
    }

    @Nullable
    public static EntityMaid NBTToMaid(@NotNull CompoundTag tag, @NotNull Level level){
        try{
            var maid = new EntityMaid(level);
            maid.load(tag);
            return maid;
        }catch (Exception e){
            CreateTreadmillMod.LOGGER.error("加载时出现错误", e);
            return null;
        }
    }

    public void setMaid(@Nullable EntityMaid maid){
        this.maid = maid;
        maidTag = null;
        notifyUpdate();
        updateGeneratedRotation();
    }

    public @Nullable EntityMaid getMaid(){
        return maid;
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        if(clientPacket)return;
        if(this.maid != null){
            compound.put("maid", maidToNBT(maid));
        } else if (maidTag != null) {
            compound.put("maid", maidTag);
        }
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if(clientPacket)return;
        if(compound.contains("maid")){
            maidTag = compound.getCompound("maid");
        }
    }

    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider registries) {
        super.onDataPacket(net, pkt, registries);
        var tag = pkt.getTag();
        if(tag.contains("maid")){
            maidTag = tag.getCompound("maid");
        }else setMaid(null);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        var tag = super.getUpdateTag(registries);
        if(this.maid != null){
            maidTag = maidToNBT(maid);
            // todo:此处应该使用maidToNBTOnlyRender而不是maidToNBT
            // 但是这样会导致女仆物品丢失
            // 我是想不到客户端是如何影响服务端的
            // simibubi的神必代码
            tag.put("maid", maidTag);
        }
        return tag;
    }

    @Override
    public void tick() {
        super.tick();
        if(level != null && maidTag != null){
            setMaid(NBTToMaid(maidTag, level));
            if(level.isClientSide && maid != null)
                setMaid(NBTToMaid(maidToNBTOnlyRender(maid), level));
        }
        if (level != null && level.isClientSide && maid != null) {
            maid.tick();
            maid.setOnGround(true);
            maid.walkAnimation.setSpeed(Math.abs(getSpeed()) > 0 ? Math.abs(getSpeed() / 16) : 0);
            maid.calculateEntityAnimation(false);
        }
    }

    @Override
    public float calculateAddedStressCapacity() {
        return Math.abs(calculateMaidStress() / generatedSpeed.getValue());
    }

    @Override
    public float getGeneratedSpeed() {
        if(maid == null)return 0;
        return convertToDirection(generatedSpeed.getValue(), getBlockState().getValue(CreativeMotorBlock.FACING));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateGeneratedRotation();
    }

    public float calculateMaidStress(){
        return calculateTreadmillStress() + calculateMaidUseHandCrankStress();
    }

    public float calculateTreadmillStress(){
        int maid = 1;
        if(CreateTreadmillMod.hasMaid() && MaidHelper.isMaid(this.maid)){
            maid = CreateTreadmillMod.CONFIG.MAID_MAGNIFICATION.get() * MaidHelper.getMaidLevel(this.maid);
            if(maid == 0)maid = 1;
        }
        return CreateTreadmillMod.CONFIG.TREADMILL_STRESS.get() * CreateTreadmillMod.CONFIG.TREADMILL_BASE_SPEED.get() * maid * 2;  // 2倍是被攻击加速
    }

    public float calculateMaidUseHandCrankStress(){
        if(!MaidHelper.isMaid(this.maid))return 0;
        if(CreateTreadmillMod.hasMaidUseHandCrank())
            return MaidUseHandCrankUtil.getMaidStress(maid);
        else
            return 0;
    }
}
