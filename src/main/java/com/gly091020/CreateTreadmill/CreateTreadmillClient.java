package com.gly091020.CreateTreadmill;

import com.gly091020.CreateTreadmill.config.ClothConfigScreenGetter;
import com.gly091020.CreateTreadmill.maid.MaidPlugin;
import com.gly091020.CreateTreadmill.ponder.TreadmillPonderPlugin;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SpriteShifter;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class CreateTreadmillClient {

    public static final PartialModel BELT_MODEL = PartialModel.of(new ResourceLocation(CreateTreadmillMod.MOD_ID, "block/belt"));
    public static final SpriteShiftEntry BELT_SHIFT = SpriteShifter.get(new ResourceLocation(CreateTreadmillMod.MOD_ID, "block/belt"), new ResourceLocation(CreateTreadmillMod.MOD_ID, "block/belt_shift"));

    public static void onCtorClient(ModLoadingContext context, IEventBus modEventBus) {
//        modEventBus.addListener(CreateClient::clientInit); ……
        modEventBus.addListener(CreateTreadmillClient::clientInit);

        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(
                (mc, parent) -> {
                    if (ModList.get().isLoaded("cloth_config")) {
                        return ClothConfigScreenGetter.get(parent);
                    }
                    return new BaseConfigScreen(parent, CreateTreadmillMod.MOD_ID);
                }
        ));
    }

    public static void clientInit(final FMLClientSetupEvent event) {
//        PonderIndex.addPlugin(new CreatePonderPlugin()); …………
        PonderIndex.addPlugin(new TreadmillPonderPlugin());
        if(ModList.get().isLoaded("touhou_little_maid")){
            event.enqueueWork(() ->
                    ItemBlockRenderTypes.setRenderLayer(MaidPlugin.MAID_MOTOR_BLOCK.get(), RenderType.translucent()));
        }
    }

}
