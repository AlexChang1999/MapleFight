package maplestory.ui;

import maplestory.entity.Player;
import maplestory.item.EquipSlot;
import maplestory.item.Equipment;

import java.awt.*;
import java.util.Map;

/**
 * 裝備面板（按 E 開啟）。
 *
 * Phase 4：顯示玩家目前穿著的裝備，點擊已穿裝備可脫裝放回背包。
 * 欄位佈局（2 欄 × 4 列）：
 *   頭盔  武器
 *   上衣  手套
 *   下衣  鞋子
 *   披風  耳環
 */
public class EquipPanel {

    // ── 面板尺寸 ─────────────────────────────────────────────
    private static final int PW = 220;
    private static final int PH = 420;
    private static final int PX = 800 - PW - 15;
    private static final int PY = 20;

    // ── 格子設定 ─────────────────────────────────────────────
    private static final int SLOT_SIZE = 60;
    private static final int GAP_X     = 20;
    private static final int GAP_Y     = 12;
    private static final int SLOTS_START_Y = PY + 62; // 格子起始 Y

    // ── 欄位對應（2 欄 × 4 列，共 8 格） ─────────────────────
    /** 格子索引對應的 EquipSlot（左欄偶數，右欄奇數） */
    private static final EquipSlot[] SLOT_ORDER = {
        EquipSlot.HELMET,  EquipSlot.WEAPON,
        EquipSlot.TOP,     EquipSlot.GLOVES,
        EquipSlot.BOTTOM,  EquipSlot.BOOTS,
        EquipSlot.CAPE,    EquipSlot.EARRING
    };

    // ── 狀態 ─────────────────────────────────────────────────
    private int    hoveredSlot   = -1;
    private String notice        = "";
    private double noticeTimer   = 0;

    // ─────────────────────────────────────────────────────────
    public void update(double dt) {
        if (noticeTimer > 0) noticeTimer -= dt;
    }

    // ── 繪製 ─────────────────────────────────────────────────
    public void draw(Graphics2D g, Player player) {
        Map<EquipSlot, Equipment> equips = player.getEquipments();

        // 底板
        g.setColor(new Color(10, 14, 42, 235));
        g.fillRoundRect(PX, PY, PW, PH, 12, 12);
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(180, 150, 60));
        g.drawRoundRect(PX, PY, PW, PH, 12, 12);
        g.setStroke(new BasicStroke(1f));

        int cx = PX + PW / 2;

        // 標題
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        g.setColor(new Color(255, 220, 80));
        drawCentered(g, "[ 裝 備 ]", cx, PY + 22);

        // 分隔線
        g.setColor(new Color(70, 60, 25));
        g.drawLine(PX + 10, PY + 32, PX + PW - 10, PY + 32);

