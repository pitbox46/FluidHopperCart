package github.pitbox46.fluidhoppercart;

import com.lothrazar.cyclic.registry.MaterialRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@net.minecraftforge.fml.common.Mod(Mod.MODID)
public class Mod {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "fluidhoppercart";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<Item> FLUID_HOPPER_MINECART_ITEM = ITEMS.register("fluid_hopper_minecart", () -> new FluidHopperMinecart.MinecartItem(new Item.Properties().tab(MaterialRegistry.ITEM_GROUP).stacksTo(1)));
    public static final RegistryObject<EntityType<FluidHopperMinecart>> FLUID_HOPPER_MINECART = ENTITIES.register("fluid_hopper_minecart", () -> EntityType.Builder.<FluidHopperMinecart>of(FluidHopperMinecart::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8).build("fluid_hopper_minecart.json"));

    public Mod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerEntityRenderers);
    }

    public void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FLUID_HOPPER_MINECART.get(), ctx -> new MinecartRenderer<>(ctx, ModelLayers.CHEST_MINECART));
    }
}
