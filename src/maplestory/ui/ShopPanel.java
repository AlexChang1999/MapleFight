package maplestory.ui;

import maplestory.entity.Player;
import maplestory.item.ShopEntry;

import java.awt.*;
import java.util.List;

/**
 * NPC 商店面板。
 *
 * 用法：
 *   shopPanel.open("道具商人的商店", ShopPanel.itemShopEntries());
 *   shopPanel.draw(g, player);
 *   shopPanel.mouseClicked(lx, ly, player);
 *
 * 每次購買都透過 ShopEntry.createXxx() 產生新物件實例。
 */
public class ShopPanel {

    // ── 面板尺寸 ─────────────────────────────────────────────
    private static final int PW = 370;
    private static final int PH = 415;
    private static final int PX = (800 - PW) / 2;
    private static final int PY = 65;

    private static final int ITEM_H      = 58;   // 每行高度
    private static final int ITEMS_START = PY + 60; // 第一行 Y
    private static final int BUY_W       = 48;
    private static final int BUY_H       = 26;

    // ── 狀態 ─────────────────────────────────────────────────
    private String           shopTitle  = "商店";
    private List<ShopEntry>  entries;
    private int              hoveredRow = -1;
    private String           notice     = "";
    private double           noticeTimer = 0;

    // ─────────────────────────────────────────────────────────
    // 預設商店清單
    // ─────────────────────────────────────────────────────────

    /** 道具商人商品（消耗品） */
    public static List<ShopEntry> itemShopEntries() {
        return List.of(
            ShopEntry.consumable(maplestory.item.Consumable::redPotion,    20),
            ShopEntry.consumable(maplestory.item.Consumable::bluePotion,   25),
            ShopEntry.consumable(maplestory.item.Consumable::orangePotion, 80),
            ShopEntry.consumable(maplestory.item.Consumable::manaElixir,  100),
            ShopEntry.consumable(maplestory.item.Consumable::elixir,      200)
        );
    }

    /** 武器鐵匠商品（裝備） */
    public static List<ShopEntry> weaponShopEntries() {
        return List.of(
            ShopEntry.equipment(maplestory.item.Equipment::noviceHelmet,    150),
            ShopEntry.equipment(maplestory.item.Equipment::leatherTop,      200),
            ShopEntry.equipment(maplestory.item.Equipment::magicEarring,    250),
            ShopEntry.equipment(maplestory.item.Equipment::reinforcedSword, 300),
            ShopEntry.equipment(maplestory.item.Equipment::steelCape,       500)
        );
    }

    // ─────────────────────────────────────────────────────────

    /** 開啟商店（設定標題與商品清單） */
    public void open(String title, List<ShopEntry> entries) {
        this.shopTitle   = title;
        this.entries     = entries;
        this.hoveredRow  = -1;
        this.notice      = "";
        this.noticeTimer = 0;
    }

    /** 更新通知計時 */
    public void update(double dt) {
        if (noticeTimer > 0) noticeTimer -= dt;
    }

    // ── 繪製 ─────────────────────────────────────────────────

    public void draw(Graphics2D g, Player player) {
        // 暗化背景
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, 800, 580);

