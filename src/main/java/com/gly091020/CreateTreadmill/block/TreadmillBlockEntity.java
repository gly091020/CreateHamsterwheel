package com.gly091020.CreateTreadmill.block;

import com.gly091020.CreateTreadmill.CreateTreadmillMod;
import com.gly091020.CreateTreadmill.Part;
import com.gly091020.CreateTreadmill.maid.MaidHelper;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static com.gly091020.CreateTreadmill.block.TreadmillBlock.PART;
import static com.gly091020.CreateTreadmill.block.TreadmillBlock.findPart;
import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class TreadmillBlockEntity extends GeneratingKineticBlockEntity {
    private LivingEntity onTreadmillEntity;
    private boolean isRunning = false;
    private boolean isRuned = false;
    private int speedUpTimer = 0;
    private int entityTimer = Integer.MAX_VALUE;
    private UUID entityUUID;
    public TreadmillBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setChanged();
    }

    @Override
    public void tick() {
        if(this.getBlockState().getValue(PART) != Part.BOTTOM_FRONT){return;}
        if(entityUUID != null && level instanceof ServerLevel serverLevel){
            if(serverLevel.getEntity(entityUUID) instanceof LivingEntity entity){
                setOnTreadmillEntity(entity);
            }
            entityUUID = null;
        }
        if(onTreadmillEntity != null){
            if(onTreadmillEntity.isRemoved()){
                setOnTreadmillEntity(null);
                return;
            }
            if(!onTreadmillEntity.position().closerThan(getFixedPos(), 1)){
                setOnTreadmillEntity(null);
                return;
            }
            setPos();
            speedUp();
            if (onTreadmillEntity instanceof Player player) {
                if (player.isShiftKeyDown() || player.getPose() == Pose.SITTING) {
                    setOnTreadmillEntity(null);
                }
            }else{
                onTreadmillEntity.setDeltaMovement(Vec3.atLowerCornerOf(getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING).getNormal()).multiply(0.3f, 0, 0.3f));
                onTreadmillEntity.lookAt(EntityAnchorArgument.Anchor.EYES, onTreadmillEntity.getEyePosition().relative(getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING), 1));
                onTreadmillEntity.setPose(Pose.STANDING);
                if(onTreadmillEntity instanceof TamableAnimal tamableAnimal){
                    tamableAnimal.setInSittingPose(false);
                }
                lazyTick();
            }
            dropIt();
        }
        super.tick();
        if(speedUpTimer == 0){
            update();
            speedUpTimer = -1;
        }
        if(speedUpTimer > 0){
            speedUpTimer--;
        }
        if(entityTimer <= 0){
            setOnTreadmillEntity(null);
        }else if(entityTimer < Integer.MAX_VALUE){
            entityTimer--;
            if(speedUpTimer > 0){
                entityTimer--;
            }
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        var update = super.getUpdateTag(registries);
        update.putInt("speedup_timer", speedUpTimer);
        update.putInt("entity_timer", entityTimer);
        update.putInt("entity", onTreadmillEntity == null ? -1 : onTreadmillEntity.getId());
        return update;
    }

    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt,
                             HolderLookup.@NotNull Provider registries) {
        super.onDataPacket(net, pkt, registries);
        speedUpTimer = pkt.getTag().getInt("speedup_timer");
        entityTimer = pkt.getTag().getInt("entity_timer");
        var id = pkt.getTag().getInt("entity");
        if(id != -1){
            if (level != null) {
                var entity = level.getEntity(id);
                if(entity == null){return;}
                setOnTreadmillEntity((LivingEntity) entity);
            }
        }else {
            setOnTreadmillEntity(null);
        }
    }

    public void setEntityTimer(int entityTimer) {
        if(!CreateTreadmillMod.CONFIG.TREADMILL_BREAK.get()){
            this.entityTimer = Integer.MAX_VALUE;
            return;
        }
        this.entityTimer = entityTimer;
    }

    public void setOnTreadmillEntity(@Nullable LivingEntity onTreadmillEntity) {
        if(onTreadmillEntity == null && this.onTreadmillEntity != null){
            if(!canDropIt())
                this.onTreadmillEntity.setDeltaMovement(Vec3.ZERO);
            CreateTreadmillMod.WALKING_ENTITY.remove(this.onTreadmillEntity.getId());
            this.onTreadmillEntity.walkAnimation.setSpeed(0);
        }
        if (onTreadmillEntity != null) {
            CreateTreadmillMod.WALKING_ENTITY.put(onTreadmillEntity.getId(), onTreadmillEntity);
        }else{
            speedUpTimer = 0;
            entityTimer = Integer.MAX_VALUE;
        }
        this.onTreadmillEntity = onTreadmillEntity;
        setChanged();
        setPos();
        update();
    }

    @Override
    public void remove() {
        setOnTreadmillEntity(null);
    }

    public Entity getOnTreadmillEntity() {
        return onTreadmillEntity;
    }

    public void setPos(){
        if(onTreadmillEntity != null){
            onTreadmillEntity.setPos(getFixedPos());
            onTreadmillEntity.setOnGround(true);
        }
    }

    public void speedUp(){
        if(!CreateTreadmillMod.CONFIG.TREADMILL_SPEED_UP.get()){return;}
        if(onTreadmillEntity.hurtTime > 0 && !(onTreadmillEntity.getLastHurtMob() instanceof Player)){
            var damageSource = onTreadmillEntity.getLastDamageSource();
            if(damageSource != null && damageSource.getWeaponItem() != null){
                speedUpTimer = 1200;
                update();
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if(getBlockState().getValue(PART) != Part.BOTTOM_FRONT){
            var p = findPart(level, getBlockState(), getBlockPos(), Part.BOTTOM_FRONT);
            if (level != null && level.getBlockState(p).getValue(PART) == Part.BOTTOM_FRONT &&
                    level.getBlockEntity(p) instanceof TreadmillBlockEntity treadmillBlockEntity) {
                treadmillBlockEntity.addToGoggleTooltip(tooltip, isPlayerSneaking);
                return true;
            }
        }
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        addToolTip(tooltip);
        return true;
    }

    public void addToolTip(List<Component> tooltip){
        if(getBlockState().getValue(PART) != Part.BOTTOM_FRONT){
            var p = findPart(level, getBlockState(), getBlockPos(), Part.BOTTOM_FRONT);
            if (level != null && level.getBlockState(p).getValue(PART) == Part.BOTTOM_FRONT &&
                    level.getBlockEntity(p) instanceof TreadmillBlockEntity treadmillBlockEntity) {
                treadmillBlockEntity.addToolTip(tooltip);
                return;
            }
        }
        if(speedUpTimer > 0){
            tooltip.add(Component.translatable("tip.createtreadmill.speedup", speedUpTimer / 20));
        }
        if(entityTimer > 0 && entityTimer < Integer.MAX_VALUE){
            tooltip.add(Component.translatable("tip.createtreadmill.break", entityTimer / 20));
        }
    }

    public int getSpeedUpTimer() {
        return speedUpTimer;
    }

    public Vec3 getFixedPos(){
        var p = this.getBlockPos().above();
        var y = p.getY() + 5.5 / 16;
        switch (getBlockState().getValue(HORIZONTAL_FACING)){
            case WEST -> {
                return new Vec3(p.getX() + 1, y, p.getZ() + 0.5);
            }
            case EAST -> {
                return new Vec3(p.getX(), y, p.getZ() + 0.5);
            }
            case NORTH -> {
                return new Vec3(p.getX() + 0.5, y, p.getZ() + 1);
            }
            case SOUTH -> {
                return new Vec3(p.getX() + 0.5, y, p.getZ());
            }
        }
        return Vec3.atCenterOf(p);
    }

    public static TreadmillBlockEntity getBlockEntityByEntity(Entity entity){
        var level = entity.level();
        if(level.getBlockState(entity.blockPosition()).is(CreateTreadmillMod.TREADMILL_BLOCK)){
            var part = TreadmillBlock.findPart(level, level.getBlockState(entity.blockPosition()),
                    entity.blockPosition(), Part.BOTTOM_FRONT);
            var e = level.getBlockEntity(part);
            if(e instanceof TreadmillBlockEntity treadmillBlockEntity){
                return treadmillBlockEntity;
            }
        }
        return null;
    }

    @Override
    public void initialize() {
        super.initialize();
        if(this.getBlockState().getValue(PART) != Part.BOTTOM_FRONT){return;}
        updateGeneratedRotation();
        setLazyTickRate(10);
        setChanged();
    }

    @Override
    public void lazyTick() {
        isRunning = isMoving();
        if(isRunning != isRuned){
            isRuned = isRunning;
            update();
        }
        if(isRunning && onTreadmillEntity instanceof Player player){
            player.causeFoodExhaustion(getSettingSpeed() * 0.01f);
        }
        sendData();
    }

    private void update(){
        updateGeneratedRotation();
        notifyUpdate();
        sendData();
    }

    private void dropIt(){
        if(!CreateTreadmillMod.CONFIG.TREADMILL_DROP_IT.get()){return;}
        if(onTreadmillEntity == null)return;
        switch (getBlockState().getValue(TreadmillBlock.HORIZONTAL_FACING)) {
            case NORTH, EAST -> {
                if(getSpeed() < 0){
                    float m = 3f * (Math.abs(getSpeed()) / 256);
                    onTreadmillEntity.setDeltaMovement(Vec3.atLowerCornerOf(getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING).getNormal()).multiply(m, 0, m));
                    setOnTreadmillEntity(null);
                }
            }
            case SOUTH, WEST -> {
                if(getSpeed() > 0){
                    float m = 3f * (Math.abs(getSpeed()) / 256);
                    onTreadmillEntity.setDeltaMovement(Vec3.atLowerCornerOf(getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING).getNormal()).multiply(m, 0, m));
                    setOnTreadmillEntity(null);
                }
            }
        }
    }

    private boolean canDropIt(){
        if(!CreateTreadmillMod.CONFIG.TREADMILL_DROP_IT.get()){return false;}
        if(onTreadmillEntity == null)return false;
        switch (getBlockState().getValue(TreadmillBlock.HORIZONTAL_FACING)) {
            case NORTH, EAST -> {
                if(getSpeed() < 0){
                    return true;
                }
            }
            case SOUTH, WEST -> {
                if(getSpeed() > 0){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public float getGeneratedSpeed() {
        if(canDropIt())return 0;
        int speedUp = this.speedUpTimer > 0 ? 2 : 1;
        if (isRunning) {
            switch (getBlockState().getValue(TreadmillBlock.HORIZONTAL_FACING)) {
                case NORTH, EAST -> {
                    return getSettingSpeed() * speedUp;
                }
                case SOUTH, WEST -> {
                    return -getSettingSpeed() * speedUp;
                }
            }
        }
        return 0;
    }

    public float getSettingSpeed(){
        return CreateTreadmillMod.CONFIG.TREADMILL_BASE_SPEED.get();
    }

    @Override
    public float calculateAddedStressCapacity() {
        int maid = 1;
        if(CreateTreadmillMod.hasMaid() && MaidHelper.isMaid(onTreadmillEntity)){
            maid = CreateTreadmillMod.CONFIG.MAID_MAGNIFICATION.get() * MaidHelper.getMaidLevel(onTreadmillEntity);
            if(maid == 0)maid = 1;
        }
        return super.calculateAddedStressCapacity() * maid;
    }

    public boolean isMoving(){
        var min = 0.1f;
        if(onTreadmillEntity == null){
            return false;
        }
        switch (getBlockState().getValue(TreadmillBlock.HORIZONTAL_FACING)){
            case EAST -> {
                return onTreadmillEntity.getKnownMovement().x > min;
            }
            case WEST -> {
                return onTreadmillEntity.getKnownMovement().x < -min;
            }
            case SOUTH -> {
                return onTreadmillEntity.getKnownMovement().z > min;
            }
            case NORTH -> {
                return onTreadmillEntity.getKnownMovement().z < -min;
            }
        }
        return false;
    }

    @Override
    protected Block getStressConfigKey() {
        return super.getStressConfigKey();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if(this.getBlockState().getValue(PART) != Part.BOTTOM_FRONT){return;}
        if(clientPacket)return;
        if(onTreadmillEntity != null)
            tag.putUUID("onTreadmillEntity", onTreadmillEntity.getUUID());
        tag.putInt("entityTimer", entityTimer);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if(this.getBlockState().getValue(PART) != Part.BOTTOM_FRONT){return;}
        if(clientPacket)return;
        if(compound.contains("onTreadmillEntity")){
            entityUUID = compound.getUUID("onTreadmillEntity");
        }
        entityTimer = compound.contains("entityTimer") ? compound.getInt("entityTimer") : 0;
    }
}
