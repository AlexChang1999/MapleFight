package maplestory.item;

import java.awt.Color;

/**
 * 單件裝備的資料類別。
 * 使用靜態工廠方法建立各種預設裝備。
 * displayColor 用於在火柴人上顯示裝備顏色。
 */
public class Equipment {

    private final String    name;
    private final EquipSlot slot;

    // 屬性加成
    private final int strBonus;
    private final int dexBonus;
    private final int intelBonus;
    private final int lukBonus;
    private final int atkBonus;
    private final int defBonus;
    private final int hpBonus;
    private final int mpBonus;

    // 在火柴人上顯示的顏色
    private final Color displayColor;

    private Equipment(String name, EquipSlot slot,
                      int str, int dex, int intel, int luk,
                      int atk, int def, int hp, int mp,
                      Color color) {
        this.name        = name;
        this.slot        = slot;
        this.strBonus    = str;
        this.dexBonus    = dex;
        this.intelBonus  = intel;
        this.lukBonus    = luk;
        this.atkBonus    = atk;
        this.defBonus    = def;
        this.hpBonus     = hp;
        this.mpBonus     = mp;
        this.displayColor = color;
    }

    // ── 新手預設裝備工廠方法 ──────────────────────────────────

    /** 棉布上衣：+2 DEF, +10 HP（淡藍色顯示在身體） */
    public static Equipment cottonShirt() {
        return new Equipment("棉布上衣", EquipSlot.TOP,
            0, 0, 0, 0, 0, 2, 10, 0,
            new Color(100, 140, 210));
    }

    /** 棉布長褲：+2 DEF, +5 HP（深藍色顯示在腿） */
    public static Equipment cottonPants() {
        return new Equipment("棉布長褲", EquipSlot.BOTTOM,
            0, 0, 0, 0, 0, 2, 5, 0,
            new Color(60, 90, 160));
    }

    /** 破舊短劍：+3 ATK（灰色顯示為劍身） */
    public static Equipment oldSword() {
        return new Equipment("破舊短劍", EquipSlot.WEAPON,
            0, 0, 0, 0, 3, 0, 0, 0,
            new Color(190, 190, 190));
    }

    /** 布麻手套：+1 DEF（米色顯示在手） */
    public static Equipment hempGloves() {
        return new Equipment("布麻手套", EquipSlot.GLOVES,
            0, 0, 0, 0, 0, 1, 0, 0,
            new Color(210, 185, 140));
    }

    /** 布麻鞋：+1 DEF（棕色顯示在腳） */
    public static Equipment hempBoots() {
        return new Equipment("布麻鞋", EquipSlot.BOOTS,
            0, 0, 0, 0, 0, 1, 0, 0,
            new Color(170, 130, 85));
    }

    // ── Getter ───────────────────────────────────────────────
    public String    getName()         { return name; }
    public EquipSlot getSlot()         { return slot; }
    public int       getStrBonus()     { return strBonus; }
    public int       getDexBonus()     { return dexBonus; }
    public int       getIntelBonus()   { return intelBonus; }
    public int       getLukBonus()     { return lukBonus; }
    public int       getAtkBonus()     { return atkBonus; }
    public int       getDefBonus()     { return defBonus; }
    public int       getHpBonus()      { return hpBonus; }
    public int       getMpBonus()      { return mpBonus; }
    public Color     getDisplayColor() { return displayColor; }
}
