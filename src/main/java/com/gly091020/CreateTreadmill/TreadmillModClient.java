package com.gly091020.CreateTreadmill;

import com.gly091020.CreateTreadmill.maid.MaidPlugin;
import com.gly091020.CreateTreadmill.ponder.TreadmillPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@SuppressWarnings("all")
@Mod(value = CreateTreadmillMod.ModID, dist = Dist.CLIENT)
public class TreadmillModClient {
    public TreadmillModClient(){
        PonderIndex.addPlugin(new TreadmillPonderPlugin());
    }

    @EventBusSubscriber(modid = CreateTreadmillMod.ModID)
    public static class EventHandler{
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            if(ModList.get().isLoaded("touhou_little_maid")){
                event.enqueueWork(() -> {
                    ItemBlockRenderTypes.setRenderLayer(MaidPlugin.MAID_MOTOR_BLOCK.get(),
                            RenderType.translucent());
                });
            }
        }
    }
}
