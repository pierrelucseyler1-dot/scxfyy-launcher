package com.scxfyy.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class MenuScreen extends Screen {
    static final int CW = 200, CH = 44, GAP = 6, SKY_Y = 56;
    TextFieldWidget search; final List<Scx.Mod> shown = new ArrayList<>(); double scroll;
    public MenuScreen() { super(Text.literal("scxfyy menu")); }
    int top() { return 84; } int bottom() { return height - 22; }
    int x0() { return width / 2 - CW - GAP / 2; }
    int skyX(int i) { return x0() + 84 + i * 30; }
    int rows() { return (shown.size() + 1) / 2; }
    double maxScroll() { return Math.max(0, rows() * (CH + GAP) - (bottom() - top())); }
    @Override public boolean shouldPause() { return false; }
    @Override protected void init() {
        search = new TextFieldWidget(textRenderer, width / 2 - 100, 32, 200, 16, Text.literal("Search"));
        search.setPlaceholder(Text.literal("Search modules..."));
        search.setChangedListener(s -> filter()); addDrawableChild(search); filter();
    }
    void filter() {
        String q = search == null ? "" : search.getText().toLowerCase(); shown.clear();
        for (Scx.Mod m : Scx.MODS) if (q.isEmpty() || m.name.toLowerCase().contains(q)) shown.add(m);
        scroll = 0;
    }
    @Override public void renderBackground(DrawContext g, int mx, int my, float d) {
        if (client.world == null) Sunset.draw(g, width, height, 1f); else g.fill(0, 0, width, height, 0x77000000);
        int px = x0() - 12; g.fill(px, 6, width - px, height - 6, 0xF0150A18); g.drawBorder(px, 6, width - 2 * px, height - 12, 0xFFFF7A00);
    }
    @Override public void render(DrawContext g, int mx, int my, float d) {
        super.render(g, mx, my, d);
        g.drawCenteredTextWithShadow(textRenderer, "scxfyy client", width / 2, 14, 0xFFFF9A2E);
        g.drawText(textRenderer, "Custom sky", x0(), SKY_Y + 4, 0xFFFFE9D2, false);
        for (int i = 0; i < Scx.SKY_RGB.length; i++) {
            int x = skyX(i);
            g.fill(x, SKY_Y, x + 26, SKY_Y + 16, 0xFF000000 | Scx.SKY_RGB[i]);
            g.drawBorder(x, SKY_Y, 26, 16, Scx.sky == i ? 0xFFFFFFFF : 0xFF55405A);
            if (i == 0) g.drawCenteredTextWithShadow(textRenderer, "Off", x + 13, SKY_Y + 4, 0xFFCCCCCC);
        }
        g.drawText(textRenderer, Scx.sky == 0 ? "" : "Space: " + Scx.SKY_NAMES[Scx.sky], skyX(8) + 2, SKY_Y + 4, 0xFFFFA040, false);
        g.enableScissor(0, top(), width, bottom());
        for (int i = 0; i < shown.size(); i++) {
            Scx.Mod m = shown.get(i); int x = x0() + (i % 2) * (CW + GAP), y = top() + (i / 2) * (CH + GAP) - (int) scroll;
            if (y + CH < top() || y > bottom()) continue;
            boolean on = m.enabled && !m.todo, hov = mx >= x && mx < x + CW && my >= y && my < y + CH && my >= top() && my < bottom();
            g.fill(x, y, x + CW, y + CH, on ? 0xE05A2A0E : 0xD0201020); g.drawBorder(x, y, CW, CH, hov ? 0xFFFFFFFF : on ? 0xFFFF7A00 : 0xFF3A2438);
            g.drawText(textRenderer, m.name, x + 8, y + 6, 0xFFFFFFFF, false);
            List<OrderedText> lines = textRenderer.wrapLines(Text.literal(m.todo ? "Not built in. Get it in the launcher: Store > Extras." : m.desc), CW - 84);
            for (int k = 0; k < Math.min(2, lines.size()); k++) g.drawText(textRenderer, lines.get(k), x + 8, y + 18 + k * 10, m.todo ? 0xFFFFA040 : 0xFFC9B3BE, false);
            int tx = x + CW - 36, ty = y + 15;
            if (m.hasSettings()) { g.fill(x + CW - 62, ty - 2, x + CW - 44, ty + 16, 0xFF332A3A); g.drawCenteredTextWithShadow(textRenderer, "...", x + CW - 53, ty + 2, 0xFFFFFFFF); }
            g.fill(tx, ty, tx + 28, ty + 14, m.todo ? 0xFF2A2230 : on ? 0xFFFF7A00 : 0xFF3A3040);
            g.fill(on ? tx + 16 : tx + 2, ty + 2, on ? tx + 26 : tx + 12, ty + 12, 0xFFFFFFFF);
        }
        g.disableScissor();
        g.drawCenteredTextWithShadow(textRenderer, "Click: on/off   Right click or ...: keybind and settings   Scroll for more", width / 2, height - 14, 0xFFFFE9D2);
    }
    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (my >= SKY_Y && my < SKY_Y + 16) for (int i = 0; i < Scx.SKY_RGB.length; i++)
            if (mx >= skyX(i) && mx < skyX(i) + 26) { Scx.sky = i; Scx.save(); return true; }
        if (my >= top() && my < bottom()) for (int i = 0; i < shown.size(); i++) {
            int x = x0() + (i % 2) * (CW + GAP), y = top() + (i / 2) * (CH + GAP) - (int) scroll;
            if (mx >= x && mx < x + CW && my >= y && my < y + CH) {
                Scx.Mod m = shown.get(i); if (m.todo) return true;
                boolean gear = m.hasSettings() && mx >= x + CW - 62 && mx < x + CW - 44;
                if (button == 1 || gear) { if (m.hasSettings()) client.setScreen(new BindScreen(this, m)); }
                else { m.enabled = !m.enabled; Scx.save(); }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }
    @Override public boolean mouseScrolled(double mx, double my, double hx, double vy) { scroll = Math.max(0, Math.min(maxScroll(), scroll - vy * 24)); return true; }
    @Override public boolean keyPressed(int key, int scan, int mods) {
        if (Scx.MENU_KEY.matchesKey(key, scan) && !search.isFocused()) { close(); return true; }
        return super.keyPressed(key, scan, mods);
    }
    @Override public void removed() { Scx.save(); }
}
