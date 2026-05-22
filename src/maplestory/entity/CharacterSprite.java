package maplestory.entity;

import maplestory.item.Equipment;
import java.awt.*;

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
    /** 眼白寬 */
    public static final int EYE_W   = 10;
    /** 眼白高（比寬略高，楓之谷杏眼感） */
    public static final int EYE_H   = 11;
    /** 嘴巴 Y — 距眼底留 2px 鼻子空間 */
    public static final int MOUTH_Y = 24;

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

        // 1. 黑色輪廓（擴大 1px）
        g.setColor(OUTLINE);
        g.fillRoundRect(hx - 1, sy - 1, HEAD_W + 2, HEAD_H + 2, 12, 12);

        // 2. 膚色本體
        g.setColor(skin);
        g.fillRoundRect(hx, sy, HEAD_W, HEAD_H, 11, 11);

        // 3. 側面暗影（面向背面那側稍暗）
        Color shadow = new Color(
            Math.max(0, skin.getRed()   - 22),
            Math.max(0, skin.getGreen() - 28),
            Math.max(0, skin.getBlue()  - 22), 80);
        int shdX = fr ? hx + HEAD_W * 6 / 10 : hx;
        int shdW = HEAD_W * 4 / 10;
        g.setColor(shadow);
        g.fillRoundRect(shdX, sy + 5, shdW, HEAD_H - 10, 5, 5);

        // 4. 臉頰紅暈
        int blushX = fr ? hx + HEAD_W / 2 + 1 : hx + 2;
        g.setColor(new Color(255, 148, 128, 55));
        g.fillOval(blushX, sy + MOUTH_Y - 4, 10, 5);
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
            case SPIKY    -> hairSpiky   (g, cx, sy, hc, hcd, fr);
            case SHORT    -> hairShort   (g, cx, sy, hc, hcd);
            case STRAIGHT -> hairStraight(g, cx, sy, hc, hcd, fr);
            case BOWL     -> hairBowl    (g, cx, sy, hc, hcd);
            case LONG     -> hairLong    (g, cx, sy, hc, hcd, fr);
        }
        g.setStroke(new BasicStroke(1f));
    }

    /** 刺刺頭：三根高低錯落尖刺 + 圓弧髮根 + 後腦側發 */
    private static void hairSpiky(Graphics2D g, int cx, int sy,
                                  Color hc, Color hcd, boolean fr) {
        int hx = cx - HEAD_W / 2;
        // 髮根底座
        g.setColor(hc);
        g.fillArc(hx - 1, sy - 2, HEAD_W + 2, HEAD_H / 2 + 2, 0, 180);

        // 三根尖刺（先畫黑邊再疊髮色）
        int[][] spikes = {
            {cx - 12, sy + 3,  cx - 6, sy - 14, cx - 1,  sy + 2},
            {cx -  3, sy + 1,  cx + 1, sy - 18, cx + 6,  sy + 1},
            {cx +  3, sy + 3,  cx + 9, sy - 12, cx + 12, sy + 3}
        };
        for (int[] s : spikes) {
            // 黑邊（外擴 1px）
            g.setColor(OUTLINE);
            g.fillPolygon(
                new int[]{s[0] - 1, s[2], s[4] + 1},
                new int[]{s[1] + 1, s[3] - 1, s[5] + 1}, 3);
            // 髮色
            g.setColor(hc);
            g.fillPolygon(new int[]{s[0], s[2], s[4]},
                          new int[]{s[1], s[3], s[5]}, 3);
            g.setColor(hcd);
            g.drawPolygon(new int[]{s[0], s[2], s[4]},
                          new int[]{s[1], s[3], s[5]}, 3);
        }

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
        g.setColor(hc);
        g.fillArc(hx - 1, sy - 1, HEAD_W + 2, HEAD_H / 2 + 2, 0, 180);

        int[][] spikes = {
            {cx - 10, sy + 3, cx - 4, sy - 9,  cx,      sy + 2},
            {cx +  1, sy + 2, cx + 6, sy - 8,  cx + 11, sy + 3}
        };
        for (int[] s : spikes) {
            g.setColor(OUTLINE);
            g.fillPolygon(new int[]{s[0]-1,s[2],s[4]+1}, new int[]{s[1]+1,s[3]-1,s[5]+1}, 3);
            g.setColor(hc);
            g.fillPolygon(new int[]{s[0],s[2],s[4]}, new int[]{s[1],s[3],s[5]}, 3);
        }
    }

    /** 直髮蓋額：圓頂 + 兩側垂髮 + 瀏海 */
    private static void hairStraight(Graphics2D g, int cx, int sy,
                                     Color hc, Color hcd, boolean fr) {
        int hx = cx - HEAD_W / 2;
        // 輪廓（黑邊）
        g.setColor(OUTLINE);
        g.fillRoundRect(hx - 2, sy - 4, HEAD_W + 4, HEAD_H / 2 + 4, 12, 12);
        g.fillRect(hx - 2, sy + 5, 10, 20);
        g.fillRect(cx + HEAD_W / 2 - 8, sy + 5, 10, 20);
        // 髮色
        g.setColor(hc);
        g.fillRoundRect(hx - 1, sy - 3, HEAD_W + 2, HEAD_H / 2 + 2, 11, 11);
        g.fillRect(hx - 1, sy + 6, 8, 18);
        g.fillRect(cx + HEAD_W / 2 - 7, sy + 6, 8, 18);
        // 瀏海（朝臉部側偏移）
        int fx = fr ? hx + 1 : cx + 1;
        g.fillRoundRect(fx, sy + 9, 9, 10, 4, 4);
    }

    /** 西瓜頭：碗狀弧形覆蓋 */
    private static void hairBowl(Graphics2D g, int cx, int sy,
                                 Color hc, Color hcd) {
        int hx = cx - HEAD_W / 2;
        g.setColor(OUTLINE);
        g.fillArc(hx - 2, sy - 4, HEAD_W + 4, HEAD_H / 2 + 10, 0, 180);
        g.fillRect(hx - 2, sy + 10, HEAD_W + 4, 5);
        g.setColor(hc);
        g.fillArc(hx - 1, sy - 3, HEAD_W + 2, HEAD_H / 2 + 8, 0, 180);
        g.fillRect(hx - 1, sy + 11, HEAD_W + 2, 4);
        g.setColor(hcd);
        g.setStroke(new BasicStroke(1.2f));
        g.drawLine(hx - 1, sy + 14, cx + HEAD_W / 2 + 1, sy + 14);
        g.setStroke(new BasicStroke(1f));
    }

    /** 長髮：圓頂 + 垂肩長側發（覆蓋肩部上衣） */
    private static void hairLong(Graphics2D g, int cx, int sy,
                                 Color hc, Color hcd, boolean fr) {
        int hx = cx - HEAD_W / 2;
        // 黑邊側發
        g.setColor(OUTLINE);
        g.fillRect(hx - 2, sy + 5, 10, 38);
        g.fillRect(cx + HEAD_W / 2 - 8, sy + 5, 10, 38);
        // 髮色
        g.setColor(hc);
        g.fillRoundRect(hx - 1, sy - 3, HEAD_W + 2, HEAD_H / 2 + 4, 11, 11);
        g.fillRect(hx - 1, sy + 5, 8, 36);
        g.fillRect(cx + HEAD_W / 2 - 7, sy + 5, 8, 36);
        // 瀏海
        int fx = fr ? hx + 1 : cx + 1;
        g.fillRoundRect(fx, sy + 9, 9, 10, 4, 4);
        // 長髮底端收尾
        g.setColor(hcd);
        g.drawLine(hx - 1, sy + 41, cx - 5,            sy + 41);
        g.drawLine(cx + HEAD_W / 2 + 1, sy + 41, cx + 5, sy + 41);
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
        // 瞳孔
        g.setColor(OUTLINE);
        g.fillOval(ex - 2, ey + 2, 4, 7);

        // 高光（依眼型）
        switch (app.eyeStyle) {
            case BRIGHT -> {
                // 楓之谷主角：大高光 + 小高光
                g.setColor(new Color(255, 255, 255, 235));
                g.fillOval(ex + 1, ey + 2, 3, 3);
                g.fillOval(ex - 3, ey + 7, 2, 2);
            }
            case ROUND -> {
                // 大圓高光
                g.setColor(new Color(255, 255, 255, 220));
                g.fillOval(ex + 1, ey + 2, 4, 4);
            }
            case SHARP -> {
                // 單點細小高光
                g.setColor(new Color(255, 255, 255, 190));
                g.fillOval(ex + 2, ey + 2, 2, 2);
            }
        }
        // 眼線
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(1f));
        g.drawOval(ex - EYE_W / 2, ey, EYE_W, EYE_H);
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
            case THICK -> {
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
            case ARCHED -> {
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (hurt || angry) {
                    if (fr) g.drawLine(bx - 5, by + 2, bx + 4, by);
                    else    g.drawLine(bx - 4, by,     bx + 5, by + 2);
                } else {
                    g.drawArc(bx - 6, by - 3, 12, 8, 0, 180);
                }
            }
            case FLAT -> {
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (angry) {
                    if (fr) g.drawLine(bx - 5, by + 2, bx + 4, by);
                    else    g.drawLine(bx - 4, by,     bx + 5, by + 2);
                } else {
                    g.drawLine(bx - 5, by, bx + 4, by);
                }
            }
            case THIN -> {
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
        // 黑邊
        g.setColor(OUTLINE);
        g.fillRoundRect(bx - 1, by - 1, BODY_W + 2, BODY_H + 2, 5, 5);
        // 主色
        g.setColor(topColor);
        g.fillRoundRect(bx, by, BODY_W, BODY_H, 4, 4);
        // 上方高光
        g.setColor(new Color(255, 255, 255, 45));
        g.fillRoundRect(bx + 2, by + 2, BODY_W - 4, BODY_H / 3, 3, 3);
        // 下方陰影
        g.setColor(new Color(0, 0, 0, 35));
        g.fillRoundRect(bx + 1, by + BODY_H * 2 / 3, BODY_W - 2, BODY_H / 3, 3, 3);
    }

    /**
     * 腿部：6px 粗輪廓線 + 褲子色填充，走路時左右交替擺動。
     */
    public static void drawLegs(Graphics2D g, int cx, int sy,
                                Color botColor, int swing) {
        int ty = sy + LEG_Y;
        int by = sy + BOOT_Y;
        // 黑邊（8px 的黑線 + 6px 褲色線，形成輪廓）
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 3, ty, cx - 9 - swing, by);
        g.drawLine(cx + 3, ty, cx + 9 + swing, by);
        g.setColor(botColor);
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 3, ty, cx - 9 - swing, by);
        g.drawLine(cx + 3, ty, cx + 9 + swing, by);
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
        // 黑邊
        g.setColor(OUTLINE);
        int[] obx = {bx[0] - 1, bx[1] + 1, bx[2] + 1, bx[3] - 1};
        int[] oby = {by[0] - 1, by[1] - 1, by[2] + 1, by[3] + 1};
        g.fillPolygon(obx, oby, 4);
        // 主色
        g.setColor(color);
        g.fillPolygon(bx, by, 4);
        // 鞋面高光
        g.setColor(new Color(255, 255, 255, 40));
        g.fillRect(Math.min(bx[0], bx[3]) + 1, fy + 1, 6, 3);
    }

    // ══════════════════════════════════════════════════════════
    // 裝備
    // ══════════════════════════════════════════════════════════

    /** 頭盔：弧形覆蓋頭頂 + 護頰（配合 26x28 頭部） */
    public static void drawHelmet(Graphics2D g, int cx, int sy, Color color) {
        int hx = cx - HEAD_W / 2;
        // 黑邊
        g.setColor(OUTLINE);
        g.fillArc(hx - 2, sy - 7, HEAD_W + 4, HEAD_W + 4, 8, 164);
        g.fillRect(hx - 2, sy + 7,             5, 18);
        g.fillRect(cx + HEAD_W / 2 - 3, sy + 7, 5, 18);
        // 主色
        g.setColor(color);
        g.fillArc(hx - 1, sy - 6, HEAD_W + 2, HEAD_W + 2, 8, 164);
        g.fillRect(hx - 1, sy + 8,             4, 17);
        g.fillRect(cx + HEAD_W / 2 - 2, sy + 8, 4, 17);
        // 高光
        g.setColor(new Color(255, 255, 255, 55));
        g.fillArc(hx + 3, sy - 3, HEAD_W / 2, HEAD_W / 3, 30, 120);
    }

    /** 耳環：圓珠形，帶黑邊和高光 */
    public static void drawEarring(Graphics2D g, int cx, int sy,
                                   Color color, boolean fr) {
        int earX = fr ? cx + HEAD_W / 2     : cx - HEAD_W / 2;
        int earY = sy + EYE_Y + EYE_H / 2;
        g.setColor(OUTLINE);
        g.fillOval(earX - 4, earY - 4, 9, 9);
        g.setColor(color);
        g.fillOval(earX - 3, earY - 3, 7, 7);
        g.setColor(new Color(255, 255, 255, 130));
        g.fillOval(earX - 2, earY - 2, 3, 3);
    }

    /** 披風：背後飄逸三角形，含黑邊 */
    public static void drawCape(Graphics2D g, int cx, int sy,
                                Color color, boolean fr) {
        int d  = fr ? -1 : 1;
        int py = sy + BODY_Y;
        int[] px  = {cx + d * 3, cx + d * 23, cx + d * 16};
        int[] ppy = {py,         py + BODY_H + 10, py + 5};
        // 黑邊
        g.setColor(OUTLINE);
        int[] opx = {px[0] - d, px[1] + d * 2, px[2] + d};
        g.fillPolygon(opx, new int[]{ppy[0] - 1, ppy[1] + 2, ppy[2] - 1}, 3);
        // 主色
        g.setColor(color);
        g.fillPolygon(px, ppy, 3);
        // 高光
        g.setColor(new Color(255, 255, 255, 35));
        g.fillPolygon(
            new int[]{px[0], px[0] + d * 4, px[2]},
            new int[]{ppy[0], ppy[1] / 2, ppy[2]}, 3);
    }
}
