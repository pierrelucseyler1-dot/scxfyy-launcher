package com.scxfyy.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class BindScreen extends Screen {
    final Screen parent; final Scx.Mod m; boolean waiting; int keyY, labelY = -1;
    public BindScreen(Screen parent, Scx.Mod m) { super(Text.literal(m.name + " settings")); this.parent = parent; this.m = m; }
    @Override public boolean shouldPause() { return false; }
    @Override protected void init() {
        int cx = width / 2;
        int total = (m.bind ? 44 : 0) + 24 * (m.nums.size() + m.flags.size()) + (m.textName != null ? 40 : 0) + (m.id.equals("waypoints") ? 24 : 0) + 34;
        int y = Math.max(40, height / 2 - total / 2); keyY = y;
        if (m.bind) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Change key"), b -> waiting = true).dimensions(cx - 100, y + 14, 98, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Clear key"), b -> { m.key = -1; Scx.save(); }).dimensions(cx + 2, y + 14, 98, 20).build());
            y += 44;
        }
        for (final Scx.Num n : m.nums) {
            addDrawableChild(new SliderWidget(cx - 100, y, 200, 20, Text.empty(), (n.v - n.min) / (n.max - n.min)) {
                { updateMessage(); }
                @Override protected void updateMessage() { setMessage(Text.literal(n.name + ": " + (int) n.v)); }
                @Override protected void applyValue() { n.v = Math.max(n.min, Math.min(n.max, Math.round((n.min + value * (n.max - n.min)) / n.step) * n.step)); Scx.save(); }
            });
            y += 24;
        }
        for (final Scx.Flag f : m.flags) {
            addDrawableChild(ButtonWidget.builder(Text.literal(f.name + ": " + (f.v ? "ON" : "OFF")), b -> { f.v = !f.v; b.setMessage(Text.literal(f.name + ": " + (f.v ? "ON" : "OFF"))); Scx.save(); }).dimensions(cx - 100, y, 200, 20).build());
            y += 24;
        }
        if (m.textName != null) {
            labelY = y; TextFieldWidget tf = new TextFieldWidget(textRenderer, cx - 100, y + 14, 200, 18, Text.literal(m.textName));
            tf.setMaxLength(200); tf.setText(m.text); tf.setChangedListener(s -> m.text = s); addDrawableChild(tf); y += 40;
        }
        if (m.id.equals("waypoints")) { addDrawableChild(ButtonWidget.builder(Text.literal("Clear waypoints"), b -> { m.text = ""; Scx.save(); }).dimensions(cx - 100, y, 200, 20).build()); y += 24; }
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close()).dimensions(cx - 100, y + 6, 200, 20).build());
    }
    @Override public void renderBackground(DrawContext g, int mx, int my, float d) { if (client.world == null) Sunset.draw(g, width, height, 1f); else g.fill(0, 0, width, height, 0x99000000); g.fill(0, 0, width, height, 0x55100510); }
    @Override public void render(DrawContext g, int mx, int my, float d) {
        super.render(g, mx, my, d); int cx = width / 2;
        g.drawCenteredTextWithShadow(textRenderer, m.name, cx, keyY - 12, 0xFFFF9A2E);
        if (m.bind) {
            String mode = m.action ? "(press)" : m.hold ? "(hold)" : "(press to turn on/off)";
            g.drawCenteredTextWithShadow(textRenderer, waiting ? "Press a key... (Esc to cancel)" : "Key: " + Scx.keyName(m.key) + "  " + mode, cx, keyY + 2, 0xFFFFE9D2);
        }
        if (m.textName != null && labelY >= 0) g.drawText(textRenderer, m.textName, cx - 100, labelY + 2, 0xFFFFE9D2, false);
    }
    @Override public boolean keyPressed(int key, int scan, int mods) {
        if (waiting) { if (key != GLFW.GLFW_KEY_ESCAPE) { m.key = key; Scx.save(); } waiting = false; return true; }
        return super.keyPressed(key, scan, mods);
    }
    @Override public void close() { Scx.save(); client.setScreen(parent); }
}
