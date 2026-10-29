package com.scxfyy.client.mixin;

import com.scxfyy.client.Scx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public class ChatMixin {
    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), argsOnly = true)
    private Text scx(Text t) {
        var p = MinecraftClient.getInstance().player; if (!Scx.on("nickhider") || p == null) return t;
        String me = p.getName().getString(), s = t.getString();
        return s.contains(me) ? Text.literal(s.replace(me, Scx.m("nickhider").text)) : t;
    }
}
