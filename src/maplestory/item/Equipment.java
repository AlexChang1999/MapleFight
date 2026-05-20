package maplestory.item;

import java.awt.Color;

/**
 * 單件裝備的資料類別。
 * 使用靜態工廠方法建立各種預設裝備。
 * displayColor 用於在火柴人上顯示裝備顏色。
 */
public class Equipment {

    private final String     name;
    private final EquipSlot  slot;
    private final ItemRarity rarity;
    private final String     description;

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

    private Equipment(String name, EquipSlot slot, ItemRarity rarity, String desc,
                      int str, int dex, int intel, int luk,
                      int atk, int def, int hp, int mp,
                      Color color) {
        this.name         = name;
        this.slot         = slot;
        this.rarity       = rarity;
        this.description  = desc;
        this.strBonus     = str;
        this.dexBonus     = dex;
        this.intelBonus   = intel;
        this.lukBonus     = luk;
        this.atkBonus     = atk;
        this.defBonus     = def;
        this.hpBonus      = hp;
        this.mpBonus      = mp;
        this.displayColor = color;
    }

    // ── 新手預設裝備工廠方法 ──────────────────────────────────

    // ── 新手預設裝備（開局穿著） ──────────────────────────────

    /** 棉布上衣：+2 DEF, +10 HP */
    public static Equipment cottonShirt() {
        return new Equipment("棉布上衣", EquipSlot.TOP,
            ItemRarity.COMMON, "DEF +2  HP +10",
            0, 0, 0, 0, 0, 2, 10, 0, new Color(100, 140, 210));
    }

    /** 棉布長褲：+2 DEF, +5 HP */
    public static Equipment cottonPants() {
        return new Equipment("棉布長褲", EquipSlot.BOTTOM,
            ItemRarity.COMMON, "DEF +2  HP +5",
            0, 0, 0, 0, 0, 2, 5, 0, new Color(60, 90, 160));
    }

    /** 破舊短劍：+3 ATK */
    public static Equipment oldSword() {
        return new Equipment("破舊短劍", EquipSlot.WEAPON,
            ItemRarity.COMMON, "ATK +3",
            0, 0, 0, 0, 3, 0, 0, 0, new Color(190, 190, 190));
    }

    /** 布麻手套：+1 DEF */
    public static Equipment hempGloves() {
        return new Equipment("布麻手套", EquipSlot.GLOVES,
            ItemRarity.COMMON, "DEF +1",
            0, 0, 0, 0, 0, 1, 0, 0, new Color(210, 185, 140));
    }

    /** 布麻鞋：+1 DEF */
    public static Equipment hempBoots() {
        return new Equipment("布麻鞋", EquipSlot.BOOTS,
            ItemRarity.COMMON, "DEF +1",
            0, 0, 0, 0, 0, 1, 0, 0, new Color(170, 130, 85));
    }

    // ── 掉落裝備 ─────────────────────────────────────────────

    /** 怪物掉落：新手頭盔（普通） */
    public static Equipment noviceHelmet() {
        return new Equipment("新手頭盔", EquipSlot.HELMET,
            ItemRarity.COMMON, "DEF +3  HP +20",
            0, 0, 0, 0, 0, 3, 20, 0, new Color(160, 140, 100));
    }

    /** 怪物掉落：強化短劍（優良） */
    public static Equipment reinforcedSword() {
        return new Equipment("強化短劍", EquipSlot.WEAPON,
            ItemRarity.UNCOMMON, "ATK +7  STR +1",
            1, 0, 0, 0, 7, 0, 0, 0, new Color(120, 180, 255));
    }

    /** 怪物掉落：皮革上衣（普通） */
    public static Equipment leatherTop() {
        return new Equipment("皮革上衣", EquipSlot.TOP,
            ItemRarity.COMMON, "DEF +5  HP +30",
            0, 0, 0, 0, 0, 5, 30, 0, new Color(155, 115, 75));
    }

    /** 怪物掉落：魔法耳環（優良） */
    public static Equipment magicEarring() {
        return new Equipment("魔法耳環", EquipSlot.EARRING,
            ItemRarity.UNCOMMON, "INT +2  MP +40",
            0, 0, 2, 0, 0, 0, 0, 40, new Color(160, 80, 220));
    }

    /** 怪物掉落：鋼鐵披風（稀有） */
    public static Equipment steelCape() {
        return new Equipment("鋼鐵披風", EquipSlot.CAPE,
            ItemRarity.RARE, "DEF +8  STR +2  HP +50",
            2, 0, 0, 0, 0, 8, 50, 0, new Color(80, 140, 255));
    }

    // ── Getter ───────────────────────────────────────────────
    public String     getName()         { return name; }
    public EquipSlot  getSlot()         { return slot; }
    public ItemRarity getRarity()       { return rarity; }
    public String     getDescription()  { return description; }
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
