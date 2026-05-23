package maplestory.map;

import maplestory.core.Camera;
import maplestory.core.GamePanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 古老森林地圖（Lv20–29）。
 *
 * 特色：
 *   - 深綠色系視覺（苔蘚地面、枯木平台、纏藤裝飾）
 *   - 多層次視差背景（遠景樹冠 → 中景深林 → 近景藤蔓）
 *   - 飄落葉片粒子系統
 *   - 梯子（攀爬至高層平台）
 *   - Boss 觸發區在地圖右端（X ≥ 1920）
 *
 * 地圖流程：
 *   左邊傳送門 ← 極地冰原   右邊傳送門 → 沙漠廢墟（Lv30）
 */
public class ForestMap extends BaseMap {

    public static final int MAP_WIDTH = 2600;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Portal>   portals   = new ArrayList<>();
    private final List<Ladder>   ladders   = new ArrayList<>();

    // 飄落葉片粒子
    private final List<double[]> leaves = new ArrayList<>();
    private static final int LEAF_COUNT = 55;

    // ── 顏色常數 ───────────────────────────────────────────────
    private static final Color MOSS_GROUND  = new Color(45,  90,  30);
    private static final Color BARK_DARK    = new Color(60,  40,  20);
    private static final Color BARK_MID     = new Color(80,  55,  28);
    private static final Color LEAF_PLAT    = new Color(40,  110, 35);

    // ─────────────────────────────────────────────────────────
    public ForestMap() {
        buildMap();
        initLeaves();

        int gY = GamePanel.GAME_HEIGHT - 40;

        // 左側傳送門 → 回極地冰原
        portals.add(new Portal(
            28, gY - Portal.HEIGHT,
            "arctic", ArcticMap.MAP_WIDTH - 130, gY - 80,
            "回極地冰原", 1
        ));

        // 右側傳送門 → 前往叢林前哨站（需 Lv18）
        portals.add(new Portal(
            MAP_WIDTH - 68, gY - Portal.HEIGHT,
            "junglepost", 60, gY - 80,
            "叢林前哨站(Lv.18)", 18
        ));
    }

    // ─────────────────────────────────────────────────────────
    private void buildMap() {
        int gY   = GamePanel.GAME_HEIGHT - 40; // 地面 Y = 460
        int midY = gY - 135;                   // 中層 Y = 325
        int hiY  = gY - 278;                   // 高層 Y = 182

        // ── 地面（苔蘚分段，帶深淺變化）
        platforms.add(new Platform(   0, gY,  550, 40, MOSS_GROUND));
        platforms.add(new Platform( 600, gY,  640, 40, new Color(50, 100, 35)));
        platforms.add(new Platform(1300, gY,  560, 40, MOSS_GROUND));
        platforms.add(new Platform(1920, gY,  680, 40, new Color(40,  85, 28)));

        // ── 中層木台（樹幹感）
        platforms.add(new Platform( 180, midY,        200, 20, BARK_MID));
        platforms.add(new Platform( 460, midY - 20,   185, 20, BARK_DARK));
        platforms.add(new Platform( 740, midY,        210, 20, BARK_MID));
        platforms.add(new Platform(1020, midY - 25,   180, 20, BARK_DARK));
        platforms.add(new Platform(1280, midY,        220, 20, BARK_MID));
        platforms.add(new Platform(1570, midY - 20,   190, 20, BARK_DARK));
        platforms.add(new Platform(1850, midY,        210, 20, BARK_MID));
        platforms.add(new Platform(2180, midY - 25,   220, 20, BARK_DARK));

        // ── 高層葉台（Boss 前置區）
        platforms.add(new Platform( 320, hiY,        165, 18, LEAF_PLAT));
        platforms.add(new Platform( 630, hiY - 25,   160, 18, new Color(35, 100, 28)));
        platforms.add(new Platform( 960, hiY,        175, 18, LEAF_PLAT));
        platforms.add(new Platform(1330, hiY - 25,   165, 18, new Color(35, 100, 28)));
        platforms.add(new Platform(1700, hiY,        175, 18, LEAF_PLAT));
        platforms.add(new Platform(2070, hiY - 25,   220, 18, new Color(35, 100, 28)));

        // ── 梯子（深褐木柱 + 橫木）
        Color postC = new Color(70, 48, 22);
        Color rungC = new Color(95, 65, 30);

        // 地面 → 中層
        ladders.add(new Ladder( 310, midY, gY,         postC, rungC));
        ladders.add(new Ladder( 850, midY, gY,         postC, rungC));
        ladders.add(new Ladder(1450, midY, gY,         postC, rungC));
        ladders.add(new Ladder(2100, midY, gY,         postC, rungC));

        // 中層 → 高層
        ladders.add(new Ladder( 420, hiY, midY - 20,  postC, rungC));
        ladders.add(new Ladder(1090, hiY, midY - 25,  postC, rungC));
        ladders.add(new Ladder(1760, hiY, midY - 20,  postC, rungC));
        ladders.add(new Ladder(2240, hiY, midY - 25,  postC, rungC));
    }

    // ─────────────────────────────────────────────────────────
    private void initLeaves() {
        Random rng = new Random(77);
        for (int i = 0; i < LEAF_COUNT; i++) {
            leaves.add(new double[]{
                rng.nextDouble() * MAP_WIDTH,              // x
                rng.nextDouble() * GamePanel.GAME_HEIGHT,  // y
                25 + rng.nextDouble() * 50,                // fall speed
                (rng.nextDouble() - 0.5) * 35,            // horizontal drift
                rng.nextDouble() * Math.PI * 2,            // rotation
                rng.nextDouble() * 2.0                     // rotation speed
            });
        }
    }

