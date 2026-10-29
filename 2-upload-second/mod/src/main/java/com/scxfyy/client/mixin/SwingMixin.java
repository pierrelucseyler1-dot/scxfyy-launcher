package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class SwingMixin {
    @Inject(method = "getHandSwingDuration", at = @At("RETURN"), cancellable = true)
    private void scx(CallbackInfoReturnable<Integer> cir) {
        if (Scx.on("swing") && (Object) this instanceof ClientPlayerEntity) {
            double speed = Scx.v("swing", 0) / 100.0;
            cir.setReturnValue(Math.max(1, (int) Math.round(cir.getReturnValue() / speed)));
        }
    }
}
