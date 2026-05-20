package maplestory.map;

import maplestory.core.Camera;
import maplestory.core.GamePanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 極地冰原地圖（第三張圖）。
 *
 * 特色：
 *   - 冰藍色系視覺（地面、平台）
 *   - 極光（Aurora Borealis）背景
 *   - 雪粒子系統
 *   - 梯子區塊（爬至高層平台）
 *   - 冰系怪物：冰晶史萊姆、極地熊、冰蝠
 *
 * 地圖結構（左 → 右）：
 *   地面層  →  梯子  →  中層冰台  →  梯子  →  高層冰台
 *   左邊有傳送門回到冒險平原
 */
public class ArcticMap extends BaseMap {

    public static final int MAP_WIDTH = 2400;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Portal>   portals   = new ArrayList<>();
    private final List<Ladder>   ladders   = new ArrayList<>();

    // 雪粒子系統
    private final List<double[]> snowParticles = new ArrayList<>(); // [x, y, speed, size, drift]
    private static final int SNOW_COUNT = 80;

    // ─────────────────────────────────────────────────────────
    public ArcticMap() {
        buildMap();
        initSnow();

        // 左側傳送門 → 回冒險平原（出生在右側平台附近）
        int gY = GamePanel.GAME_HEIGHT - 40;
        portals.add(new Portal(
            30, gY - Portal.HEIGHT,
            "battle", 1700, gY - 100,
            "回冒險平原"
        ));
    }

    // ─────────────────────────────────────────────────────────
    // 建構地圖
    // ─────────────────────────────────────────────────────────

    private void buildMap() {
        int gY   = GamePanel.GAME_HEIGHT - 40; // 地面 Y
        int midY = gY - 130;                   // 中層 Y
        int hiY  = gY - 270;                   // 高層 Y

        // ── 冰地面（三段，淡藍色）─────────────────────────────
        Color iceGround = new Color(160, 210, 240);
        platforms.add(new Platform(   0, gY, 560, 40, iceGround));
        platforms.add(new Platform( 600, gY, 700, 40, new Color(150, 200, 235)));
        platforms.add(new Platform(1360, gY, 500, 40, iceGround));
        platforms.add(new Platform(1920, gY, 480, 40, new Color(150, 200, 235)));

        // ── 中層浮台（冰磚感，深藍） ──────────────────────────
        Color iceMid = new Color(100, 170, 220);
        platforms.add(new Platform( 200, midY,        180, 18, iceMid));
        platforms.add(new Platform( 480, midY - 30,   180, 18, iceMid));
        platforms.add(new Platform( 780, midY,        200, 18, iceMid));
        platforms.add(new Platform(1060, midY - 30,   170, 18, iceMid));
        platforms.add(new Platform(1320, midY,        200, 18, iceMid));
        platforms.add(new Platform(1620, midY - 30,   180, 18, iceMid));
        platforms.add(new Platform(1900, midY,        200, 18, iceMid));
        platforms.add(new Platform(2160, midY - 30,   200, 18, iceMid));

        // ── 高層冰台（最深藍）────────────────────────────────
        Color iceHigh = new Color(65, 130, 190);
        platforms.add(new Platform( 350, hiY,       160, 18, iceHigh));
        platforms.add(new Platform( 680, hiY - 30,  160, 18, iceHigh));
        platforms.add(new Platform(1000, hiY,       170, 18, iceHigh));
        platforms.add(new Platform(1380, hiY - 30,  160, 18, iceHigh));
        platforms.add(new Platform(1750, hiY,       170, 18, iceHigh));
        platforms.add(new Platform(2100, hiY - 30,  200, 18, iceHigh));

        // ── 梯子（冰柱外觀，連接地面↔中層↔高層）──────────────
        Color icePost = new Color(130, 195, 240);
        Color iceRung = new Color(100, 170, 220);

        // 地面 → 中層
        ladders.add(new Ladder(330,  midY, gY,        icePost, iceRung));
        ladders.add(new Ladder(900,  midY, gY,        icePost, iceRung));
        ladders.add(new Ladder(1450, midY, gY,        icePost, iceRung));
        ladders.add(new Ladder(2050, midY, gY,        icePost, iceRung));

        // 中層 → 高層
        ladders.add(new Ladder(430,  hiY, midY - 28, icePost, iceRung));
        ladders.add(new Ladder(1060, hiY, midY - 30, icePost, iceRung));
        ladders.add(new Ladder(1660, hiY, midY - 28, icePost, iceRung));
        ladders.add(new Ladder(2180, hiY, midY - 30, icePost, iceRung));
    }

