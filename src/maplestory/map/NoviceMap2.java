package maplestory.map;

import maplestory.core.Camera;
import maplestory.core.GamePanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 新手森林二區（novice2）
 * 推薦等級：Lv 3~7
 * 怪物：史萊姆 x2 + 蝙蝠 x2
 * 特色：林木漸密，光線偏暗，多層平台，三座梯子。
 *
 * 傳送門：
 *   左側 → 新手森林一區
 *   右側 → 新手森林三區
 */
public class NoviceMap2 extends BaseMap {

    public static final int MAP_WIDTH = 2000;
    static final        int GROUND_Y  = GamePanel.GAME_HEIGHT - 40;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Portal>   portals   = new ArrayList<>();
    private final List<Ladder>   ladders   = new ArrayList<>();

    public NoviceMap2() {
        buildPlatforms();
        buildLadders();
        buildPortals();
    }

    // ── 地形 ─────────────────────────────────────────────────
    private void buildPlatforms() {
        Color grass  = new Color(58, 132, 48);
        Color wood   = new Color(128, 92, 50);
        Color dkWood = new Color(104, 72, 40);

        // 地面（無縫連接）
        platforms.add(new Platform(   0, GROUND_Y, 742, 40, grass));
        platforms.add(new Platform( 742, GROUND_Y, 696, 40, new Color(52, 122, 44)));
        platforms.add(new Platform(1438, GROUND_Y, 562, 40, grass));

        // 中層
        platforms.add(new Platform( 148, GROUND_Y - 132, 172, 18, wood));
        platforms.add(new Platform( 455, GROUND_Y - 165, 188, 18, dkWood));
        platforms.add(new Platform( 775, GROUND_Y - 145, 168, 18, wood));
        platforms.add(new Platform(1075, GROUND_Y - 175, 198, 18, dkWood));
        platforms.add(new Platform(1375, GROUND_Y - 150, 182, 18, wood));
        platforms.add(new Platform(1695, GROUND_Y - 172, 180, 18, dkWood));

        // 高層
        platforms.add(new Platform( 295, GROUND_Y - 272, 152, 18, new Color(92,  62, 32)));
        platforms.add(new Platform( 638, GROUND_Y - 298, 158, 18, new Color(86,  58, 30)));
        platforms.add(new Platform( 998, GROUND_Y - 312, 165, 18, new Color(92,  62, 32)));
        platforms.add(new Platform(1518, GROUND_Y - 302, 170, 18, new Color(86,  58, 30)));
        platforms.add(new Platform(1838, GROUND_Y - 288, 155, 18, new Color(92,  62, 32)));
    }

    private void buildLadders() {
        ladders.add(new Ladder(166, GROUND_Y - 132, GROUND_Y));
        ladders.add(new Ladder(795, GROUND_Y - 145, GROUND_Y));
        ladders.add(new Ladder(1395, GROUND_Y - 150, GROUND_Y));
    }

    private void buildPortals() {
        portals.add(new Portal(
            22, GROUND_Y - Portal.HEIGHT,
            "novice1", NoviceMap1.MAP_WIDTH - 68, GROUND_Y - 80,
            "回一區"
        ));
        portals.add(new Portal(
            MAP_WIDTH - 68, GROUND_Y - Portal.HEIGHT,
            "novice3", 70, GROUND_Y - 80,
            "前往三區"
        ));
    }

    // ── 更新 ─────────────────────────────────────────────────
    @Override public void update(double dt) { /* 靜態地圖 */ }

    // ── 繪製 ─────────────────────────────────────────────────
    @Override
    public void draw(Graphics2D g, Camera camera) {
        drawSky(g);
        drawBackground(g, camera);
        for (Platform p : platforms) p.draw(g, camera);
        for (Ladder   l : ladders)   l.draw(g, camera);
        for (Portal   p : portals)   p.draw(g, camera);
    }

    private void drawSky(Graphics2D g) {
        // 稍深的藍天（林間感）
        GradientPaint sky = new GradientPaint(
            0, 0,                    new Color(98, 172, 238),
            0, GamePanel.GAME_HEIGHT, new Color(172, 218, 252)
        );
        g.setPaint(sky);
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.GAME_HEIGHT);
    }

    private void drawBackground(Graphics2D g, Camera camera) {
        double cx = camera.getOffsetX();

        // 遠山（偏深綠）
        g.setColor(new Color(98, 158, 96));
        int[] mX = {0, 278, 558, 840, 1118, 1400, 1682};
        int[] mH = {132, 168, 146, 185, 154, 174, 150};
        for (int i = 0; i < mX.length; i++) {
            int hx = (int)(mX[i] - cx * 0.20);
            g.fillOval(hx, GROUND_Y - mH[i], 282, mH[i] * 2);
        }

        // 中景樹影（較深綠）
        g.setColor(new Color(48, 115, 52, 145));
        int[] tX = {55, 295, 575, 870, 1155, 1440, 1718};
        for (int tx : tX) {
            int hx = (int)(tx - cx * 0.50);
            g.fillOval(hx - 18, GROUND_Y - 102, 142, 178);
        }
    }

    // ── 介面實作 ─────────────────────────────────────────────
    @Override public List<Platform> getPlatforms() { return platforms; }
    @Override public List<Portal>   getPortals()   { return portals; }
    @Override public List<Ladder>   getLadders()   { return ladders; }
    @Override public int            getMapWidth()  { return MAP_WIDTH; }
    @Override public String         getMapId()     { return "novice2"; }
}
