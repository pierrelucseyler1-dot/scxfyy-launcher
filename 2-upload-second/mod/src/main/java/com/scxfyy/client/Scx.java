package com.scxfyy.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import com.scxfyy.client.mixin.HandledAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Scx implements ClientModInitializer {
    public static class Num { public final String name; public double v, min, max, step;
        Num(String n, double v, double min, double max, double step) { name = n; this.v = v; this.min = min; this.max = max; this.step = step; } }
    public static class Flag { public final String name; public boolean v; Flag(String n, boolean v) { name = n; this.v = v; } }
    public static class Mod {
        public final String id, name, desc; public boolean hold, action, bind, enabled, todo, down, was; public int key = -1;
        public final List<Num> nums = new ArrayList<>(); public final List<Flag> flags = new ArrayList<>(); public String textName, text = "";
        Mod(String id, String name, String desc) { this.id = id; this.name = name; this.desc = desc; }
        public Mod hold(int k) { hold = true; bind = true; key = k; return this; }
        public Mod action(int k) { action = true; bind = true; key = k; return this; }
        public Mod bind() { bind = true; return this; }
        public Mod on() { enabled = true; return this; }
        public Mod todo() { todo = true; return this; }
        public Mod num(String n, double v, double a, double b, double st) { nums.add(new Num(n, v, a, b, st)); return this; }
        public Mod flag(String n, boolean v) { flags.add(new Flag(n, v)); return this; }
        public Mod text(String n, String d) { textName = n; text = d; return this; }
        public boolean hasSettings() { return !todo && (bind || !nums.isEmpty() || !flags.isEmpty() || textName != null); }
    }
    public static final List<Mod> MODS = new ArrayList<>();
    static final Map<String, Mod> BY = new HashMap<>();
    static Mod add(String id, String name, String desc) { Mod m = new Mod(id, name, desc); MODS.add(m); BY.put(id, m); return m; }
    public static Mod m(String id) { return BY.get(id); }
    public static boolean on(String id) { Mod m = BY.get(id); return m != null && m.enabled && !m.todo; }
    public static boolean flag(String id, int i) { return BY.get(id).flags.get(i).v; }
    public static double v(String id, int i) { return BY.get(id).nums.get(i).v; }

    static {
        add("fps", "FPS counter", "Keep your frame rate in view.").on();
        add("coords", "Coordinates", "Your position in the world.").flag("Show decimals", false);
        add("ping", "Ping", "Your connection latency to the server.");
        add("clock", "Clock", "Local time, visible while you play.").flag("24-hour clock", true);
        add("direction", "Direction", "Which way you face, with heading in degrees.");
        add("speed", "Speed", "Your horizontal movement speed.").flag("Show km/h", false);
        add("memory", "Memory", "Watch Java heap usage while you play.");
        add("keys", "Keystrokes", "Show movement keys and mouse buttons.");
        add("cps", "Clicks per second", "Track left and right clicks over the last second.");
        add("combo", "Combo counter", "Consecutive melee hits you land on a target.");
        add("waypoints", "Waypoints", "Press the key to drop a marker; shows distance and direction.").action(-1);
        add("armor", "Armor status", "Equipment icons and durability for armor and held weapons.").flag("Show durability numbers", true);
        add("potions", "Potion status", "Active effects and their remaining time.");
        add("potcount", "Pot counter", "Count splash potions in your inventory.");
        add("server", "Server info", "Server address, player count and connection latency.");
        add("biome", "Biome", "The biome at your current position.");
        add("itemphysics", "Item physics", "Dropped items tumble in the air and settle flat on the ground.").todo();
        add("chunkborders", "Chunk borders", "Toggle Minecraft's chunk boundary guide.").bind();
        add("fog", "Fog customizer", "Make distance fog thicker or push it further away.").num("Fog distance (%)", 100, 20, 300, 10);
        add("freelook", "Free look", "Hold a key to look around without turning your character.").hold(GLFW.GLFW_KEY_LEFT_ALT).num("Sensitivity (%)", 100, 10, 200, 10).flag("Third person view", true).on();
        add("quickaction", "Quick action", "Send your saved chat message or command with a key.").action(-1).text("Message or /command", "");
        add("viewmodel", "Viewmodel", "Set the size and position of the item in your hand.").num("Size (%)", 100, 30, 150, 5).num("X offset", 0, -50, 50, 5).num("Y offset", 0, -50, 50, 5);
        add("swing", "Swing speed", "Speed up or slow down your hand swing animation.").num("Swing speed (%)", 100, 25, 300, 5);
        add("handsway", "Hand sway", "Reduce or remove the delayed hand movement while turning.").num("Sway reduction (%)", 100, 0, 100, 10);
        add("crosshair", "Crosshair", "Customize your crosshair size, spacing and color.").num("Size", 6, 2, 20, 1).num("Thickness", 2, 1, 6, 1).num("Gap", 2, 0, 12, 1).num("Color hue", 8, 0, 100, 2).num("Opacity (%)", 100, 20, 100, 10).flag("Center dot", false).on();
        add("togglesneak", "Toggle sneak", "Press sneak once to stay crouched; press again to release.").bind();
        add("scoreboard", "Scoreboard", "Hide the server sidebar.");
        add("bossbars", "Boss bars", "Hide the game boss bars.");
        add("menublur", "Menu blur", "In-world menus use Minecraft's blur instead of the sunset tint.").num("Blur (%)", 50, 0, 100, 10);
        add("titles", "Titles", "Hide server titles and action-bar messages.");
        add("soundfilters", "Sound filters", "Turn down ambient cave sounds and thunder.").num("Volume (%)", 30, 0, 100, 10);
        add("worldclock", "World clock", "World day count and in-game time.");
        add("cooldowns", "Cooldowns", "Remaining time for active item cooldowns.");
        add("food", "Food preview", "Hunger, saturation and the food in your hand.");
        add("reach", "Reach display", "Distance to your last entity hit.");
        add("itemcounter", "Item counter", "Track up to ten chosen items across your inventory.").text("Item ids, comma separated", "minecraft:golden_apple,minecraft:ender_pearl");
        add("profile", "Player profile", "Your Minecraft name with your skin avatar.");
        add("team", "Team view", "Health and armor of nearby scoreboard teammates.");
        add("tnt", "TNT countdown", "Fuse and distance of nearby primed TNT.");
        add("packs", "Pack display", "Show the active resource packs in their current order.");
        add("staticfov", "Static FOV", "Keep sprinting and effects from changing FOV.").num("Effect reduction (%)", 100, 0, 100, 10);
        add("nickhider", "Nick hider", "Replace your own name in chat.").text("Fake name", "Player");
        add("sprint", "Always sprint", "Sprint automatically while moving forward.").bind();
        add("staticsky", "Static sky", "Stop clouds, sun, moon and star movement.");
        add("weather", "Change weather", "Pick your own weather: 0 clear, 1 rain, 2 thunderstorm.").num("Weather", 0, 0, 2, 1).num("Intensity (%)", 100, 10, 100, 10);
        add("lowfire", "Low fire", "Lower the fire on your screen.").num("Lower by (%)", 70, 0, 100, 10);
        add("cleanedges", "Clean edges", "Remove the dark vignette around the screen.").num("Vignette removed (%)", 100, 0, 100, 10);
        add("particles", "Particle limiter", "Hide far particles and keep only a share of the rest.").num("Max distance", 24, 4, 64, 4).num("Particles kept (%)", 100, 0, 100, 10);
        add("mousetweaks", "Mouse tweaks", "Scroll over an item in a menu to send its stack to the other inventory.");
        add("fullbright", "Fullbright", "Brighten dark areas without placing lights.").bind().num("Brightness (%)", 100, 0, 100, 5);
        add("zoom", "Zoom", "Hold a key to get a closer view.").hold(GLFW.GLFW_KEY_C).num("Max zoom (%)", 70, 10, 100, 5).num("Zoom speed (%)", 60, 10, 100, 5).on();
        add("steady", "Steady camera", "Remove view bobbing and reduce damage tilt.").num("Tilt reduction (%)", 100, 0, 100, 10);
        add("skins3d", "3D skins", "Give nearby player skin overlays real depth.").todo();
        add("nobedrock", "No bedrock", "Render bedrock as deepslate.");
        add("dynlights", "Dynamic lights", "Light nearby blocks with the luminous item in your hand.").todo();
        add("glint", "Glint color", "Choose the enchantment shimmer color.").todo();
        add("borderless", "Borderless fullscreen", "Use a desktop-sized window with no border (start windowed).").bind();
        add("stretched", "Stretched resolution", "Play at a different screen shape, like 4:3 stretched to fill your screen.").bind().num("Width (px)", 1440, 640, 3840, 20).num("Height (px)", 1080, 480, 2160, 20);
        add("saturation", "Color saturation", "Adjust world color intensity without changing the HUD.").todo();
        add("hittint", "Hit tint", "Choose the color of the hurt flash on entities.").todo();
        add("outline", "Block outline", "Change the color and thickness of the targeted block.").todo();
    }

    static class Saver<T> { T saved; boolean set;
        void apply(SimpleOption<T> o, boolean want, T val) {
            if (want) { if (!set) { saved = o.getValue(); set = true; } if (!o.getValue().equals(val)) o.setValue(val); }
            else if (set) { o.setValue(saved); set = false; } } }
    static final Saver<Integer> S_FOV = new Saver<>(), S_BLUR = new Saver<>();
    static final Saver<Double> S_GAMMA = new Saver<>(), S_FE = new Saver<>(), S_TILT = new Saver<>(), S_AMB = new Saver<>(), S_WEA = new Saver<>();
    static final Saver<Boolean> S_SNEAK = new Saver<>(), S_BOB = new Saver<>();

    public static KeyBinding MENU_KEY; static boolean hinted;
    public static boolean freelook; public static float fyaw, fpitch, skyFrozen;
    static boolean rsWas, atkWas, skySet, cbShown, bl; static Perspective savedPersp;
    static double px, pz, speed, reach; static int combo; static long lastHit; static int bx, by, bw, bh;
    static final Deque<Long> LC = new ArrayDeque<>(), RC = new ArrayDeque<>(); static boolean lw, rw;
    static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("scxfyy-client.json");

    @Override public void onInitializeClient() {
        load();
        MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.scxfyy.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.categories.scxfyy"));
        ClientTickEvents.END_CLIENT_TICK.register(Scx::tick);
        HudRenderCallback.EVENT.register(Scx::hud);
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof HandledScreen<?> hs) ScreenMouseEvents.allowMouseScroll(screen).register((sc, mx, my, hx, vy) -> {
                if (!on("mousetweaks") || client.player == null || client.interactionManager == null) return true;
                Slot sl = ((HandledAccessor) (Object) hs).scxSlotAt(mx, my);
                if (sl == null || !sl.hasStack()) return true;
                client.interactionManager.clickSlot(hs.getScreenHandler().syncId, sl.id, 0, SlotActionType.QUICK_MOVE, client.player);
                return false;
            });
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> save());
    }
    public static String keyName(int k) { return k <= 0 ? "None" : InputUtil.fromKeyCode(k, 0).getLocalizedText().getString(); }

    static class S { boolean e; int k; double[] n; String t; boolean[] f; }
    public static int sky;
    public static final String[] SKY_NAMES = {"Off", "Blue", "Red", "Purple", "Black", "Yellow", "Green", "Orange"};
    public static final int[] SKY_RGB = {0x2A2330, 0x0A2A8A, 0x7A0C16, 0x4A1A8A, 0x020208, 0x8A7410, 0x0E6A34, 0x9A4210};
    public static int skyRgb() { return SKY_RGB[Math.max(0, Math.min(SKY_RGB.length - 1, sky))]; }
    public static void save() {
        try { Map<String, S> m = new LinkedHashMap<>();
            for (Mod x : MODS) { S s = new S(); s.e = x.enabled; s.k = x.key; s.t = x.text; s.n = x.nums.stream().mapToDouble(n -> n.v).toArray();
                s.f = new boolean[x.flags.size()]; for (int i = 0; i < s.f.length; i++) s.f[i] = x.flags.get(i).v; m.put(x.id, s); }
            S ss = new S(); ss.n = new double[]{sky}; m.put("_sky", ss);
            Files.writeString(FILE, new Gson().toJson(m)); } catch (Exception ignored) {}
    }
    static void load() {
        try { Map<String, S> m = new Gson().fromJson(Files.readString(FILE), new TypeToken<Map<String, S>>() {}.getType());
            S ss = m.get("_sky"); if (ss != null && ss.n != null && ss.n.length > 0) sky = (int) ss.n[0];
            for (Mod x : MODS) { S s = m.get(x.id); if (s == null || x.todo) continue; x.enabled = s.e; x.key = s.k; if (s.t != null) x.text = s.t;
                if (s.n != null) for (int i = 0; i < Math.min(s.n.length, x.nums.size()); i++) x.nums.get(i).v = s.n[i];
                if (s.f != null) for (int i = 0; i < Math.min(s.f.length, x.flags.size()); i++) x.flags.get(i).v = s.f[i]; }
        } catch (Exception ignored) {}
    }

    static void fire(MinecraftClient c, Mod m) {
        if (m.id.equals("quickaction")) {
            String t = m.text.trim(); if (t.isEmpty()) return;
            if (t.startsWith("/")) c.player.networkHandler.sendChatCommand(t.substring(1)); else c.player.networkHandler.sendChatMessage(t);
        } else if (m.id.equals("waypoints")) {
            int n = m.text.isBlank() ? 0 : m.text.split("\\|").length; if (n >= 8) return;
            m.text += "WP" + (n + 1) + "," + (int) c.player.getX() + "," + (int) c.player.getY() + "," + (int) c.player.getZ() + "|"; save();
        }
    }
    static boolean strOn, nbShown; static int sw, sh, pw, ph, pt, sbx, sby, sbw, sbh;
    static void stretched(MinecraftClient c, boolean on, int w, int h) {
        long win = c.getWindow().getHandle();
        if (on) {
            if (!strOn) { int[] x = new int[1], y = new int[1], ww = new int[1], hh = new int[1]; GLFW.glfwGetWindowPos(win, x, y); GLFW.glfwGetWindowSize(win, ww, hh); sbx = x[0]; sby = y[0]; sbw = ww[0]; sbh = hh[0]; }
            long mon = GLFW.glfwGetPrimaryMonitor(); GLFWVidMode vm = GLFW.glfwGetVideoMode(mon);
            GLFW.glfwSetWindowMonitor(win, mon, 0, 0, w, h, vm == null ? GLFW.GLFW_DONT_CARE : vm.refreshRate());
        } else if (sbw > 0) { GLFW.glfwSetWindowMonitor(win, 0L, sbx, sby, sbw, sbh, GLFW.GLFW_DONT_CARE); }
    }
    static void borderless(MinecraftClient c, boolean on) {
        long h = c.getWindow().getHandle();
        if (on) {
            int[] x = new int[1], y = new int[1], w = new int[1], hh = new int[1];
            GLFW.glfwGetWindowPos(h, x, y); GLFW.glfwGetWindowSize(h, w, hh); bx = x[0]; by = y[0]; bw = w[0]; bh = hh[0];
            long mon = GLFW.glfwGetPrimaryMonitor(); GLFWVidMode vm = GLFW.glfwGetVideoMode(mon); int[] mx = new int[1], my = new int[1];
            GLFW.glfwGetMonitorPos(mon, mx, my);
            GLFW.glfwSetWindowAttrib(h, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE); GLFW.glfwSetWindowPos(h, mx[0], my[0]); GLFW.glfwSetWindowSize(h, vm.width(), vm.height());
        } else if (bw > 0) {
            GLFW.glfwSetWindowAttrib(h, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE); GLFW.glfwSetWindowPos(h, bx, by); GLFW.glfwSetWindowSize(h, bw, bh);
        }
    }

    static void tick(MinecraftClient c) {
        if (c.player == null || c.world == null) { hinted = false; return; }
        var p = c.player;
        if (!hinted) { hinted = true; p.sendMessage(Text.literal("scxfyy client loaded. Press " + MENU_KEY.getBoundKeyLocalizedText().getString() + " for the menu."), true); } long h = c.getWindow().getHandle(); boolean free = c.currentScreen == null; long now = System.currentTimeMillis();
        while (MENU_KEY.wasPressed()) { if (c.currentScreen == null) c.setScreen(new MenuScreen()); }
        for (Mod m : MODS) {
            if (m.todo) continue;
            boolean d = m.key > 0 && InputUtil.isKeyPressed(h, m.key); m.down = d && free;
            if (m.action) { if (m.enabled && m.down && !m.was) fire(c, m); }
            else if (!m.hold && m.down && !m.was) m.enabled = !m.enabled;
            m.was = d;
        }
        // options-based modules (restored when turned off)
        S_GAMMA.apply(c.options.getGamma(), on("fullbright"), 1.0 + v("fullbright", 0) / 100.0 * 14.0);
        S_SNEAK.apply(c.options.getSneakToggled(), on("togglesneak"), true);
        var fe = c.options.getFovEffectScale(); S_FE.apply(fe, on("staticfov"), (S_FE.set ? S_FE.saved : fe.getValue()) * (1 - v("staticfov", 0) / 100.0));
        S_BOB.apply(c.options.getBobView(), on("steady"), false);
        var tl = c.options.getDamageTiltStrength(); S_TILT.apply(tl, on("steady"), (S_TILT.set ? S_TILT.saved : tl.getValue()) * (1 - v("steady", 0) / 100.0));
        S_BLUR.apply(c.options.getMenuBackgroundBlurriness(), on("menublur"), (int) Math.round(v("menublur", 0) / 10.0));
        var ao = c.options.getSoundVolumeOption(SoundCategory.AMBIENT); var wo = c.options.getSoundVolumeOption(SoundCategory.WEATHER);
        double f = v("soundfilters", 0) / 100.0, sf = on("soundfilters") ? 1 : 0;
        S_AMB.apply(ao, sf == 1, (S_AMB.set ? S_AMB.saved : ao.getValue()) * f); S_WEA.apply(wo, sf == 1, (S_WEA.set ? S_WEA.saved : wo.getValue()) * f);
        // free look
        boolean fl = on("freelook") && m("freelook").down;
        if (fl && !freelook) { fyaw = p.getYaw(); fpitch = p.getPitch(); savedPersp = c.options.getPerspective(); if (flag("freelook", 0)) c.options.setPerspective(Perspective.THIRD_PERSON_BACK); }
        if (!fl && freelook && savedPersp != null) c.options.setPerspective(savedPersp);
        freelook = fl;
        if (on("sprint") && c.options.forwardKey.isPressed() && !p.isSneaking() && p.getHungerManager().getFoodLevel() > 6) p.setSprinting(true);
        if (on("handsway")) { float k = (float) (v("handsway", 0) / 100.0); p.renderYaw = MathHelper.lerp(k, p.renderYaw, p.getYaw()); p.lastRenderYaw = MathHelper.lerp(k, p.lastRenderYaw, p.getYaw());
            p.renderPitch = MathHelper.lerp(k, p.renderPitch, p.getPitch()); p.lastRenderPitch = MathHelper.lerp(k, p.lastRenderPitch, p.getPitch()); }
        if (on("weather")) { int md = (int) v("weather", 0); float in = (float) (v("weather", 1) / 100.0); c.world.setRainGradient(md >= 1 ? in : 0f); c.world.setThunderGradient(md >= 2 ? in : 0f); }
        if (on("staticsky") && !skySet) { skyFrozen = c.world.getDimension().getSkyAngle(c.world.getTimeOfDay()); skySet = true; }
        if (!on("staticsky")) skySet = false;
        if (on("chunkborders") != cbShown) { c.debugRenderer.toggleShowChunkBorder(); cbShown = !cbShown; }
        if (on("borderless") != bl) { borderless(c, on("borderless")); bl = on("borderless"); }
        boolean so = on("stretched"); int wantW = (int) v("stretched", 0), wantH = (int) v("stretched", 1);
        if (so && (!strOn || wantW != sw || wantH != sh)) {       // wait until the sliders stop moving, then switch
            if (wantW == pw && wantH == ph) pt++; else { pw = wantW; ph = wantH; pt = 0; }
            if (pt >= 20 || !strOn && pt >= 2) { stretched(c, true, wantW, wantH); strOn = true; sw = wantW; sh = wantH; pt = 0; }
        } else if (!so && strOn) { stretched(c, false, 0, 0); strOn = false; }
        if (on("nobedrock") != nbShown) { nbShown = on("nobedrock"); if (c.worldRenderer != null) c.worldRenderer.reload(); }
        // stats
        speed = Math.hypot(p.getX() - px, p.getZ() - pz) * 20; px = p.getX(); pz = p.getZ();
        boolean atk = c.options.attackKey.isPressed();
        if (atk && !atkWas && free && c.crosshairTarget instanceof EntityHitResult eh) { reach = p.getEyePos().distanceTo(eh.getPos()); combo++; lastHit = now; }
        atkWas = atk; if (p.hurtTime > 0 || now - lastHit > 2500) combo = 0;
    }

    static double zoomCur = -1, zoomBase; static long zoomT;
    static void zoom(MinecraftClient c, boolean want) {
        var o = c.options.getFov(); long now = System.nanoTime(); double dt = zoomT == 0 ? 0 : Math.min(0.1, (now - zoomT) / 1e9); zoomT = now;
        if (want && zoomCur < 0) { zoomBase = o.getValue(); zoomCur = zoomBase; }
        if (zoomCur < 0) return;
        double target = want ? Math.max(1, zoomBase * (1 - v("zoom", 0) / 100.0 * 0.95)) : zoomBase;
        double rate = 4 + 26 * v("zoom", 1) / 100.0; zoomCur += (target - zoomCur) * (1 - Math.exp(-rate * dt));
        if (!want && Math.abs(zoomCur - zoomBase) < 0.6) { o.setValue((int) Math.round(zoomBase)); zoomCur = -1; return; }
        o.setValue((int) Math.round(zoomCur));
    }
    static void clicks(MinecraftClient c) {
        if (c.currentScreen != null) return; long h = c.getWindow().getHandle(), n = System.currentTimeMillis();
        boolean l = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS, r = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (l && !lw) LC.add(n); if (r && !rw) RC.add(n); lw = l; rw = r;
        while (!LC.isEmpty() && n - LC.peek() > 1000) LC.poll(); while (!RC.isEmpty() && n - RC.peek() > 1000) RC.poll();
    }
    static void box(DrawContext g, TextRenderer tr, String s, int x, int y, int w, boolean on) {
        g.fill(x, y, x + w, y + 16, on ? 0xFFFF7A00 : 0x66000000); g.drawCenteredTextWithShadow(tr, s, x + w / 2, y + 4, 0xFFFFFFFF);
    }
    static int count(PlayerInventory inv, Item it) { int n = 0; for (int i = 0; i < inv.size(); i++) { ItemStack s = inv.getStack(i); if (s.isOf(it)) n += s.getCount(); } return n; }
    static String fmt(int ticks) { int s = ticks / 20; return s / 60 + ":" + String.format("%02d", s % 60); }

    static void edges(DrawContext g, int W, int H, float a) {
        g.fillGradient(0, 0, W, H / 5, ((int) (a * 255) << 24), 0); g.fillGradient(0, H - H / 5, W, H, 0, ((int) (a * 255) << 24));
        int step = Math.max(2, W / 60);
        for (int i = 0; i < 12; i++) { int al = (int) (a * 255 * (1 - i / 12f) * (1 - i / 12f)); g.fill(i * step, 0, (i + 1) * step, H, al << 24); g.fill(W - (i + 1) * step, 0, W - i * step, H, al << 24); }
    }
    static void hud(DrawContext g, RenderTickCounter t) {
        MinecraftClient c = MinecraftClient.getInstance();
        if (c.player != null && c.world != null) zoom(c, on("zoom") && m("zoom").down);
        if (c.player == null || c.world == null || c.options.hudHidden) return;
        var p = c.player; TextRenderer tr = c.textRenderer; int W = g.getScaledWindowWidth(), H = g.getScaledWindowHeight(), col = 0xFFFFA040;
        List<String> L = new ArrayList<>(), R = new ArrayList<>(); clicks(c);
        var nh = c.getNetworkHandler(); PlayerListEntry pe = nh != null ? nh.getPlayerListEntry(p.getUuid()) : null;
        if (on("fps")) L.add(c.getCurrentFps() + " FPS");
        if (on("coords")) L.add(flag("coords", 0) ? String.format("XYZ %.1f %.1f %.1f", p.getX(), p.getY(), p.getZ()) : "XYZ " + p.getBlockX() + " " + p.getBlockY() + " " + p.getBlockZ());
        if (on("ping") && pe != null) L.add(pe.getLatency() + " ms");
        if (on("clock")) L.add(LocalTime.now().format(DateTimeFormatter.ofPattern(flag("clock", 0) ? "HH:mm" : "h:mm a")));
        if (on("direction")) { String f = p.getHorizontalFacing().asString(); L.add(Character.toUpperCase(f.charAt(0)) + f.substring(1) + " " + (int) MathHelper.wrapDegrees(p.getYaw()) + "\u00B0"); }
        if (on("speed")) L.add(flag("speed", 0) ? String.format("%.1f km/h", speed * 3.6) : String.format("%.1f b/s", speed));
        if (on("memory")) { Runtime r = Runtime.getRuntime(); L.add("Mem " + (r.totalMemory() - r.freeMemory()) / 1048576 + "/" + r.maxMemory() / 1048576 + " MB"); }
        if (on("cps")) L.add("CPS " + LC.size() + " | " + RC.size());
        if (on("combo")) L.add("Combo " + combo);
        if (on("reach")) L.add(String.format("Reach %.2f", reach));
        if (on("server")) { var si = c.getCurrentServerEntry(); L.add((si != null ? si.address : "Singleplayer") + (nh != null ? "  " + nh.getPlayerList().size() + " online" : "") + (pe != null ? "  " + pe.getLatency() + " ms" : "")); }
        if (on("biome")) L.add("Biome " + c.world.getBiome(p.getBlockPos()).getKey().map(k -> k.getValue().getPath()).orElse("?"));
        if (on("worldclock")) { long tt = c.world.getTimeOfDay(), dt = tt % 24000; L.add(String.format("Day %d  %02d:%02d", tt / 24000 + 1, (dt / 1000 + 6) % 24, (dt % 1000) * 60 / 1000)); }
        if (on("food")) { String s = "Food " + p.getHungerManager().getFoodLevel() + "/20 sat " + String.format("%.1f", p.getHungerManager().getSaturationLevel());
            FoodComponent fc = p.getMainHandStack().get(DataComponentTypes.FOOD); if (fc != null) s += "  +" + fc.nutrition(); L.add(s); }
        if (on("potcount")) L.add("Pots " + count(p.getInventory(), Items.SPLASH_POTION));
        if (on("itemcounter")) { int n = 0; for (String s : m("itemcounter").text.split(",")) { if (n++ >= 10) break; Identifier id = Identifier.tryParse(s.trim()); if (id == null) continue;
            Item it = Registries.ITEM.get(id); L.add(it.getName().getString() + " x" + count(p.getInventory(), it)); } }
        if (on("cooldowns")) { var cm = p.getItemCooldownManager(); for (int i = 0; i < 9; i++) { ItemStack s = p.getInventory().getStack(i); float pr = s.isEmpty() ? 0 : cm.getCooldownProgress(s, 0f); if (pr > 0) L.add(s.getName().getString() + " " + (int) (pr * 100) + "%"); } }
        if (on("tnt")) for (TntEntity e : c.world.getEntitiesByClass(TntEntity.class, p.getBoundingBox().expand(24), x -> true)) L.add(String.format("TNT %.1fs %dm", e.getFuse() / 20.0, (int) p.distanceTo(e)));
        if (on("waypoints")) for (String w : m("waypoints").text.split("\\|")) { if (w.isBlank()) continue; String[] a = w.split(","); if (a.length < 4) continue;
            double dx = Double.parseDouble(a[1]) - p.getX(), dz = Double.parseDouble(a[3]) - p.getZ(), dy = Double.parseDouble(a[2]) - p.getY();
            float rel = MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(-dx, dz)) - p.getYaw()); int idx = (int) (((Math.round(rel / 45.0) % 8) + 8) % 8);
            String[] AR = {"\u2191", "\u2197", "\u2192", "\u2198", "\u2193", "\u2199", "\u2190", "\u2196"};
            L.add(a[0] + " " + AR[idx] + " " + (int) Math.sqrt(dx * dx + dy * dy + dz * dz) + "m"); }
        if (on("potions")) for (StatusEffectInstance e : p.getStatusEffects()) R.add(e.getEffectType().value().getName().getString() + " " + (e.getAmplifier() + 1) + "  " + (e.getDuration() < 0 ? "inf" : fmt(e.getDuration())));
        if (on("packs")) { int n = 0; for (ResourcePackProfile rp : c.getResourcePackManager().getEnabledProfiles()) { if (n++ >= 6) break; R.add(rp.getDisplayName().getString()); } }
        if (on("team")) for (PlayerEntity o : c.world.getPlayers()) if (o != p && o.isTeammate(p) && o.squaredDistanceTo(p) < 4096) R.add(o.getName().getString() + " " + (int) o.getHealth() + "hp " + o.getArmor() + "ar");
        int y = 4; for (String s : L) { g.drawTextWithShadow(tr, s, 4, y, col); y += 11; }
        if (on("keys")) { y += 3; box(g, tr, "W", 22, y, 16, c.options.forwardKey.isPressed()); box(g, tr, "A", 4, y + 18, 16, c.options.leftKey.isPressed());
            box(g, tr, "S", 22, y + 18, 16, c.options.backKey.isPressed()); box(g, tr, "D", 40, y + 18, 16, c.options.rightKey.isPressed());
            box(g, tr, "LMB", 4, y + 36, 26, c.options.attackKey.isPressed()); box(g, tr, "RMB", 32, y + 36, 26, c.options.useKey.isPressed()); }
        int ry = on("profile") ? 24 : 4;
        if (on("profile")) { String nm = on("nickhider") ? m("nickhider").text : p.getName().getString(); PlayerSkinDrawer.draw(g, p.getSkinTextures(), W - 20, 4, 16); g.drawTextWithShadow(tr, nm, W - 24 - tr.getWidth(nm), 8, col); }
        for (String s : R) { g.drawTextWithShadow(tr, s, W - 4 - tr.getWidth(s), ry, col); ry += 11; }
        if (on("armor")) { EquipmentSlot[] SL = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND}; int ay = H / 2 - 44;
            for (EquipmentSlot sl : SL) { ItemStack s = p.getEquippedStack(sl); if (s.isEmpty()) continue; g.drawItem(s, W - 22, ay); g.drawStackOverlay(tr, s, W - 22, ay);
                if (flag("armor", 0) && s.isDamageable()) { String d = String.valueOf(s.getMaxDamage() - s.getDamage()); g.drawTextWithShadow(tr, d, W - 26 - tr.getWidth(d), ay + 4, col); } ay += 18; } }
        if (on("cleanedges")) { double k = 1 - v("cleanedges", 0) / 100.0; if (k > 0.01) edges(g, W, H, (float) (0.5 * k)); }
        if (on("crosshair") && c.currentScreen == null && c.options.getPerspective().isFirstPerson()) {
            int cx = W / 2, cy = H / 2, s = (int) v("crosshair", 0), th = (int) v("crosshair", 1), gp = (int) v("crosshair", 2), o = ((int) (255 * v("crosshair", 4) / 100.0) << 24) | MathHelper.hsvToRgb((float) v("crosshair", 3) / 100f, 1f, 1f), a = th / 2, b = th - a;
            g.fill(cx - a, cy - gp - s, cx + b, cy - gp, o); g.fill(cx - a, cy + gp, cx + b, cy + gp + s, o);
            g.fill(cx - gp - s, cy - a, cx - gp, cy + b, o); g.fill(cx + gp, cy - a, cx + gp + s, cy + b, o);
            if (flag("crosshair", 0)) g.fill(cx - 1, cy - 1, cx + 1, cy + 1, o);
        }
    }
}
