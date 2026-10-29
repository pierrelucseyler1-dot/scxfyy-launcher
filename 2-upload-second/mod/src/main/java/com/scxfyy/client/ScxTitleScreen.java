package com.scxfyy.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ScxTitleScreen extends Screen {
    static final String TITLE = "scxfyy";
    public ScxTitleScreen() { super(Text.literal("scxfyy")); }
    @Override protected void init() {
        int cx = width / 2, y = height / 2 + 10;
        addDrawableChild(ButtonWidget.builder(Text.literal("Singleplayer"), b -> client.setScreen(new SelectWorldScreen(this))).dimensions(cx - 100, y, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Multiplayer"), b -> client.setScreen(new MultiplayerScreen(this))).dimensions(cx - 100, y + 24, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Options"), b -> client.setScreen(new OptionsScreen(this, client.options))).dimensions(cx - 100, y + 48, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Quit"), b -> client.scheduleStop()).dimensions(cx + 2, y + 48, 98, 20).build());
    }
    @Override public void renderBackground(DrawContext g, int mx, int my, float d) { Sunset.draw(g, width, height, 1f); }
    @Override public void render(DrawContext g, int mx, int my, float d) {
        super.render(g, mx, my, d);
        float s = 4f; var ms = g.getMatrices(); ms.push(); ms.scale(s, s, 1f);
        int w = textRenderer.getWidth(TITLE);
        g.drawText(textRenderer, TITLE, (int) ((width / 2f) / s - w / 2f), (int) ((height / 2f - 70) / s), 0xFFFFFFFF, true);
        ms.pop();
        g.drawCenteredTextWithShadow(textRenderer, "Press Right Shift to open the scxfyy menu", width / 2, height - 20, 0xFFFFE9D2);
    }
    @Override public boolean shouldCloseOnEsc() { return false; }
    @Override public boolean keyPressed(int key, int scan, int mods) {
        if (Scx.MENU_KEY.matchesKey(key, scan)) { client.setScreen(new MenuScreen()); return true; }
        return super.keyPressed(key, scan, mods);
    }
}
