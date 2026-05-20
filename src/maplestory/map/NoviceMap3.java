package maplestory.map;

import maplestory.core.Camera;
import maplestory.core.GamePanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 新手森林三區（novice3）
 * 推薦等級：Lv 5~9
 * 怪物：野豬 x2 + 蝙蝠 x2
 * 特色：傍晚橘色林，四座梯子，右側傳送門通往冒險平原。
 *
 * 傳送門：
 *   左側 → 新手森林二區
 *   右側 → 冒險平原（GameMap）
 */
public class NoviceMap3 extends BaseMap {

    public static final int MAP_WIDTH = 2200;
    static final        int GROUND_Y  = GamePanel.GAME_HEIGHT - 40;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Portal>   portals   = new ArrayList<>();
    private final List<Ladder>   ladders   = new ArrayList<>();

    public NoviceMap3() {
        buildPlatforms();
        buildLadders();
        buildPortals();
    }

    // ── 地形 ─────────────────────────────────────────────────
    private void buildPlatforms() {
        Color grass  = new Color(72, 148, 50);
        Color wood   = new Color(148, 102, 54);
        Color dkWood = new Color(112, 78, 40);

        // 地面
        platforms.add(new Platform(   0, GROUND_Y, 758, 40, grass));
        platforms.add(new Platform( 785, GROUND_Y, 712, 40, new Color(65, 138, 46)));
        platforms.add(new Platform(1535, GROUND_Y, 665, 40, grass));

        // 中層（七段，路線更複雜）
        platforms.add(new Platform( 118, GROUND_Y - 135, 168, 18, wood));
        platforms.add(new Platform( 402, GROUND_Y - 170, 188, 18, dkWood));
        platforms.add(new Platform( 702, GROUND_Y - 150, 168, 18, wood));
        platforms.add(new Platform(1005, GROUND_Y - 182, 195, 18, dkWood));
        platforms.add(new Platform(1305, GROUND_Y - 155, 178, 18, wood));
        platforms.add(new Platform(1605, GROUND_Y - 178, 182, 18, dkWood));
        platforms.add(new Platform(1905, GROUND_Y - 152, 180, 18, wood));

        // 高層（六段）
        platforms.add(new Platform( 242, GROUND_Y - 288, 155, 18, new Color(96,  66, 33)));
        platforms.add(new Platform( 562, GROUND_Y - 312, 162, 18, new Color(89,  61, 31)));
        platforms.add(new Platform( 870, GROUND_Y - 298, 170, 18, new Color(96,  66, 33)));
        platforms.add(new Platform(1162, GROUND_Y - 322, 168, 18, new Color(89,  61, 31)));
        platforms.add(new Platform(1482, GROUND_Y - 308, 175, 18, new Color(96,  66, 33)));
        platforms.add(new Platform(1782, GROUND_Y - 292, 162, 18, new Color(89,  61, 31)));
    }

    private void buildLadders() {
        ladders.add(new Ladder(138, GROUND_Y - 135, GROUND_Y));
        ladders.add(new Ladder(720, GROUND_Y - 150, GROUND_Y));
        ladders.add(new Ladder(1322, GROUND_Y - 155, GROUND_Y));
        ladders.add(new Ladder(1922, GROUND_Y - 152, GROUND_Y));
    }

    private void buildPortals() {
        portals.add(new Portal(
            22, GROUND_Y - Portal.HEIGHT,
            "novice2", NoviceMap2.MAP_WIDTH - 68, GROUND_Y - 80,
            "回二區"
        ));
        portals.add(new Portal(
            MAP_WIDTH - 68, GROUND_Y - Portal.HEIGHT,
            "battle", 150, GROUND_Y - 80,
            "冒險平原"
        ));
    }

    // ── 更新 ─────────────────────────────────────────────────
    @Override public void update(double dt) { /* 靜態地圖 */ }

    // ── 繪製 ─────────────────────────────────────────────────
    @Override
    public void draw(Graphics2D g, Camera camera) {
        drawSky(g, camera);
        drawBackground(g, camera);
        for (Platform p : platforms) p.draw(g, camera);
        for (Ladder   l : ladders)   l.draw(g, camera);
        for (Portal   p : portals)   p.draw(g, camera);
    }

    private void drawSky(Graphics2D g, Camera camera) {
        // 傍晚橘紅天空
        GradientPaint sky = new GradientPaint(
            0, 0,                    new Color(252, 168, 88),
            0, GamePanel.GAME_HEIGHT, new Color(255, 208, 138)
        );
        g.setPaint(sky);
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.GAME_HEIGHT);

        // 淡橘雲
        g.setColor(new Color(255, 235, 200, 180));
        drawCloudAt(g, camera, 80,   38, 115, 42);
        drawCloudAt(g, camera, 440,  28, 138, 50);
        drawCloudAt(g, camera, 850,  48, 108, 38);
        drawCloudAt(g, camera, 1200, 32, 125, 45);
        drawCloudAt(g, camera, 1650, 52, 110, 40);
    }

    private void drawCloudAt(Graphics2D g, Camera cam, int wx, int wy, int w, int h) {
        int sx = (int)(wx - cam.getOffsetX() * 0.12);
        g.fillOval(sx,          wy + h / 3, w / 2, h * 2 / 3);
        g.fillOval(sx + w / 4,  wy,         w / 2, h);
        g.fillOval(sx + w / 2,  wy + h / 4, w / 2, h * 3 / 4);
    }

    private void drawBackground(Graphics2D g, Camera camera) {
        double cx = camera.getOffsetX();

        // 夕陽剪影山丘（暗橘褐色）
        g.setColor(new Color(138, 98, 58, 195));
        int[] mX = {0, 302, 604, 908, 1212, 1516, 1820};
        int[] mH = {142, 178, 152, 192, 162, 182, 156};
        for (int i = 0; i < mX.length; i++) {
            int hx = (int)(mX[i] - cx * 0.20);
            g.fillOval(hx, GROUND_Y - mH[i], 292, mH[i] * 2);
        }

        // 樹木剪影（傍晚暗色）
        g.setColor(new Color(62, 48, 32, 158));
        int[] tX = {48, 352, 682, 1008, 1328, 1688, 2018};
        for (int tx : tX) {
            int hx = (int)(tx - cx * 0.50);
            g.fillOval(hx - 14, GROUND_Y - 108, 148, 188);
        }
    }

    // ── 介面實作 ─────────────────────────────────────────────
    @Override public List<Platform> getPlatforms() { return platforms; }
    @Override public List<Portal>   getPortals()   { return portals; }
    @Override public List<Ladder>   getLadders()   { return ladders; }
    @Override public int            getMapWidth()  { return MAP_WIDTH; }
    @Override public String         getMapId()     { return "novice3"; }
}
