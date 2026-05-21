package maplestory.ui;

import maplestory.entity.Player;
import maplestory.item.Consumable;
import maplestory.item.Equipment;
import maplestory.item.Inventory;

import java.awt.*;
import java.util.List;

/**
 * 背包面板（I 鍵開啟）。
 *
 * 三頁分頁：消耗品 / 裝備 / 其他
 * 5×6 格，點擊消耗品可直接使用。
 * draw() 回傳點擊事件給 GamePanel 處理。
 */
public class InventoryPanel {

    // ── 面板尺寸（邏輯 800x580 空間） ─────────────────────────
    private static final int PW = 440;
    private static final int PH = 370;
    private static final int PX = (800 - PW) / 2;
    private static final int PY = 80;

    // ── 格子設定 ─────────────────────────────────────────────
    private static final int COLS      = 5;
    private static final int ROWS      = 6;
    private static final int CELL_SIZE = 60;
    private static final int GRID_X    = PX + 20;
    private static final int GRID_Y    = PY + 80;

    // ── 分頁 ─────────────────────────────────────────────────
    public enum Tab { CONSUMABLE, EQUIPMENT, MISC }
    private Tab activeTab = Tab.CONSUMABLE;

    // ── 懸停 / 選取 ──────────────────────────────────────────
    private int hoveredCell  = -1;
    private int selectedCell = -1;

    // ── 通知（使用道具後閃過的文字） ─────────────────────────
    private String notice     = "";
    private double noticeTimer = 0;

    // ─────────────────────────────────────────────────────────
    public void draw(Graphics2D g, Player player) {
        Inventory inv = player.getInventory();

        // ── 底板 ─────────────────────────────────────────────
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, 800, 580);

