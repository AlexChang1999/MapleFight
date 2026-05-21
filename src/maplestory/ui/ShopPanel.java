package maplestory.ui;

import maplestory.audio.SFX;
import maplestory.audio.SoundManager;
import maplestory.entity.Player;
import maplestory.item.Consumable;
import maplestory.item.Equipment;
import maplestory.item.Inventory;
import maplestory.item.ShopEntry;

import java.awt.*;
import java.util.List;

/**
 * NPC 商店面板（BUY / SELL 雙分頁）。
 *
 * 用法：
 *   shopPanel.open("道具商人的商店", ShopPanel.itemShopEntries(), "item");
 *   shopPanel.draw(g, player);
 *   shopPanel.mouseClicked(lx, ly, player);
 *
 * shopType = "item"   → 購買消耗品 / 賣出消耗品
 * shopType = "weapon" → 購買裝備   / 賣出裝備
 */
public class ShopPanel {

    // ── 面板尺寸 ─────────────────────────────────────────────
    private static final int PW = 380;
    private static final int PH = 425;
    private static final int PX = (800 - PW) / 2;
    private static final int PY = 60;

    private static final int ITEM_H      = 56;
    private static final int ITEMS_START = PY + 90; // 第一行 Y（tab 列下方）
    private static final int BTN_W       = 48;
    private static final int BTN_H       = 26;
    private static final int TAB_W       = 80;
    private static final int TAB_H       = 24;

    // ── 分頁 ─────────────────────────────────────────────────
    public enum Tab { BUY, SELL }
    private Tab activeTab = Tab.BUY;

    // ── 狀態 ─────────────────────────────────────────────────
    private String          shopTitle  = "商店";
    private String          shopType   = "item"; // "item" or "weapon"
    private List<ShopEntry> entries;
    private int             hoveredRow = -1;
    private String          notice     = "";
    private double          noticeTimer = 0;

    // ── 預設商品清單 ─────────────────────────────────────────

    public static List<ShopEntry> itemShopEntries() {
        return List.of(
            ShopEntry.consumable(maplestory.item.Consumable::redPotion,          20),
            ShopEntry.consumable(maplestory.item.Consumable::bluePotion,         25),
            ShopEntry.consumable(maplestory.item.Consumable::orangePotion,       80),
            ShopEntry.consumable(maplestory.item.Consumable::manaElixir,        100),
            ShopEntry.consumable(maplestory.item.Consumable::elixir,            200),
            ShopEntry.consumable(maplestory.item.Consumable::returnToVillage,   500),
            ShopEntry.consumable(maplestory.item.Consumable::returnToFrontier,  800),
            ShopEntry.consumable(maplestory.item.Consumable::returnToBattle,   1200)
        );
    }

    public static List<ShopEntry> weaponShopEntries() {
        return java.util.Arrays.asList(
            // 散賣裝備
            ShopEntry.equipment(maplestory.item.Equipment::noviceHelmet,      150),
            ShopEntry.equipment(maplestory.item.Equipment::leatherTop,        200),
            ShopEntry.equipment(maplestory.item.Equipment::magicEarring,      250),
            ShopEntry.equipment(maplestory.item.Equipment::reinforcedSword,   300),
            ShopEntry.equipment(maplestory.item.Equipment::steelCape,         500),
            // 新手布衣套裝（Lv.1+）
            ShopEntry.equipment(maplestory.item.Equipment::noviceHelmetSet,   120),
            ShopEntry.equipment(maplestory.item.Equipment::noviceTopSet,       90),
            ShopEntry.equipment(maplestory.item.Equipment::noviceBottomSet,    80),
            ShopEntry.equipment(maplestory.item.Equipment::noviceGlovesSet,    60),
            ShopEntry.equipment(maplestory.item.Equipment::noviceBootsSet,     60),
            ShopEntry.equipment(maplestory.item.Equipment::noviceWeaponSet,   150),
            ShopEntry.equipment(maplestory.item.Equipment::noviceCapeSet,      70)
        );
    }

    /** 前線前哨站武器商店（Lv.10+ 青銅套裝） */
    public static List<ShopEntry> frontierWeaponEntries() {
        return java.util.Arrays.asList(
            ShopEntry.equipment(maplestory.item.Equipment::bronzeHelmet,     800),
            ShopEntry.equipment(maplestory.item.Equipment::bronzeTop,        650),
            ShopEntry.equipment(maplestory.item.Equipment::bronzeBottom,     550),
            ShopEntry.equipment(maplestory.item.Equipment::bronzeGloves,     400),
            ShopEntry.equipment(maplestory.item.Equipment::bronzeBoots,      400),
            ShopEntry.equipment(maplestory.item.Equipment::bronzeSword,     1200),
            ShopEntry.equipment(maplestory.item.Equipment::bronzeCape,       500)
        );
    }

