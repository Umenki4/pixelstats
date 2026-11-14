package com.pixelstats.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pixelstats.PixelStatsMod;
import com.pixelstats.config.PixelConfig;
import com.pixelstats.network.ModNetwork;
import com.pixelstats.network.UpgradeStatPacket;
import com.pixelstats.registry.ModSounds;
import com.pixelstats.stats.StatType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The pixel-art MMORPG style "Status" window. Opened with the K key.
 * Panel, rows, banner, buttons and icons are PNG textures (textures/gui); the panel and rows are 9-sliced.
 */
public class StatsScreen extends Screen {
    private static final ResourceLocation PANEL = tex("panel");
    private static final ResourceLocation ROW = tex("row");
    private static final ResourceLocation ROW_HOVER = tex("row_hover");
    private static final ResourceLocation BANNER = tex("banner");
    private static final ResourceLocation BUTTON = tex("button");
    private static final ResourceLocation ICON_XP = tex("icon_xp");

    private static final int PANEL_W = 264;
    private static final int PANEL_H = 220;
    private static final int ROW_W = 236;
    private static final int ROW_H = 26;
    private static final int ROW_GAP = 3;
    private static final int BTN = 20;

    private final int[] flash = new int[StatType.COUNT];   // white flash after a successful upgrade
    private final int[] press = new int[StatType.COUNT];   // button "pressed" frames
    private final int[] deny = new int[StatType.COUNT];    // red shake when an upgrade is refused
    private final List<Floater> floaters = new ArrayList<>();

    private int left;
    private int top;
    private int age;
    private int lastHoverButton = -1;

    /** Little "+1" text that floats upwards. */
    private static final class Floater {
        final int row;
        final String text;
        int age;

        Floater(int row, String text) {
            this.row = row;
            this.text = text;
        }
    }