    // ─────────────────────────────────────────────────────────
    @Override
    public void update(double dt) {
        for (double[] lf : leaves) {
            lf[1] += lf[2] * dt;
            lf[0] += lf[3] * dt;
            lf[4] += lf[5] * dt;
            if (lf[1] > GamePanel.GAME_HEIGHT + 10) {
                lf[1] = -10;
                lf[0] = Math.random() * MAP_WIDTH;
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    @Override
    public void draw(Graphics2D g, Camera camera) {
        drawBackground(g, camera);
        drawLadders(g, camera);
        for (Platform p : platforms) p.draw(g, camera);
        drawVineDecor(g, camera);
        drawLeaves(g, camera);
        for (Portal p : portals) p.draw(g, camera);
    }

    /** 多層次森林背景 */
    private void drawBackground(Graphics2D g, Camera camera) {
        double camX = camera.getOffsetX();

        // 天空（深林透光，深藍綠漸層）
        GradientPaint sky = new GradientPaint(
            0, 0,                       new Color(8,  30, 18),
            0, GamePanel.GAME_HEIGHT,   new Color(15, 55, 25)
        );
        g.setPaint(sky);
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.GAME_HEIGHT);

        // 遠景樹冠（parallax 0.15）
        g.setColor(new Color(10, 45, 20, 200));
        drawTreeSilhouettes(g, camX * 0.15,
            new int[]{0, 220, 440, 660, 880, 1100},
            new int[]{180, 220, 160, 200, 175, 210});

        // 中景深林（parallax 0.35）
        g.setColor(new Color(15, 65, 28, 220));
        drawTreeSilhouettes(g, camX * 0.35,
            new int[]{80, 300, 520, 740, 960, 1200},
            new int[]{130, 160, 120, 155, 140, 165});

        // 近景樹幹（parallax 0.6）
        g.setColor(new Color(35, 55, 20, 180));
        drawTreeSilhouettes(g, camX * 0.6,
            new int[]{150, 380, 610, 840},
            new int[]{90, 110, 85, 100});

        // 地面苔蘚漸層光暈
        int gY = GamePanel.GAME_HEIGHT - 40;
        GradientPaint mossGlow = new GradientPaint(
            0, gY - 30, new Color(30, 90, 20, 80),
            0, gY,      new Color(20, 60, 12, 0)
        );
        g.setPaint(mossGlow);
        g.fillRect(0, gY - 30, GamePanel.SCREEN_WIDTH, 30);
    }

    /** 畫一排三角形樹冠剪影 */
    private void drawTreeSilhouettes(Graphics2D g, double offsetX,
                                      int[] baseX, int[] heights) {
        for (int i = 0; i < baseX.length; i++) {
            int tx = (int)(baseX[i] - offsetX);
            int h  = heights[i];
            int gY = GamePanel.GAME_HEIGHT - 40;
            // 主樹冠
            g.fillOval(tx - h / 3, gY - h, (int)(h * 0.85), h);
            // 次樹冠（偏右稍高）
            g.fillOval(tx + h / 5, gY - (int)(h * 1.12), (int)(h * 0.7), (int)(h * 1.1));
        }
    }

    /** 平台邊緣藤蔓裝飾 */
    private void drawVineDecor(Graphics2D g, Camera camera) {
        double camX = camera.getOffsetX();
        g.setColor(new Color(40, 120, 30, 160));
        g.setStroke(new BasicStroke(1.5f));
        // 中層平台邊緣藤蔓
        int[] vinePlatX = {180, 460, 740, 1020, 1280, 1570, 1850, 2180};
        int midY = GamePanel.GAME_HEIGHT - 40 - 135;
        for (int vx : vinePlatX) {
            int sx = (int)(vx - camX);
            if (sx < -80 || sx > GamePanel.SCREEN_WIDTH + 80) continue;
            for (int j = 0; j < 3; j++) {
                int voff = j * 22;
                g.drawLine(sx + voff + 10, midY + 20,
                           sx + voff + 6,  midY + 48);
                g.drawArc(sx + voff, midY + 42, 14, 10, 0, -180);
            }
        }
        g.setStroke(new BasicStroke(1f));
    }

    private void drawLeaves(Graphics2D g, Camera camera) {
        double camX = camera.getOffsetX();
        g.setStroke(new BasicStroke(1f));
        for (double[] lf : leaves) {
            int sx = (int)(lf[0] - camX);
            int sy = (int) lf[1];
            if (sx < -12 || sx > GamePanel.SCREEN_WIDTH + 12) continue;

            // 旋轉並畫橢圓葉片
            Graphics2D lg = (Graphics2D) g.create();
            lg.translate(sx, sy);
            lg.rotate(lf[4]);
            lg.setColor(new Color(50, 140, 30, 180));
            lg.fillOval(-5, -2, 10, 5);
            lg.setColor(new Color(35, 110, 22, 120));
            lg.drawOval(-5, -2, 10, 5);
            lg.dispose();
        }
    }

    private void drawLadders(Graphics2D g, Camera camera) {
        for (Ladder lad : ladders) lad.draw(g, camera);
    }

    // ─────────────────────────────────────────────────────────
    @Override public List<Platform> getPlatforms() { return platforms; }
    @Override public List<Portal>   getPortals()   { return portals;   }
    @Override public List<Ladder>   getLadders()   { return ladders;   }
    @Override public int            getMapWidth()  { return MAP_WIDTH; }
    @Override public String         getMapId()     { return "forest";  }
    @Override public String         getMapName()   { return "古老森林"; }
    @Override public int            getMinLevel()  { return 20;        }
}
