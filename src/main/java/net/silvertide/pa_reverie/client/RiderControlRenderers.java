package net.silvertide.pa_reverie.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.registry.ReverieEntities;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = PAReverie.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
@SuppressWarnings("removal")
public final class RiderControlRenderers {

    private RiderControlRenderers() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ReverieEntities.ESCAPE_SHAFT_RISE.get(), InvisibleEntityRenderer::new);
    }

    private static final class InvisibleEntityRenderer<T extends Entity> extends EntityRenderer<T> {
        InvisibleEntityRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
            return ResourceLocation.fromNamespaceAndPath(PAReverie.MOD_ID, "textures/entity/empty.png");
        }
    }
}
