package maplestory.ui;

import java.awt.*;

/**
 * 裝備面板（按 E 開啟）
 * Phase 6：顯示 8 個空裝備格，Phase 8 接入真實裝備資料。
 *
 * 欄位順序（照楓之谷排列）：
 *   頭盔、上衣、下衣、武器、手套、鞋子、披風、耳環
 */
public class EquipPanel {

    private static final int PW = 220;
    private static final int PH = 400;
    private static final int PX = 800 - PW - 15;
    private static final int PY = 20;

    private static final Color BG_COLOR     = new Color(10,  15,  40,  230);
    private static final Color BORDER_COLOR = new Color(180, 150, 60);
    private static final Color TITLE_COLOR  = new Color(255, 220, 80);
    private static final Color SLOT_EMPTY   = new Color(25,  25,  55);
    private static final Color SLOT_BORDER  = new Color(80,  80,  130);
    private static final Color SLOT_LABEL   = new Color(130, 130, 170);

    // 8 個裝備欄位名稱（Phase 8 會改成實際裝備物品）
    private static final String[] SLOT_NAMES = {
        "頭盔", "上衣", "下衣", "武器",
        "手套", "鞋子", "披風", "耳環"
    };

    // 對應的小圖示符號（用文字代替圖片）
    private static final String[] SLOT_ICONS = {
        "⛑", "👕", "👖", "⚔",
        "🧤", "👟", "🧣", "💍"
    };

    public void draw(Graphics2D g) {
        // 底板
        g.setColor(BG_COLOR);
        g.fillRoundRect(PX, PY, PW, PH, 12, 12);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(PX, PY, PW, PH, 12, 12);
        g.setStroke(new BasicStroke(1f));

        int cx = PX + PW / 2;
        int y  = PY + 22;

        // 標題
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        g.setColor(TITLE_COLOR);
        drawCentered(g, "[ 裝備 ]", cx, y);

        // 分隔線
        y += 14;
        g.setColor(new Color(60, 60, 100));
        g.drawLine(PX + 10, y, PX + PW - 10, y);

        // ── 裝備格（2 欄 × 4 列）──────────────────────────────
        y += 14;
        int slotSize = 60;
        int slotGapX = 20;
        int slotGapY = 14;
        int totalW   = slotSize * 2 + slotGapX;
        int startX   = cx - totalW / 2;

        for (int i = 0; i < SLOT_NAMES.length; i++) {
            int col = i % 2;
            int row = i / 2;
            int sx  = startX + col * (slotSize + slotGapX);
            int sy  = y + row * (slotSize + slotGapY);

            // 格位底色 + 框
            g.setColor(SLOT_EMPTY);
            g.fillRoundRect(sx, sy, slotSize, slotSize, 8, 8);
            g.setColor(SLOT_BORDER);
            g.drawRoundRect(sx, sy, slotSize, slotSize, 8, 8);

            // 圖示（使用 emoji 字符，若字型不支援則退化為文字）
            g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            g.setColor(new Color(100, 100, 140));
            FontMetrics fm = g.getFontMetrics();
            String icon = SLOT_ICONS[i];
            g.drawString(icon,
                sx + (slotSize - fm.stringWidth(icon)) / 2,
                sy + 30);

            // 欄位名稱（格子底部小字）
            g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
            g.setColor(SLOT_LABEL);
            fm = g.getFontMetrics();
            g.drawString(SLOT_NAMES[i],
                sx + (slotSize - fm.stringWidth(SLOT_NAMES[i])) / 2,
                sy + slotSize - 6);
        }

        // Phase 8 提示
        int tipY = y + 4 * (slotSize + slotGapY) + 10;
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        g.setColor(new Color(100, 180, 100));
        drawCentered(g, "Phase 8 將實作完整裝備", cx, tipY);

        // 關閉提示
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(120, 120, 160));
        drawCentered(g, "按 E 關閉", cx, PY + PH - 10);
    }

    private void drawCentered(Graphics2D g, String text, int cx, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, y);
    }
}
