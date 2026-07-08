package net.silvertide.pa_reverie.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.silvertide.pa_reverie.client.HunterRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftGlowMixin {

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void ror$hunterForceGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (HunterRenderState.isHighlighted(entity)) {
            cir.setReturnValue(true);
        }
    }
}
