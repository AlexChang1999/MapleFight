package maplestory.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 玩家背包。
 * 分三格：消耗品（40格）/ 裝備（40格）/ 其他（40格）。
 * InventoryPanel 負責 UI 顯示，Inventory 只管資料。
 */
public class Inventory {

    public static final int MAX_PER_TAB = 40;

    private final List<Consumable> consumables = new ArrayList<>();
    private final List<Equipment>  equipments  = new ArrayList<>();
    private final List<String>     misc        = new ArrayList<>(); // 未來：任務物品等

    // ── 加入 ─────────────────────────────────────────────────

    /** 加入消耗品。滿了回傳 false。 */
    public boolean addConsumable(Consumable c) {
        if (consumables.size() >= MAX_PER_TAB) return false;
        consumables.add(c);
        return true;
    }

    /** 加入裝備。滿了回傳 false。 */
    public boolean addEquipment(Equipment e) {
        if (equipments.size() >= MAX_PER_TAB) return false;
        equipments.add(e);
        return true;
    }

    // ── 移除 ─────────────────────────────────────────────────

    /**
     * 使用（移除）指定索引的消耗品。
     * @return 被移除的消耗品，索引無效回傳 null
     */
    public Consumable useConsumable(int index) {
        if (index < 0 || index >= consumables.size()) return null;
        return consumables.remove(index);
    }

    /** 移除（丟棄）裝備 */
    public Equipment removeEquipment(int index) {
        if (index < 0 || index >= equipments.size()) return null;
        return equipments.remove(index);
    }

    // ── 查詢 ─────────────────────────────────────────────────

    public List<Consumable> getConsumables() { return Collections.unmodifiableList(consumables); }
    public List<Equipment>  getEquipments()  { return Collections.unmodifiableList(equipments); }

    public int consumableCount() { return consumables.size(); }
    public int equipmentCount()  { return equipments.size(); }

    public boolean isConsumableFull() { return consumables.size() >= MAX_PER_TAB; }
    public boolean isEquipmentFull()  { return equipments.size()  >= MAX_PER_TAB; }

    /** 按名稱查找消耗品，回傳第一個符合的索引；找不到回傳 -1 */
    public int findConsumable(String name) {
        for (int i = 0; i < consumables.size(); i++) {
            if (consumables.get(i).getName().equals(name)) return i;
        }
        return -1;
    }
}
