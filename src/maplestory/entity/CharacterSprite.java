package maplestory.entity;

import maplestory.item.Equipment;
import maplestory.item.EquipSlot;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * CharacterSprite — 楓之谷風格角色繪製模組
 *
 * 【設計原則】
 *  - 與 Player 邏輯完全分離，只負責「畫」
 *  - 所有尺寸常數集中在頂部，一處修改全局生效
 *  - 每個部位獨立 static 方法，方便個別替換
 *  - 使用楓之谷特色：黑色輪廓線 + 抗鋸齒
 *
 * 【擴充方式】
 *  - 新增髮型：在 Appearance.HairStyle 加值 → 在 drawHair() 加 case
 *  - 新增眼型：在 Appearance.EyeStyle 加值 → 在 drawNormalEye() 加 case
 *  - 新增眉型：在 Appearance.EyebrowStyle 加值 → 在 drawEyebrow() 加 case
 *  - 自訂部位：直接覆寫對應 static 方法（可在子類呼叫不同實作）
 */
public class CharacterSprite {

    // ══════════════════════════════════════════════════════════
    // 比例常數（楓之谷 chibi 風格）
    // 修改這裡即可整體調整比例，不需到處改
    // ══════════════════════════════════════════════════════════

    /** 頭寬（圓角矩形，比原本 oval 更有楓之谷方臉感） */
    public static final int HEAD_W = 26;
    /** 頭高 */
    public static final int HEAD_H = 28;
    /** 身體寬（比頭稍窄，符合 chibi 比例） */
    public static final int BODY_W = 22;
    /** 身體高 */
    public static final int BODY_H = 18;
    /** 腿長 */
    public static final int LEG_LEN = 12;
    /** 靴子高 */
    public static final int BOOT_H = 8;

    // 各部位起始 Y（相對於 sy = 角色頂部）
    /** 身體起點（與頭部有 5px 頸部重疊，讓連接自然） */
    public static final int BODY_Y = HEAD_H - 5;   // 23
    /** 腿部起點 */
    public static final int LEG_Y  = BODY_Y + BODY_H; // 41
    /** 靴子起點 */
    public static final int BOOT_Y = LEG_Y + LEG_LEN; // 53

    // 臉部比例（相對於 sy）
    /** 眉毛 Y */
    public static final int BROW_Y  = 9;
    /** 眼睛上緣 Y — 距眉毛留 3px，呼吸感更自然 */
    public static final int EYE_Y   = 12;
    /** 眼白寬（chibi 大眼，約佔臉寬 46%） */
    public static final int EYE_W   = 12;
    /** 眼白高（比寬略高，楓之谷大眼萌感） */
    public static final int EYE_H   = 13;
    /** 嘴巴 Y — 距眼底保留 1px 鼻子暗示空間 */
    public static final int MOUTH_Y = 26;

    /** 眼睛水平錨點偏移（面右 +3，面左 -3） */
    public static final int EYE_SHIFT = 3;

    /** 楓之谷標誌黑邊色 */
    public static final Color OUTLINE = new Color(18, 8, 4);

    // ══════════════════════════════════════════════════════════
    // 主繪製入口
    // ══════════════════════════════════════════════════════════

