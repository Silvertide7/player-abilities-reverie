package net.silvertide.pa_reverie.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.silvertide.pa_reverie.PAReverie;
import net.silvertide.pa_reverie.registry.ReverieEntities;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = PAReverie.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
            return new ResourceLocation(PAReverie.MOD_ID, "textures/entity/empty.png");
        }
    }
}