        // 面板底板
        g.setColor(new Color(10, 14, 42));
        g.fillRoundRect(PX, PY, PW, PH, 14, 14);
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(200, 170, 65));
        g.drawRoundRect(PX, PY, PW, PH, 14, 14);
        g.setStroke(new BasicStroke(1f));

        // 標題列底色
        g.setColor(new Color(22, 18, 8));
        g.fillRoundRect(PX + 2, PY + 2, PW - 4, 48, 12, 12);

        // 標題文字
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        g.setColor(new Color(255, 220, 80));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(shopTitle, PX + (PW - fm.stringWidth(shopTitle)) / 2, PY + 30);

        // 分隔線
        g.setColor(new Color(80, 65, 25));
        g.drawLine(PX + 12, PY + 48, PX + PW - 12, PY + 48);

        // 商品列表
        if (entries != null) {
            for (int i = 0; i < entries.size(); i++) {
                drawEntry(g, i, entries.get(i), player.getGold());
            }
        }

        // 底部：玩家金幣
        int goldBarY = PY + PH - 44;
        g.setColor(new Color(30, 26, 8));
        g.fillRoundRect(PX + 10, goldBarY, 175, 26, 8, 8);
        g.setColor(new Color(100, 80, 20));
        g.drawRoundRect(PX + 10, goldBarY, 175, 26, 8, 8);
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g.setColor(new Color(255, 215, 0));
        g.drawString("持有金幣：" + player.getGold() + " G", PX + 20, goldBarY + 18);

        // 通知文字（成功/失敗）
        if (noticeTimer > 0) {
            float alpha = (float) Math.min(1.0, noticeTimer);
            boolean isError = notice.startsWith("！");
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
            g.setColor(isError
                    ? new Color(255,  80,  80, (int)(alpha * 220))
                    : new Color(100, 255, 140, (int)(alpha * 220)));
            fm = g.getFontMetrics();
            g.drawString(notice,
                         PX + (PW - fm.stringWidth(notice)) / 2,
                         goldBarY - 6);
        }

        // 關閉提示
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(110, 100, 60));
        g.drawString("[F] 關閉商店", PX + PW - 82, PY + PH - 10);
    }

    private void drawEntry(Graphics2D g, int idx, ShopEntry entry, int gold) {
        int ey      = ITEMS_START + idx * ITEM_H;
        boolean hov = (hoveredRow == idx);
        boolean ok  = gold >= entry.getPrice();

        // 行背景
        g.setColor(hov ? new Color(32, 42, 88) : new Color(16, 20, 52));
        g.fillRect(PX + 8, ey, PW - 16, ITEM_H - 4);
        g.setColor(hov ? new Color(120, 140, 230) : new Color(48, 52, 110));
        g.drawRect(PX + 8, ey, PW - 16, ITEM_H - 4);

        // 稀有度左側色條
        Color rc = entry.getRarity().color;
        g.setColor(new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), 200));
        g.fillRect(PX + 12, ey + 6, 6, ITEM_H - 16);

        // 物品名稱
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        g.setColor(ok ? rc.brighter() : new Color(100, 100, 100));
        g.drawString(entry.getName(), PX + 26, ey + 22);

        // 物品說明
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(150, 155, 195));
        g.drawString(entry.getDesc(), PX + 26, ey + 38);

        // 售價
        int priceX = PX + PW - BUY_W - 88;
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g.setColor(ok ? new Color(255, 215, 0) : new Color(110, 90, 30));
        g.drawString(entry.getPrice() + " G", priceX, ey + 30);

        // 購買按鈕
        int bx = PX + PW - BUY_W - 14;
        int by = ey + (ITEM_H - 4 - BUY_H) / 2;
        g.setColor(ok ? (hov ? new Color(55, 145, 75) : new Color(35, 105, 50))
                      : new Color(35, 35, 35));
        g.fillRoundRect(bx, by, BUY_W, BUY_H, 7, 7);
        g.setColor(ok ? new Color(100, 230, 120) : new Color(70, 70, 70));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(bx, by, BUY_W, BUY_H, 7, 7);
        g.setStroke(new BasicStroke(1f));
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        String btn = ok ? "購買" : "不足";
        g.setColor(ok ? Color.WHITE : new Color(90, 90, 90));
        g.drawString(btn, bx + (BUY_W - fm.stringWidth(btn)) / 2, by + 18);
    }

    // ── 事件處理 ─────────────────────────────────────────────

    /**
     * 處理點擊（邏輯座標）。
     * @return true 若購買成功（供 GamePanel 刷新顯示）
     */
    public boolean mouseClicked(int lx, int ly, Player player) {
        if (entries == null) return false;
        for (int i = 0; i < entries.size(); i++) {
            int ey = ITEMS_START + i * ITEM_H;
            // 整行可點（或單獨偵測購買按鈕）
            if (lx >= PX + 8 && lx <= PX + PW - 8
                    && ly >= ey && ly <= ey + ITEM_H - 4) {
                return tryBuy(entries.get(i), player);
            }
        }
        return false;
    }

    private boolean tryBuy(ShopEntry entry, Player player) {
        if (!player.spendGold(entry.getPrice())) {
            notice      = "！金幣不足！";
            noticeTimer = 2.0;
            return false;
        }
        boolean added;
        if (entry.getKind() == ShopEntry.Kind.CONSUMABLE) {
            added = player.getInventory().addConsumable(entry.createConsumable());
        } else {
            added = player.getInventory().addEquipment(entry.createEquipment());
        }
        if (!added) {
            player.gainGold(entry.getPrice()); // 退款
            notice      = "！背包已滿，無法購買！";
            noticeTimer = 2.0;
            return false;
        }
        notice      = "購買成功：" + entry.getName();
        noticeTimer = 2.0;
        return true;
    }

    /** 滑鼠移動（更新懸停狀態） */
    public void mouseMoved(int lx, int ly) {
        hoveredRow = -1;
        if (entries == null) return;
        for (int i = 0; i < entries.size(); i++) {
            int ey = ITEMS_START + i * ITEM_H;
            if (lx >= PX + 8 && lx <= PX + PW - 8
                    && ly >= ey && ly <= ey + ITEM_H - 4) {
                hoveredRow = i;
                return;
            }
        }
    }
}
