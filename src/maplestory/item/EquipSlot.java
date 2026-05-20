package maplestory.item;

/**
 * 裝備欄位枚舉，共 8 格（照楓之谷舊版設計）。
 * ordinal() 對應 Equipment[] 陣列索引。
 */
public enum EquipSlot {
    HELMET  ("頭盔"),
    TOP     ("上衣"),
    BOTTOM  ("下衣"),
    WEAPON  ("武器"),
    GLOVES  ("手套"),
    BOOTS   ("鞋子"),
    CAPE    ("披風"),
    EARRING ("耳環");

    private final String displayName;

    EquipSlot(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
