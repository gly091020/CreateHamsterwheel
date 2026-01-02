package com.gly091020.CreateTreadmill.ponder;

import com.gly091020.CreateTreadmill.CreateTreadmillMod;
import com.gly091020.CreateTreadmill.maid.MaidPonder;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

public class TreadmillPonderPlugin implements PonderPlugin {
    @Override
    public @NotNull String getModId() {
        return CreateTreadmillMod.ModID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        var r = HELPER.forComponents(CreateTreadmillMod.TREADMILL_BLOCK)
                .addStoryBoard("treadmill/run", Scenes::treadmillRun, AllCreatePonderTags.KINETIC_SOURCES)
                .addStoryBoard("treadmill/run", Scenes::treadmillFly)
                .addStoryBoard("treadmill/speedup", Scenes::treadmillSpeedUp);
        if(ModList.get().isLoaded("touhou_little_maid")){
            MaidPonder.registry(r, helper);
        }
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        HELPER.addToTag(AllCreatePonderTags.KINETIC_SOURCES).add(CreateTreadmillMod.TREADMILL_BLOCK);
    }
}
