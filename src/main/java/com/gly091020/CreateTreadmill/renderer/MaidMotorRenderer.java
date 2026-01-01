package com.gly091020.CreateTreadmill.renderer;

import com.gly091020.CreateTreadmill.block.MaidMotorBlock;
import com.gly091020.CreateTreadmill.block.MaidMotorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public class MaidMotorRenderer extends KineticBlockEntityRenderer<MaidMotorBlockEntity> {
    private final EntityRenderDispatcher ENTITY_RENDER_DISPATCHER;

	public MaidMotorRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
        ENTITY_RENDER_DISPATCHER = context.getEntityRenderer();
	}

	@Override
	protected SuperByteBuffer getRotatedModel(MaidMotorBlockEntity be, BlockState state) {
		return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state);
	}

    @Override
    protected void renderSafe(MaidMotorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        if(!be.getBlockState().getValue(MaidMotorBlock.GLASS))
            return;
        ms.pushPose();
        ms.scale(0.2f, 0.2f, 0.2f);
        switch (be.getBlockState().getValue(MaidMotorBlock.FACING)){
            case NORTH -> {
                ms.translate(2.5, 1.5, 4);
                ms.mulPose(Axis.YP.rotationDegrees(90));
                if(be.getSpeed() < 0)
                    ms.mulPose(Axis.YP.rotationDegrees(180));
            }
            case EAST -> {
                ms.translate(1, 1.5, 2.5);
                if(be.getSpeed() > 0)
                    ms.mulPose(Axis.YP.rotationDegrees(180));
            }
            case WEST -> {
                ms.translate(4, 1.5, 2.5);
                if(be.getSpeed() > 0)
                    ms.mulPose(Axis.YP.rotationDegrees(180));
            }
            case SOUTH -> {
                ms.translate(2.5, 1.5, 1);
                ms.mulPose(Axis.YP.rotationDegrees(90));
                if(be.getSpeed() < 0)
                    ms.mulPose(Axis.YP.rotationDegrees(180));
            }
            case UP -> {
                ms.scale(0.8f, 0.8f, 0.8f);
                ms.translate(3.2, 0.01, 3.2);
                var time = System.currentTimeMillis() / 5000d * Math.abs(be.getSpeed());
                double dx, dz;
                float v;
                if(be.getSpeed() < 0) {
                    dx = Math.cos(time) * 0.6;
                    dz = Math.sin(time) * 0.6;
                    v = (float) Math.toDegrees(Math.atan2(dx, dz)) - 90.0f;
                }else{
                    dx = Math.sin(time) * 0.6;
                    dz = Math.cos(time) * 0.6;
                    v = (float) Math.toDegrees(Math.atan2(dx, dz)) + 90.0f;
                }
                ms.translate(dx, 0, dz);
                ms.mulPose(Axis.YP.rotationDegrees(v));
            }
            case DOWN -> {
                ms.scale(0.8f, 0.8f, 0.8f);
                ms.translate(3.2, 3.85, 3.2);
                var time = System.currentTimeMillis() / 5000d * Math.abs(be.getSpeed());
                double dx, dz;
                float v;
                if(be.getSpeed() < 0) {
                    dx = Math.cos(time) * 0.6;
                    dz = Math.sin(time) * 0.6;
                    v = (float) Math.toDegrees(Math.atan2(dx, dz)) - 90.0f;
                }else{
                    dx = Math.sin(time) * 0.6;
                    dz = Math.cos(time) * 0.6;
                    v = (float) Math.toDegrees(Math.atan2(dx, dz)) + 90.0f;
                }
                ms.translate(dx, 0, dz);
                ms.mulPose(Axis.YP.rotationDegrees(v));
            }
        }
        var maid = be.getMaid();
        if(maid != null) {
            EntityRenderer<? super Entity> renderer = ENTITY_RENDER_DISPATCHER.getRenderer(maid);
            renderer.render(maid, 0, partialTicks, ms, buffer, light);
        }
        ms.popPose();
    }
}
