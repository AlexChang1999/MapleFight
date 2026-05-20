package maplestory.ui;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * NPC 對話面板（底部錨定）。
 *
 * 顯示 NPC 名稱標籤、多行對話文字、右側選項列表（▶ 游標）。
 * 操作方式：↑↓ 移動游標，Enter 確認，F / ESC 關閉。
 */
public class DialoguePanel {

    // ── 面板尺寸（邏輯座標 800×580）─────────────────────────────
    private static final int PW = 758;
    private static final int PH = 150;
    private static final int PX = (800 - PW) / 2;
    private static final int PY = 580 - PH - 12; // 底部錨定

    // ── 狀態 ─────────────────────────────────────────────────────
    private String       npcName;
    private List<String> textLines;   // 換行後的對話行
    private List<String> options;     // 選項文字
    private List<String> actionIds;   // 選項對應的動作 ID
    private int          cursor = 0;

    // ── 動畫 ─────────────────────────────────────────────────────
    private double animTimer = 0;

    // ─────────────────────────────────────────────────────────────

    /**
     * 開啟對話。
     * @param npcName   NPC 名稱（顯示在標籤上）
     * @param rawText   對話文字，用 \n 換行
     * @param options   選項顯示文字列表
     * @param actionIds 各選項對應的動作 ID（與 options 等長）
     */
    public void open(String npcName, String rawText,
                     List<String> options, List<String> actionIds) {
        this.npcName   = npcName;
        this.textLines = Arrays.asList(rawText.split("\n"));
        this.options   = options;
        this.actionIds = actionIds;
        this.cursor    = 0;
        this.animTimer = 0;
    }

    public void update(double dt) {
        animTimer += dt;
    }

    // ── 游標導航 ─────────────────────────────────────────────────

    public void navUp() {
        if (options != null && !options.isEmpty())
            cursor = (cursor - 1 + options.size()) % options.size();
    }

    public void navDown() {
        if (options != null && !options.isEmpty())
            cursor = (cursor + 1) % options.size();
    }

    /**
     * 確認目前游標所在選項，回傳其 actionId。
     * 若無選項，回傳 "dismiss"。
     */
    public String confirm() {
        if (actionIds == null || actionIds.isEmpty()) return "dismiss";
        return actionIds.get(cursor);
    }

    // ── 繪製 ─────────────────────────────────────────────────────

    public void draw(Graphics2D g) {
        if (npcName == null) return;

        // 半透明遮罩
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, 800, 580);

        // 面板背景
        g.setColor(new Color(10, 13, 42, 248));
        g.fillRoundRect(PX, PY, PW, PH, 12, 12);
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(200, 175, 80));
        g.drawRoundRect(PX, PY, PW, PH, 12, 12);
        g.setStroke(new BasicStroke(1f));

        // ── NPC 名稱標籤（懸浮在面板上方） ───────────────────────
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        FontMetrics fm = g.getFontMetrics();
        String tag = "【" + npcName + "】";
        int tagW = fm.stringWidth(tag) + 18;
        int tagH = 22;
        int tagX = PX + 18;
        int tagY = PY - tagH - 2;
        g.setColor(new Color(18, 14, 5, 235));
        g.fillRoundRect(tagX, tagY, tagW, tagH, 6, 6);
        g.setColor(new Color(200, 175, 80));
        g.drawRoundRect(tagX, tagY, tagW, tagH, 6, 6);
        g.setColor(new Color(255, 230, 100));
        g.drawString(tag, tagX + 9, tagY + tagH - 5);

        // ── 對話文字（左側） ──────────────────────────────────────
        int textX = PX + 20;
        int textY = PY + 26;
        int optWidth = (options != null && !options.isEmpty()) ? 250 : 0;
        int maxTextX = PX + PW - optWidth - 20;

        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        g.setColor(new Color(220, 225, 255));
        if (textLines != null) {
            for (String line : textLines) {
                // 若文字超出邊界則截斷
                while (g.getFontMetrics().stringWidth(line) > maxTextX - textX && line.length() > 1) {
                    line = line.substring(0, line.length() - 1);
                }
                g.drawString(line, textX, textY);
                textY += 19;
                if (textY > PY + PH - 20) break;
            }
        }

        // ── 選項列表（右側） ──────────────────────────────────────
        if (options != null && !options.isEmpty()) {
            int optX   = PX + PW - 248;
            int optY   = PY + 16;
            int optBH  = options.size() * 28 + 14;

            // 選項區背景
            g.setColor(new Color(8, 10, 32, 225));
            g.fillRoundRect(optX - 8, optY - 4, 244, optBH, 8, 8);
            g.setColor(new Color(80, 68, 28));
            g.drawRoundRect(optX - 8, optY - 4, 244, optBH, 8, 8);

            for (int i = 0; i < options.size(); i++) {
                boolean sel = (i == cursor);
                float pulse = sel ? (float)(0.75 + 0.25 * Math.sin(animTimer * 5)) : 1f;

                if (sel) {
                    g.setColor(new Color(55, 48, 14));
                    g.fillRoundRect(optX - 6, optY + i * 28 - 2, 240, 24, 5, 5);
                }

                // ▶ 游標三角
                if (sel) {
                    int tx = optX;
                    int ty = optY + i * 28 + 8;
                    int[] xs = {tx, tx + 7, tx};
                    int[] ys = {ty, ty + 5, ty + 10};
                    g.setColor(new Color(255, 225, 60, (int)(200 * pulse)));
                    g.fillPolygon(xs, ys, 3);
                }

                g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
                g.setColor(sel ? new Color(255, 230, 80) : new Color(180, 172, 130));
                g.drawString(options.get(i), optX + 12, optY + i * 28 + 16);
            }
        }

        // ── 底部操作提示 ──────────────────────────────────────────
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(100, 92, 58));
        g.drawString("↑↓ 選擇  Enter 確認  F/ESC 關閉", PX + 10, PY + PH - 7);
    }
}
