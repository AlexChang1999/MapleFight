package maplestory.map;

import maplestory.core.Camera;
import maplestory.core.GamePanel;
import maplestory.entity.NPC;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 叢林前哨站（junglepost）
 * 古老森林通往沙漠廢墟前的中繼村莊。
 *
 * 傳送門：
 *   左側 → 古老森林（無等級限制）
 *   右側 → 沙漠廢墟（Lv.30）
 */
public class JungleOutpost extends BaseMap {

    public static final int MAP_WIDTH = 1200;
    private static final int GROUND_Y = GamePanel.GAME_HEIGHT - 40;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Portal>   portals   = new ArrayList<>();
    private final List<NPC>      npcs      = new ArrayList<>();

    public JungleOutpost() {
        buildPlatforms();
        buildNPCs();
        buildPortals();
    }

    // ── 地形 ─────────────────────────────────────────────────
    private void buildPlatforms() {
        Color mossGround = new Color(45, 85, 30);
        Color woodPlank  = new Color(100, 68, 32);
        Color woodDark   = new Color(75, 50, 22);

        // 主地面（完整連續苔蘚地面）
        platforms.add(new Platform(0, GROUND_Y, MAP_WIDTH, 40, mossGround));

        // 左側木棧台（道具商旁）
        platforms.add(new Platform(300, GROUND_Y - 110, 200, 16, woodPlank));
        platforms.add(new Platform(150, GROUND_Y - 200, 160, 16, woodDark));

        // 中央木棧台（裝飾）
        platforms.add(new Platform(550, GROUND_Y - 130, 180, 16, woodPlank));

        // 右側木棧台（武器商旁）
        platforms.add(new Platform(800, GROUND_Y - 105, 200, 16, woodPlank));
        platforms.add(new Platform(950, GROUND_Y - 195, 150, 16, woodDark));

        // 地面裝飾小矮台
        platforms.add(new Platform(90,  GROUND_Y - 28, 50, 12, new Color(60, 100, 38)));
        platforms.add(new Platform(1060, GROUND_Y - 28, 50, 12, new Color(60, 100, 38)));
    }

    // ── NPC ──────────────────────────────────────────────────
    private void buildNPCs() {
        // 道具商人（道具補給）
        npcs.add(new NPC(400, GROUND_Y - NPC.HEIGHT,
                         "叢林補給商", new Color(60, 150, 70), false, "item", null));
        // 武器商人（Lv20-30 裝備）
        npcs.add(new NPC(750, GROUND_Y - NPC.HEIGHT,
                         "叢林鐵匠", new Color(140, 100, 50), true, "weapon", null));
    }

    // ── 傳送門 ───────────────────────────────────────────────
    private void buildPortals() {
        // 左側 → 古老森林（無限制，回頭路）
        portals.add(new Portal(
            22, GROUND_Y - Portal.HEIGHT,
            "forest", 2400, GROUND_Y - 80,
            "回古老森林", 1
        ));
        // 右側 → 沙漠廢墟（Lv.30）
        portals.add(new Portal(
            MAP_WIDTH - 68, GROUND_Y - Portal.HEIGHT,
            "desert", 60, GROUND_Y - 80,
            "沙漠廢墟(Lv.30)", 30
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
        // 溫暖叢林綠天空
        GradientPaint sky = new GradientPaint(
            0, 0,                     new Color(20, 60, 25),
            0, GamePanel.GAME_HEIGHT, new Color(40, 90, 35)
        );
        g.setPaint(sky);
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.GAME_HEIGHT);

        // 透光光暈（樹冠縫隙光）
        g.setColor(new Color(80, 180, 60, 25));
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, 80);
        g.setColor(new Color(50, 140, 40, 18));
        g.fillRect(0, 60, GamePanel.SCREEN_WIDTH, 60);
    }

    private void drawBackground(Graphics2D g, Camera camera) {
        double cx = camera.getOffsetX();

        // 遠景叢林樹冠剪影（parallax 0.18）
        g.setColor(new Color(18, 60, 15, 210));
        int[] farX = {0, 200, 400, 600, 800, 1000};
        int[] farH = {160, 200, 175, 210, 185, 195};
        for (int i = 0; i < farX.length; i++) {
            int hx = (int)(farX[i] - cx * 0.18);
            g.fillOval(hx - farH[i] / 4, GROUND_Y - farH[i], (int)(farH[i] * 0.9), farH[i]);
            g.fillOval(hx + farH[i] / 5, GROUND_Y - (int)(farH[i] * 1.15),
                       (int)(farH[i] * 0.7), (int)(farH[i] * 1.1));
        }

        // 中景叢林（parallax 0.38）
        g.setColor(new Color(28, 80, 22, 200));
        int[] midX = {80, 270, 460, 660, 870, 1100};
        int[] midH = {120, 150, 130, 160, 135, 145};
        for (int i = 0; i < midX.length; i++) {
            int hx = (int)(midX[i] - cx * 0.38);
            g.fillOval(hx, GROUND_Y - midH[i], (int)(midH[i] * 0.85), midH[i]);
        }

        // 木柵欄哨站剪影（中景）
        g.setColor(new Color(55, 38, 18, 190));
        int[] postX = {110, 420, 790, 1080};
        for (int px : postX) {
            int hx = (int)(px - cx * 0.45);
            // 哨站塔身
            g.fillRect(hx, GROUND_Y - 130, 28, 90);
            // 塔頂三角
            int[] triX = {hx - 6, hx + 14, hx + 34};
            int[] triY = {GROUND_Y - 130, GROUND_Y - 156, GROUND_Y - 130};
            g.fillPolygon(triX, triY, 3);
            // 木柵欄（哨站兩側各 3 根）
            for (int j = 0; j < 4; j++) {
                g.fillRect(hx - 18 + j * 8,  GROUND_Y - 65, 5, 25);
                g.fillRect(hx + 34 + j * 8, GROUND_Y - 65, 5, 25);
            }
        }

        // 旗幟（綠色）
        g.setColor(new Color(40, 160, 50, 170));
        for (int px : new int[]{110, 790}) {
            int hx = (int)(px - cx * 0.45);
            g.fillRect(hx + 28, GROUND_Y - 176, 20, 13);
        }
    }

    // ── 介面實作 ─────────────────────────────────────────────
    @Override public List<Platform> getPlatforms() { return platforms; }
    @Override public List<Portal>   getPortals()   { return portals; }
    @Override public List<NPC>      getNPCs()      { return npcs; }
    @Override public int            getMapWidth()  { return MAP_WIDTH; }
    @Override public String         getMapId()     { return "junglepost"; }
    @Override public String         getMapName()   { return "叢林前哨站"; }
    @Override public int            getMinLevel()  { return 18; }
}
