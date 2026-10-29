package com.scxfyy.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

/** Sunset photo background used on every menu and loading screen. */
public class Sunset {
    static final Identifier TEX = Identifier.of("scxfyy-client", "textures/gui/sunset.png");
    static final int TW = 1600, TH = 900;
    static int tint(int rgb, float a) { return ((int) (Math.max(0f, Math.min(1f, a)) * 255) << 24) | (rgb & 0xFFFFFF); }
    public static void draw(DrawContext g, int w, int h, float a) {
        if (a < 0.99f) { g.fillGradient(0, 0, w, h, tint(0x20103A, a * 0.8f), tint(0xE8501E, a * 0.9f)); return; }
        float s = Math.max(w / (float) TW, h / (float) TH) * 1.06f;
        float ox = (w - TW * s) / 2f + (float) Math.sin(Util.getMeasuringTimeMs() / 7000.0) * w * 0.012f, oy = (h - TH * s) * 0.72f;
        var ms = g.getMatrices(); ms.push(); ms.translate(ox, oy, 0f); ms.scale(s, s, 1f);
        g.drawTexture(RenderLayer::getGuiTextured, TEX, 0, 0, 0f, 0f, TW, TH, TW, TH);
        ms.pop();
        g.fillGradient(0, 0, w, h, 0x00000000, 0x55100510);
    }
}
