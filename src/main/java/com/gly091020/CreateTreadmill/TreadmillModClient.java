package com.gly091020.CreateTreadmill;

import com.gly091020.CreateTreadmill.maid.MaidPlugin;
import com.gly091020.CreateTreadmill.ponder.TreadmillPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@SuppressWarnings("all")
public class TreadmillModClient {
    public TreadmillModClient(){
        PonderIndex.addPlugin(new TreadmillPonderPlugin());
    }

    @Mod.EventBusSubscriber(modid = CreateTreadmillMod.MOD_ID, value = Dist.CLIENT)
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
