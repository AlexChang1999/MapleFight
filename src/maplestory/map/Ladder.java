package maplestory.map;

import maplestory.core.Camera;

import java.awt.*;

/**
 * 梯子（爬梯物件）。
 * 玩家按 ↑ / ↓ 時會進入爬梯模式，可垂直攀爬。
 *
 * 碰撞區域比視覺稍寬，方便玩家對準。
 * 繪製為木頭質感的雙柱梯子。
 */
public class Ladder {

    private static final int VIS_W = 16; // 視覺寬度
    private static final int HIT_W = 28; // 碰撞寬度（稍寬）

    private final int x;
    private final int topY;
    private final int botY;

    // 木頭顏色（可由地圖設定）
    private final Color colorPost;  // 柱子
    private final Color colorRung;  // 橫桿

    // ─────────────────────────────────────────────────────────
    public Ladder(int x, int topY, int botY) {
        this(x, topY, botY,
             new Color(160, 110, 55),
             new Color(130,  88, 40));
    }

    /** 自訂顏色（例如冰製梯子用藍色） */
    public Ladder(int x, int topY, int botY, Color post, Color rung) {
        this.x         = x;
        this.topY      = topY;
        this.botY      = botY;
        this.colorPost = post;
        this.colorRung = rung;
    }

    // ─────────────────────────────────────────────────────────
    // 幾何資料
    // ─────────────────────────────────────────────────────────

    /** 碰撞判定區域（玩家矩形與此相交 → 可爬） */
    public Rectangle getZone() {
        int hw = HIT_W / 2;
        return new Rectangle(x + VIS_W / 2 - hw, topY, HIT_W, botY - topY);
    }

    /** 梯子視覺中心 X（玩家爬梯時對齊用） */
    public int getCenterX() { return x + VIS_W / 2; }

    public int getTopY() { return topY; }
    public int getBotY() { return botY; }

    // ─────────────────────────────────────────────────────────
    // 繪製
    // ─────────────────────────────────────────────────────────

    public void draw(Graphics2D g, Camera camera) {
        int sx = x    - (int) camera.getOffsetX();
        int sy = topY - (int) camera.getOffsetY();
        int h  = botY - topY;

        // ── 兩條垂直柱子 ─────────────────────────────────────
        g.setColor(colorPost);
        g.setStroke(new BasicStroke(3.5f));
        g.drawLine(sx + 3,          sy, sx + 3,          sy + h);
        g.drawLine(sx + VIS_W - 3,  sy, sx + VIS_W - 3,  sy + h);

        // 柱子高光（左柱右側）
        g.setColor(new Color(colorPost.getRed() + 40,
                             colorPost.getGreen() + 30,
                             colorPost.getBlue() + 20, 160));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(sx + 5, sy, sx + 5, sy + h);

        // ── 橫桿（每 20px 一根）─────────────────────────────
        g.setColor(colorRung);
        g.setStroke(new BasicStroke(2.5f));
        for (int dy = 8; dy < h; dy += 20) {
            g.drawLine(sx + 1, sy + dy, sx + VIS_W - 1, sy + dy);
        }

        g.setStroke(new BasicStroke(1f));
    }
}
