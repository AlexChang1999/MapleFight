package maplestory.map;

import maplestory.core.Camera;
import maplestory.core.GamePanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 沙漠廢墟地圖（Lv30–39）。
 *
 * 特色：
 *   - 橙黃色系視覺（廢墟石柱、沙色地面、斷壁殘垣）
 *   - 多層視差背景（遠景沙丘 → 廢墟建築剪影）
 *   - 沙塵粒子系統（從右向左漂移）
 *   - Boss 觸發區在地圖右端（X ≥ 2100）
 *
 * 地圖流程：
 *   左邊傳送門 ← 古老森林   （右端為 Boss 戰鬥區，無傳送門）
 */
public class DesertMap extends BaseMap {

    public static final int MAP_WIDTH = 2800;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Portal>   portals   = new ArrayList<>();
    private final List<Ladder>   ladders   = new ArrayList<>();

    // 沙塵粒子
    private final List<double[]> sandDust = new ArrayList<>();
    private static final int DUST_COUNT = 70;

    // ── 顏色常數 ───────────────────────────────────────────────
    private static final Color SAND_GROUND = new Color(180, 145, 75);
    private static final Color RUIN_STONE  = new Color(150, 120, 60);
    private static final Color DARK_STONE  = new Color(110,  85, 40);
    private static final Color CARVED_PLAT = new Color(165, 130, 55);

    // ─────────────────────────────────────────────────────────
    public DesertMap() {
        buildMap();
        initDust();

        int gY = GamePanel.GAME_HEIGHT - 40;

        // 左側傳送門 → 回古老森林
        portals.add(new Portal(
            28, gY - Portal.HEIGHT,
            "junglepost", JungleOutpost.MAP_WIDTH - 130, gY - 80,
            "回叢林前哨站", 1
        ));
        // 右側無傳送門（Boss 戰鬥區，擊敗 Boss 後地圖保持開放）
    }

    // ─────────────────────────────────────────────────────────
    private void buildMap() {
        int gY   = GamePanel.GAME_HEIGHT - 40; // 地面 Y = 460
        int midY = gY - 140;                   // 中層 Y = 320
        int hiY  = gY - 285;                   // 高層 Y = 175

        // ── 地面（沙質分段）
        platforms.add(new Platform(   0, gY,  580, 40, SAND_GROUND));
        platforms.add(new Platform( 640, gY,  600, 40, new Color(170, 135, 65)));
        platforms.add(new Platform(1300, gY,  580, 40, SAND_GROUND));
        platforms.add(new Platform(1940, gY,  860, 40, new Color(160, 128, 60)));

        // ── 中層廢墟石台
        platforms.add(new Platform( 200, midY,        190, 22, RUIN_STONE));
        platforms.add(new Platform( 490, midY - 18,   180, 22, DARK_STONE));
        platforms.add(new Platform( 780, midY,        200, 22, RUIN_STONE));
        platforms.add(new Platform(1070, midY - 22,   180, 22, DARK_STONE));
        platforms.add(new Platform(1360, midY,        210, 22, RUIN_STONE));
        platforms.add(new Platform(1660, midY - 18,   190, 22, DARK_STONE));
        platforms.add(new Platform(1970, midY,        220, 22, RUIN_STONE));
        platforms.add(new Platform(2300, midY - 22,   240, 22, DARK_STONE));

        // ── 高層雕刻石台（通往 Boss 前的高層路徑）
        platforms.add(new Platform( 340, hiY,        170, 20, CARVED_PLAT));
        platforms.add(new Platform( 670, hiY - 22,   165, 20, DARK_STONE));
        platforms.add(new Platform(1010, hiY,        175, 20, CARVED_PLAT));
        platforms.add(new Platform(1390, hiY - 22,   170, 20, DARK_STONE));
        platforms.add(new Platform(1780, hiY,        180, 20, CARVED_PLAT));
        platforms.add(new Platform(2180, hiY - 22,   240, 20, new Color(130, 100, 45)));

        // ── 梯子（沙岩柱外觀）
        Color postC = new Color(140, 110, 50);
        Color rungC = new Color(160, 130, 65);

        // 地面 → 中層
        ladders.add(new Ladder( 350, midY, gY,         postC, rungC));
        ladders.add(new Ladder( 900, midY, gY,         postC, rungC));
        ladders.add(new Ladder(1510, midY, gY,         postC, rungC));
        ladders.add(new Ladder(2180, midY, gY,         postC, rungC));

        // 中層 → 高層
        ladders.add(new Ladder( 460, hiY, midY - 22,  postC, rungC));
        ladders.add(new Ladder(1120, hiY, midY - 22,  postC, rungC));
        ladders.add(new Ladder(1820, hiY, midY - 18,  postC, rungC));
        ladders.add(new Ladder(2420, hiY, midY - 22,  postC, rungC));
    }

    // ─────────────────────────────────────────────────────────
    private void initDust() {
        Random rng = new Random(42);
        for (int i = 0; i < DUST_COUNT; i++) {
            sandDust.add(new double[]{
                rng.nextDouble() * MAP_WIDTH,              // x
                rng.nextDouble() * GamePanel.GAME_HEIGHT,  // y
                30 + rng.nextDouble() * 55,                // speed (leftward)
                -5 + rng.nextDouble() * 10,                // vertical drift
                1.5 + rng.nextInt(4)                       // size
            });
        }
    }

