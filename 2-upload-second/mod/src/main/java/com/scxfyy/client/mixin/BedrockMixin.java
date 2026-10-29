package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.block.BlockRenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockRenderManager.class)
public class BedrockMixin {
    @ModifyVariable(method = "renderBlock", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private BlockState scx(BlockState s) { return Scx.on("nobedrock") && s.isOf(Blocks.BEDROCK) ? Blocks.DEEPSLATE.getDefaultState() : s; }
}
