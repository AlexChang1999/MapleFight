package maplestory.map;

import maplestory.core.Camera;
import maplestory.core.GamePanel;
import maplestory.entity.NPC;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 起始村莊地圖。
 * 包含：地面、建築、樹木、3 位 NPC、右側傳送門（通往戰鬥地圖）。
 */
public class VillageMap extends BaseMap {

    public static final int MAP_WIDTH = 1600;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Portal>   portals   = new ArrayList<>();
    private final List<NPC>      npcs      = new ArrayList<>();

    // 地面 Y 座標
    private final int groundY = GamePanel.GAME_HEIGHT - 40;

    public VillageMap() {
        buildPlatforms();
        buildNPCs();
        buildPortals();
    }

    // ── 地形 ─────────────────────────────────────────────────
    private void buildPlatforms() {
        Color grass = new Color(80, 145, 60);

        // 主地面（全寬）
        platforms.add(new Platform(0, groundY, MAP_WIDTH, 40, grass));

        // 建築前的小階梯（裝飾用，可站上去）
        platforms.add(new Platform(455, groundY - 28, 50, 12, new Color(160, 130, 90)));
        platforms.add(new Platform(840, groundY - 28, 55, 12, new Color(160, 130, 90)));
    }

    // ── NPC ──────────────────────────────────────────────────
    private void buildNPCs() {
        // 村長老人（橘色，面右；dialogueId="elder"）
        npcs.add(new NPC(260, groundY - NPC.HEIGHT,
                         "村長老人", new Color(230, 150, 60), true, null, "elder"));
        // 道具商人（青色，面左，站在道具店前；shopId="item"）
        npcs.add(new NPC(590, groundY - NPC.HEIGHT,
                         "道具商人", new Color(60, 200, 180), false, "item"));
        // 武器鐵匠（紅色，面右，站在武器店前；shopId="weapon"）
        npcs.add(new NPC(990, groundY - NPC.HEIGHT,
                         "武器鐵匠", new Color(210, 80, 80), true, "weapon"));
    }

    // ── 傳送門 ───────────────────────────────────────────────
    private void buildPortals() {
        // 左側傳送門 → 新手森林一區（無等級限制）
        portals.add(new Portal(
            22, groundY - Portal.HEIGHT,
            "novice1", NoviceMap1.MAP_WIDTH - 130, groundY - 80,
            "前往新手林", 1
        ));
        // 右側傳送門 → 冒險平原（需要 Lv.10）
        portals.add(new Portal(
            MAP_WIDTH - 90, groundY - Portal.HEIGHT,
            "battle", 150, 300,
            "冒險平原(Lv.10)", 10
        ));
    }

    // ── 每幀更新 ─────────────────────────────────────────────
    @Override
    public void update(double dt) {
        for (NPC npc : npcs) npc.update(dt);
    }

    // ── 繪製 ─────────────────────────────────────────────────
    @Override
    public void draw(Graphics2D g, Camera camera) {
        drawSky(g);
        drawBackground(g, camera);
        drawTrees(g, camera);
        drawBuildings(g, camera);
        for (Platform p : platforms) p.draw(g, camera);
        for (Portal   p : portals)   p.draw(g, camera);
        for (NPC      n : npcs)      n.draw(g, camera);
    }

