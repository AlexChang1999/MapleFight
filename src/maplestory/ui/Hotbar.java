package maplestory.ui;

import maplestory.entity.Player;
import maplestory.item.Consumable;
import maplestory.item.Inventory;

import java.awt.*;

/**
 * 快捷欄（Hotbar）— 5 格，常駐於 HUD 中央。
 *
 * 使用方式：
 *   - 在背包（消耗品頁）懸停道具後按 1-5 鍵：指派到對應槽位
 *   - 正常遊戲中按 1-5：直接使用對應槽位的消耗品
 *
 * 不做存檔持久化（每次開遊戲重新指派）。
 */
public class Hotbar {

    private static final int SLOTS     = 5;
    private static final int SLOT_SIZE = 24;
    private static final int GAP       = 3;

    private final Consumable[] slots = new Consumable[SLOTS];

    // ── 指派 / 清除 ───────────────────────────────────────────

    public void assign(int idx, Consumable c) {
        if (idx >= 0 && idx < SLOTS) slots[idx] = c;
    }

    public void clear(int idx) {
        if (idx >= 0 && idx < SLOTS) slots[idx] = null;
    }

    public Consumable getSlot(int idx) {
        return (idx >= 0 && idx < SLOTS) ? slots[idx] : null;
    }

    // ── 使用 ─────────────────────────────────────────────────

    /**
     * 使用第 idx 槽的消耗品。背包中若已無該物品則清空槽位。
     * @return Consumable.apply() 的結果字串；空字串 = 無物品或無效果
     */
    public String use(int idx, Player player) {
        if (idx < 0 || idx >= SLOTS || slots[idx] == null) return "";
        int invIdx = player.getInventory().findConsumable(slots[idx].getName());
        if (invIdx < 0) {
            slots[idx] = null; // 背包已耗盡，清空槽
            return "";
        }
        Consumable used = player.getInventory().useConsumable(invIdx);
        return used != null ? used.apply(player) : "";
    }

    // ── 繪製 ─────────────────────────────────────────────────

    /**
     * 繪製於 HUD 中央，y = hudY + 48（26px 高）。
     * @param keyLabels 每格目前綁定的按鍵名稱（長度 5），傳入 null 則顯示 1-5
     * @param inv       玩家背包，用於顯示總數量；傳 null 則不顯示數量
     */
    public void draw(Graphics2D g, int hudY, String[] keyLabels, Inventory inv) {
        int totalW = SLOTS * SLOT_SIZE + (SLOTS - 1) * GAP;
        int startX = (800 - totalW) / 2;
        int startY = hudY + 48;

        // 標題小字（帶陰影）
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 7));
        g.setColor(new Color(0, 0, 0, 120));
        g.drawString("快捷欄", startX + 1, startY - 1);
        g.setColor(new Color(140, 150, 210));
        g.drawString("快捷欄", startX, startY - 2);

        for (int i = 0; i < SLOTS; i++) {
            int sx = startX + i * (SLOT_SIZE + GAP);
            int sy = startY;

            // ── 外框陰影 ───────────────────────────────────────
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRoundRect(sx + 1, sy + 1, SLOT_SIZE, SLOT_SIZE, 6, 6);

            // ── 格子背景漸層 ───────────────────────────────────
            GradientPaint slotGrad;
            if (slots[i] != null) {
                Color col = slots[i].getRarity().color;
                slotGrad = new GradientPaint(
                    sx, sy,             new Color(col.getRed() / 5, col.getGreen() / 5, col.getBlue() / 5, 210),
                    sx, sy + SLOT_SIZE, new Color(col.getRed() / 8, col.getGreen() / 8, col.getBlue() / 8, 210)
                );
            } else {
                slotGrad = new GradientPaint(
                    sx, sy,             new Color(24, 30, 68),
                    sx, sy + SLOT_SIZE, new Color(14, 18, 46)
                );
            }
            g.setPaint(slotGrad);
            g.fillRoundRect(sx, sy, SLOT_SIZE, SLOT_SIZE, 6, 6);
            g.setPaint(null);

            // ── 頂部高光 ───────────────────────────────────────
            g.setColor(new Color(255, 255, 255, 22));
            g.fillRoundRect(sx + 1, sy + 1, SLOT_SIZE - 2, SLOT_SIZE / 2, 4, 4);

            if (slots[i] != null) {
                Color col = slots[i].getRarity().color;
                // 名稱縮寫（帶陰影）
                g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 8));
                String abbr = slots[i].getName().length() > 2
                    ? slots[i].getName().substring(0, 2) : slots[i].getName();
                FontMetrics fm = g.getFontMetrics();
                int ax = sx + (SLOT_SIZE - fm.stringWidth(abbr)) / 2;
                int ay = sy + 14;
                g.setColor(new Color(0, 0, 0, 180));
                g.drawString(abbr, ax + 1, ay + 1);
                g.setColor(col.brighter());
                g.drawString(abbr, ax, ay);
                // 稀有度底條
                g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 180));
                g.fillRoundRect(sx + 2, sy + SLOT_SIZE - 4, SLOT_SIZE - 4, 3, 2, 2);

                // ── 總數量（左下角，白色小字）──────────────────
                if (inv != null) {
                    int total = inv.countConsumable(slots[i].getName());
                    if (total > 0) {
                        String countStr = total >= 1000 ? (total / 1000) + "k" : String.valueOf(total);
                        g.setFont(new Font("Arial", Font.BOLD, 7));
                        g.setColor(new Color(0, 0, 0, 160));
                        g.drawString(countStr, sx + 2, sy + SLOT_SIZE - 2 + 1);
                        g.setColor(Color.WHITE);
                        g.drawString(countStr, sx + 2, sy + SLOT_SIZE - 2);
                    }
                }
            }

            // ── 格子邊框（有物品金色，空格深藍）──────────────
            g.setStroke(new java.awt.BasicStroke(1.2f));
            g.setColor(slots[i] != null ? new Color(180, 140, 40) : new Color(48, 58, 128));
            g.drawRoundRect(sx, sy, SLOT_SIZE, SLOT_SIZE, 6, 6);
            g.setStroke(new java.awt.BasicStroke(1f));

            // ── 按鍵標籤（右下角，帶陰影）────────────────────
            String keyLabel = (keyLabels != null && i < keyLabels.length)
                ? keyLabels[i] : String.valueOf(i + 1);
            g.setFont(new Font("Arial", Font.BOLD, 7));
            FontMetrics fm2 = g.getFontMetrics();
            int kx = sx + SLOT_SIZE - fm2.stringWidth(keyLabel) - 2;
            int ky = sy + SLOT_SIZE - 2;
            g.setColor(new Color(0, 0, 0, 150));
            g.drawString(keyLabel, kx + 1, ky + 1);
            g.setColor(slots[i] != null ? new Color(220, 180, 60) : new Color(115, 120, 185));
            g.drawString(keyLabel, kx, ky);
        }
    }
}
