package maplestory.ui;

import java.awt.*;

/**
 * 技能面板（按 K 開啟）
 * Phase 6：顯示空的技能格位，Phase 7 接入真實技能資料。
 */
public class SkillPanel {

    private static final int PW = 260;
    private static final int PH = 300;
    private static final int PX = 800 - PW - 15;
    private static final int PY = 20;

    private static final Color BG_COLOR     = new Color(10,  15,  40,  230);
    private static final Color BORDER_COLOR = new Color(180, 150, 60);
    private static final Color TITLE_COLOR  = new Color(255, 220, 80);
    private static final Color SLOT_EMPTY   = new Color(30,  30,  60);
    private static final Color SLOT_BORDER  = new Color(80,  80,  130);

    // Phase 7 接入：技能名稱、描述、MP 消耗等
    // 目前先放空格位讓版面正確顯示
    private static final String[] SLOT_LABELS = {
        "技能 1", "技能 2", "─ 空 ─", "─ 空 ─"
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
        drawCentered(g, "[ 技能 ]", cx, y);

        // 職業說明
        y += 26;
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        g.setColor(new Color(160, 160, 200));
        drawCentered(g, "戰士 · 第一轉", cx, y);

        // 分隔線
        y += 12;
        g.setColor(new Color(60, 60, 100));
        g.drawLine(PX + 10, y, PX + PW - 10, y);

        // 技能格位（2 欄 × 2 列）
        y += 16;
        int slotSize = 52;
        int slotGap  = 18;
        int totalW   = slotSize * 2 + slotGap;
        int startX   = cx - totalW / 2;

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                int idx  = row * 2 + col;
                int sx   = startX + col * (slotSize + slotGap);
                int sy   = y + row * (slotSize + slotGap);

                // 格位底色
                g.setColor(SLOT_EMPTY);
                g.fillRoundRect(sx, sy, slotSize, slotSize, 8, 8);
                g.setColor(SLOT_BORDER);
                g.drawRoundRect(sx, sy, slotSize, slotSize, 8, 8);

                // 格位標籤
                g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
                g.setColor(new Color(120, 120, 160));
                FontMetrics fm = g.getFontMetrics();
                String lbl = SLOT_LABELS[idx];
                g.drawString(lbl,
                    sx + (slotSize - fm.stringWidth(lbl)) / 2,
                    sy + slotSize / 2 + 4);
            }
        }

        // Phase 7 提示
        y += slotSize * 2 + slotGap + 18;
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        g.setColor(new Color(100, 180, 100));
        drawCentered(g, "Phase 7 將實作完整技能", cx, y);

        // 關閉提示
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(120, 120, 160));
        drawCentered(g, "按 K 關閉", cx, PY + PH - 10);
    }

    private void drawCentered(Graphics2D g, String text, int cx, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, y);
    }
}
