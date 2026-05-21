package maplestory.map;

import maplestory.core.Camera;
import maplestory.entity.Player;

import java.awt.*;

/**
 * 傳送門：放在地圖邊界，玩家碰到後切換到另一張地圖。
 * 帶有旋轉光暈動畫。
 */
public class Portal {

    public static final int WIDTH  = 38;
    public static final int HEIGHT = 78;

    private final int    x, y;
    private final String targetMapId; // 目標地圖 ID
    private final double spawnX;      // 玩家在目標地圖的出生 X
    private final double spawnY;      // 玩家在目標地圖的出生 Y
    private final String label;       // 顯示在傳送門上方的文字
    private final int    minLevel;    // 通過所需最低等級（1 = 無限制）

    private double animTimer = 0;

    /** 舊版建構子，minLevel 預設為 1（無限制） */
    public Portal(int x, int y, String targetMapId,
                  double spawnX, double spawnY, String label) {
        this(x, y, targetMapId, spawnX, spawnY, label, 1);
    }

    public Portal(int x, int y, String targetMapId,
                  double spawnX, double spawnY, String label, int minLevel) {
        this.x           = x;
        this.y           = y;
        this.targetMapId = targetMapId;
        this.spawnX      = spawnX;
        this.spawnY      = spawnY;
        this.label       = label;
        this.minLevel    = minLevel;
    }

    public void update(double dt) {
        animTimer += dt;
    }

    /** 檢查玩家碰撞箱是否與傳送門重疊 */
    public boolean collidesWith(Player player) {
        Rectangle portalBox = new Rectangle(x, y, WIDTH, HEIGHT);
        Rectangle playerBox = new Rectangle(
            (int) player.getX(), (int) player.getY(),
            Player.WIDTH, Player.HEIGHT);
        return portalBox.intersects(playerBox);
    }

    public void draw(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());
        int cx = sx + WIDTH / 2;
        int cy = sy + HEIGHT / 2;

        float pulse = (float)(0.65 + 0.35 * Math.sin(animTimer * 3.0));

        // ── 外層光暈（層層疊加，越外越透明）────────────────────
        for (int i = 6; i >= 1; i--) {
            float alpha = pulse * (i / 7.0f) * 0.25f;
            g.setColor(new Color(0.45f, 0.0f, 0.9f, alpha));
            g.fillOval(sx - i * 3, sy - i * 2,
                       WIDTH + i * 6, HEIGHT + i * 4);
        }

        // ── 傳送門主體橢圓 ───────────────────────────────────
        g.setColor(new Color(60, 0, 160));
        g.fillOval(sx, sy, WIDTH, HEIGHT);

        // ── 內部旋轉光點（三顆繞圈） ─────────────────────────
        g.setColor(new Color(180, 100, 255, 220));
        for (int i = 0; i < 3; i++) {
            double angle = animTimer * 2.5 + i * Math.PI * 2.0 / 3.0;
            int px = cx + (int)(11 * Math.cos(angle));
            int py = cy + (int)(20 * Math.sin(angle));
            g.fillOval(px - 5, py - 5, 10, 10);
        }

        // ── 中心亮點 ─────────────────────────────────────────
        g.setColor(new Color(220, 180, 255, (int)(150 * pulse)));
        g.fillOval(cx - 5, cy - 8, 10, 16);

        // ── 上方標籤 ─────────────────────────────────────────
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        g.setColor(new Color(230, 210, 255));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, sx + (WIDTH - fm.stringWidth(label)) / 2, sy - 5);
    }

    // ── Getter ───────────────────────────────────────────────
    public String getTargetMapId() { return targetMapId; }
    public double getSpawnX()      { return spawnX; }
    public double getSpawnY()      { return spawnY; }
    public int    getMinLevel()    { return minLevel; }
}
