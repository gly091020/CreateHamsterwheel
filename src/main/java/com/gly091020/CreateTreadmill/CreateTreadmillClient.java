package com.gly091020.CreateTreadmill;

import com.gly091020.CreateTreadmill.block.TreadmillBlockEntity;
import com.gly091020.CreateTreadmill.config.ClothConfigScreenGetter;
import com.gly091020.CreateTreadmill.maid.MaidPlugin;
import com.gly091020.CreateTreadmill.ponder.TreadmillPonderPlugin;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SpriteShifter;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public final class CreateTreadmillClient {

    public static final PartialModel BELT_MODEL = PartialModel.of(new ResourceLocation(CreateTreadmillMod.MOD_ID, "block/belt"));
    public static final SpriteShiftEntry BELT_SHIFT = SpriteShifter.get(new ResourceLocation(CreateTreadmillMod.MOD_ID, "block/belt"), new ResourceLocation(CreateTreadmillMod.MOD_ID, "block/belt_shift"));
    public static final UUID _5112151111121 = UUID.fromString("91bd580f-5f17-4e30-872f-2e480dd9a220");
    public static final UUID N44 = UUID.fromString("5a33e9b0-35bc-44ed-9b4e-03e3e180a3d2");

    public static void onCtorClient(ModLoadingContext context, IEventBus modEventBus) {
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
        PonderIndex.addPlugin(new TreadmillPonderPlugin());
        if(ModList.get().isLoaded("touhou_little_maid")){
            event.enqueueWork(() ->
                    ItemBlockRenderTypes.setRenderLayer(MaidPlugin.MAID_MOTOR_BLOCK.get(), RenderType.translucent()));
        }
    }

    public static final Map<Integer, LivingEntity> WALKING_ENTITY = new HashMap<>();

    public static boolean isCreator(){
        return Objects.equals(Minecraft.getInstance().getUser().getGameProfile().getId(), N44) ||
                Objects.equals(Minecraft.getInstance().getUser().getGameProfile().getId(), _5112151111121);
        //todo: 需要 uTa4u 的 uuid
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class EventHandler{
        @SubscribeEvent
        public static void onRenderEntity(RenderLivingEvent.Pre<?, ?> event){
            if(WALKING_ENTITY.containsKey(event.getEntity().getId()) && !(event.getEntity() instanceof Player)) {
                var speed = 1;
                var entity = TreadmillBlockEntity.getBlockEntityByEntity(event.getEntity());
                if(entity != null && Math.abs(entity.getSpeed()) > entity.getSettingSpeed()){
                    speed = (int) (Math.abs(entity.getSpeed()) / 32);
                }
                event.getEntity().walkAnimation.setSpeed(speed);
            }
        }
    }
}