    // ─────────────────────────────────────────────────────────
    @Override
    public void update(double dt) {
        for (double[] p : sandDust) {
            p[0] -= p[2] * dt; // 向左飄
            p[1] += p[3] * dt;
            if (p[0] < -8) {
                p[0] = MAP_WIDTH + 8;
                p[1] = Math.random() * GamePanel.GAME_HEIGHT;
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    @Override
    public void draw(Graphics2D g, Camera camera) {
        drawBackground(g, camera);
        drawRuinDecor(g, camera);
        drawLadders(g, camera);
        for (Platform p : platforms) p.draw(g, camera);
        drawDust(g, camera);
        for (Portal p : portals) p.draw(g, camera);
    }

    /** 沙漠廢墟背景（多層視差） */
    private void drawBackground(Graphics2D g, Camera camera) {
        double camX = camera.getOffsetX();

        // 天空（橙黃漸層，烈日感）
        GradientPaint sky = new GradientPaint(
            0, 0,                       new Color(40, 20, 8),
            0, GamePanel.GAME_HEIGHT,   new Color(80, 50, 18)
        );
        g.setPaint(sky);
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.GAME_HEIGHT);

        // 遠景沙丘輪廓（parallax 0.12）
        g.setColor(new Color(60, 40, 15, 180));
        drawDunes(g, camX * 0.12,
            new int[]{0, 250, 500, 750, 1000, 1250},
            new int[]{90, 120, 75, 105, 95, 115});

        // 中景廢墟剪影（parallax 0.3）
        g.setColor(new Color(50, 35, 12, 200));
        drawRuinSilhouette(g, camX * 0.3);

        // 近景沙丘（parallax 0.55）
        g.setColor(new Color(45, 32, 10, 190));
        drawDunes(g, camX * 0.55,
            new int[]{120, 380, 640, 900},
            new int[]{55, 75, 50, 70});

        // 地面沙塵積散漸層
        int gY = GamePanel.GAME_HEIGHT - 40;
        GradientPaint sandGlow = new GradientPaint(
            0, gY - 25, new Color(200, 160, 70, 90),
            0, gY,      new Color(150, 120, 55, 0)
        );
        g.setPaint(sandGlow);
        g.fillRect(0, gY - 25, GamePanel.SCREEN_WIDTH, 25);
    }

    /** 畫一排圓弧沙丘 */
    private void drawDunes(Graphics2D g, double offsetX, int[] baseX, int[] heights) {
        int gY = GamePanel.GAME_HEIGHT - 40;
        for (int i = 0; i < baseX.length; i++) {
            int dx = (int)(baseX[i] - offsetX);
            int h  = heights[i];
            g.fillArc(dx - h / 2, gY - h, (int)(h * 2.5), (int)(h * 1.8), 0, 180);
        }
    }

    /** 廢墟建築剪影（方形石塊 + 三角屋頂殘骸） */
    private void drawRuinSilhouette(Graphics2D g, double offsetX) {
        int gY = GamePanel.GAME_HEIGHT - 40;
        int[][] ruins = {
            {100, gY - 130, 50, 130},
            {300, gY - 100, 40, 100},
            {550, gY - 160, 60, 160},
            {800, gY - 110, 45, 110},
        };
        for (int[] r : ruins) {
            int rx = (int)(r[0] - offsetX);
            // 主體
            g.fillRect(rx, r[1], r[2], r[3]);
            // 破損三角頂
            int[] px = {rx - 5, rx + r[2] / 2, rx + r[2] + 5};
            int[] py = {r[1], r[1] - (int)(r[2] * 0.9), r[1]};
            g.fillPolygon(px, py, 3);
        }
    }

    /** 廢墟裝飾（石柱 + 刻紋）*/
    private void drawRuinDecor(Graphics2D g, Camera camera) {
        double camX = camera.getOffsetX();
        g.setColor(new Color(130, 100, 40, 130));
        g.setStroke(new BasicStroke(1.5f));

        // 地面的廢棄石柱殘骸
        int[] pillarX = {300, 650, 1000, 1400, 1800, 2250};
        int gY = GamePanel.GAME_HEIGHT - 40;
        for (int px : pillarX) {
            int sx = (int)(px - camX);
            if (sx < -60 || sx > GamePanel.SCREEN_WIDTH + 60) continue;
            int h = 50 + (int)(Math.sin(px * 0.01) * 20);
            // 柱身
            g.setColor(new Color(140, 108, 48, 160));
            g.fillRect(sx - 10, gY - h, 20, h);
            // 斷裂頂部鋸齒
            g.setColor(new Color(120,  90, 38));
            g.drawLine(sx - 10, gY - h, sx - 14, gY - h - 8);
            g.drawLine(sx - 3,  gY - h, sx - 2,  gY - h - 12);
            g.drawLine(sx + 5,  gY - h, sx + 7,  gY - h - 7);
            g.drawLine(sx + 10, gY - h, sx + 13, gY - h - 10);
            // 側面刻紋
            g.setColor(new Color(100, 75, 30, 90));
            for (int li = 0; li < 3; li++) {
                g.drawLine(sx - 10, gY - h + 12 + li * 12,
                           sx + 10, gY - h + 12 + li * 12);
            }
        }
        g.setStroke(new BasicStroke(1f));
    }

    private void drawDust(Graphics2D g, Camera camera) {
        double camX = camera.getOffsetX();
        g.setColor(new Color(200, 170, 100, 80));
        for (double[] p : sandDust) {
            int sx = (int)(p[0] - camX);
            int sy = (int) p[1];
            if (sx < -5 || sx > GamePanel.SCREEN_WIDTH + 5) continue;
            int size = (int) p[4];
            g.fillOval(sx, sy, size, (int)(size * 0.5));
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
    @Override public String         getMapId()     { return "desert";  }
    @Override public String         getMapName()   { return "沙漠廢墟"; }
    @Override public int            getMinLevel()  { return 30;        }
}
