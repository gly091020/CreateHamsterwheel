package com.gly091020.CreateTreadmill.maid;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.ExtraMaidBrainManager;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.gly091020.CreateTreadmill.CreateTreadmillMod;
import com.gly091020.CreateTreadmill.block.MaidMotorBlock;
import com.gly091020.CreateTreadmill.block.MaidMotorBlockEntity;
import com.gly091020.CreateTreadmill.item.MaidMotorItem;
import com.gly091020.CreateTreadmill.maid.treadmill.TreadmillSensor;
import com.gly091020.CreateTreadmill.maid.treadmill.UseTreadmillTask;
import com.gly091020.CreateTreadmill.renderer.MaidMotorRenderer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

@LittleMaidExtension
public class MaidPlugin implements ILittleMaid {
    public static final BlockEntry<MaidMotorBlock> MAID_MOTOR_BLOCK = CreateTreadmillMod.REGISTRIES
            .block("maid_motor", MaidMotorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_BROWN))
            .transform(axeOrPickaxe())
            .item(MaidMotorItem::new)
            .transform(customItemModel())
            .register();
    public static final BlockEntityEntry<MaidMotorBlockEntity> MAID_MOTOR_ENTITY = CreateTreadmillMod.REGISTRIES
            .blockEntity("maid_motor_entity", MaidMotorBlockEntity::new)
            .visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), true)
            .renderer(() -> MaidMotorRenderer::new)
            .validBlock(MAID_MOTOR_BLOCK)
            .register();

    public static RegistryObject<MemoryModuleType<BlockPos>> TREADMILL_MEMORY;
    public static RegistryObject<SensorType<TreadmillSensor>> TREADMILL_SENSOR;
    public static void registryData(IEventBus bus){
        var MEMORY = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, CreateTreadmillMod.MOD_ID);
        var SENSOR = DeferredRegister.create(Registries.SENSOR_TYPE, CreateTreadmillMod.MOD_ID);
        TREADMILL_MEMORY = MEMORY.register("treadmill_memory", () -> new MemoryModuleType<>(Optional.of(BlockPos.CODEC)));
        TREADMILL_SENSOR = SENSOR.register("treadmill_sensor", () -> new SensorType<>(TreadmillSensor::new));
        SENSOR.register(bus);
        MEMORY.register(bus);
    }

    @Override
    public void addExtraMaidBrain(ExtraMaidBrainManager manager) {
        manager.addExtraMaidBrain(new ExtraMaidBrain());
    }

    @Override
    public void addMaidTask(TaskManager manager) {
        manager.add(new UseTreadmillTask());
    }
}
