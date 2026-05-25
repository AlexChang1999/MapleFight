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
    private final String  shopId;      // null = 無商店；"item" / "weapon" 等
    private final String  dialogueId;  // null = 無對話；"elder" 等

    private double     idleTimer  = 0;     // 靜待動畫計時
    private boolean    showHint   = false; // 是否顯示互動提示
    private final Appearance appearance;   // 由 bodyColor 衍生（供 CharacterSprite 使用）

    /** 無商店、無對話的 NPC */
    public NPC(double x, double y, String name, Color bodyColor, boolean facingRight) {
        this(x, y, name, bodyColor, facingRight, null, null);
    }

    /** 有商店的 NPC（無對話） */
    public NPC(double x, double y, String name, Color bodyColor, boolean facingRight,
               String shopId) {
        this(x, y, name, bodyColor, facingRight, shopId, null);
    }

    /** 完整建構子 */
    public NPC(double x, double y, String name, Color bodyColor, boolean facingRight,
               String shopId, String dialogueId) {
        this.x           = x;
        this.y           = y;
        this.name        = name;
        this.bodyColor   = bodyColor;
        this.facingRight = facingRight;
        this.shopId      = shopId;
        this.dialogueId  = dialogueId;
        this.appearance  = deriveAppearance(bodyColor);
    }

    /**
     * 從 NPC 身體色衍生外觀，與原 draw() 內的色彩公式完全一致：
     *   hair = bodyColor × ⅓ + 固定亮度偏移（偏深）
     *   eye  = bodyColor × ½ + 固定藍偏加成（偏淡）
     *   skin = 固定蜜桃膚 (255, 220, 178)
     */
    private static Appearance deriveAppearance(Color bc) {
        Color hair = new Color(
            Math.max(0, Math.min(255, bc.getRed()   / 3 + 18)),
            Math.max(0, Math.min(255, bc.getGreen() / 3 + 12)),
            Math.max(0, Math.min(255, bc.getBlue()  / 3 +  8)));
        Color eye = new Color(
            Math.min(255, bc.getRed()   / 2 + 55),
            Math.min(255, bc.getGreen() / 2 + 55),
            Math.min(255, bc.getBlue()  / 2 + 80));
        return new Appearance(
            Appearance.HairStyle.MUSHROOM_BOWL,
            hair,
            Appearance.EyeStyle.DEWY_BRIGHT,
            eye,
            Appearance.EyebrowStyle.HERO_THICK,
            new Color(255, 220, 178),
            new Color(255, 148, 128, 68));
    }

    public void update(double dt) {
        idleTimer += dt;
    }

    public void draw(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());
        int cx = sx + WIDTH / 2;

        // ── 呼吸節奏：頭部比身體晚 0.3 弧度（彈性擠壓感）───────
        double phase  = idleTimer * 1.6;
        int bodySy = sy + (int)(Math.sin(phase)        * 2.0);
        int headSy = sy + (int)(Math.sin(phase - 0.30) * 2.0);

        // ── 互動提示 & 名稱標籤（錨定在碰撞箱頂，不隨呼吸飄動）
        if (showHint && (shopId != null || dialogueId != null)) {
            String hintText = (shopId != null) ? "按 [F] 購物" : "按 [F] 對話";
            drawInteractHint(g, cx, sy - 30, hintText);
        }
        drawNameTag(g, cx, sy - 12);

        // ── 配色衍生（從 bodyColor 自動推算，無需額外欄位）──────
        final Color skin     = new Color(255, 220, 178);
        final Color outfit   = bodyColor;
        final Color pants    = new Color(
            Math.max(0, bodyColor.getRed()   - 45),
            Math.max(0, bodyColor.getGreen() - 55),
            Math.max(0, bodyColor.getBlue()  - 35));
        final Color boots   = new Color(88, 58, 32);
        final Color outline = new Color(18, 8, 4);
        // 頭髮 / 眼睛色已由建構子衍生至 this.appearance，交給 CharacterSprite 使用

        // ── 抗鋸齒 + 筆觸控制（與 CharacterSprite 一致）────────
        CharacterSprite.enableAA(g);

        // ════════ 繪製順序：身體層 → 頭部層 → 臉部層 ════════

        // ── 1. 上衣（22×18 圓角矩形，跟隨 bodySy）──────────
        int bodyTop = bodySy + 23;  // 頭高(28) − 頸部重疊(5) = 23
        g.setColor(outfit);
        g.fillRoundRect(cx - 11, bodyTop, 22, 18, 4, 4);
        g.setColor(new Color(255, 255, 255, 45));
        g.fillRoundRect(cx -  9, bodyTop + 2, 18, 6, 3, 3);
        g.setColor(new Color(0, 0, 0, 30));
        g.fillRoundRect(cx -  9, bodyTop + 12, 18, 5, 3, 3);
        g.setColor(outline);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(cx - 11, bodyTop, 22, 18, 4, 4);
        g.setStroke(new BasicStroke(1f));

        // ── 2. 手臂（粗圓潤筆觸，左右反相擺動）─────────────
        int armSwingL = (int)(Math.sin(phase + Math.PI) * 5);
        int armSwingR = (int)(Math.sin(phase)            * 5);
        // 黑邊輪廓
        g.setColor(outline);
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 5, bodySy + 27, cx - 14, bodySy + 37 + armSwingL);
        g.drawLine(cx + 5, bodySy + 27, cx + 14, bodySy + 37 - armSwingR);
        // 衣袖顏色
        g.setColor(outfit);
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 5, bodySy + 27, cx - 14, bodySy + 37 + armSwingL);
        g.drawLine(cx + 5, bodySy + 27, cx + 14, bodySy + 37 - armSwingR);
        g.setStroke(new BasicStroke(1f));
        // 小手（膚色圓球）
        g.setColor(outline);
        g.fillOval(cx - 19, bodySy + 33 + armSwingL, 9, 9);
        g.fillOval(cx + 11, bodySy + 33 - armSwingR, 9, 9);
        g.setColor(skin);
        g.fillOval(cx - 18, bodySy + 34 + armSwingL, 7, 7);
        g.fillOval(cx + 12, bodySy + 34 - armSwingR, 7, 7);

        // ── 3. 腿（粗輪廓線，跟隨 bodySy）──────────────────
        int legTop = bodySy + 41;
        int legBot = bodySy + 53;
        g.setColor(outline);
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 3, legTop, cx - 9, legBot);
        g.drawLine(cx + 3, legTop, cx + 9, legBot);
        g.setColor(pants);
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 3, legTop, cx - 9, legBot);
        g.drawLine(cx + 3, legTop, cx + 9, legBot);
        g.setStroke(new BasicStroke(1f));

        // ── 4. 靴子（梯形，跟隨 bodySy）────────────────────
        drawNpcBoot(g, cx - 9, bodySy + 53, boots, outline, !facingRight);
        drawNpcBoot(g, cx + 9, bodySy + 53, boots, outline,  facingRight);

        // ── 5-9. 大頭 + 蘑菇髮型 + 臉部 → CharacterSprite pipeline ───────
        // 外觀（hairColor / eyeColor / skinColor / blushColor）已在建構子由
        // bodyColor 衍生並存入 this.appearance，色彩公式與原繪製邏輯完全相同。
        CharacterSprite.drawHead(g, cx, headSy, appearance, facingRight);
        CharacterSprite.drawHair(g, cx, headSy, appearance, facingRight);
        CharacterSprite.drawFace(g, cx, headSy, appearance, facingRight, false, 0);
    }

    /** NPC 靴子：梯形 + 黑邊 + 鞋面高光 */
    private void drawNpcBoot(Graphics2D g, int fx, int fy,
                             Color color, Color outline, boolean toeRight) {
        int toe  = toeRight ? 5 : -5;
        int[] bx = {fx - 5 + toe, fx + 7 + toe, fx + 7, fx - 5};
        int[] by = {fy,            fy,            fy + 8,  fy + 8};
        g.setColor(outline);
        g.fillPolygon(new int[]{bx[0] - 1, bx[1] + 1, bx[2] + 1, bx[3] - 1},
                      new int[]{by[0] - 1, by[1] - 1, by[2] + 1, by[3] + 1}, 4);
        g.setColor(color);
        g.fillPolygon(bx, by, 4);
        g.setColor(new Color(255, 255, 255, 40));
        g.fillRect(Math.min(bx[0], bx[3]) + 1, fy + 1, 6, 3);
    }

    /** 互動提示氣泡（黃色） */
    private void drawInteractHint(Graphics2D g, int cx, int hintBottom, String text) {
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text);
        int bw = tw + 12, bh = 17;
        int bx = cx - bw / 2;
        int by = hintBottom - bh;

        g.setColor(new Color(200, 175, 30, 210));
        g.fillRoundRect(bx, by, bw, bh, 5, 5);
        g.setColor(new Color(255, 230, 80));
        g.drawRoundRect(bx, by, bw, bh, 5, 5);
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

    public double  getX()           { return x; }
    public double  getY()           { return y; }
    public String  getShopId()      { return shopId; }
    public boolean hasShop()        { return shopId != null; }
    public String  getDialogueId()  { return dialogueId; }
    public boolean hasDialogue()    { return dialogueId != null; }
    /** NPC 是否可互動（有商店或有對話） */
    public boolean isInteractable() { return shopId != null || dialogueId != null; }
}
