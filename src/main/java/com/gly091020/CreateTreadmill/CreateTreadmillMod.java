package com.gly091020.CreateTreadmill;

import com.gly091020.CreateTreadmill.block.TreadmillBlock;
import com.gly091020.CreateTreadmill.block.TreadmillBlockEntity;
import com.gly091020.CreateTreadmill.config.TreadmillConfig;
import com.gly091020.CreateTreadmill.item.TreadmillItem;
import com.gly091020.CreateTreadmill.maid.MaidPlugin;
import com.gly091020.CreateTreadmill.renderer.TreadmillRenderer;
import com.gly091020.CreateTreadmill.renderer.TreadmillVisual;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SpriteShifter;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

@Mod(CreateTreadmillMod.MOD_ID)
public class CreateTreadmillMod {
    public static final String MOD_ID = "createtreadmill";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static TreadmillConfig CONFIG = new TreadmillConfig();

    public static final CreateRegistrate REGISTRIES = CreateRegistrate.create(MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final ItemEntry<TreadmillItem> TREADMILL_ITEM = REGISTRIES
            .item("treadmill", TreadmillItem::new)
            .register();
    public static final RegistryObject<CreativeModeTab> CREATIVE_MODE_TAB = CREATIVE_MODE_TAB_REGISTER.register("treadmill",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("tab.createtreadmill.title"))
                    .withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getId())
                    .icon(TREADMILL_ITEM::asStack)
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(TREADMILL_ITEM, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                        if(ModList.get().isLoaded("touhou_little_maid")){
                            output.accept(MaidPlugin.MAID_MOTOR_BLOCK, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                        }
                        if(CreateTreadmillClient.isCreator()){
                            ItemStack playerHand = new ItemStack(Items.PLAYER_HEAD, 1);
//                            playerHand.set(DataComponents.PROFILE, new ResolvableProfile(Minecraft.getInstance().getGameProfile()));
                            output.accept(playerHand, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                        }
                    })
                    .build()
    );
    public static final BlockEntry<TreadmillBlock> TREADMILL_BLOCK = REGISTRIES
            .block("treadmill", TreadmillBlock::new)
            .initialProperties(SharedProperties::stone)
            .onRegister(b -> BlockStressValues.CAPACITIES.register(b, CONFIG.TREADMILL_STRESS::get))
            .transform(axeOrPickaxe())
            .register();
    public static final BlockEntityEntry<TreadmillBlockEntity> TREADMILL_ENTITY = REGISTRIES
            .blockEntity("treadmill_entity", TreadmillBlockEntity::new)
            .visual(() -> TreadmillVisual::new)
            .renderer(() -> TreadmillRenderer::new)
            .validBlock(TREADMILL_BLOCK)
            .register();
    public static final DeferredRegister<PaintingVariant> PAINTING_VARIANTS = DeferredRegister.create(Registries.PAINTING_VARIANT, MOD_ID);
    public static final RegistryObject<PaintingVariant> LITTLE_MAD = PAINTING_VARIANTS.register("little_mad", () -> new PaintingVariant(16, 16));
    public static final PartialModel BELT_MODEL = PartialModel.of(new ResourceLocation(MOD_ID, "block/belt"));
    public static final SpriteShiftEntry BELT_SHIFT = SpriteShifter.get(new ResourceLocation(MOD_ID, "block/belt"), new ResourceLocation(MOD_ID, "block/belt_shift"));

    public CreateTreadmillMod(IEventBus bus, ModLoadingContext context) {
        var specPair = new ForgeConfigSpec.Builder().configure((builder) -> {
            var cfg = new TreadmillConfig();
            cfg.registerAll(builder);
            return cfg;
        });
        CONFIG = specPair.getLeft();
        CONFIG.specification = specPair.getRight();
        context.registerConfig(ModConfig.Type.COMMON, CONFIG.specification);
        REGISTRIES.registerEventListeners(bus);
        CREATIVE_MODE_TAB_REGISTER.register(bus);
        PAINTING_VARIANTS.register(bus);
        if(ModList.get().isLoaded("touhou_little_maid")){
            MaidPlugin.registryData(bus);
        }

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateTreadmillClient.onCtorClient(context, bus));
    }

    public CreateTreadmillMod() {
        this(FMLJavaModLoadingContext.get().getModEventBus(), ModLoadingContext.get());
    }

    public static boolean hasMaid(){
        return ModList.get().isLoaded("touhou_little_maid");
    }

    public static boolean hasMaidUseHandCrank(){
        return ModList.get().isLoaded("muhc");
    }

    @Mod.EventBusSubscriber
    public static class HandleEvent{
        @SubscribeEvent
        public static void onEntityDie(LivingDeathEvent deathEvent){
            var entity = deathEvent.getEntity();
            if(entity.level().getBlockState(entity.blockPosition()).is(TREADMILL_BLOCK.get())){
                var last = entity.getLastAttacker();
                if(last instanceof ServerPlayer player){
                    grantAdvancement(player, new ResourceLocation(MOD_ID, "run_to_die"), "0");
                }
            }
        }

        public static void grantAdvancement(ServerPlayer player, ResourceLocation advancementId, String key) {
            ServerAdvancementManager manager = player.server.getAdvancements();
            Advancement advancement = manager.getAdvancement(advancementId);

            if (advancement != null) {
                player.getAdvancements().award(advancement, key);
            }
        }
    }
}
