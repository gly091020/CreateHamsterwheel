package com.gly091020.CreateTreadmill.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.gly091020.CreateTreadmill.CreateTreadmillMod;
import com.gly091020.CreateTreadmill.block.MaidMotorBlock;
import com.gly091020.CreateTreadmill.block.MaidMotorBlockEntity;
import com.gly091020.CreateTreadmill.block.TreadmillBlockEntity;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.registration.MultiSceneBuilder;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

public class MaidPonder {
    public static void registry(MultiSceneBuilder builder, PonderSceneRegistrationHelper<ResourceLocation> helper){
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        builder.addStoryBoard("treadmill/run", MaidPonder::treadmillMaid);
        HELPER.forComponents(MaidPlugin.MAID_MOTOR_BLOCK)
                .addStoryBoard("maid_motor/maid_motor", MaidPonder::maidMotor);
    }

    public static void maidMotor(SceneBuilder builder, SceneBuildingUtil util){
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("maid_motor", "");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos pos1 = util.grid().at(1, 1, 2);
        BlockPos pos2 = util.grid().at(2, 1, 2);
        Selection otherSelection = util.select().fromTo(pos1, pos2);
        Selection maidMotorPos = util.select().position(util.grid().at(3, 1, 2));
        Selection all = otherSelection.add(maidMotorPos);
        BlockPos maidMotorBlockPos = util.grid().at(3, 1, 2);

        scene.idle(5);
        scene.world().showSection(otherSelection, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(maidMotorPos, Direction.DOWN);

        scene.overlay().showText(20)
                .placeNearTarget()
                .text("")
                .pointAt(maidMotorPos.getCenter());
        scene.idle(25);
        var smartSlabMaid = InitItems.SMART_SLAB_HAS_MAID.toStack();
        scene.overlay().showControls(maidMotorPos.getCenter(),
                        Pointing.UP, 20).rightClick()
                .withItem(smartSlabMaid);
        scene.idle(10);
        scene.world().modifyBlockEntity(maidMotorBlockPos,
                MaidMotorBlockEntity.class, entity -> {
                    if (Minecraft.getInstance().level != null) {
                        var maid = new EntityMaid(Minecraft.getInstance().level);
                        if(CreateTreadmillMod.isCreator())
                            maid.setCustomName(Component.literal("=>")
                                    .append(Component.literal(Minecraft.getInstance().getGameProfile().getName())));
                        entity.setMaid(maid);
                    }
                });
        scene.world().setKineticSpeed(all, 16);
        scene.idle(15);
        scene.overlay().showText(40)
                .placeNearTarget()
                .text("")
                .pointAt(maidMotorPos.getCenter().add(0, 0, 1));
        scene.overlay().showText(40)
                .placeNearTarget()
                .text("")
                .pointAt(maidMotorPos.getCenter().add(0, -1, 1));
        scene.addKeyframe();
        scene.idle(45);

        Vec3 blockSurface = util.vector().blockSurface(maidMotorBlockPos, Direction.NORTH)
                .add(1 / 16f, 0, 3 / 16f);
        scene.overlay().showFilterSlotInput(blockSurface, Direction.NORTH, 45);
        scene.overlay().showControls(blockSurface, Pointing.DOWN, 40).rightClick();
        scene.overlay().showText(40)
                .placeNearTarget()
                .text("")
                .pointAt(maidMotorPos.getCenter().add(0, 0, 1));
        scene.idle(5);
        scene.world().setKineticSpeed(all, 32);
        scene.addKeyframe();
        scene.idle(40);

        scene.rotateCameraY(90);
        scene.idle(40);
        var wrench = AllItems.WRENCH.asStack();
        scene.overlay().showControls(maidMotorPos.getCenter().add(0, -0.5, 0),
                        Pointing.UP, 20).rightClick()
                .withItem(wrench);
        scene.idle(5);
        var newState = MaidPlugin.MAID_MOTOR_BLOCK.getDefaultState()
                .setValue(DirectionalKineticBlock.FACING, Direction.WEST)
                .setValue(MaidMotorBlock.GLASS, true);
        scene.world().setBlock(maidMotorBlockPos, newState, false);
        scene.idle(5);
        scene.overlay().showText(40)
                .placeNearTarget()
                .text("")
                .pointAt(maidMotorPos.getCenter());
        scene.addKeyframe();
        scene.idle(45);
        scene.overlay().showText(40)
                .placeNearTarget()
                .text("")
                .pointAt(maidMotorPos.getCenter());
        scene.idle(45);
        scene.overlay().showText(40)
                .placeNearTarget()
                .text("")
                .pointAt(maidMotorPos.getCenter());
        scene.idle(45);

        scene.markAsFinished();
    }

    public static void treadmillMaid(SceneBuilder builder, SceneBuildingUtil util){
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("treadmill_maid", "跑步机的使用");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos pos1 = util.grid().at(2, 1, 2);
        BlockPos pos2 = util.grid().at(2, 2, 3);
        Selection selection = util.select().fromTo(pos1, pos2);

        scene.idle(5);
        scene.world().showSection(selection, Direction.DOWN);
        scene.idle(5);
        scene.world().createEntity(level -> {
            var entity = new EntityMaid(level);
            entity.setPos(1, 1, 1);
            var e = level.getBlockEntity(util.grid().at(2, 1, 3));
            if(e instanceof TreadmillBlockEntity treadmillBlockEntity){
                treadmillBlockEntity.setOnTreadmillEntity(entity);
            }
            if(CreateTreadmillMod.isCreator()){
                entity.setCustomName(Component.literal(String.format("=>%s",
                        Minecraft.getInstance().getGameProfile().getName())));
            }
            entity.walkAnimation.setSpeed(3);
            return entity;
        });
        scene.world().setKineticSpeed(selection, -32);
        scene.idle(5);
        scene.overlay().showText(40)
                .placeNearTarget()
                .text("如果你安装了车万女仆，你可以指定女仆任务为“跑步机”来让女仆使用跑步机")
                .pointAt(util.vector().of(2, 2, 2));
        scene.idle(50);
        scene.overlay().showText(60)
                .placeNearTarget()
                .text("而且随着好感度的增加，女仆产生的应力会更多")
                .pointAt(util.vector().of(2, 2, 3));
        scene.addKeyframe();
        scene.idle(70);
        scene.overlay().showText(40)
                .placeNearTarget()
                .text("当然，之前的操作也是通用的……")
                .pointAt(util.vector().of(2, 2, 2));
        scene.addKeyframe();
        scene.idle(45);
        scene.overlay().showText(40)
                .placeNearTarget()
                .text("而且女仆不会轻易逃脱……")
                .pointAt(util.vector().of(2, 2, 3));
        if(ModList.get().isLoaded("touhou_little_mad")){
            scene.overlay().showText(40)
                    .placeNearTarget()
                    .text("也许吧")
                    .pointAt(util.vector().of(2, 2, 2));
        }
        scene.addKeyframe();
        scene.idle(45);
        scene.markAsFinished();
    }
}
