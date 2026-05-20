package maplestory.entity;

import maplestory.core.Camera;

import java.awt.*;

/**
 * 村莊 NPC（火柴人造型）。
 * 有名稱標籤、呼吸起伏的靜待動畫、面向玩家方向。
 */
public class NPC {

    // NPC 碰撞箱尺寸（與 Player 類似）
    public static final int WIDTH  = 24;
    public static final int HEIGHT = 52;

    private final double x, y;
    private final String  name;
    private final Color   bodyColor;
    private final boolean facingRight;
    private final String  shopId;   // null = 無商店；"item" / "weapon" 等

    private double idleTimer   = 0;   // 靜待動畫計時
    private boolean showHint   = false; // 是否顯示「按 F 購物」提示

    /** 無商店的 NPC */
    public NPC(double x, double y, String name, Color bodyColor, boolean facingRight) {
        this(x, y, name, bodyColor, facingRight, null);
    }

    /** 有商店的 NPC */
    public NPC(double x, double y, String name, Color bodyColor, boolean facingRight,
               String shopId) {
        this.x           = x;
        this.y           = y;
        this.name        = name;
        this.bodyColor   = bodyColor;
        this.facingRight = facingRight;
        this.shopId      = shopId;
    }

    public void update(double dt) {
        idleTimer += dt;
    }

    public void draw(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());

        // 呼吸起伏：身體每 2 秒上下浮動 2px
        int bob = (int)(Math.sin(idleTimer * 1.6) * 2.0);
        sy += bob;

        int cx = sx + WIDTH / 2;

        // ── 互動提示（有商店且玩家接近時）────────────────────
        if (showHint && shopId != null) {
            drawInteractHint(g, cx, sy - 30);
        }

        // ── 名稱標籤 ─────────────────────────────────────────
        drawNameTag(g, cx, sy - 12);

        // ── 火柴人本體 ───────────────────────────────────────
        g.setStroke(new BasicStroke(2.0f));
        g.setColor(bodyColor);

        // 頭（帶輕微搖晃）
        int headSway = (int)(Math.sin(idleTimer * 0.7) * 1.2);
        g.drawOval(cx - 9 + headSway, sy, 18, 18);

        // 身體
        g.drawLine(cx, sy + 18, cx, sy + 36);

        // 手臂（靜待時自然下垂，帶輕微擺動）
        int armSway = (int)(Math.sin(idleTimer * 1.1) * 2);
        if (facingRight) {
            g.drawLine(cx, sy + 23, cx - 11, sy + 33 + armSway);
            g.drawLine(cx, sy + 23, cx + 11, sy + 31 - armSway);
        } else {
            g.drawLine(cx, sy + 23, cx - 11, sy + 31 - armSway);
            g.drawLine(cx, sy + 23, cx + 11, sy + 33 + armSway);
        }

        // 腳（靜態站立）
        g.drawLine(cx, sy + 36, cx - 8, sy + 52);
        g.drawLine(cx, sy + 36, cx + 8, sy + 52);

        g.setStroke(new BasicStroke(1f));
    }

    /** 按 F 互動提示（黃色氣泡） */
    private void drawInteractHint(Graphics2D g, int cx, int hintBottom) {
        String text = "按 [F] 購物";
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text);
        int bw = tw + 12, bh = 17;
        int bx = cx - bw / 2;
        int by = hintBottom - bh;

        // 黃色氣泡
        g.setColor(new Color(200, 175, 30, 210));
        g.fillRoundRect(bx, by, bw, bh, 5, 5);
        g.setColor(new Color(255, 230, 80));
        g.drawRoundRect(bx, by, bw, bh, 5, 5);
        // 文字
        g.setColor(new Color(30, 20, 0));
        g.drawString(text, cx - tw / 2, hintBottom - 3);
    }

    /** 繪製浮動在頭上的名稱標籤 */
    private void drawNameTag(Graphics2D g, int cx, int tagBottom) {
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int tw      = fm.stringWidth(name);
        int padding = 6;
        int bx      = cx - tw / 2 - padding;
        int bw      = tw + padding * 2;
        int bh      = 16;
        int by      = tagBottom - bh;

        // 標籤底板
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(bx, by, bw, bh, 6, 6);
        // 金色邊框
        g.setColor(new Color(220, 190, 100));
        g.drawRoundRect(bx, by, bw, bh, 6, 6);
        // 名字文字
        g.setColor(Color.YELLOW);
        g.drawString(name, cx - tw / 2, tagBottom - 3);
    }

    /** 設定「按 F 互動」提示是否顯示（由 GamePanel 每幀根據距離呼叫） */
    public void setShowHint(boolean show) { showHint = show; }

    /** 玩家中心是否在 range px 內 */
    public boolean isNearPlayer(double px, double py, double range) {
        double dx = (px + 12) - (x + WIDTH  / 2.0);
        double dy = (py + 29) - (y + HEIGHT / 2.0);
        return Math.abs(dx) < range && Math.abs(dy) < range * 2.0;
    }

    public double  getX()       { return x; }
    public double  getY()       { return y; }
    public String  getShopId()  { return shopId; }
    public boolean hasShop()    { return shopId != null; }
}
