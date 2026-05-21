package maplestory.ui;

import maplestory.entity.Player;
import maplestory.item.Consumable;

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

    /** 繪製於 HUD 中央，y = hudY + 48（26px 高） */
    public void draw(Graphics2D g, int hudY) {
        int totalW = SLOTS * SLOT_SIZE + (SLOTS - 1) * GAP;
        int startX = (800 - totalW) / 2;
        int startY = hudY + 48;

        // 標題小字
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 7));
        g.setColor(new Color(100, 110, 160));
        g.drawString("快捷欄", startX, startY - 2);

        for (int i = 0; i < SLOTS; i++) {
            int sx = startX + i * (SLOT_SIZE + GAP);
            int sy = startY;

            // 格子背景
            g.setColor(new Color(22, 28, 68));
            g.fillRect(sx, sy, SLOT_SIZE, SLOT_SIZE);

            // 格子邊框（有物品時亮邊）
            g.setColor(slots[i] != null ? new Color(90, 120, 220) : new Color(45, 55, 120));
            g.drawRect(sx, sy, SLOT_SIZE - 1, SLOT_SIZE - 1);

            if (slots[i] != null) {
                Color col = slots[i].getRarity().color;
                // 稀有度底色
                g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 70));
                g.fillRect(sx + 1, sy + 1, SLOT_SIZE - 2, SLOT_SIZE - 2);
                // 名稱縮寫（前 2 個字元）
                g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 8));
                g.setColor(col.brighter());
                String abbr = slots[i].getName().length() > 2
                    ? slots[i].getName().substring(0, 2) : slots[i].getName();
                FontMetrics fm = g.getFontMetrics();
                g.drawString(abbr, sx + (SLOT_SIZE - fm.stringWidth(abbr)) / 2, sy + 14);
            }

            // 槽位數字（右下角）
            g.setFont(new Font("Arial", Font.BOLD, 7));
            g.setColor(new Color(130, 135, 185));
            g.drawString(String.valueOf(i + 1), sx + SLOT_SIZE - 7, sy + SLOT_SIZE - 2);
        }
    }
}