    // ── 開啟商店 ─────────────────────────────────────────────

    /** @param shopType "item" 或 "weapon"，決定 SELL 頁顯示哪類道具 */
    public void open(String title, List<ShopEntry> entries, String shopType) {
        this.shopTitle   = title;
        this.entries     = entries;
        this.shopType    = shopType;
        this.activeTab   = Tab.BUY;
        this.hoveredRow  = -1;
        this.notice      = "";
        this.noticeTimer = 0;
    }

    /** 向下相容舊呼叫 */
    public void open(String title, List<ShopEntry> entries) {
        open(title, entries, "item");
    }

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
        g.fillRoundRect(PX + 2, PY + 2, PW - 4, 42, 12, 12);

        // 標題文字
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        g.setColor(new Color(255, 220, 80));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(shopTitle, PX + (PW - fm.stringWidth(shopTitle)) / 2, PY + 28);

        // ── BUY / SELL 分頁按鈕 ───────────────────────────────
        drawTab(g, "購買", Tab.BUY,  PX + 18, PY + 52);
        drawTab(g, "賣出", Tab.SELL, PX + 18 + TAB_W + 8, PY + 52);

        g.setColor(new Color(80, 65, 25));
        g.drawLine(PX + 12, PY + 82, PX + PW - 12, PY + 82);

        // ── 內容區 ────────────────────────────────────────────
        if (activeTab == Tab.BUY) {
            drawBuyTab(g, player);
        } else {
            drawSellTab(g, player);
        }

        // ── 底部金幣列 ────────────────────────────────────────
        int goldBarY = PY + PH - 44;
        g.setColor(new Color(30, 26, 8));
        g.fillRoundRect(PX + 10, goldBarY, 180, 26, 8, 8);
        g.setColor(new Color(100, 80, 20));
        g.drawRoundRect(PX + 10, goldBarY, 180, 26, 8, 8);
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g.setColor(new Color(255, 215, 0));
        g.drawString("持有金幣：" + player.getGold() + " G", PX + 20, goldBarY + 18);

        // 通知文字
        if (noticeTimer > 0) {
            float alpha = (float) Math.min(1.0, noticeTimer);
            boolean isErr = notice.startsWith("！");
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
            g.setColor(isErr ? new Color(255, 80, 80, (int)(alpha * 220))
                             : new Color(100, 255, 140, (int)(alpha * 220)));
            fm = g.getFontMetrics();
            g.drawString(notice, PX + (PW - fm.stringWidth(notice)) / 2, goldBarY - 6);
        }