    /** 溫暖的早晨天空 */
    private void drawSky(Graphics2D g) {
        GradientPaint sky = new GradientPaint(
            0, 0,                    new Color(150, 210, 255),
            0, GamePanel.GAME_HEIGHT, new Color(220, 240, 255)
        );
        g.setPaint(sky);
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.GAME_HEIGHT);
    }

    /** 視差背景：遠山 + 草地 */
    private void drawBackground(Graphics2D g, Camera camera) {
        double cx = camera.getOffsetX();

        // 遠山（parallax 0.2）
        g.setColor(new Color(140, 190, 130));
        int[] mX = {0, 200, 420, 650, 870, 1100, 1320};
        int[] mH = {130, 170, 145, 190, 155, 175, 160};
        for (int i = 0; i < mX.length; i++) {
            int hx = (int)(mX[i] - cx * 0.2);
            g.fillOval(hx, groundY - mH[i], 280, mH[i] * 2);
        }

        // 近草坡（parallax 0.5）
        g.setColor(new Color(100, 170, 80));
        int[] gX = {50, 340, 620, 890, 1160};
        int[] gH = {70, 90, 75, 95, 80};
        for (int i = 0; i < gX.length; i++) {
            int hx = (int)(gX[i] - cx * 0.5);
            g.fillOval(hx, groundY - gH[i], 240, gH[i] * 2);
        }

        // 花朵點綴
        drawFlowers(g, camera);
    }

    /** 小花裝飾 */
    private void drawFlowers(Graphics2D g, Camera camera) {
        int[][] flowers = {
            {100, groundY - 5, 0xFF6699}, {190, groundY - 5, 0xFFCC00},
            {350, groundY - 5, 0xFF6699}, {780, groundY - 5, 0xFF9933},
            {1050, groundY - 5, 0xFFCC00}, {1250, groundY - 5, 0xFF6699}
        };
        for (int[] f : flowers) {
            int sx = (int)(f[0] - camera.getOffsetX());
            g.setColor(new Color(f[2]));
            g.fillOval(sx - 3, f[1] - 3, 6, 6);
            g.setColor(new Color(60, 160, 40));
            g.drawLine(sx, f[1] + 3, sx, f[1] + 10);
        }
    }

    /** 樹木 */
    private void drawTrees(Graphics2D g, Camera camera) {
        int[][] trees = {
            {55,  groundY - 80},
            {390, groundY - 90},
            {710, groundY - 75},
            {1110, groundY - 85},
            {1380, groundY - 80}
        };
        for (int[] t : trees) drawOneTree(g, camera, t[0], t[1]);
    }

    private void drawOneTree(Graphics2D g, Camera camera, int wx, int wy) {
        int sx = (int)(wx - camera.getOffsetX());
        int sy = (int)(wy - camera.getOffsetY());

        // 樹幹
        g.setColor(new Color(110, 75, 40));
        g.fillRect(sx + 11, sy + 32, 10, 48);

        // 葉（三個橢圓疊成）
        g.setColor(new Color(55, 155, 65));
        g.fillOval(sx,      sy + 10, 32, 28);
        g.fillOval(sx + 4,  sy,      26, 26);
        g.fillOval(sx + 8,  sy + 16, 28, 22);
        g.setColor(new Color(38, 125, 48));
        g.drawOval(sx + 4,  sy,      26, 26);
    }

    /** 建築物（道具店、武器店、村長家） */
    private void drawBuildings(Graphics2D g, Camera camera) {
        drawHouse(g, camera, 150,  groundY - 90,  105, 90,  "村長家",  new Color(200, 175, 130));
        drawHouse(g, camera, 460,  groundY - 105, 120, 105, "道具店",  new Color(195, 165, 120));
        drawHouse(g, camera, 855,  groundY - 120, 135, 120, "武器店",  new Color(185, 155, 110));
    }

    private void drawHouse(Graphics2D g, Camera camera,
                            int wx, int wy, int w, int h,
                            String signText, Color wallColor) {
        int sx = (int)(wx - camera.getOffsetX());
        int sy = (int)(wy - camera.getOffsetY());

        // 牆壁
        g.setColor(wallColor);
        g.fillRect(sx, sy, w, h);
        g.setColor(wallColor.darker());
        g.drawRect(sx, sy, w, h);

        // 門
        g.setColor(new Color(110, 75, 45));
        g.fillRoundRect(sx + w / 2 - 12, sy + h - 38, 24, 38, 4, 4);
        g.setColor(new Color(80, 50, 25));
        g.drawRoundRect(sx + w / 2 - 12, sy + h - 38, 24, 38, 4, 4);
        // 門把
        g.setColor(new Color(220, 180, 80));
        g.fillOval(sx + w / 2 + 5, sy + h - 22, 4, 4);

        // 窗戶
        g.setColor(new Color(180, 220, 255));
        g.fillRect(sx + 12, sy + 18, 26, 22);
        g.setColor(new Color(130, 180, 220));
        g.drawRect(sx + 12, sy + 18, 26, 22);
        // 窗格
        g.drawLine(sx + 12 + 13, sy + 18, sx + 12 + 13, sy + 40);
        g.drawLine(sx + 12,      sy + 29, sx + 38,       sy + 29);

        // 屋頂（三角形）
        int[] rx = {sx - 8, sx + w / 2, sx + w + 8};
        int[] ry = {sy,      sy - 38,    sy};
        g.setColor(new Color(175, 55, 55));
        g.fillPolygon(rx, ry, 3);
        g.setColor(new Color(135, 35, 35));
        g.drawPolygon(rx, ry, 3);
        // 屋脊
        g.setColor(new Color(140, 40, 40));
        g.drawLine(sx + w / 2 - 5, sy - 36, sx + w / 2 + 5, sy - 36);

        // 招牌文字
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(signText, sx + (w - fm.stringWidth(signText)) / 2, sy + 14);
    }

    // ── 介面實作 ─────────────────────────────────────────────
    @Override public List<Platform> getPlatforms() { return platforms; }
    @Override public List<Portal>   getPortals()   { return portals; }
    @Override public List<NPC>      getNPCs()       { return npcs; }
    @Override public int            getMapWidth()   { return MAP_WIDTH; }
    @Override public String         getMapId()      { return "village"; }
}
