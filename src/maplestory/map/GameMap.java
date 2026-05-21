package maplestory.map;

import maplestory.core.Camera;
import maplestory.core.GamePanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 戰鬥地圖（原 GameMap），繼承 BaseMap。
 * 地圖總寬度 2000 像素，有怪物和平台。
 * 左側有傳送門可回到村莊。
 */
public class GameMap extends BaseMap {

    public static final int MAP_WIDTH = 2000;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Portal>   portals   = new ArrayList<>();

    public GameMap() {
        buildMap();
        int groundY = GamePanel.GAME_HEIGHT - 40;

        // 左側傳送門 → 前線前哨站（無等級限制）
        portals.add(new Portal(
            30, groundY - Portal.HEIGHT,
            "frontier", FrontierTown.MAP_WIDTH - 68, groundY - 80,
            "回前哨站", 1
        ));

        // 右側傳送門 → 冰原驛站（需要 Lv.15）
        portals.add(new Portal(
            MAP_WIDTH - 68, groundY - Portal.HEIGHT,
            "icepost", 80, groundY - 80,
            "冰原驛站(Lv.15)", 15
        ));
    }

    /** 手動排列所有平台（地面 + 中層 + 高層） */
    private void buildMap() {
        int gY = GamePanel.GAME_HEIGHT - 40; // 地面 Y 座標


// ── 地面（完整一條，消除缺口） ───────────────────────
        platforms.add(new Platform(0, gY, MAP_WIDTH, 40, new Color(80, 140, 60)));

        // ── 中層浮台（棕色木頭感） ────────────────────────────
        platforms.add(new Platform( 150, gY - 110, 180, 18, new Color(140, 100, 55)));
        platforms.add(new Platform( 420, gY - 160, 200, 18, new Color(130,  90, 50)));
        platforms.add(new Platform( 700, gY - 125, 170, 18, new Color(140, 100, 55)));
        platforms.add(new Platform( 970, gY - 180, 210, 18, new Color(130,  90, 50)));
        platforms.add(new Platform(1230, gY - 135, 190, 18, new Color(140, 100, 55)));
        platforms.add(new Platform(1500, gY - 200, 200, 18, new Color(130,  90, 50)));
        platforms.add(new Platform(1760, gY - 130, 170, 18, new Color(140, 100, 55)));

        // ── 高層浮台（深棕，顏色更深代表更高） ───────────────
        platforms.add(new Platform( 300, gY - 260, 150, 18, new Color(110,  70, 35)));
        platforms.add(new Platform( 630, gY - 290, 160, 18, new Color(100,  65, 30)));
        platforms.add(new Platform(1060, gY - 320, 175, 18, new Color(110,  70, 35)));
        platforms.add(new Platform(1380, gY - 310, 160, 18, new Color(100,  65, 30)));
        platforms.add(new Platform(1680, gY - 340, 175, 18, new Color(110,  70, 35)));
    }

    // ── 繪製 ─────────────────────────────────────────────────
    public void draw(Graphics2D g, Camera camera) {
        drawBackground(g, camera);
        for (Platform p : platforms) p.draw(g, camera);
        for (Portal   p : portals)   p.draw(g, camera);
    }

    /**
     * 視差背景：山丘和雲移動速度比鏡頭慢，製造遠近感。
     * parallaxFactor 越小 = 移動越慢 = 看起來越遠。
     */
    private void drawBackground(Graphics2D g, Camera camera) {
        double camX = camera.getOffsetX();

        // 遠山（parallax 0.2）
        g.setColor(new Color(120, 170, 110));
        drawHills(g, camX * 0.2, GamePanel.GAME_HEIGHT,
                  new int[]{0, 250, 500, 750, 1000},
                  new int[]{150, 200, 160, 220, 170});

        // 近山（parallax 0.4）
        g.setColor(new Color(90, 150, 80));
        drawHills(g, camX * 0.4, GamePanel.GAME_HEIGHT,
                  new int[]{100, 380, 640, 900, 1150},
                  new int[]{110, 140, 120, 160, 130});

        // 雲朵（parallax 0.15，非常慢）
        g.setColor(new Color(255, 255, 255, 180));
        drawCloud(g, 80  - (int)(camX * 0.15), 60,  120, 45);
        drawCloud(g, 350 - (int)(camX * 0.15), 40,  100, 38);
        drawCloud(g, 620 - (int)(camX * 0.15), 70,  140, 50);
        drawCloud(g, 900 - (int)(camX * 0.15), 50,  110, 42);
    }

    /** 畫一排橢圓形山丘 */
    private void drawHills(Graphics2D g, double offsetX, int groundY,
                           int[] baseX, int[] heights) {
        for (int i = 0; i < baseX.length; i++) {
            int hx = (int)(baseX[i] - offsetX);
            int h  = heights[i];
            g.fillOval(hx, groundY - h, 280, h * 2);
        }
    }

    /** 畫一朵雲（多個橢圓組合） */
    private void drawCloud(Graphics2D g, int x, int y, int w, int h) {
        g.fillOval(x,          y + h / 3, w / 2, h * 2 / 3);
        g.fillOval(x + w / 4,  y,         w / 2, h);
        g.fillOval(x + w / 2,  y + h / 4, w / 2, h * 3 / 4);
    }

    // ── BaseMap 介面實作 ──────────────────────────────────────
    @Override public void           update(double dt)  { /* 戰鬥地圖無需每幀更新 */ }
    @Override public List<Platform> getPlatforms()     { return platforms; }
    @Override public List<Portal>   getPortals()       { return portals; }
    @Override public int            getMapWidth()      { return MAP_WIDTH; }
    @Override public String         getMapId()         { return "battle"; }
    @Override public String         getMapName()       { return "冒險平原"; }
    @Override public int            getMinLevel()      { return 10; }
}