        // 關閉提示
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(110, 100, 60));
        g.drawString("[F] 關閉商店", PX + PW - 82, PY + PH - 10);
    }

    private void drawTab(Graphics2D g, String label, Tab tab, int tx, int ty) {
        boolean active = (activeTab == tab);
        g.setColor(active ? new Color(55, 45, 12) : new Color(25, 20, 6));
        g.fillRoundRect(tx, ty, TAB_W, TAB_H, 6, 6);
        g.setColor(active ? new Color(200, 170, 60) : new Color(80, 65, 25));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(tx, ty, TAB_W, TAB_H, 6, 6);
        g.setStroke(new BasicStroke(1f));
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g.setColor(active ? new Color(255, 220, 80) : new Color(140, 118, 50));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, tx + (TAB_W - fm.stringWidth(label)) / 2, ty + 16);
    }

    // ── BUY 分頁 ─────────────────────────────────────────────

    private void drawBuyTab(Graphics2D g, Player player) {
        if (entries == null) return;
        int maxVisible = maxVisibleRows();
        for (int i = 0; i < entries.size() && i < maxVisible; i++) {
            drawBuyEntry(g, i, entries.get(i), player.getGold());
        }
    }

    private void drawBuyEntry(Graphics2D g, int idx, ShopEntry entry, int gold) {
        int ey  = ITEMS_START + idx * ITEM_H;
        boolean hov = (activeTab == Tab.BUY && hoveredRow == idx);
        boolean ok  = gold >= entry.getPrice();

        g.setColor(hov ? new Color(32, 42, 88) : new Color(16, 20, 52));
        g.fillRect(PX + 8, ey, PW - 16, ITEM_H - 4);
        g.setColor(hov ? new Color(120, 140, 230) : new Color(48, 52, 110));
        g.drawRect(PX + 8, ey, PW - 16, ITEM_H - 4);

        Color rc = entry.getRarity().color;
        g.setColor(new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), 200));
        g.fillRect(PX + 12, ey + 6, 5, ITEM_H - 16);

        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        g.setColor(ok ? rc.brighter() : new Color(100, 100, 100));
        g.drawString(entry.getName(), PX + 24, ey + 20);

        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(150, 155, 195));
        g.drawString(entry.getDesc(), PX + 24, ey + 36);

        int priceX = PX + PW - BTN_W - 88;
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g.setColor(ok ? new Color(255, 215, 0) : new Color(110, 90, 30));
        g.drawString(entry.getPrice() + " G", priceX, ey + 28);

        int bx = PX + PW - BTN_W - 14;
        int by = ey + (ITEM_H - 4 - BTN_H) / 2;
        g.setColor(ok ? (hov ? new Color(55, 145, 75) : new Color(35, 105, 50))
                      : new Color(35, 35, 35));
        g.fillRoundRect(bx, by, BTN_W, BTN_H, 7, 7);
        g.setColor(ok ? new Color(100, 230, 120) : new Color(70, 70, 70));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(bx, by, BTN_W, BTN_H, 7, 7);
        g.setStroke(new BasicStroke(1f));
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        String btn = ok ? "購買" : "不足";
        g.setColor(ok ? Color.WHITE : new Color(90, 90, 90));
        g.drawString(btn, bx + (BTN_W - fm.stringWidth(btn)) / 2, by + 18);
    }

    // ── SELL 分頁 ────────────────────────────────────────────

    private void drawSellTab(Graphics2D g, Player player) {
        List<?> items = getSellItems(player);
        int maxVisible = maxVisibleRows();

        if (items.isEmpty()) {
            g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
            g.setColor(new Color(120, 120, 160));
            g.drawString("背包中沒有可賣出的物品", PX + 60, ITEMS_START + 40);
            return;
        }

        for (int i = 0; i < items.size() && i < maxVisible; i++) {
            drawSellEntry(g, i, items.get(i));
        }
    }

    private void drawSellEntry(Graphics2D g, int idx, Object item) {
        int ey = ITEMS_START + idx * ITEM_H;
        boolean hov = (activeTab == Tab.SELL && hoveredRow == idx);

        String name, desc;
        Color  rc;
        int    sellPrice;

        if (item instanceof Consumable c) {
            name      = c.getName();
            desc      = c.getDescription();
            rc        = c.getRarity().color;
            sellPrice = c.getRarity().sellValue;
        } else if (item instanceof Equipment e) {
            name      = e.getName();
            desc      = e.getDescription();
            rc        = e.getRarity().color;
            sellPrice = e.getRarity().sellValue;
        } else {
            return;
        }

        g.setColor(hov ? new Color(40, 30, 8) : new Color(16, 20, 52));
        g.fillRect(PX + 8, ey, PW - 16, ITEM_H - 4);
        g.setColor(hov ? new Color(200, 160, 60) : new Color(48, 52, 110));
        g.drawRect(PX + 8, ey, PW - 16, ITEM_H - 4);

        g.setColor(new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), 200));
        g.fillRect(PX + 12, ey + 6, 5, ITEM_H - 16);

        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        g.setColor(rc.brighter());
        g.drawString(name, PX + 24, ey + 20);

        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(150, 155, 195));
        g.drawString(desc, PX + 24, ey + 36);

        // 賣出價格
        int priceX = PX + PW - BTN_W - 88;
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g.setColor(new Color(255, 200, 0));
        g.drawString(sellPrice + " G", priceX, ey + 28);

        // 賣出按鈕
        int bx = PX + PW - BTN_W - 14;
        int by = ey + (ITEM_H - 4 - BTN_H) / 2;
        g.setColor(hov ? new Color(145, 75, 20) : new Color(105, 50, 14));
        g.fillRoundRect(bx, by, BTN_W, BTN_H, 7, 7);
        g.setColor(new Color(230, 140, 60));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(bx, by, BTN_W, BTN_H, 7, 7);
        g.setStroke(new BasicStroke(1f));
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        String btn = "賣出";
        g.setColor(Color.WHITE);
        g.drawString(btn, bx + (BTN_W - fm.stringWidth(btn)) / 2, by + 18);
    }

    // ── 事件處理 ─────────────────────────────────────────────

    public boolean mouseClicked(int lx, int ly, Player player) {
        // 分頁點擊
        if (ly >= PY + 52 && ly < PY + 52 + TAB_H) {
            if (lx >= PX + 18 && lx < PX + 18 + TAB_W) {
                activeTab = Tab.BUY;  hoveredRow = -1; return false;
            }
            if (lx >= PX + 18 + TAB_W + 8 && lx < PX + 18 + TAB_W * 2 + 8) {
                activeTab = Tab.SELL; hoveredRow = -1; return false;
            }
        }

        if (activeTab == Tab.BUY) {
            if (entries == null) return false;
            for (int i = 0; i < entries.size() && i < maxVisibleRows(); i++) {
                int ey = ITEMS_START + i * ITEM_H;
                if (lx >= PX + 8 && lx <= PX + PW - 8 && ly >= ey && ly <= ey + ITEM_H - 4) {
                    return tryBuy(entries.get(i), player);
                }
            }
        } else {
            List<?> items = getSellItems(player);
            for (int i = 0; i < items.size() && i < maxVisibleRows(); i++) {
                int ey = ITEMS_START + i * ITEM_H;
                if (lx >= PX + 8 && lx <= PX + PW - 8 && ly >= ey && ly <= ey + ITEM_H - 4) {
                    return trySell(i, items, player);
                }
            }
        }
        return false;
    }

    public void mouseMoved(int lx, int ly) {
        hoveredRow = -1;
        int maxVisible = maxVisibleRows();
        List<?> displayList = null; // used for count check only
        if (activeTab == Tab.BUY && entries != null) {
            for (int i = 0; i < entries.size() && i < maxVisible; i++) {
                int ey = ITEMS_START + i * ITEM_H;
                if (lx >= PX + 8 && lx <= PX + PW - 8 && ly >= ey && ly <= ey + ITEM_H - 4) {
                    hoveredRow = i; return;
                }
            }
        } else {
            // SELL: count is determined at draw time; just check all rows
            for (int i = 0; i < maxVisible; i++) {
                int ey = ITEMS_START + i * ITEM_H;
                if (lx >= PX + 8 && lx <= PX + PW - 8 && ly >= ey && ly <= ey + ITEM_H - 4) {
                    hoveredRow = i; return;
                }
            }
        }
    }

    // ── 購買邏輯 ─────────────────────────────────────────────

    private boolean tryBuy(ShopEntry entry, Player player) {
        if (!player.spendGold(entry.getPrice())) {
            notice = "！金幣不足！"; noticeTimer = 2.0; return false;
        }
        boolean added;
        if (entry.getKind() == ShopEntry.Kind.CONSUMABLE) {
            added = player.getInventory().addConsumable(entry.createConsumable());
        } else {
            added = player.getInventory().addEquipment(entry.createEquipment());
        }
        if (!added) {
            player.gainGold(entry.getPrice());
            notice = "！背包已滿！"; noticeTimer = 2.0; return false;
        }
        notice = "購買成功：" + entry.getName();
        noticeTimer = 2.0;
        SoundManager.get().playSFX(SFX.SHOP_BUY);
        return true;
    }

    // ── 賣出邏輯 ─────────────────────────────────────────────

    private boolean trySell(int idx, List<?> items, Player player) {
        if (idx >= items.size()) return false;
        Object item = items.get(idx);
        Inventory inv = player.getInventory();

        if (item instanceof Consumable c) {
            int sellPrice = c.getRarity().sellValue;
            // 找到背包中對應索引
            List<Consumable> cons = inv.getConsumables();
            int invIdx = cons.indexOf(c);
            if (invIdx < 0) return false;
            inv.useConsumable(invIdx); // 移除（不套用效果）
            player.gainGold(sellPrice);
            notice = "賣出：" + c.getName() + "（+" + sellPrice + " G）";
            noticeTimer = 2.0;
            return true;
        } else if (item instanceof Equipment e) {
            int sellPrice = e.getRarity().sellValue;
            List<Equipment> equips = inv.getEquipments();
            int invIdx = equips.indexOf(e);
            if (invIdx < 0) return false;
            inv.removeEquipment(invIdx);
            player.gainGold(sellPrice);
            notice = "賣出：" + e.getName() + "（+" + sellPrice + " G）";
            noticeTimer = 2.0;
            return true;
        }
        return false;
    }

    // ── 輔助 ─────────────────────────────────────────────────

    private List<?> getSellItems(Player player) {
        Inventory inv = player.getInventory();
        if ("weapon".equals(shopType)) return inv.getEquipments();
        return inv.getConsumables();
    }

    private int maxVisibleRows() {
        int contentH = PH - 90 - 48; // 去掉標題+tab+金幣列+邊距
        return contentH / ITEM_H;
    }
}