    public StatsScreen() {
        super(Component.translatable("screen.pixelstats.title"));
        UiSounds.play(ModSounds.UI_OPEN);
    }

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(PixelStatsMod.MODID, "textures/gui/" + name + ".png");
    }

    // ------------------------------------------------------------------ layout

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = Math.max(14, (height - PANEL_H) / 2);
    }

    private int rowX() {
        return left + (PANEL_W - ROW_W) / 2;
    }

    private int rowY(int i) {
        return top + 40 + i * (ROW_H + ROW_GAP);
    }

    private int btnX() {
        return rowX() + ROW_W - BTN - 4;
    }

    private int btnY(int i) {
        return rowY(i) + 3;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ------------------------------------------------------------------ rules (client-side prediction only)

    private static boolean isFree(LocalPlayer player) {
        return player != null && player.isCreative() && PixelConfig.get(PixelConfig.CREATIVE_FREE);
    }

    private static int cost() {
        return Math.max(1, PixelConfig.get(PixelConfig.LEVEL_COST));
    }

    private static boolean canUpgrade(LocalPlayer player, StatType type) {
        if (player == null || ClientStats.get(type) >= PixelConfig.get(PixelConfig.MAX_POINTS)) {
            return false;
        }
        return isFree(player) || player.experienceLevel >= cost();
    }

    // ------------------------------------------------------------------ rendering

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        LocalPlayer player = Minecraft.getInstance().player;

        // panel + sheen + twinkling corner gems
        nineSlice(g, PANEL, left, top, PANEL_W, PANEL_H, 32, 8);
        g.fillGradient(left + 6, top + 6, left + PANEL_W - 6, top + 40, 0x30FFFFFF, 0x00FFFFFF);
        twinkle(g, left + 5, top + 5, 0.0, partialTick);
        twinkle(g, left + PANEL_W - 6, top + 5, 1.6, partialTick);
        twinkle(g, left + 5, top + PANEL_H - 6, 3.1, partialTick);
        twinkle(g, left + PANEL_W - 6, top + PANEL_H - 6, 4.7, partialTick);

        // title banner
        g.blit(BANNER, left + (PANEL_W - 128) / 2, top - 12, 0, 0, 128, 24, 128, 24);
        g.drawCenteredString(font, title, left + PANEL_W / 2, top - 12 + 8, 0xFFFFE9A8);

        // info bar: XP level (left), total points + cost (right)
        int level = player == null ? 0 : player.experienceLevel;
        g.blit(ICON_XP, left + 16, top + 14, 0, 0, 16, 16, 16, 16);
        g.drawString(font, Component.translatable("screen.pixelstats.xp", level), left + 36, top + 19, 0xFFB6F25A, true);

        Component totalText = Component.translatable("screen.pixelstats.total", ClientStats.total());
        Component costText = isFree(player)
                ? Component.translatable("screen.pixelstats.cost_free")
                : Component.translatable("screen.pixelstats.cost", cost());
        int rightEdge = left + PANEL_W - 16;
        g.drawString(font, totalText, rightEdge - font.width(totalText), top + 14, 0xFFAAB0C8, true);
        g.drawString(font, costText, rightEdge - font.width(costText), top + 25, 0xFF8FD6FF, true);

        // stat rows
        int hoveredRow = -1;
        int hoveredButton = -1;
        int max = PixelConfig.get(PixelConfig.MAX_POINTS);
        StatType[] types = StatType.values();
        for (int i = 0; i < types.length; i++) {
            StatType type = types[i];
            int points = ClientStats.get(type);
            int shake = deny[i] > 0 ? ((deny[i] & 1) == 0 ? 2 : -2) : 0;
            int rx = rowX() + shake;
            int ry = rowY(i);
            int bx = btnX() + shake;
            int by = btnY(i);

            boolean overRow = inside(mouseX, mouseY, rx, ry, ROW_W, ROW_H);
            boolean overButton = inside(mouseX, mouseY, bx, by, BTN, BTN);
            if (overRow) {
                hoveredRow = i;
            }
            if (overButton) {
                hoveredButton = i;
            }

            nineSlice(g, overRow ? ROW_HOVER : ROW, rx, ry, ROW_W, ROW_H, 16, 3);
            if (flash[i] > 0) {
                g.fill(rx + 2, ry + 2, rx + ROW_W - 2, ry + ROW_H - 2, (Math.min(150, flash[i] * 14) << 24) | 0xFFFFFF);
            }
            if (deny[i] > 0) {
                g.fill(rx + 2, ry + 2, rx + ROW_W - 2, ry + ROW_H - 2, (0x50 << 24) | 0xFF2020);
            }

            g.blit(type.icon(), rx + 5, ry + 5, 0, 0, 16, 16, 16, 16);
            g.drawString(font, type.name(), rx + 28, ry + 4, type.color, true);
            g.drawString(font, String.valueOf(points), rx + 28 + font.width(type.name()) + 6, ry + 4, 0xFFFFFFFF, true);

            PoseStack pose = g.pose();
            pose.pushPose();
            pose.translate(rx + 28, ry + 15, 0);
            pose.scale(0.75F, 0.75F, 1.0F);
            g.drawString(font, StatTexts.summary(type, points), 0, 0, 0xFFB8BED8, false);
            pose.popPose();

            if (points >= max) {
                g.drawCenteredString(font, Component.translatable("screen.pixelstats.maxed"), bx + BTN / 2, by + 6, 0xFFFFD166);
            } else {
                int v = !canUpgrade(player, type) ? 60 : (press[i] > 0 ? 40 : (overButton ? 20 : 0));
                g.blit(BUTTON, bx, by, 0, v, BTN, BTN, 20, 80);
            }
        }

        if (hoveredButton != lastHoverButton && hoveredButton >= 0) {
            UiSounds.play(ModSounds.UI_HOVER, 1.0F, 0.6F);
        }
        lastHoverButton = hoveredButton;

        // floating "+N"
        for (Floater f : floaters) {
            int alpha = Math.max(16, 255 - f.age * 10);
            int color = (alpha << 24) | (types[f.row].color & 0xFFFFFF);
            g.drawCenteredString(font, f.text, btnX() - 14, btnY(f.row) + 6 - (int) (f.age * 0.9F), color);
        }

        // footer
        g.drawCenteredString(font, Component.translatable("screen.pixelstats.hint"),
                left + PANEL_W / 2, top + PANEL_H - 30, 0xFFE8D9A0);
        g.drawCenteredString(font, Component.translatable("screen.pixelstats.close", KeyBindings.OPEN.getTranslatedKeyMessage()),
                left + PANEL_W / 2, top + PANEL_H - 18, 0xFF8890B0);

        if (hoveredRow >= 0) {
            StatType type = types[hoveredRow];
            g.renderComponentTooltip(font, StatTexts.tooltip(type, ClientStats.get(type)), mouseX, mouseY);
        }
    }

    /** Draws a texture as a 9-slice: fixed corners, stretched edges and centre. ts = texture size, b = border size. */
    private static void nineSlice(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h, int ts, int b) {
        int m = ts - 2 * b;
        // corners (1:1)
        g.blit(tex, x, y, 0, 0, b, b, ts, ts);
        g.blit(tex, x + w - b, y, ts - b, 0, b, b, ts, ts);
        g.blit(tex, x, y + h - b, 0, ts - b, b, b, ts, ts);
        g.blit(tex, x + w - b, y + h - b, ts - b, ts - b, b, b, ts, ts);
        // edges (stretched)
        g.blit(tex, x + b, y, w - 2 * b, b, (float) b, 0.0F, m, b, ts, ts);
        g.blit(tex, x + b, y + h - b, w - 2 * b, b, (float) b, (float) (ts - b), m, b, ts, ts);
        g.blit(tex, x, y + b, b, h - 2 * b, 0.0F, (float) b, b, m, ts, ts);
        g.blit(tex, x + w - b, y + b, b, h - 2 * b, (float) (ts - b), (float) b, b, m, ts, ts);
        // centre
        g.blit(tex, x + b, y + b, w - 2 * b, h - 2 * b, (float) b, (float) b, m, m, ts, ts);
    }

    private void twinkle(GuiGraphics g, int cx, int cy, double phase, float partialTick) {
        int alpha = (int) (140 + 115 * Math.sin((age + partialTick) * 0.2 + phase));
        int color = (alpha << 24) | 0xFFE9A8;
        g.fill(cx - 1, cy, cx + 2, cy + 1, color);
        g.fill(cx, cy - 1, cx + 1, cy + 2, color);
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < StatType.COUNT; i++) {
                if (inside(mouseX, mouseY, btnX(), btnY(i), BTN, BTN)) {
                    clickUpgrade(i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void clickUpgrade(int index) {
        StatType type = StatType.byId(index);
        LocalPlayer player = Minecraft.getInstance().player;
        if (type == null) {
            return;
        }
        if (!canUpgrade(player, type)) {
            if (ClientStats.get(type) < PixelConfig.get(PixelConfig.MAX_POINTS)) {
                deny[index] = 8;
                UiSounds.play(ModSounds.UI_DENY);
            }
            return;
        }
        press[index] = 4;
        UiSounds.play(ModSounds.UI_CLICK);
        ModNetwork.sendToServer(new UpgradeStatPacket(index, hasShiftDown() ? 10 : 1));   // server decides the real result
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyBindings.OPEN.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        UiSounds.play(ModSounds.UI_CLOSE);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ------------------------------------------------------------------ animation / server feedback

    @Override
    public void tick() {
        age++;
        for (int i = 0; i < StatType.COUNT; i++) {
            if (flash[i] > 0) {
                flash[i]--;
            }
            if (press[i] > 0) {
                press[i]--;
            }
            if (deny[i] > 0) {
                deny[i]--;
            }
        }
        floaters.removeIf(f -> ++f.age > 24);
    }

    /** Called when the server sent new numbers while this window is open: flash, "+N" text and success sound. */
    public void onStatsChanged(int[] before, int[] now) {
        boolean gained = false;
        for (int i = 0; i < StatType.COUNT && i < before.length && i < now.length; i++) {
            int diff = now[i] - before[i];
            if (diff > 0) {
                flash[i] = 12;
                floaters.add(new Floater(i, "+" + diff));
                gained = true;
            }
        }
        if (gained) {
            UiSounds.play(ModSounds.UI_UPGRADE, 0.95F + (float) Math.random() * 0.1F, 1.0F);
        }
    }
}