    // ─────────────────────────────────────────────────────────
    // 雪粒子初始化
    // ─────────────────────────────────────────────────────────

    private void initSnow() {
        Random rng = new Random(42);
        for (int i = 0; i < SNOW_COUNT; i++) {
            snowParticles.add(new double[]{
                rng.nextDouble() * MAP_WIDTH,             // x
                rng.nextDouble() * GamePanel.GAME_HEIGHT, // y
                40 + rng.nextDouble() * 60,               // 下落速度
                1 + rng.nextInt(3),                       // 大小（1-3px）
                (rng.nextDouble() - 0.5) * 20             // 水平漂移
            });
        }
    }

    // ─────────────────────────────────────────────────────────
    // 每幀更新
    // ─────────────────────────────────────────────────────────

    @Override
    public void update(double dt) {
        for (double[] p : snowParticles) {
            p[1] += p[2] * dt;     // 下落
            p[0] += p[4] * dt;     // 左右漂移
            // 超出螢幕底部 → 重置到頂部
            if (p[1] > GamePanel.GAME_HEIGHT + 5) {
                p[1] = -5;
                p[0] = Math.random() * MAP_WIDTH;
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // 繪製
    // ─────────────────────────────────────────────────────────

    @Override
    public void draw(Graphics2D g, Camera camera) {
        drawBackground(g, camera);
        drawLadders(g, camera);
        for (Platform p : platforms) p.draw(g, camera);
        drawSnow(g, camera);
        for (Portal p : portals) p.draw(g, camera);
    }

    /** 極地夜空 + 極光 */
    private void drawBackground(Graphics2D g, Camera camera) {
        double camX = camera.getOffsetX();

        // ── 夜空漸層（深藍到深靛藍）─────────────────────────
        GradientPaint sky = new GradientPaint(
            0, 0,                       new Color(5,  10, 30),
            0, GamePanel.GAME_HEIGHT,   new Color(15, 35, 70)
        );
        g.setPaint(sky);
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.GAME_HEIGHT);

        // ── 星星（固定圓點，隨機排佈） ──────────────────────
        drawStars(g, camX);

        // ── 極光（Aurora）波浪帶 ─────────────────────────────
        drawAurora(g, camX);

        // ── 遠景冰山（parallax 0.2） ─────────────────────────
        g.setColor(new Color(30, 70, 120, 200));
        drawIcebergs(g, camX * 0.2,
                     new int[]{ 0, 300, 600, 900, 1200},
                     new int[]{160, 200, 140, 180, 160});

        // ── 近景冰山（parallax 0.5） ─────────────────────────
        g.setColor(new Color(20, 55, 100, 220));
        drawIcebergs(g, camX * 0.5,
                     new int[]{100, 400, 700, 1000},
                     new int[]{110, 140, 100, 130});

        // ── 地面積雪感（漸層）───────────────────────────────
        int gY = GamePanel.GAME_HEIGHT - 40;
        GradientPaint snow = new GradientPaint(
            0, gY - 20, new Color(180, 220, 250, 100),
            0, gY,      new Color(160, 210, 240, 0)
        );
        g.setPaint(snow);
        g.fillRect(0, gY - 20, GamePanel.SCREEN_WIDTH, 20);
    }

    private void drawStars(Graphics2D g, double camX) {
        // 固定種子的星星位置（不隨 camX 移動 → 最遠視差）
        long[] seed = {3141, 2718, 1618, 9973, 4321, 7777, 1111, 8888, 2222, 5555,
                       6666, 9999, 1234, 5678, 3456, 7890, 2345, 6789, 4567, 8901};
        g.setColor(new Color(255, 255, 255, 200));
        for (int i = 0; i < seed.length; i++) {
            int sx = (int)(seed[i] % GamePanel.SCREEN_WIDTH);
            int sy = (int)((seed[i] * 7 + i * 137) % (GamePanel.GAME_HEIGHT - 80));
            g.fillOval(sx, sy, 2, 2);
        }
    }

    /** 三條正弦波極光帶（綠/青/紫） */
    private void drawAurora(Graphics2D g, double camX) {
        double t = System.currentTimeMillis() / 4000.0; // 緩慢飄動

        Color[] auroraColors = {
            new Color(30, 200, 120, 45),
            new Color(20, 180, 200, 35),
            new Color(120, 60, 220, 30)
        };
        double[] freqs  = {0.008, 0.006, 0.010};
        double[] amps   = {30, 25, 20};
        double[] yBases = {80, 120, 100};
        double[] phases = {0, Math.PI / 3, Math.PI * 2 / 3};

        for (int band = 0; band < 3; band++) {
            g.setColor(auroraColors[band]);
            int prevX = 0, prevY = (int) yBases[band];
            for (int px = 0; px <= GamePanel.SCREEN_WIDTH; px += 4) {
                double worldX = px + camX * 0.1; // 極慢視差
                int curY = (int)(yBases[band]
                         + Math.sin(worldX * freqs[band] + t + phases[band]) * amps[band]);
                // 填色帶（從 y 到 y+20）
                int[] xs = {prevX, px,    px,          prevX};
                int[] ys = {prevY, curY,  curY + 20,   prevY + 20};
                g.fillPolygon(xs, ys, 4);
                prevX = px; prevY = curY;
            }
        }
    }

    /** 畫一排三角形冰山 */
    private void drawIcebergs(Graphics2D g, double offsetX,
                               int[] baseX, int[] heights) {
        for (int i = 0; i < baseX.length; i++) {
            int ix = (int)(baseX[i] - offsetX);
            int h  = heights[i];
            int gY = GamePanel.GAME_HEIGHT - 40;
            // 主三角
            int[] xs = {ix, ix + 160, ix + 80};
            int[] ys = {gY, gY,       gY - h};
            g.fillPolygon(xs, ys, 3);
            // 次三角（稍高）
            int[] xs2 = {ix + 40, ix + 130, ix + 85};
            int[] ys2 = {gY,       gY,       gY - (int)(h * 1.15)};
            g.setColor(new Color(g.getColor().getRed(),
                                 g.getColor().getGreen(),
                                 g.getColor().getBlue(), 140));
            g.fillPolygon(xs2, ys2, 3);
            g.setColor(new Color(g.getColor().getRed(),
                                 g.getColor().getGreen(),
                                 g.getColor().getBlue(), 220));
        }
    }

    /** 繪製雪粒子（相對螢幕，但要抵消鏡頭位移） */
    private void drawSnow(Graphics2D g, Camera camera) {
        double camX = camera.getOffsetX();
        g.setColor(new Color(230, 245, 255, 200));
        for (double[] p : snowParticles) {
            int sx = (int)(p[0] - camX);
            int sy = (int) p[1];
            // 只畫在螢幕內的雪花
            if (sx < -5 || sx > GamePanel.SCREEN_WIDTH + 5) continue;
            int size = (int) p[3];
            g.fillOval(sx, sy, size, size);
        }
    }

    private void drawLadders(Graphics2D g, Camera camera) {
        for (Ladder lad : ladders) lad.draw(g, camera);
    }

    // ─────────────────────────────────────────────────────────
    // BaseMap 介面
    // ─────────────────────────────────────────────────────────

    @Override public List<Platform> getPlatforms() { return platforms; }
    @Override public List<Portal>   getPortals()   { return portals;   }
    @Override public List<Ladder>   getLadders()   { return ladders;   }
    @Override public int            getMapWidth()  { return MAP_WIDTH; }
    @Override public String         getMapId()     { return "arctic";  }
}
