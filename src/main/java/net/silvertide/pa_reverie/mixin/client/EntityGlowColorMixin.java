package net.silvertide.pa_reverie.mixin.client;

import net.minecraft.world.entity.Entity;
import net.silvertide.pa_reverie.client.HunterRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityGlowColorMixin {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void ror$hunterHighlightColor(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity) (Object) this;
        if (HunterRenderState.isHighlighted(self)) {
            cir.setReturnValue(HunterRenderState.highlightColor(self));
        }
    }
}
