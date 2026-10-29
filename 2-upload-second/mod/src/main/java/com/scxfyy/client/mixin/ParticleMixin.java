package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class ParticleMixin {
    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void scx(ParticleEffect e, double x, double y, double z, double vx, double vy, double vz, CallbackInfoReturnable<Particle> cir) {
        var p = MinecraftClient.getInstance().player;
        if (Scx.on("particles") && p != null) { double r = Scx.m("particles").nums.get(0).v; if (p.squaredDistanceTo(x, y, z) > r * r || Math.random() * 100 > Scx.v("particles", 1)) cir.setReturnValue(null); }
    }
}