        // 屬性摘要（ATK / DEF / HP bonus）
        int atkB = equips.values().stream().mapToInt(Equipment::getAtkBonus).sum();
        int defB = equips.values().stream().mapToInt(Equipment::getDefBonus).sum();
        int hpB  = equips.values().stream().mapToInt(Equipment::getHpBonus).sum();
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(180, 190, 230));
        String stats = "ATK+" + atkB + "  DEF+" + defB + "  HP+" + hpB;
        drawCentered(g, stats, cx, PY + 48);

        // 計算格子起始 X
        int totalW  = SLOT_SIZE * 2 + GAP_X;
        int startX  = cx - totalW / 2;

        // 格子
        for (int i = 0; i < SLOT_ORDER.length; i++) {
            int col = i % 2;
            int row = i / 2;
            int sx  = startX + col * (SLOT_SIZE + GAP_X);
            int sy  = SLOTS_START_Y + row * (SLOT_SIZE + GAP_Y);

            drawSlot(g, sx, sy, i, SLOT_ORDER[i], equips.get(SLOT_ORDER[i]));
        }

        // 通知文字
        if (noticeTimer > 0) {
            float alpha = (float) Math.min(1.0, noticeTimer);
            boolean err = notice.startsWith("！");
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
            g.setColor(err ? new Color(255, 80, 80, (int)(alpha * 220))
                           : new Color(100, 255, 140, (int)(alpha * 220)));
            FontMetrics fm = g.getFontMetrics();
            int tipY = SLOTS_START_Y + 4 * (SLOT_SIZE + GAP_Y) + 18;
            g.drawString(notice, PX + (PW - fm.stringWidth(notice)) / 2, tipY);
        }

        // 提示文字
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(110, 110, 155));
        drawCentered(g, "點擊裝備可脫裝  按 E 關閉", cx, PY + PH - 10);
    }

    private void drawSlot(Graphics2D g, int sx, int sy, int idx,
                          EquipSlot slot, Equipment equip) {
        boolean hov = (hoveredSlot == idx);

        // 格子背景
        if (equip != null) {
            Color rc = equip.getRarity().color;
            g.setColor(new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), 30));
            g.fillRoundRect(sx, sy, SLOT_SIZE, SLOT_SIZE, 8, 8);
            g.setColor(hov ? rc.brighter() : rc.darker().darker());
            g.setStroke(new BasicStroke(hov ? 2f : 1.5f));
            g.drawRoundRect(sx, sy, SLOT_SIZE, SLOT_SIZE, 8, 8);
            g.setStroke(new BasicStroke(1f));

            // 裝備名稱（換行顯示）
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 9));
            g.setColor(hov ? rc.brighter() : rc);
            drawSlotText(g, equip.getName(), sx + 4, sy + 14, SLOT_SIZE - 8, 10);

            // 稀有度點
            g.setColor(rc);
            g.fillOval(sx + SLOT_SIZE - 11, sy + 4, 7, 7);

            // 懸停時顯示說明
            if (hov) {
                g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 9));
                g.setColor(new Color(200, 210, 240));
                g.drawString(equip.getDescription(), sx + 4, sy + SLOT_SIZE - 8);
            }
        } else {
            // 空格
            g.setColor(new Color(20, 22, 58));
            g.fillRoundRect(sx, sy, SLOT_SIZE, SLOT_SIZE, 8, 8);
            g.setColor(hov ? new Color(70, 80, 140) : new Color(45, 48, 105));
            g.drawRoundRect(sx, sy, SLOT_SIZE, SLOT_SIZE, 8, 8);

            // 欄位名稱
            g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
            g.setColor(new Color(80, 85, 130));
            FontMetrics fm = g.getFontMetrics();
            String slotName = slot.getDisplayName();
            g.drawString(slotName,
                         sx + (SLOT_SIZE - fm.stringWidth(slotName)) / 2,
                         sy + SLOT_SIZE / 2 + 4);
        }
    }

    /** 簡單的格內換行文字 */
    private void drawSlotText(Graphics2D g, String text,
                               int x, int y, int maxW, int lineH) {
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

    // ── 事件處理 ─────────────────────────────────────────────

    /**
     * 點擊處理：點擊已穿裝備 → 脫裝放回背包。
     * @return true 若脫裝成功（供 GamePanel 更新顯示）
     */
    public boolean mouseClicked(int lx, int ly, Player player) {
        int idx = slotAt(lx, ly);
        if (idx < 0) return false;

        EquipSlot slot  = SLOT_ORDER[idx];
        Equipment equip = player.getEquipments().get(slot);
        if (equip == null) return false;

        boolean ok = player.unequip(slot);
        if (ok) {
            notice      = "脫下：" + equip.getName();
            noticeTimer = 2.0;
        } else {
            notice      = "！背包已滿，無法脫裝！";
            noticeTimer = 2.0;
        }
        return ok;
    }

    /** 滑鼠移動（更新懸停格） */
    public void mouseMoved(int lx, int ly) {
        hoveredSlot = slotAt(lx, ly);
    }

    // ── 工具方法 ─────────────────────────────────────────────

    private int slotAt(int lx, int ly) {
        int cx     = PX + PW / 2;
        int totalW = SLOT_SIZE * 2 + GAP_X;
        int startX = cx - totalW / 2;

        for (int i = 0; i < SLOT_ORDER.length; i++) {
            int col = i % 2;
            int row = i / 2;
            int sx  = startX + col * (SLOT_SIZE + GAP_X);
            int sy  = SLOTS_START_Y + row * (SLOT_SIZE + GAP_Y);
            if (lx >= sx && lx < sx + SLOT_SIZE && ly >= sy && ly < sy + SLOT_SIZE) {
                return i;
            }
        }
        return -1;
    }

    private void drawCentered(Graphics2D g, String text, int cx, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, y);
    }
}
