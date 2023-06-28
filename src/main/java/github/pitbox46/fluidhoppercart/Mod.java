package github.pitbox46.fluidhoppercart;

import com.mojang.logging.LogUtils;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@net.minecraftforge.fml.common.Mod(Mod.MODID)
public class Mod {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "fluidhoppercart";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceKey<CreativeModeTab> TAB_ITEMS = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(MODID, "tab"));
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<Item> FLUID_HOPPER_MINECART_ITEM = ITEMS.register("fluid_hopper_minecart", () -> new FluidHopperMinecart.MinecartItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<EntityType<FluidHopperMinecart>> FLUID_HOPPER_MINECART = ENTITIES.register("fluid_hopper_minecart", () -> EntityType.Builder.<FluidHopperMinecart>of(FluidHopperMinecart::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8).build("fluid_hopper_minecart.json"));

    public Mod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerEntityRenderers);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCreativeModeTabRegister);
    }

    public void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FLUID_HOPPER_MINECART.get(), ctx -> new MinecartRenderer<>(ctx, ModelLayers.CHEST_MINECART));
    }

    public void onCreativeModeTabRegister(RegisterEvent event) {
        event.register(Registries.CREATIVE_MODE_TAB, (helper) ->
                helper.register(TAB_ITEMS, CreativeModeTab.builder()
                        .icon(() -> new ItemStack(FLUID_HOPPER_MINECART_ITEM.get()))
                        .title(Component.translatable("itemGroup.fluidhoppercart"))
                        .displayItems((enabledFlags, populator) -> FLUID_HOPPER_MINECART_ITEM.ifPresent(populator::accept))
                        .build()));
    }
}
