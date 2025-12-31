package com.gly091020.CreateTreadmill.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CreateTreadmill.CreateTreadmillMod;
import com.gly091020.CreateTreadmill.block.TreadmillBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.registration.MultiSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

public class MaidPonder {
    public static void registry(MultiSceneBuilder builder) {
        builder.addStoryBoard("treadmill/run", MaidPonder::treadmillMaid);
    }

    public static void treadmillMaid(SceneBuilder builder, SceneBuildingUtil util) {
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
            if (e instanceof TreadmillBlockEntity treadmillBlockEntity) {
                treadmillBlockEntity.setOnTreadmillEntity(entity);
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
