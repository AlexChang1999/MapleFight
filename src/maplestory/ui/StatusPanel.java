package maplestory.ui;

import maplestory.entity.Player;

import java.awt.*;

/**
 * 狀態面板（按 S 開啟）
 * 顯示角色的所有 RPG 數值：職業、等級、STR/DEX/INT/LUK、HP/MP、EXP
 */
public class StatusPanel {

    // 面板位置與大小
    private static final int PW = 260;  // Panel Width
    private static final int PH = 360;  // Panel Height
    private static final int PX = 800 - PW - 15; // 靠右
    private static final int PY = 20;

    // 配色
    private static final Color BG_COLOR     = new Color(10,  15,  40,  230); // 深藍底
    private static final Color BORDER_COLOR = new Color(180, 150, 60);        // 金色框
    private static final Color TITLE_COLOR  = new Color(255, 220, 80);        // 金色標題
    private static final Color LABEL_COLOR  = new Color(160, 160, 200);       // 灰藍標籤
    private static final Color VALUE_COLOR  = Color.WHITE;
    private static final Color SEP_COLOR    = new Color(60,  60,  100);       // 分隔線

    public void draw(Graphics2D g, Player player) {
        // ── 底板 ─────────────────────────────────────────────
        g.setColor(BG_COLOR);
        g.fillRoundRect(PX, PY, PW, PH, 12, 12);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(PX, PY, PW, PH, 12, 12);
        g.setStroke(new BasicStroke(1f));

        int cx = PX + PW / 2;
        int y  = PY + 22;

        // ── 標題 ─────────────────────────────────────────────
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        g.setColor(TITLE_COLOR);
        drawCentered(g, "[ 角色狀態 ]", cx, y);

        // ── 職業 / 等級 ───────────────────────────────────────
        y += 30;
        drawSeparator(g, y - 8);
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        drawRow(g, PX + 20, y, "職業", player.getJobName());
        y += 22;
        drawRow(g, PX + 20, y, "等級", "Lv. " + player.getLevel());

        // ── 四維數值 ─────────────────────────────────────────
        y += 30;
        drawSeparator(g, y - 8);
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        g.setColor(TITLE_COLOR);
        g.drawString("能力值", PX + 20, y);

        y += 22;
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        drawStatRow(g, PX + 20, y, "力量 STR", player.getStr(),
                    "物理攻擊加成", new Color(255, 100, 100));
        y += 22;
        drawStatRow(g, PX + 20, y, "敏捷 DEX", player.getDex(),
                    "命中率加成",  new Color(100, 220, 255));
        y += 22;
        drawStatRow(g, PX + 20, y, "智力 INT", player.getIntel(),
                    "MP 上限加成", new Color(150, 120, 255));
        y += 22;
        drawStatRow(g, PX + 20, y, "幸運 LUK", player.getLuk(),
                    "爆擊率加成", new Color(255, 220, 80));

        // ── HP / MP ───────────────────────────────────────────
        y += 30;
        drawSeparator(g, y - 8);
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        g.setColor(TITLE_COLOR);
        g.drawString("生命 / 魔力", PX + 20, y);

        y += 22;
        drawBarRow(g, PX + 20, y,
                   "HP", player.getHp(), player.getMaxHp(),
                   new Color(220, 50, 50));
        y += 28;
        drawBarRow(g, PX + 20, y,
                   "MP", player.getMp(), player.getMaxMp(),
                   new Color(60, 110, 220));

        // ── EXP ──────────────────────────────────────────────
        y += 35;
        drawSeparator(g, y - 8);
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        g.setColor(LABEL_COLOR);
        g.drawString("經驗值", PX + 20, y);

        // EXP 長條
        y += 8;
        int barW = PW - 40;
        g.setColor(new Color(0, 50, 0));
        g.fillRoundRect(PX + 20, y, barW, 14, 6, 6);
        g.setColor(new Color(70, 200, 70));
        g.fillRoundRect(PX + 20, y, (int)(barW * player.getExpRatio()), 14, 6, 6);
        g.setColor(Color.WHITE);
        g.drawRoundRect(PX + 20, y, barW, 14, 6, 6);

        // EXP 數字
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        String expText = player.getExp() + " / " + player.getExpToNextLevel();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(expText, PX + 20 + (barW - fm.stringWidth(expText)) / 2, y + 11);

        // ── 關閉提示 ─────────────────────────────────────────
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(120, 120, 160));
        drawCentered(g, "按 S 關閉", cx, PY + PH - 10);
    }

    /** 畫「標籤 + 彩色數值 + 小說明」的屬性列 */
    private void drawStatRow(Graphics2D g, int x, int y,
                              String label, int value,
                              String desc, Color valueColor) {
        g.setColor(LABEL_COLOR);
        g.drawString(label, x, y);

        g.setColor(valueColor);
        g.setFont(new Font("Arial", Font.BOLD, 13));
        g.drawString(String.valueOf(value), x + 120, y);

        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(120, 120, 150));
        g.drawString(desc, x + 150, y);

        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
    }

    /** 畫「標籤 + 值」的簡單列 */
    private void drawRow(Graphics2D g, int x, int y, String label, String value) {
        g.setColor(LABEL_COLOR);
        g.drawString(label, x, y);
        g.setColor(VALUE_COLOR);
        g.drawString(value, x + 100, y);
    }

    /** 畫帶進度條的 HP / MP 列 */
    private void drawBarRow(Graphics2D g, int x, int y,
                             String label, int cur, int max, Color barColor) {
        int barW = PW - 80;

        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        g.setColor(LABEL_COLOR);
        g.drawString(label, x, y + 12);

        g.setColor(barColor.darker());
        g.fillRoundRect(x + 30, y, barW, 16, 6, 6);

        double ratio = max > 0 ? (double) cur / max : 0;
        g.setColor(barColor);
        g.fillRoundRect(x + 30, y, (int)(barW * ratio), 16, 6, 6);

        g.setColor(Color.WHITE);
        g.drawRoundRect(x + 30, y, barW, 16, 6, 6);

        g.setFont(new Font("Arial", Font.BOLD, 11));
        String txt = cur + "/" + max;
        FontMetrics fm = g.getFontMetrics();
        g.drawString(txt, x + 30 + (barW - fm.stringWidth(txt)) / 2, y + 12);
    }

    private void drawSeparator(Graphics2D g, int y) {
        g.setColor(SEP_COLOR);
        g.drawLine(PX + 10, y, PX + PW - 10, y);
    }

    private void drawCentered(Graphics2D g, String text, int cx, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, y);
    }
}
