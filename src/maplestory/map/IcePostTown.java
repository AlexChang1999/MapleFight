package maplestory.map;

import maplestory.core.Camera;
import maplestory.core.GamePanel;
import maplestory.entity.NPC;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 冰原驛站（icepost）
 * 冒險平原通往極地冰原前的中繼村莊。
 *
 * 傳送門：
 *   左側 → 冒險平原（無等級限制）
 *   右側 → 極地冰原（Lv.15）
 */
public class IcePostTown extends BaseMap {

    public static final int MAP_WIDTH = 1600;
    private static final int GROUND_Y = GamePanel.GAME_HEIGHT - 40;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Portal>   portals   = new ArrayList<>();
    private final List<NPC>      npcs      = new ArrayList<>();

    // 雪粒子（輕量版）
    private final double[][] snow;
    private static final int SNOW_COUNT = 40;

    public IcePostTown() {
        buildPlatforms();
        buildNPCs();
        buildPortals();

        // 初始化雪花
        Random rng = new Random(42);
        snow = new double[SNOW_COUNT][4]; // [x, y, speed, drift]
        for (int i = 0; i < SNOW_COUNT; i++) {
            snow[i][0] = rng.nextDouble() * MAP_WIDTH;
            snow[i][1] = rng.nextDouble() * GamePanel.GAME_HEIGHT;
            snow[i][2] = 18 + rng.nextDouble() * 22;
            snow[i][3] = (rng.nextDouble() - 0.5) * 12;
        }
    }

    // ── 地形 ─────────────────────────────────────────────────
    private void buildPlatforms() {
        Color ice   = new Color(140, 190, 220);
        Color snow  = new Color(200, 225, 245);
        Color stone = new Color(110, 130, 155);

        // 主地面（冰雪地面）
        platforms.add(new Platform(0, GROUND_Y, MAP_WIDTH, 40, ice));

        // 中層冰台
        platforms.add(new Platform(420, GROUND_Y - 100, 210, 16, snow));
        platforms.add(new Platform(820, GROUND_Y - 115, 190, 16, snow));
        platforms.add(new Platform(1160, GROUND_Y - 100, 200, 16, snow));

        // 高層石台
        platforms.add(new Platform(550, GROUND_Y - 200, 155, 16, stone));
        platforms.add(new Platform(960, GROUND_Y - 210, 155, 16, stone));

        // 小裝飾台
        platforms.add(new Platform(220, GROUND_Y - 28, 55, 12, new Color(160, 195, 225)));
        platforms.add(new Platform(1020, GROUND_Y - 28, 55, 12, new Color(160, 195, 225)));
    }

    // ── NPC ──────────────────────────────────────────────────
    private void buildNPCs() {
        // 道具商人（賣高階回復藥）
        npcs.add(new NPC(560, GROUND_Y - NPC.HEIGHT,
                         "冰原補給商", new Color(80, 160, 200), false, "item", null));
        // 武器商人
        npcs.add(new NPC(1050, GROUND_Y - NPC.HEIGHT,
                         "極地鐵匠", new Color(130, 160, 190), true, "weapon", null));
    }

    // ── 傳送門 ───────────────────────────────────────────────
    private void buildPortals() {
        // 左側 → 冒險平原（無限制）
        portals.add(new Portal(
            22, GROUND_Y - Portal.HEIGHT,
            "battle", GameMap.MAP_WIDTH - 130, GROUND_Y - 80,
            "回冒險平原", 1
        ));
        // 右側 → 極地冰原（Lv.15）
        portals.add(new Portal(
            MAP_WIDTH - 68, GROUND_Y - Portal.HEIGHT,
            "arctic", 120, GROUND_Y - 90,
            "極地冰原(Lv.15)", 15
        ));
    }

    // ── 更新 ─────────────────────────────────────────────────
    @Override
    public void update(double dt) {
        for (double[] s : snow) {
            s[1] += s[2] * dt;
            s[0] += s[3] * dt;
            if (s[1] > GamePanel.GAME_HEIGHT) { s[1] = -4; s[0] = new Random().nextDouble() * MAP_WIDTH; }
            if (s[0] < 0) s[0] = MAP_WIDTH;
            if (s[0] > MAP_WIDTH) s[0] = 0;
        }
    }

    // ── 繪製 ─────────────────────────────────────────────────
    @Override
    public void draw(Graphics2D g, Camera camera) {
        drawSky(g);
        drawBackground(g, camera);
        for (Platform p : platforms) p.draw(g, camera);
        for (Portal   p : portals)   p.draw(g, camera);
        for (NPC      n : npcs)      n.draw(g, camera);
        drawSnow(g, camera);
    }

    private void drawSky(Graphics2D g) {
        // 深藍冰夜天空
        GradientPaint sky = new GradientPaint(
            0, 0,                    new Color(18, 28, 68),
            0, GamePanel.GAME_HEIGHT, new Color(38, 58, 112)
        );
        g.setPaint(sky);
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.GAME_HEIGHT);

        // 極光（靜態色帶）
        g.setColor(new Color(50, 180, 140, 40));
        g.fillRect(0, 30, GamePanel.SCREEN_WIDTH, 60);
        g.setColor(new Color(80, 140, 200, 30));
        g.fillRect(0, 70, GamePanel.SCREEN_WIDTH, 50);
    }

    private void drawBackground(Graphics2D g, Camera camera) {
        double cx = camera.getOffsetX();
        // 冰山輪廓
        g.setColor(new Color(80, 110, 160, 200));
        int[] mX = {0, 300, 600, 900, 1200, 1500};
        int[] mH = {145, 185, 160, 195, 165, 180};
        for (int i = 0; i < mX.length; i++) {
            int hx = (int)(mX[i] - cx * 0.18);
            g.fillOval(hx, GROUND_Y - mH[i], 310, mH[i] * 2);
        }
    }

    private void drawSnow(Graphics2D g, Camera camera) {
        double cx = camera.getOffsetX();
        g.setColor(new Color(240, 248, 255, 200));
        for (double[] s : snow) {
            int sx = (int)(s[0] - cx * 0.05);
            int sy = (int)s[1];
            g.fillOval(sx, sy, 3, 3);
        }
    }

    // ── 介面實作 ─────────────────────────────────────────────
    @Override public List<Platform> getPlatforms() { return platforms; }
    @Override public List<Portal>   getPortals()   { return portals; }
    @Override public List<NPC>      getNPCs()      { return npcs; }
    @Override public int            getMapWidth()  { return MAP_WIDTH; }
    @Override public String         getMapId()     { return "icepost"; }
    @Override public String         getMapName()   { return "冰原驛站"; }
    @Override public int            getMinLevel()  { return 15; }
}