        g.setColor(new Color(16, 20, 50));
        g.fillRoundRect(PX, PY, PW, PH, 16, 16);
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(80, 110, 220));
        g.drawRoundRect(PX, PY, PW, PH, 16, 16);
        g.setStroke(new BasicStroke(1f));

        // ── 標題 ─────────────────────────────────────────────
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 17));
        g.setColor(new Color(200, 220, 255));
        g.drawString("背  包", PX + 18, PY + 28);

        // 格子統計
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        g.setColor(new Color(140, 155, 200));
        String countStr = inv.consumableCount() + "/" + Inventory.MAX_PER_TAB
                        + "  裝備:" + inv.equipmentCount() + "/" + Inventory.MAX_PER_TAB;
        g.drawString(countStr, PX + 18, PY + 48);

        // ── 分頁按鈕 ─────────────────────────────────────────
        drawTab(g, "消耗品", Tab.CONSUMABLE, PX + 18,  PY + 58);
        drawTab(g, "裝  備", Tab.EQUIPMENT,  PX + 120, PY + 58);
        drawTab(g, "其  他", Tab.MISC,       PX + 222, PY + 58);

        // ── 格子 ─────────────────────────────────────────────
        drawGrid(g, inv);

        // ── 提示文字 ─────────────────────────────────────────
        drawTooltip(g, inv);

        // ── 底部說明 ─────────────────────────────────────────
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(110, 120, 165));
        g.drawString("[I] 關閉  消耗品：點擊使用 / 懸停+1-5指派快捷欄", PX + 18, PY + PH - 10);

        // ── 使用道具通知 ─────────────────────────────────────
        if (noticeTimer > 0) {
            float alpha = (float) Math.min(1.0, noticeTimer);
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
            g.setColor(new Color(100, 255, 140, (int)(alpha * 230)));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(notice, PX + (PW - fm.stringWidth(notice)) / 2, PY + PH / 2);
        }
    }

    private void drawTab(Graphics2D g, String label, Tab tab, int tx, int ty) {
        boolean active = (activeTab == tab);
        g.setColor(active ? new Color(55, 85, 175) : new Color(28, 36, 90));
        g.fillRoundRect(tx, ty, 92, 22, 6, 6);
        g.setColor(active ? new Color(120, 170, 255) : new Color(70, 90, 160));
        g.drawRoundRect(tx, ty, 92, 22, 6, 6);
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        g.setColor(active ? Color.WHITE : new Color(160, 175, 220));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, tx + (92 - fm.stringWidth(label)) / 2, ty + 15);
    }

    private void drawGrid(Graphics2D g, Inventory inv) {
        List<?> items = getActiveItems(inv);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int cellIdx = row * COLS + col;
                int cx = GRID_X + col * CELL_SIZE;
                int cy = GRID_Y + row * CELL_SIZE;

                boolean hovered  = (hoveredCell  == cellIdx);
                boolean selected = (selectedCell == cellIdx);

                // 格子背景
                g.setColor(selected ? new Color(60, 90, 180)
                         : hovered  ? new Color(40, 55, 120)
                         :            new Color(22, 28, 68));
                g.fillRect(cx + 1, cy + 1, CELL_SIZE - 2, CELL_SIZE - 2);

                // 格子邊框
                g.setColor(selected ? new Color(140, 180, 255)
                         : hovered  ? new Color( 90, 120, 210)
                         :            new Color( 50,  65, 140));
                g.drawRect(cx + 1, cy + 1, CELL_SIZE - 3, CELL_SIZE - 3);

                if (cellIdx < items.size()) {
                    drawCellItem(g, items.get(cellIdx), cx, cy, cellIdx);
                }
            }
        }
    }

    private void drawCellItem(Graphics2D g, Object item, int cx, int cy, int idx) {
        String name;
        Color  col;

        if (item instanceof Consumable c) {
            name = c.getName();
            col  = c.getRarity().color;
        } else if (item instanceof Equipment e) {
            name = e.getName();
            col  = e.getRarity().color;
        } else {
            name = item.toString();
            col  = Color.WHITE;
        }

        // 稀有度色塊
        g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 80));
        g.fillRect(cx + 4, cy + 4, CELL_SIZE - 8, CELL_SIZE - 8);

        // 名稱（換行顯示在格內）
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 9));
        g.setColor(col.brighter());
        drawWrapped(g, name, cx + 4, cy + 14, CELL_SIZE - 8, 10);

        // 稀有度點
        g.setColor(col);
        g.fillOval(cx + CELL_SIZE - 12, cy + 4, 7, 7);
    }

    /** 簡單文字換行 */
    private void drawWrapped(Graphics2D g, String text, int x, int y, int maxW, int lineH) {
        FontMetrics fm = g.getFontMetrics();
        StringBuilder line = new StringBuilder();
        int curY = y;
        for (char ch : text.toCharArray()) {
            if (fm.stringWidth(line + String.valueOf(ch)) > maxW) {
                g.drawString(line.toString(), x, curY);
                line = new StringBuilder();
                curY += lineH;
                if (curY > y + lineH * 3) break;
            }
            line.append(ch);
        }
        if (!line.isEmpty()) g.drawString(line.toString(), x, curY);
    }

    private void drawTooltip(Graphics2D g, Inventory inv) {
        List<?> items = getActiveItems(inv);
        int idx = (hoveredCell >= 0 && hoveredCell < items.size()) ? hoveredCell : -1;
        if (idx < 0) return;

        Object item = items.get(idx);
        String name, desc;
        Color  col;

        if (item instanceof Consumable c) {
            name = "[消耗品] " + c.getName();
            desc = c.getDescription();
            col  = c.getRarity().color;
        } else if (item instanceof Equipment e) {
            name = "[" + e.getSlot().getDisplayName() + "] " + e.getName();
            desc = e.getDescription();
            col  = e.getRarity().color;
        } else {
            return;
        }

        // 工具提示框（在格子區右方）
        int tx = GRID_X + COLS * CELL_SIZE + 12;
        int ty = GRID_Y;
        int tw = PX + PW - tx - 10;

        g.setColor(new Color(10, 14, 40, 220));
        g.fillRoundRect(tx, ty, tw, 100, 8, 8);
        g.setColor(col);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(tx, ty, tw, 100, 8, 8);
        g.setStroke(new BasicStroke(1f));

        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        g.setColor(col);
        drawWrapped(g, name, tx + 8, ty + 18, tw - 16, 13);

        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(190, 200, 230));
        drawWrapped(g, desc, tx + 8, ty + 46, tw - 16, 12);

        if (activeTab == Tab.CONSUMABLE) {
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 10));
            g.setColor(new Color(100, 200, 120));
            g.drawString("左鍵使用 / 1-5鍵指派快捷欄", tx + 8, ty + 90);
        }
    }

    // ── 事件處理 ─────────────────────────────────────────────

    /**
     * 處理滑鼠點擊（邏輯座標）。
     * @return 消耗品使用結果字串（null = 無動作；"TELEPORT:xxx" = 傳送）
     */
    public String mouseClicked(int lx, int ly, Player player) {
        // 分頁點擊
        int[][] tabBounds = {
            {PX + 18, PY + 58, 92, 22},
            {PX + 120, PY + 58, 92, 22},
            {PX + 222, PY + 58, 92, 22}
        };
        Tab[] tabs = {Tab.CONSUMABLE, Tab.EQUIPMENT, Tab.MISC};
        for (int i = 0; i < tabs.length; i++) {
            int[] b = tabBounds[i];
            if (lx >= b[0] && lx < b[0] + b[2] && ly >= b[1] && ly < b[1] + b[3]) {
                activeTab = tabs[i];
                selectedCell = -1;
                return null;
            }
        }

        // 格子點擊
        int cellIdx = cellAt(lx, ly);
        if (cellIdx < 0) return null;
        selectedCell = cellIdx;

        if (activeTab == Tab.CONSUMABLE) {
            List<Consumable> cons = player.getInventory().getConsumables();
            if (cellIdx < cons.size()) {
                Consumable c = player.getInventory().useConsumable(cellIdx);
                if (c != null) {
                    String result = c.apply(player);
                    if (result.startsWith("TELEPORT:")) {
                        notice      = "使用：" + c.getName();
                        noticeTimer = 1.5;
                        selectedCell = -1;
                        return result; // 傳送卷軸：讓 GamePanel 處理
                    }
                    if (!result.isEmpty()) {
                        notice      = c.getName() + "：" + result;
                        noticeTimer = 2.0;
                    }
                    selectedCell = -1;
                    return result;
                }
            }
        } else if (activeTab == Tab.EQUIPMENT) {
            List<Equipment> equips = player.getInventory().getEquipments();
            if (cellIdx < equips.size()) {
                Equipment toEquip = equips.get(cellIdx);
                boolean ok = player.equipFromInventory(cellIdx);
                if (ok) {
                    notice      = "裝備：" + toEquip.getName();
                    noticeTimer = 2.0;
                    selectedCell = -1;
                }
            }
        }
        return null;
    }

    /** 滑鼠移動（邏輯座標） */
    public void mouseMoved(int lx, int ly) {
        hoveredCell = cellAt(lx, ly);
    }

    /** 更新通知計時 */
    public void update(double dt) {
        if (noticeTimer > 0) noticeTimer -= dt;
    }

    // ── 工具方法 ─────────────────────────────────────────────

    private int cellAt(int lx, int ly) {
        int relX = lx - GRID_X;
        int relY = ly - GRID_Y;
        if (relX < 0 || relY < 0) return -1;
        int col = relX / CELL_SIZE;
        int row = relY / CELL_SIZE;
        if (col >= COLS || row >= ROWS) return -1;
        return row * COLS + col;
    }

    private List<?> getActiveItems(Inventory inv) {
        return switch (activeTab) {
            case CONSUMABLE -> inv.getConsumables();
            case EQUIPMENT  -> inv.getEquipments();
            case MISC       -> java.util.Collections.emptyList();
        };
    }

    /** 回傳目前懸停格位的消耗品（僅消耗品分頁有效；否則回傳 null） */
    public Consumable getHoveredConsumable(Inventory inv) {
        if (activeTab != Tab.CONSUMABLE || hoveredCell < 0) return null;
        List<Consumable> cons = inv.getConsumables();
        return hoveredCell < cons.size() ? cons.get(hoveredCell) : null;
    }
}