    /**
     * 繪製角色（不含攻擊手臂動畫，那部分保留在 Player）。
     * Player.draw() 只需呼叫此方法的各部位段落即可。
     */
    public static void enableAA(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                           RenderingHints.VALUE_STROKE_PURE);
    }

    // ══════════════════════════════════════════════════════════
    // 頭部
    // ══════════════════════════════════════════════════════════

    /**
     * 楓之谷大頭：圓角矩形（非橢圓）+ 輪廓線 + 側面陰影 + 臉頰紅暈。
     * 外框先填黑色（擴大 1px）→ 再疊膚色，產生黑邊效果。
     */
    public static void drawHead(Graphics2D g, int cx, int sy,
                                Appearance app, boolean fr) {
        int hx = cx - HEAD_W / 2;
        Color skin = app.skinColor;

        // 1. 膚色本體
        g.setColor(skin);
        g.fillRoundRect(hx, sy, HEAD_W, HEAD_H, 11, 11);

        // 1b. 頭頂受光帶（頂部圓弧高光，模擬半球受頂光感）
        g.setColor(new Color(255, 255, 255, 38));
        g.fillRoundRect(hx + 3, sy + 2, HEAD_W - 6, HEAD_H / 4, 5, 5);

        // 2. 側面暗影（面向背面那側稍暗）
        Color shadow = new Color(
            Math.max(0, skin.getRed()   - 22),
            Math.max(0, skin.getGreen() - 28),
            Math.max(0, skin.getBlue()  - 22), 80);
        int shdX = fr ? hx + HEAD_W * 6 / 10 : hx;
        int shdW = HEAD_W * 4 / 10;
        g.setColor(shadow);
        g.fillRoundRect(shdX, sy + 5, shdW, HEAD_H - 10, 5, 5);

        // 3. 臉頰紅暈（外層大柔化暈 + 內層集中色，模擬動畫彩色擴散暈）
        int blushX = fr ? hx + HEAD_W / 2 + 1 : hx + 2;
        g.setColor(new Color(app.blushColor.getRed(), app.blushColor.getGreen(),
                             app.blushColor.getBlue(), Math.max(0, app.blushColor.getAlpha() / 3)));
        g.fillOval(blushX - 1, sy + MOUTH_Y - 6, 12, 7);
        g.setColor(app.blushColor);
        g.fillOval(blushX, sy + MOUTH_Y - 4, 10, 5);

        // 4. 柔化輪廓線（stroke 最後畫，AA 邊緣自然羽化，比 fill-expand 更圓潤）
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(hx, sy, HEAD_W, HEAD_H, 11, 11);
        g.setStroke(new BasicStroke(1f));
    }

    // ══════════════════════════════════════════════════════════
    // 髮型
    // ══════════════════════════════════════════════════════════

    /**
     * 髮型分派器：根據 HairStyle 呼叫對應繪製方法。
     * 新增髮型只需：① Appearance.HairStyle 加 enum 值
     *             ② 這裡加 case → 實作私有方法即可
     */
    public static void drawHair(Graphics2D g, int cx, int sy,
                                Appearance app, boolean fr) {
        Color hc  = app.hairColor;
        Color hcd = new Color(
            Math.max(0, hc.getRed()   - 35),
            Math.max(0, hc.getGreen() - 35),
            Math.max(0, hc.getBlue()  - 35));

        g.setStroke(new BasicStroke(1f));
        switch (app.hairStyle) {
            case FLUFFY_SPIKE    -> hairSpiky   (g, cx, sy, hc, hcd, fr);
            case PIXEL_SHORT     -> hairShort   (g, cx, sy, hc, hcd);
            case FEATHER_STRAIGHT-> hairStraight(g, cx, sy, hc, hcd, fr);
            case MUSHROOM_BOWL   -> hairBowl    (g, cx, sy, hc, hcd);
            case PRINCESS_LONG   -> hairLong    (g, cx, sy, hc, hcd, fr);
        }
        g.setStroke(new BasicStroke(1f));
    }

    /** 刺刺頭：三根高低錯落尖刺 + 圓弧髮根 + 後腦側發 */
    private static void hairSpiky(Graphics2D g, int cx, int sy,
                                  Color hc, Color hcd, boolean fr) {
        int hx = cx - HEAD_W / 2;
        // 三根尖刺座標（統一宣告，fill + stroke 各跑一次）
        int[][] spikes = {
            {cx - 12, sy + 3,  cx - 6, sy - 14, cx - 1,  sy + 2},
            {cx -  3, sy + 1,  cx + 1, sy - 18, cx + 6,  sy + 1},
            {cx +  3, sy + 3,  cx + 9, sy - 12, cx + 12, sy + 3}
        };
        // 髮根底座 + 尖刺填色
        g.setColor(hc);
        g.fillArc(hx - 1, sy - 2, HEAD_W + 2, HEAD_H / 2 + 2, 0, 180);
        for (int[] s : spikes)
            g.fillPolygon(new int[]{s[0], s[2], s[4]},
                          new int[]{s[1], s[3], s[5]}, 3);
        // 柔化輪廓線（一次 setStroke，統一描全部形狀）
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(hx - 1, sy - 2, HEAD_W + 2, HEAD_H / 2 + 2, 0, 180);
        for (int[] s : spikes)
            g.drawPolygon(new int[]{s[0], s[2], s[4]},
                          new int[]{s[1], s[3], s[5]}, 3);
        g.setStroke(new BasicStroke(1f));

        // 後腦側發絲
        int bx = fr ? hx : cx + HEAD_W / 2;
        int bd = fr ? -1 : 1;
        g.setColor(hc);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(bx, sy + 6, bx + bd * 6, sy + 16);
        g.setStroke(new BasicStroke(1f));
    }

    /** 短刺頭：兩根矮刺 + 低弧髮根 */
    private static void hairShort(Graphics2D g, int cx, int sy,
                                  Color hc, Color hcd) {
        int hx = cx - HEAD_W / 2;
        int[][] spikes = {
            {cx - 10, sy + 3, cx - 4, sy - 9,  cx,      sy + 2},
            {cx +  1, sy + 2, cx + 6, sy - 8,  cx + 11, sy + 3}
        };
        g.setColor(hc);
        g.fillArc(hx - 1, sy - 1, HEAD_W + 2, HEAD_H / 2 + 2, 0, 180);
        for (int[] s : spikes)
            g.fillPolygon(new int[]{s[0],s[2],s[4]}, new int[]{s[1],s[3],s[5]}, 3);
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(hx - 1, sy - 1, HEAD_W + 2, HEAD_H / 2 + 2, 0, 180);
        for (int[] s : spikes)
            g.drawPolygon(new int[]{s[0],s[2],s[4]}, new int[]{s[1],s[3],s[5]}, 3);
        g.setStroke(new BasicStroke(1f));
    }

    /** 直髮蓋額：圓頂 + 兩側垂髮 + 瀏海 */
    private static void hairStraight(Graphics2D g, int cx, int sy,
                                     Color hc, Color hcd, boolean fr) {
        int hx = cx - HEAD_W / 2;
        // 髮色填充
        g.setColor(hc);
        g.fillRoundRect(hx - 1, sy - 3, HEAD_W + 2, HEAD_H / 2 + 2, 11, 11);
        g.fillRect(hx - 1, sy + 6, 8, 18);
        g.fillRect(cx + HEAD_W / 2 - 7, sy + 6, 8, 18);
        // 瀏海（朝臉部側偏移）
        int fx = fr ? hx + 1 : cx + 1;
        g.fillRoundRect(fx, sy + 9, 9, 10, 4, 4);
        // 柔化輪廓線
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(hx - 1, sy - 3, HEAD_W + 2, HEAD_H / 2 + 2, 11, 11);
        g.drawRect(hx - 1, sy + 6, 8, 18);
        g.drawRect(cx + HEAD_W / 2 - 7, sy + 6, 8, 18);
        g.drawRoundRect(fx, sy + 9, 9, 10, 4, 4);
        g.setStroke(new BasicStroke(1f));
    }

    /** 西瓜頭：碗狀弧形覆蓋 */
    private static void hairBowl(Graphics2D g, int cx, int sy,
                                 Color hc, Color hcd) {
        int hx = cx - HEAD_W / 2;
        g.setColor(hc);
        g.fillArc(hx - 1, sy - 3, HEAD_W + 2, HEAD_H / 2 + 8, 0, 180);
        g.fillRect(hx - 1, sy + 11, HEAD_W + 2, 4);
        // 西瓜髮線（底端切線，帶暗色）
        g.setColor(hcd);
        g.setStroke(new BasicStroke(1.2f));
        g.drawLine(hx - 1, sy + 14, cx + HEAD_W / 2 + 1, sy + 14);
        // 柔化輪廓線
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(hx - 1, sy - 3, HEAD_W + 2, HEAD_H / 2 + 8, 0, 180);
        g.drawRect(hx - 1, sy + 11, HEAD_W + 2, 4);
        g.setStroke(new BasicStroke(1f));
    }

    /** 長髮：圓頂 + 垂肩長側發（覆蓋肩部上衣） */
    private static void hairLong(Graphics2D g, int cx, int sy,
                                 Color hc, Color hcd, boolean fr) {
        int hx = cx - HEAD_W / 2;
        // 髮色填充
        g.setColor(hc);
        g.fillRoundRect(hx - 1, sy - 3, HEAD_W + 2, HEAD_H / 2 + 4, 11, 11);
        g.fillRect(hx - 1, sy + 5, 8, 36);
        g.fillRect(cx + HEAD_W / 2 - 7, sy + 5, 8, 36);
        // 瀏海
        int fx = fr ? hx + 1 : cx + 1;
        g.fillRoundRect(fx, sy + 9, 9, 10, 4, 4);
        // 長髮底端收尾（暗色）
        g.setColor(hcd);
        g.drawLine(hx - 1, sy + 41, cx - 5,            sy + 41);
        g.drawLine(cx + HEAD_W / 2 + 1, sy + 41, cx + 5, sy + 41);
        // 柔化輪廓線
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(hx - 1, sy - 3, HEAD_W + 2, HEAD_H / 2 + 4, 11, 11);
        g.drawRect(hx - 1, sy + 5, 8, 36);
        g.drawRect(cx + HEAD_W / 2 - 7, sy + 5, 8, 36);
        g.drawRoundRect(fx, sy + 9, 9, 10, 4, 4);
        g.setStroke(new BasicStroke(1f));
    }

    // ══════════════════════════════════════════════════════════
    // 臉部（眉毛 + 眼睛 + 嘴）
    // ══════════════════════════════════════════════════════════

    /**
     * 臉部總繪製：依狀態選擇正常/攻擊/受傷表情。
     * 所有狀態都有眉毛。
     */
    public static void drawFace(Graphics2D g, int cx, int sy,
                                Appearance app, boolean fr,
                                boolean attacking, double hurtTimer) {
        int ex = cx + (fr ? EYE_SHIFT : -EYE_SHIFT);
        int ey = sy + EYE_Y;

        if (hurtTimer > 0) {
            // ── 受傷：X 眼 + 紅色下彎嘴 + 下垂眉 ──────────────
            drawEyebrow(g, sy, ex, app, fr, true, false);
            g.setColor(OUTLINE);
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(ex - 4, ey,     ex + 4, ey + 6);
            g.drawLine(ex - 4, ey + 6, ex + 4, ey);
            g.setColor(new Color(200, 30, 30));
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawArc(cx - 4, sy + MOUTH_Y, 9, 5, 0, -180);

        } else if (attacking) {
            // ── 攻擊：橫向縮瞳 + 咬牙線 + 皺眉 ────────────────
            drawEyebrow(g, sy, ex, app, fr, false, true);
            g.setColor(Color.WHITE);
            g.fillOval(ex - EYE_W / 2, ey + 3, EYE_W, EYE_H - 5);
            g.setColor(app.eyeColor.darker());
            g.fillOval(ex - EYE_W / 2 + 1, ey + 4, EYE_W - 3, EYE_H - 8);
            g.setColor(OUTLINE);
            g.fillOval(ex - 2, ey + 4, 4, 3);
            g.setColor(new Color(255, 255, 255, 180));
            g.fillOval(ex, ey + 4, 2, 2);
            g.setColor(OUTLINE);
            g.setStroke(new BasicStroke(1f));
            g.drawOval(ex - EYE_W / 2, ey + 3, EYE_W, EYE_H - 5);
            g.setColor(new Color(90, 45, 20));
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(cx - 2, sy + MOUTH_Y + 1, cx + 2, sy + MOUTH_Y + 1);

        } else {
            // ── 正常：完整大眼 + 平眉 + 微笑 ───────────────────
            drawEyebrow(g, sy, ex, app, fr, false, false);
            drawNormalEye(g, ex, ey, app);
            g.setColor(new Color(140, 70, 50));
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawArc(cx - 4, sy + MOUTH_Y, 9, 5, 0, 180);
        }
        g.setStroke(new BasicStroke(1f));
    }

    /**
     * 正常眼睛：眼白 → 虹膜 → 瞳孔 → 高光 → 眼線。
     * 高光樣式依 EyeStyle 變化。
     */
    public static void drawNormalEye(Graphics2D g, int ex, int ey,
                                     Appearance app) {
        // 眼白
        g.setColor(Color.WHITE);
        g.fillOval(ex - EYE_W / 2, ey, EYE_W, EYE_H);
        // 虹膜
        g.setColor(app.eyeColor);
        g.fillOval(ex - EYE_W / 2 + 1, ey + 1, EYE_W - 2, EYE_H - 1);
        // 瞳孔（按比例放大）
        g.setColor(OUTLINE);
        g.fillOval(ex - 2, ey + 2, 5, 8);

        // 高光（依眼型）
        switch (app.eyeStyle) {
            case DEWY_BRIGHT -> {
                // 虹膜亮環（3層漸層：淺→中→深，楓之谷主角水眸感）
                Color eyeHL = new Color(
                    Math.min(255, app.eyeColor.getRed()   + 55),
                    Math.min(255, app.eyeColor.getGreen() + 55),
                    Math.min(255, app.eyeColor.getBlue()  + 55));
                g.setColor(eyeHL);
                g.fillOval(ex - 3, ey + 2, 6, 7);
                // 主高光 2×2（右上，生命光點）
                g.setColor(new Color(255, 255, 255, 245));
                g.fillRect(ex + 1, ey + 2, 2, 2);
                // 副高光 1×1（左下，水汪汪底部反光）
                g.setColor(new Color(255, 255, 255, 185));
                g.fillRect(ex - 3, ey + EYE_H - 4, 1, 1);
                // 下緣水光薄膜（底部透明細橢圓）
                g.setColor(new Color(255, 255, 255, 55));
                g.fillOval(ex - EYE_W / 2 + 2, ey + EYE_H - 4, EYE_W - 4, 4);
            }
            case WIDE_ROUND -> {
                // 超大圓眼：誇張大反光 + 底部小光，超可愛
                g.setColor(new Color(255, 255, 255, 225));
                g.fillOval(ex + 1, ey + 1, 5, 5);
                g.setColor(new Color(255, 255, 255, 120));
                g.fillOval(ex - 4, ey + EYE_H - 4, 2, 2);
            }
            case SHARP_COOL -> {
                // 銳利冷眼：單點小高光，霸氣感
                g.setColor(new Color(255, 255, 255, 195));
                g.fillOval(ex + 2, ey + 2, 2, 2);
            }
        }

        // 眼線：上弧加粗（睫毛感）+ 下弧細線（自然下睫），分段 stroke
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(ex - EYE_W / 2, ey, EYE_W, EYE_H,   0, 180);  // 上弧（粗睫毛弧）
        g.setStroke(new BasicStroke(0.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(ex - EYE_W / 2, ey, EYE_W, EYE_H, 180, 180);  // 下弧（細下睫）
        g.setStroke(new BasicStroke(1f));
    }

    /**
     * 眉毛：依眉型 + 狀態（正常/受傷/憤怒）調整形狀。
     * 眉色 = 髮色加深（視覺自然融合）。
     */
    public static void drawEyebrow(Graphics2D g, int sy, int ex,
                                   Appearance app, boolean fr,
                                   boolean hurt, boolean angry) {
        int bx = ex;
        int by = sy + BROW_Y;
        Color ec = new Color(
            Math.max(0, app.hairColor.getRed()   - 10),
            Math.max(0, app.hairColor.getGreen() - 10),
            Math.max(0, app.hairColor.getBlue()  - 10));
        g.setColor(ec);

        switch (app.eyebrowStyle) {
            case HERO_THICK -> {
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (hurt) {
                    if (fr) g.drawLine(bx - 5, by + 3, bx + 4, by + 1);
                    else    g.drawLine(bx - 4, by + 1, bx + 5, by + 3);
                } else if (angry) {
                    if (fr) g.drawLine(bx - 5, by + 3, bx + 4, by - 1);
                    else    g.drawLine(bx - 4, by - 1, bx + 5, by + 3);
                } else {
                    if (fr) g.drawLine(bx - 5, by + 1, bx + 4, by - 1);
                    else    g.drawLine(bx - 4, by - 1, bx + 5, by + 1);
                }
            }
            case SOFT_ARCHED -> {
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (hurt || angry) {
                    if (fr) g.drawLine(bx - 5, by + 2, bx + 4, by);
                    else    g.drawLine(bx - 4, by,     bx + 5, by + 2);
                } else {
                    g.drawArc(bx - 6, by - 3, 12, 8, 0, 180);
                }
            }
            case COOL_FLAT -> {
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (angry) {
                    if (fr) g.drawLine(bx - 5, by + 2, bx + 4, by);
                    else    g.drawLine(bx - 4, by,     bx + 5, by + 2);
                } else {
                    g.drawLine(bx - 5, by, bx + 4, by);
                }
            }
            case FAIRY_THIN -> {
                g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (hurt || angry) {
                    if (fr) g.drawLine(bx - 4, by + 1, bx + 3, by);
                    else    g.drawLine(bx - 3, by,     bx + 4, by + 1);
                } else {
                    g.drawArc(bx - 5, by - 2, 10, 7, 0, 180);
                }
            }
        }
        g.setStroke(new BasicStroke(1f));
    }

    // ══════════════════════════════════════════════════════════
    // 身體 / 腿 / 靴子
    // ══════════════════════════════════════════════════════════

    /**
     * 上衣：22px 寬圓角矩形（比舊版 16px 寬 38%），含黑邊+高光+陰影。
     */
    public static void drawBody(Graphics2D g, int cx, int sy, Color topColor) {
        int bx = cx - BODY_W / 2;
        int by = sy + BODY_Y;
        // 主色
        g.setColor(topColor);
        g.fillRoundRect(bx, by, BODY_W, BODY_H, 4, 4);
        // 上方高光
        g.setColor(new Color(255, 255, 255, 45));
        g.fillRoundRect(bx + 2, by + 2, BODY_W - 4, BODY_H / 3, 3, 3);
        // 下方陰影
        g.setColor(new Color(0, 0, 0, 35));
        g.fillRoundRect(bx + 1, by + BODY_H * 2 / 3, BODY_W - 2, BODY_H / 3, 3, 3);
        // 柔化輪廓線
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(bx, by, BODY_W, BODY_H, 4, 4);
        g.setStroke(new BasicStroke(1f));
    }

    /**
     * 腿部：6px 粗輪廓線 + 褲子色填充，走路時左右交替擺動。
     */
    public static void drawLegs(Graphics2D g, int cx, int sy,
                                Color botColor, int swing) {
        int ty = sy + LEG_Y;
        int by = sy + BOOT_Y;
        // 黑邊（8px 黑線 + 6px 褲色，形成輪廓）
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 3, ty, cx - 9 - swing, by);
        g.drawLine(cx + 3, ty, cx + 9 + swing, by);
        g.setColor(botColor);
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 3, ty, cx - 9 - swing, by);
        g.drawLine(cx + 3, ty, cx + 9 + swing, by);
        // 腿部高光細線（內側亮邊，模擬布料受光）
        Color legHL = new Color(
            Math.min(255, botColor.getRed()   + 45),
            Math.min(255, botColor.getGreen() + 45),
            Math.min(255, botColor.getBlue()  + 45), 170);
        g.setColor(legHL);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 2, ty + 2, cx - 8  - swing, by - 1);
        g.drawLine(cx + 4, ty + 2, cx + 10 + swing, by - 1);
        g.setStroke(new BasicStroke(1f));
    }

    /**
     * 靴子：梯形 + 黑邊 + 鞋尖方向依面向決定。
     */
    public static void drawBoots(Graphics2D g, int cx, int sy,
                                 Color bootColor, int swing, boolean fr) {
        int lx = cx - 9 - swing;
        int rx = cx + 9 + swing;
        int by = sy + BOOT_Y;
        drawOneBoot(g, lx, by, bootColor, !fr);
        drawOneBoot(g, rx, by, bootColor,  fr);
    }

    private static void drawOneBoot(Graphics2D g, int fx, int fy,
                                    Color color, boolean toeRight) {
        int toe = toeRight ? 5 : -5;
        int[] bx = {fx - 5 + toe, fx + 7 + toe, fx + 7, fx - 5};
        int[] by = {fy,           fy,            fy + BOOT_H, fy + BOOT_H};
        // 主色
        g.setColor(color);
        g.fillPolygon(bx, by, 4);
        // 鞋面高光
        g.setColor(new Color(255, 255, 255, 40));
        g.fillRect(Math.min(bx[0], bx[3]) + 1, fy + 1, 6, 3);
        // 柔化輪廓線
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawPolygon(bx, by, 4);
        g.setStroke(new BasicStroke(1f));
    }

    // ══════════════════════════════════════════════════════════
    // 裝備
    // ══════════════════════════════════════════════════════════

    /** 頭盔：弧形覆蓋頭頂 + 護頰（配合 26x28 頭部） */
    public static void drawHelmet(Graphics2D g, int cx, int sy, Color color) {
        int hx = cx - HEAD_W / 2;
        // 主色
        g.setColor(color);
        g.fillArc(hx - 1, sy - 6, HEAD_W + 2, HEAD_W + 2, 8, 164);
        g.fillRect(hx - 1, sy + 8,             4, 17);
        g.fillRect(cx + HEAD_W / 2 - 2, sy + 8, 4, 17);
        // 高光
        g.setColor(new Color(255, 255, 255, 55));
        g.fillArc(hx + 3, sy - 3, HEAD_W / 2, HEAD_W / 3, 30, 120);
        // 柔化輪廓線
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(hx - 1, sy - 6, HEAD_W + 2, HEAD_W + 2, 8, 164);
        g.drawRect(hx - 1, sy + 8,             4, 17);
        g.drawRect(cx + HEAD_W / 2 - 2, sy + 8, 4, 17);
        g.setStroke(new BasicStroke(1f));
    }

    /** 耳環：圓珠形，帶黑邊和高光 */
    public static void drawEarring(Graphics2D g, int cx, int sy,
                                   Color color, boolean fr) {
        int earX = fr ? cx + HEAD_W / 2     : cx - HEAD_W / 2;
        int earY = sy + EYE_Y + EYE_H / 2;
        g.setColor(color);
        g.fillOval(earX - 3, earY - 3, 7, 7);
        g.setColor(new Color(255, 255, 255, 130));
        g.fillOval(earX - 2, earY - 2, 3, 3);
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawOval(earX - 3, earY - 3, 7, 7);
        g.setStroke(new BasicStroke(1f));
    }

    /** 披風：背後飄逸三角形，含黑邊 */
    public static void drawCape(Graphics2D g, int cx, int sy,
                                Color color, boolean fr) {
        int d  = fr ? -1 : 1;
        int py = sy + BODY_Y;
        int[] px  = {cx + d * 3, cx + d * 23, cx + d * 16};
        int[] ppy = {py,         py + BODY_H + 10, py + 5};
        // 主色
        g.setColor(color);
        g.fillPolygon(px, ppy, 3);
        // 高光
        g.setColor(new Color(255, 255, 255, 35));
        g.fillPolygon(
            new int[]{px[0], px[0] + d * 4, px[2]},
            new int[]{ppy[0], ppy[1] / 2, ppy[2]}, 3);
        // 柔化輪廓線
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawPolygon(px, ppy, 3);
        g.setStroke(new BasicStroke(1f));
    }

    // ══════════════════════════════════════════════════════════
    // SPRITE PIPELINE — layered drawImage compositor
    //
    // Canvas layout (64×64 px, origin = canvas top-left):
    //
    //   y= 0 - 6  : hair overflow (spikes extending above head)
    //   y= 6 - 34 : head (26 px wide, centred at x=32)
    //   y=29 - 47 : torso (5 px neck-overlap with head)
    //   y=47 - 56 : legs
    //   y=56 - 64 : boots
    //   x-centre  : 32  (character spine)
    //
    // Hitbox ↔ canvas alignment:
    //   hitbox left  = canvasX + (CANVAS_W - HITBOX_W) / 2  →  canvasX + 20
    //   hitbox top   = canvasY + HEAD_TOP_IN_CANVAS          →  canvasY +  6
    //   hitbox bottom= canvasY + CANVAS_H                    →  canvasY + 64
    //   (works for Player HEIGHT=58 because 6+58=64 ✓)
    //
    // NPC HEIGHT=52: canvasY = hitboxY + 52 - 64 = hitboxY - 12  (12 px hair overflow)
    // ══════════════════════════════════════════════════════════

    /** Sprite canvas width — all PNGs must be exactly this many pixels wide. */
    public static final int CANVAS_W = 64;
    /** Sprite canvas height — all PNGs must be exactly this many pixels tall. */
    public static final int CANVAS_H = 64;
    /** Head starts this many pixels from the canvas top edge. */
    public static final int HEAD_TOP_IN_CANVAS = 6;

    /**
     * Computes the canvas top-left X coordinate.
     * cx = hitboxX + hitboxW/2 (character horizontal centre).
     */
    public static int canvasX(int cx)                   { return cx - CANVAS_W / 2; }

    /**
     * Computes the canvas top-left Y coordinate.
     * Feet-bottom aligned: canvas bottom = hitboxY + hitboxH.
     */
    public static int canvasY(int hitboxY, int hitboxH) { return hitboxY + hitboxH - CANVAS_H; }

    // ──────────────────────────────────────────────────────────
    // drawLayered — full paper-doll compositor for Player
    //
    // Layer order (painters algorithm, back → front):
    //   [0] Back hair   — behind body, tinted at runtime
    //   [1] Cape        — behind body
    //   [2] Body/Top    — torso + shirt sprite
    //   [3] Legs+Boots  — always Graphics2D (walk swing animation)
    //   [4] Head/Skin   — skin layer over torso
    //   [5] Face expr   — eyes, brows, mouth (state-keyed sprite)
    //   [6] Front hair  — over face, under helmet, tinted at runtime
    //   [7] Helmet      — over front hair
    //   [8] Earring
    // ──────────────────────────────────────────────────────────

    /**
     * Draws a complete character using the sprite pipeline.
     * Each layer tries its PNG first; falls back to the existing
     * Graphics2D methods (defined above) when the file is absent.
     *
     * Arms are NOT drawn here — Player.drawArms() handles them
     * because they carry complex attack-animation state.
     *
     * @param cx      hitbox horizontal centre (screen coords)
     * @param bodySy  hitbox top used for body layers (may differ from headSy due to bob)
     * @param headSy  hitbox top used for head layers (1-frame lag breathing effect)
     * @param hitboxH Player.HEIGHT or NPC.HEIGHT
     */
    public static void drawLayered(
            Graphics2D g,
            int cx, int bodySy, int headSy, int hitboxH,
            Appearance app, boolean fr,
            Equipment topEq, Equipment botEq, Equipment bootEq, Equipment gloveEq,
            Equipment helmetEq, Equipment earringEq, Equipment capeEq,
            boolean attacking, double hurtTimer, int legSwing) {

        enableAA(g);

        // Resolve Equipment colours (nullsafe)
        Color topColor   = topEq   != null ? topEq.getDisplayColor()   : new Color(190, 190, 210);
        Color botColor   = botEq   != null ? botEq.getDisplayColor()   : new Color(130, 130, 170);
        Color bootColor  = bootEq  != null ? bootEq.getDisplayColor()  : new Color(150, 110, 75);
        Color gloveColor = gloveEq != null ? gloveEq.getDisplayColor() : new Color(210, 180, 130);

        // Canvas origins (body-layer and head-layer may differ by 1 px bob)
        int cdxB = canvasX(cx);
        int cdyB = canvasY(bodySy, hitboxH);
        int cdxH = canvasX(cx);
        int cdyH = canvasY(headSy, hitboxH);

        // [0] Back hair
        String hairBackPath = SpritePathResolver.hairBack(app.hairStyle);
        drawTintedSprite(g, cdxH, cdyH, hairBackPath, app.hairColor, fr);
        // (returns false when missing → full hair drawn in [6] instead)

        // [1] Cape
        if (capeEq != null) {
            String capePath = SpritePathResolver.equipment(EquipSlot.CAPE, capeEq.getName());
            if (!drawTintedSprite(g, cdxB, cdyB, capePath, capeEq.getDisplayColor(), fr))
                drawCape(g, cx, bodySy, capeEq.getDisplayColor(), fr);
        }

        // [2] Body / Top equipment
        String topPath = (topEq != null)
            ? SpritePathResolver.equipment(EquipSlot.TOP, topEq.getName()) : "";
        if (!drawTintedSprite(g, cdxB, cdyB, topPath, topColor, fr))
            drawBody(g, cx, bodySy, topColor);

        // [3] Legs + Boots — animated, always Graphics2D
        drawLegs(g, cx, bodySy, botColor, legSwing);
        drawBoots(g, cx, bodySy, bootColor, legSwing, fr);

        // [4] Head (skin)
        String skinPath = SpritePathResolver.body(app.skinColor);
        if (!drawTintedSprite(g, cdxH, cdyH, skinPath, app.skinColor, fr))
            drawHead(g, cx, headSy, app, fr);

        // [5] Face expression
        SpritePathResolver.FaceState fs = SpritePathResolver.FaceState.from(attacking, hurtTimer);
        String facePath = SpritePathResolver.face(app.eyeStyle, app.eyebrowStyle, fs);
        if (!drawSprite(g, cdxH, cdyH, facePath, fr))
            drawFace(g, cx, headSy, app, fr, attacking, hurtTimer);

        // [6] Front hair (+ back hair fallback when neither layer has a sprite)
        String hairFrontPath = SpritePathResolver.hairFront(app.hairStyle);
        boolean drewFront = drawTintedSprite(g, cdxH, cdyH, hairFrontPath, app.hairColor, fr);
        if (!drewFront && !TextureCache.has(hairBackPath)) {
            // No sprites at all for this style → full Graphics2D hair
            drawHair(g, cx, headSy, app, fr);
        }

        // [7] Helmet
        if (helmetEq != null) {
            String helmPath = SpritePathResolver.equipment(EquipSlot.HELMET, helmetEq.getName());
            if (!drawTintedSprite(g, cdxH, cdyH, helmPath, helmetEq.getDisplayColor(), fr))
                drawHelmet(g, cx, headSy, helmetEq.getDisplayColor());
        }

        // [8] Earring
        if (earringEq != null) {
            String earPath = SpritePathResolver.equipment(EquipSlot.EARRING, earringEq.getName());
            if (!drawTintedSprite(g, cdxH, cdyH, earPath, earringEq.getDisplayColor(), fr))
                drawEarring(g, cx, headSy, earringEq.getDisplayColor(), fr);
        }
    }

    // ──────────────────────────────────────────────────────────
    // Internal sprite drawing primitives
    // ──────────────────────────────────────────────────────────

    /**
     * Draws sprite at canvas origin (dx, dy), flipped horizontally when !fr.
     * Uses NEAREST_NEIGHBOR interpolation to preserve pixel-art crispness.
     * @return true iff the sprite was found and drawn
     */
    static boolean drawSprite(Graphics2D g, int dx, int dy, String path, boolean fr) {
        BufferedImage img = TextureCache.get(path);
        if (img == null) return false;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        if (fr) {
            g.drawImage(img, dx,            dy, CANVAS_W,  CANVAS_H, null);
        } else {
            // Negative width = horizontal mirror via drawImage contract
            g.drawImage(img, dx + CANVAS_W, dy, -CANVAS_W, CANVAS_H, null);
        }
        return true;
    }

    /**
     * Like drawSprite but blends a runtime tint (hair colour, skin tone).
     *
     * Tinting strategy:
     *   1. Draw base sprite (expected to be greyscale/white silhouette).
     *   2. Paint tint colour on an off-screen buffer using SRC_ATOP composite
     *      (only affects pixels where the sprite is non-transparent).
     *   3. The resulting tinted image is cached so repeated calls are cheap.
     *
     * @param tintColor pass null to skip tinting (draws raw sprite)
     * @return true iff the sprite was found and drawn
     */
    static boolean drawTintedSprite(Graphics2D g, int dx, int dy,
                                    String path, Color tintColor, boolean fr) {
        if (path == null || path.isEmpty()) return false;
        BufferedImage base = TextureCache.get(path);
        if (base == null) return false;

        BufferedImage toDraw;
        if (tintColor != null) {
            String tintKey = path + "#" + Integer.toHexString(tintColor.getRGB());
            toDraw = TextureCache.get(tintKey);
            if (toDraw == null) {
                toDraw = buildTinted(base, tintColor);
                TextureCache.put(tintKey, toDraw);
            }
        } else {
            toDraw = base;
        }
        return drawRawImage(g, dx, dy, toDraw, fr);
    }

    /** Draws a BufferedImage directly (bypasses TextureCache key lookup). */
    private static boolean drawRawImage(Graphics2D g, int dx, int dy,
                                        BufferedImage img, boolean fr) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        if (fr) {
            g.drawImage(img, dx,            dy, CANVAS_W,  CANVAS_H, null);
        } else {
            g.drawImage(img, dx + CANVAS_W, dy, -CANVAS_W, CANVAS_H, null);
        }
        return true;
    }

    /**
     * Builds a tinted copy of the source image.
     * The source should be a greyscale/white silhouette with alpha transparency.
     * SRC_ATOP composite paints the tint colour only over existing opaque pixels.
     *
     * Tint strength 0.68f produces vibrant but not fully-opaque colour overlays,
     * preserving shading detail in the source sprite.
     */
    private static BufferedImage buildTinted(BufferedImage src, Color tint) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tg = dst.createGraphics();
        tg.drawImage(src, 0, 0, null);
        tg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.68f));
        tg.setColor(tint);
        tg.fillRect(0, 0, w, h);
        tg.dispose();
        return dst;
    }
}
