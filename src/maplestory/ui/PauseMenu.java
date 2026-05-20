package maplestory.ui;

import java.awt.*;

/**
 * ESC 暫停選單（半透明疊加層）。
 *
 * 按鈕（由上到下）：
 *   繼續遊戲 / 存檔 / 刪除存檔 / 回主畫面 / 退出遊戲
 *
 * 所有座標以邏輯解析度 800x580 為準。
 * GamePanel 負責套用縮放 Transform 後再呼叫 draw()。
 */
public class PauseMenu {

    public enum Action { NONE, RESUME, SAVE, DELETE_SAVE, RETURN_TITLE, EXIT }

    // ── 卡片幾何 ─────────────────────────────────────────────
    private static final int CARD_W = 300;
    private static final int CARD_H = 330;
    private static final int CARD_X = (800 - CARD_W) / 2;
    private static final int CARD_Y = (580 - CARD_H) / 2 - 15;

    // ── 按鈕幾何 ─────────────────────────────────────────────
    private static final int BTN_W   = 240;
    private static final int BTN_H   = 40;
    private static final int BTN_X   = CARD_X + (CARD_W - BTN_W) / 2;
    private static final int BTN_Y0  = CARD_Y + 88;
    private static final int BTN_GAP = 48;

    private static final String[] LABELS = {
        "繼續遊戲",
        "存  檔  (F5)",
        "刪除此存檔",
        "回主畫面",
        "退出遊戲"
    };
    private static final Action[] ACTIONS = {
        Action.RESUME,
        Action.SAVE,
        Action.DELETE_SAVE,
        Action.RETURN_TITLE,
        Action.EXIT
    };

    private int hoveredBtn = -1;
    private double animTimer = 0;

    // ─────────────────────────────────────────────────────────
    public void update(double dt) {
        animTimer += dt;
    }

    public void draw(Graphics2D g) {
        // 全畫面半透明遮罩
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, 800, 580);

        // 卡片外層光暈
        float pulse = (float)(0.6 + 0.4 * Math.sin(animTimer * 2.0));
        g.setColor(new Color(60, 80, 200, (int)(30 * pulse)));
        g.fillRoundRect(CARD_X - 10, CARD_Y - 10, CARD_W + 20, CARD_H + 20, 28, 28);

        // 卡片主體
        g.setColor(new Color(16, 20, 48));
        g.fillRoundRect(CARD_X, CARD_Y, CARD_W, CARD_H, 20, 20);

        // 卡片邊框
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(80, 110, 220));
        g.drawRoundRect(CARD_X, CARD_Y, CARD_W, CARD_H, 20, 20);
        g.setStroke(new BasicStroke(1f));

        // 標題
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
        g.setColor(new Color(210, 225, 255));
        String title = "遊 戲 暫 停";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, CARD_X + (CARD_W - fm.stringWidth(title)) / 2, CARD_Y + 52);

        // 分隔線
        g.setColor(new Color(80, 110, 220, 100));
        g.fillRect(CARD_X + 25, CARD_Y + 64, CARD_W - 50, 1);

        // 按鈕
        for (int i = 0; i < LABELS.length; i++) {
            drawButton(g, i);
        }

        // 底部 ESC 提示
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        g.setColor(new Color(110, 120, 160));
        String hint = "[ ESC ] 繼續遊戲";
        g.drawString(hint, CARD_X + (CARD_W - g.getFontMetrics().stringWidth(hint)) / 2,
                     CARD_Y + CARD_H - 12);
    }

    private void drawButton(Graphics2D g, int i) {
        int bx = BTN_X;
        int by = BTN_Y0 + i * BTN_GAP;
        boolean hovered = (hoveredBtn == i);

        Color bg, border, fg;
        switch (i) {
            case 2 -> { // 刪除存檔 - 紅色警告
                bg     = hovered ? new Color(150, 35, 35) : new Color(85, 20, 20);
                border = hovered ? new Color(230, 80, 80)  : new Color(160, 55, 55);
                fg     = new Color(255, 190, 190);
            }
            case 4 -> { // 退出遊戲 - 深紅
                bg     = hovered ? new Color(110, 28, 28) : new Color(65, 18, 18);
                border = hovered ? new Color(210, 65, 65)  : new Color(140, 45, 45);
                fg     = new Color(255, 170, 170);
            }
            default -> { // 一般藍色
                bg     = hovered ? new Color(55, 88, 175) : new Color(30, 48, 108);
                border = hovered ? new Color(130, 175, 255) : new Color(70, 100, 190);
                fg     = hovered ? Color.WHITE : new Color(200, 215, 255);
            }
        }

        // 按鈕底色
        g.setColor(bg);
        g.fillRoundRect(bx, by, BTN_W, BTN_H, 10, 10);

        // 懸停時頂部高光
        if (hovered) {
            g.setColor(new Color(255, 255, 255, 18));
            g.fillRoundRect(bx, by, BTN_W, BTN_H / 2, 10, 10);
        }

        // 邊框
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(border);
        g.drawRoundRect(bx, by, BTN_W, BTN_H, 10, 10);
        g.setStroke(new BasicStroke(1f));

        // 文字
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        g.setColor(fg);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(LABELS[i], bx + (BTN_W - fm.stringWidth(LABELS[i])) / 2,
                     by + BTN_H / 2 + 5);
    }

    /**
     * 點擊判定（邏輯座標）。
     * @return 對應的 Action，沒有命中回傳 NONE
     */
    public Action hit(int lx, int ly) {
        for (int i = 0; i < LABELS.length; i++) {
            int by = BTN_Y0 + i * BTN_GAP;
            if (lx >= BTN_X && lx <= BTN_X + BTN_W &&
                ly >= by    && ly <= by + BTN_H) {
                return ACTIONS[i];
            }
        }
        return Action.NONE;
    }

    /** 更新懸停（邏輯座標） */
    public void updateHover(int lx, int ly) {
        hoveredBtn = -1;
        for (int i = 0; i < LABELS.length; i++) {
            int by = BTN_Y0 + i * BTN_GAP;
            if (lx >= BTN_X && lx <= BTN_X + BTN_W &&
                ly >= by    && ly <= by + BTN_H) {
                hoveredBtn = i;
                break;
            }
        }
    }

    public boolean isVisible() { return true; } // 由 GamePanel 的 paused 旗標控制
}
