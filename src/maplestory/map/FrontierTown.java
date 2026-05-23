package maplestory.map;

import maplestory.core.Camera;
import maplestory.core.GamePanel;
import maplestory.entity.NPC;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 前線前哨站（frontier）
 * 新手森林三區通往冒險平原前的中繼村莊。
 *
 * 傳送門：
 *   左側 → 新手森林三區（無等級限制）
 *   右側 → 冒險平原（Lv.10）
 */
public class FrontierTown extends BaseMap {

    public static final int MAP_WIDTH = 1600;
    private static final int GROUND_Y = GamePanel.GAME_HEIGHT - 40;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Portal>   portals   = new ArrayList<>();
    private final List<NPC>      npcs      = new ArrayList<>();

    public FrontierTown() {
        buildPlatforms();
        buildNPCs();
        buildPortals();
    }

    // ── 地形 ─────────────────────────────────────────────────
    private void buildPlatforms() {
        Color dirt  = new Color(115, 85, 50);
        Color stone = new Color(120, 110, 95);

        // 主地面（完整連續）
        platforms.add(new Platform(0, GROUND_Y, MAP_WIDTH, 40, dirt));

        // 中層木棧橋（中央哨崗）
        platforms.add(new Platform(580, GROUND_Y - 110, 200, 16, stone));
        platforms.add(new Platform(380, GROUND_Y - 190, 160, 16, new Color(100, 90, 75)));
        platforms.add(new Platform(780, GROUND_Y - 195, 160, 16, new Color(100, 90, 75)));

        // 右側高台
        platforms.add(new Platform(1140, GROUND_Y - 125, 180, 16, stone));
        platforms.add(new Platform(1300, GROUND_Y - 215, 150, 16, new Color(100, 90, 75)));

        // 左側小台階（裝飾）
        platforms.add(new Platform(200, GROUND_Y - 28, 50, 12, new Color(140, 110, 70)));
        platforms.add(new Platform(900, GROUND_Y - 28, 50, 12, new Color(140, 110, 70)));
    }

    // ── NPC ──────────────────────────────────────────────────
    private void buildNPCs() {
        // 前哨長官（對話型）
        npcs.add(new NPC(320, GROUND_Y - NPC.HEIGHT,
                         "前哨長官", new Color(180, 60, 50), true, null, "frontier_elder"));
        // 道具商人
        npcs.add(new NPC(680, GROUND_Y - NPC.HEIGHT,
                         "補給商人", new Color(60, 180, 140), false, "item", null));
        // 武器商人
        npcs.add(new NPC(1100, GROUND_Y - NPC.HEIGHT,
                         "武器鍛造師", new Color(180, 130, 50), true, "weapon", null));
    }

    // ── 傳送門 ───────────────────────────────────────────────
    private void buildPortals() {
        // 左側 → 新手森林三區
        portals.add(new Portal(
            22, GROUND_Y - Portal.HEIGHT,
            "novice3", NoviceMap3.MAP_WIDTH - 130, GROUND_Y - 80,
            "回三區", 1
        ));
        // 右側 → 冒險平原（Lv.10）
        portals.add(new Portal(
            MAP_WIDTH - 68, GROUND_Y - Portal.HEIGHT,
            "battle", 150, GROUND_Y - 80,
            "冒險平原(Lv.10)", 10
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
        for (Portal   p : portals)   p.draw(g, camera);
        for (NPC      n : npcs)      n.draw(g, camera);
    }

    private void drawSky(Graphics2D g) {
        // 黃昏橙天空（前線感）
        GradientPaint sky = new GradientPaint(
            0, 0,                    new Color(210, 140, 70),
            0, GamePanel.GAME_HEIGHT, new Color(240, 190, 120)
        );
        g.setPaint(sky);
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.GAME_HEIGHT);
    }

    private void drawBackground(Graphics2D g, Camera camera) {
        double cx = camera.getOffsetX();

        // 遠山輪廓（暗棕色）
        g.setColor(new Color(120, 80, 45, 200));
        int[] mX = {0, 280, 560, 840, 1120, 1400};
        int[] mH = {130, 165, 145, 180, 150, 170};
        for (int i = 0; i < mX.length; i++) {
            int hx = (int)(mX[i] - cx * 0.20);
            g.fillOval(hx, GROUND_Y - mH[i], 290, mH[i] * 2);
        }

        // 哨塔剪影（中景）
        g.setColor(new Color(60, 45, 28, 185));
        int[] tX = {130, 530, 960, 1420};
        for (int tx : tX) {
            int hx = (int)(tx - cx * 0.42);
            // 塔身
            g.fillRect(hx, GROUND_Y - 140, 30, 100);
            // 塔頂
            g.fillRect(hx - 8, GROUND_Y - 158, 46, 22);
        }

        // 旗幟（紅色）
        g.setColor(new Color(200, 50, 40, 160));
        for (int tx : new int[]{130, 530}) {
            int hx = (int)(tx - cx * 0.42);
            g.fillRect(hx + 30, GROUND_Y - 178, 22, 14);
        }
    }

    // ── 介面實作 ─────────────────────────────────────────────
    @Override public List<Platform> getPlatforms() { return platforms; }
    @Override public List<Portal>   getPortals()   { return portals; }
    @Override public List<NPC>      getNPCs()      { return npcs; }
    @Override public int            getMapWidth()  { return MAP_WIDTH; }
    @Override public String         getMapId()     { return "frontier"; }
    @Override public String         getMapName()   { return "前線前哨站"; }
    @Override public int            getMinLevel()  { return 10; }
}
