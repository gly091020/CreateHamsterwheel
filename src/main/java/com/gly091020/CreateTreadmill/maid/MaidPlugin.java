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
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

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

    public static DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BlockPos>> TREADMILL_MEMORY;
    public static DeferredHolder<SensorType<?>, SensorType<TreadmillSensor>> TREADMILL_SENSOR;
    public static void registryData(IEventBus bus){
        var MEMORY = DeferredRegister.create(BuiltInRegistries.MEMORY_MODULE_TYPE, CreateTreadmillMod.ModID);
        var SENSOR = DeferredRegister.create(BuiltInRegistries.SENSOR_TYPE, CreateTreadmillMod.ModID);
        TREADMILL_MEMORY = MEMORY.register("treadmill_memory", resourceLocation -> new MemoryModuleType<>(Optional.of(BlockPos.CODEC)));
        TREADMILL_SENSOR = SENSOR.register("treadmill_sensor", resourceLocation -> new SensorType<>(TreadmillSensor::new));
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
