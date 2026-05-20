package maplestory.map;

import maplestory.core.Camera;
import maplestory.core.GamePanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 新手森林一區（novice1）
 * 推薦等級：Lv 1~4
 * 怪物：史萊姆 x3
 * 特色：明亮晴天森林，有兩座梯子通往中層平台。
 *
 * 傳送門：
 *   左側 → 回新手村（VillageMap 右側）
 *   右側 → 新手森林二區
 */
public class NoviceMap1 extends BaseMap {

    public static final int MAP_WIDTH = 1800;
    static final        int GROUND_Y  = GamePanel.GAME_HEIGHT - 40;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Portal>   portals   = new ArrayList<>();
    private final List<Ladder>   ladders   = new ArrayList<>();

    public NoviceMap1() {
        buildPlatforms();
        buildLadders();
        buildPortals();
    }

    // ── 地形 ─────────────────────────────────────────────────
    private void buildPlatforms() {
        Color grass  = new Color(70, 155, 55);
        Color wood   = new Color(145, 102, 56);
        Color dkWood = new Color(118, 82, 44);

        // 地面（完整一條，消除缺口）
        platforms.add(new Platform(0, GROUND_Y, MAP_WIDTH, 40, grass));

        // 中層木台
        platforms.add(new Platform( 200, GROUND_Y - 118, 165, 18, wood));
        platforms.add(new Platform( 510, GROUND_Y - 155, 175, 18, dkWood));
        platforms.add(new Platform( 870, GROUND_Y - 132, 158, 18, wood));
        platforms.add(new Platform(1190, GROUND_Y - 162, 170, 18, dkWood));
        platforms.add(new Platform(1540, GROUND_Y - 128, 162, 18, wood));

        // 高層木台
        platforms.add(new Platform( 345, GROUND_Y - 252, 142, 18, new Color(108, 74, 36)));
        platforms.add(new Platform( 710, GROUND_Y - 272, 148, 18, new Color( 98, 66, 32)));
        platforms.add(new Platform(1040, GROUND_Y - 282, 155, 18, new Color(108, 74, 36)));
        platforms.add(new Platform(1380, GROUND_Y - 265, 148, 18, new Color( 98, 66, 32)));
    }

    private void buildLadders() {
        ladders.add(new Ladder(218, GROUND_Y - 118, GROUND_Y));
        ladders.add(new Ladder(888, GROUND_Y - 132, GROUND_Y));
    }

    private void buildPortals() {
        portals.add(new Portal(
            22, GROUND_Y - Portal.HEIGHT,
            "village", 80, GROUND_Y - 80,
            "回新手村", 1
        ));
        portals.add(new Portal(
            MAP_WIDTH - 68, GROUND_Y - Portal.HEIGHT,
            "novice2", 70, GROUND_Y - 80,
            "前往二區(Lv.3)", 3
        ));
    }

    // ── 更新 ─────────────────────────────────────────────────
    @Override public void update(double dt) { /* 靜態地圖，無需每幀更新 */ }

    // ── 繪製 ─────────────────────────────────────────────────
    @Override
    public void draw(Graphics2D g, Camera camera) {
        drawSky(g, camera);
        drawBackground(g, camera);
        drawTrees(g, camera);
        for (Platform p : platforms) p.draw(g, camera);
        for (Ladder   l : ladders)   l.draw(g, camera);
        for (Portal   p : portals)   p.draw(g, camera);
    }

    private void drawSky(Graphics2D g, Camera camera) {
        GradientPaint sky = new GradientPaint(
            0, 0,                    new Color(118, 198, 255),
            0, GamePanel.GAME_HEIGHT, new Color(192, 232, 255)
        );
        g.setPaint(sky);
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.GAME_HEIGHT);

        // 白雲
        g.setColor(new Color(255, 255, 255, 200));
        drawCloud(g, camera, 100,  45, 110, 40);
        drawCloud(g, camera, 420,  30, 130, 48);
        drawCloud(g, camera, 780,  55, 100, 36);
        drawCloud(g, camera, 1150, 35, 120, 44);
        drawCloud(g, camera, 1500, 50, 105, 38);
    }

    private void drawCloud(Graphics2D g, Camera cam, int wx, int wy, int w, int h) {
        int sx = (int)(wx - cam.getOffsetX() * 0.15);
        g.fillOval(sx,          wy + h / 3, w / 2, h * 2 / 3);
        g.fillOval(sx + w / 4,  wy,         w / 2, h);
        g.fillOval(sx + w / 2,  wy + h / 4, w / 2, h * 3 / 4);
    }

    private void drawBackground(Graphics2D g, Camera camera) {
        double cx = camera.getOffsetX();

        // 遠山（亮綠）
        g.setColor(new Color(132, 188, 118));
        int[] mX = {0, 250, 510, 770, 1040, 1300, 1560};
        int[] mH = {125, 162, 142, 178, 152, 168, 148};
        for (int i = 0; i < mX.length; i++) {
            int hx = (int)(mX[i] - cx * 0.20);
            g.fillOval(hx, GROUND_Y - mH[i], 270, mH[i] * 2);
        }

        // 近草坡（中綠）
        g.setColor(new Color(88, 155, 75, 165));
        int[] gX = {60, 340, 620, 900, 1180, 1460};
        for (int tx : gX) {
            int hx = (int)(tx - cx * 0.45);
            g.fillOval(hx, GROUND_Y - 95, 130, 165);
        }
    }

    private void drawTrees(Graphics2D g, Camera camera) {
        int[][] trees = {
            { 95, GROUND_Y -  78}, {320, GROUND_Y -  88},
            {655, GROUND_Y -  72}, {985, GROUND_Y -  82},
            {1305, GROUND_Y - 76}, {1648, GROUND_Y - 86}
        };
        for (int[] t : trees) drawTree(g, camera, t[0], t[1]);
    }

    private void drawTree(Graphics2D g, Camera cam, int wx, int wy) {
        int sx = (int)(wx - cam.getOffsetX());
        int sy = (int)(wy - cam.getOffsetY());
        g.setColor(new Color(102, 66, 36));
        g.fillRect(sx + 10, sy + 30, 11, 50);
        g.setColor(new Color(52, 148, 62));
        g.fillOval(sx,     sy +  6, 32, 30);
        g.fillOval(sx + 4, sy -  4, 26, 28);
        g.fillOval(sx + 6, sy + 18, 26, 22);
        g.setColor(new Color(36, 118, 48));
        g.drawOval(sx + 4, sy -  4, 26, 28);
    }

    // ── 介面實作 ─────────────────────────────────────────────
    @Override public List<Platform> getPlatforms() { return platforms; }
    @Override public List<Portal>   getPortals()   { return portals; }
    @Override public List<Ladder>   getLadders()   { return ladders; }
    @Override public int            getMapWidth()  { return MAP_WIDTH; }
    @Override public String         getMapId()     { return "novice1"; }
}
