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

    // 套裝系統
    private final String setId;    // null = 無套裝；"novice_set", "bronze_set" 等
    private final int    reqLevel; // 穿戴等級需求（0 = 無限制）

    // 在火柴人上顯示的顏色
    private final Color displayColor;

    private Equipment(String name, EquipSlot slot, ItemRarity rarity, String desc,
                      int str, int dex, int intel, int luk,
                      int atk, int def, int hp, int mp,
                      Color color) {
        this(name, slot, rarity, desc, str, dex, intel, luk, atk, def, hp, mp, color, null, 0);
    }

    private Equipment(String name, EquipSlot slot, ItemRarity rarity, String desc,
                      int str, int dex, int intel, int luk,
                      int atk, int def, int hp, int mp,
                      Color color, String setId, int reqLevel) {
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
        this.setId        = setId;
        this.reqLevel     = reqLevel;
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

    // ── 套裝工廠（Lv.1–9：新手布衣套裝）────────────────────
    private static final String SET_NOVICE = "novice_set";

    public static Equipment noviceHelmetSet() {
        return new Equipment("新手布衣帽", EquipSlot.HELMET, ItemRarity.COMMON,
            "DEF +4  HP +25  [新手套裝]",
            0, 0, 0, 0, 0, 4, 25, 0, new Color(200, 190, 160), SET_NOVICE, 1);
    }
    public static Equipment noviceTopSet() {
        return new Equipment("新手布衣上衣", EquipSlot.TOP, ItemRarity.COMMON,
            "DEF +3  HP +20  [新手套裝]",
            0, 0, 0, 0, 0, 3, 20, 0, new Color(195, 185, 155), SET_NOVICE, 1);
    }
    public static Equipment noviceBottomSet() {
        return new Equipment("新手布衣長褲", EquipSlot.BOTTOM, ItemRarity.COMMON,
            "DEF +3  HP +15  [新手套裝]",
            0, 0, 0, 0, 0, 3, 15, 0, new Color(175, 165, 140), SET_NOVICE, 1);
    }
    public static Equipment noviceGlovesSet() {
        return new Equipment("新手布衣手套", EquipSlot.GLOVES, ItemRarity.COMMON,
            "DEF +2  [新手套裝]",
            0, 0, 0, 0, 0, 2, 0, 0, new Color(210, 200, 175), SET_NOVICE, 1);
    }
    public static Equipment noviceBootsSet() {
        return new Equipment("新手布衣鞋", EquipSlot.BOOTS, ItemRarity.COMMON,
            "DEF +2  [新手套裝]",
            0, 0, 0, 0, 0, 2, 0, 0, new Color(185, 170, 140), SET_NOVICE, 1);
    }
    public static Equipment noviceWeaponSet() {
        return new Equipment("新手木劍", EquipSlot.WEAPON, ItemRarity.COMMON,
            "ATK +4  [新手套裝]",
            0, 0, 0, 0, 4, 0, 0, 0, new Color(160, 130, 80), SET_NOVICE, 1);
    }
    public static Equipment noviceCapeSet() {
        return new Equipment("新手布披風", EquipSlot.CAPE, ItemRarity.COMMON,
            "DEF +1  HP +10  [新手套裝]",
            0, 0, 0, 0, 0, 1, 10, 0, new Color(215, 200, 160), SET_NOVICE, 1);
    }

    // ── 套裝工廠（Lv.10–19：青銅戰士套裝）───────────────────
    private static final String SET_BRONZE = "bronze_set";

    public static Equipment bronzeHelmet() {
        return new Equipment("青銅戰士頭盔", EquipSlot.HELMET, ItemRarity.UNCOMMON,
            "DEF +8  HP +50  STR +2  [青銅套裝]",
            2, 0, 0, 0, 0, 8, 50, 0, new Color(180, 130, 60), SET_BRONZE, 10);
    }
    public static Equipment bronzeTop() {
        return new Equipment("青銅戰士上衣", EquipSlot.TOP, ItemRarity.UNCOMMON,
            "DEF +6  HP +40  STR +1  [青銅套裝]",
            1, 0, 0, 0, 0, 6, 40, 0, new Color(175, 125, 55), SET_BRONZE, 10);
    }
    public static Equipment bronzeBottom() {
        return new Equipment("青銅戰士長褲", EquipSlot.BOTTOM, ItemRarity.UNCOMMON,
            "DEF +5  HP +30  [青銅套裝]",
            0, 0, 0, 0, 0, 5, 30, 0, new Color(160, 115, 50), SET_BRONZE, 10);
    }
    public static Equipment bronzeGloves() {
        return new Equipment("青銅戰士手套", EquipSlot.GLOVES, ItemRarity.UNCOMMON,
            "DEF +4  ATK +2  [青銅套裝]",
            0, 0, 0, 0, 2, 4, 0, 0, new Color(190, 140, 65), SET_BRONZE, 10);
    }
    public static Equipment bronzeBoots() {
        return new Equipment("青銅戰士靴", EquipSlot.BOOTS, ItemRarity.UNCOMMON,
            "DEF +4  HP +20  [青銅套裝]",
            0, 0, 0, 0, 0, 4, 20, 0, new Color(165, 120, 55), SET_BRONZE, 10);
    }
    public static Equipment bronzeSword() {
        return new Equipment("青銅戰士劍", EquipSlot.WEAPON, ItemRarity.UNCOMMON,
            "ATK +12  STR +2  [青銅套裝]",
            2, 0, 0, 0, 12, 0, 0, 0, new Color(130, 180, 220), SET_BRONZE, 10);
    }
    public static Equipment bronzeCape() {
        return new Equipment("青銅戰士披風", EquipSlot.CAPE, ItemRarity.UNCOMMON,
            "DEF +3  HP +25  [青銅套裝]",
            0, 0, 0, 0, 0, 3, 25, 0, new Color(150, 110, 45), SET_BRONZE, 10);
    }

    // ── 套裝工廠（Lv.20–29：翠玉森林套裝）───────────────────
    private static final String SET_JADE = "jade_set";

    public static Equipment jadeHelmet() {
        return new Equipment("翠玉森林頭盔", EquipSlot.HELMET, ItemRarity.RARE,
            "DEF +14  HP +80  DEX +3  [翠玉套裝]",
            0, 3, 0, 0, 0, 14, 80, 0, new Color(60, 160, 90), SET_JADE, 20);
    }
    public static Equipment jadeTop() {
        return new Equipment("翠玉森林上衣", EquipSlot.TOP, ItemRarity.RARE,
            "DEF +12  HP +65  DEX +2  [翠玉套裝]",
            0, 2, 0, 0, 0, 12, 65, 0, new Color(55, 150, 85), SET_JADE, 20);
    }
    public static Equipment jadeBottom() {
        return new Equipment("翠玉森林長褲", EquipSlot.BOTTOM, ItemRarity.RARE,
            "DEF +10  HP +50  [翠玉套裝]",
            0, 0, 0, 0, 0, 10, 50, 0, new Color(50, 140, 80), SET_JADE, 20);
    }
    public static Equipment jadeGloves() {
        return new Equipment("翠玉森林手套", EquipSlot.GLOVES, ItemRarity.RARE,
            "DEF +6  ATK +5  DEX +2  [翠玉套裝]",
            0, 2, 0, 0, 5, 6, 0, 0, new Color(65, 170, 95), SET_JADE, 20);
    }
    public static Equipment jadeBoots() {
        return new Equipment("翠玉森林靴", EquipSlot.BOOTS, ItemRarity.RARE,
            "DEF +6  HP +35  DEX +1  [翠玉套裝]",
            0, 1, 0, 0, 0, 6, 35, 0, new Color(55, 155, 85), SET_JADE, 20);
    }
    public static Equipment jadeBow() {
        return new Equipment("翠玉長弓", EquipSlot.WEAPON, ItemRarity.RARE,
            "ATK +18  DEX +4  [翠玉套裝]",
            0, 4, 0, 0, 18, 0, 0, 0, new Color(80, 200, 110), SET_JADE, 20);
    }
    public static Equipment jadeStaff() {
        return new Equipment("翠玉法杖", EquipSlot.WEAPON, ItemRarity.RARE,
            "ATK +15  INT +5  MP +30  [翠玉套裝]",
            0, 0, 5, 0, 15, 0, 0, 30, new Color(100, 220, 130), SET_JADE, 20);
    }
    public static Equipment jadeCape() {
        return new Equipment("翠玉森林披風", EquipSlot.CAPE, ItemRarity.RARE,
            "DEF +5  HP +45  DEX +2  [翠玉套裝]",
            0, 2, 0, 0, 0, 5, 45, 0, new Color(45, 135, 75), SET_JADE, 20);
    }

    // ── 古老森林 Boss 掉落 ────────────────────────────────────

    /** 森之核心護符：DEF +12  HP +60  MP +20（葛羅芬掉落） */
    public static Equipment forestCoreAmulet() {
        return new Equipment("森之核心護符", EquipSlot.EARRING, ItemRarity.EPIC,
            "DEF +12  HP +60  MP +20",
            0, 0, 0, 0, 0, 12, 60, 20, new Color(60, 210, 80), null, 20);
    }

    /** 藤蔓纏腕：ATK +8  DEF +5  HP +30（葛羅芬掉落） */
    public static Equipment vineWristguard() {
        return new Equipment("藤蔓纏腕", EquipSlot.GLOVES, ItemRarity.EPIC,
            "ATK +8  DEF +5  HP +30",
            0, 0, 0, 0, 8, 5, 30, 0, new Color(50, 170, 65), null, 20);
    }

    // ── 沙漠廢墟 Boss 掉落 ────────────────────────────────────

    /** 法老彎刀：ATK +35  STR +5（法拉歐掉落，劍士最強武器） */
    public static Equipment pharaohCurvedSword() {
        return new Equipment("法老彎刀", EquipSlot.WEAPON, ItemRarity.LEGENDARY,
            "ATK +35  STR +5",
            5, 0, 0, 0, 35, 0, 0, 0, new Color(200, 170, 50), null, 30);
    }

    /** 聖甲蟲護符：ATK +20  DEF +15  HP +50（法拉歐掉落） */
    public static Equipment scarabAmulet() {
        return new Equipment("聖甲蟲護符", EquipSlot.EARRING, ItemRarity.LEGENDARY,
            "ATK +20  DEF +15  HP +50",
            0, 0, 0, 0, 20, 15, 50, 0, new Color(50, 140, 220), null, 30);
    }

    // ── 套裝加成計算（由 Player.recalculateStats() 呼叫） ──────
    /** 計算套裝件數並回傳加成（返回 {strBonus, defBonus, atkBonus, hpBonus}） */
    public static int[] calcSetBonus(java.util.Collection<Equipment> equipped) {
        java.util.Map<String, Long> counts = equipped.stream()
            .filter(e -> e.getSetId() != null)
            .collect(java.util.stream.Collectors.groupingBy(
                Equipment::getSetId, java.util.stream.Collectors.counting()));

        int str = 0, def = 0, atk = 0, hp = 0;
        for (var entry : counts.entrySet()) {
            long cnt = entry.getValue();
            switch (entry.getKey()) {
                case "novice_set" -> {
                    if (cnt >= 2) { def += 5;  hp += 20; }
                    if (cnt >= 4) { atk += 4;  hp += 30; }
                }
                case "bronze_set" -> {
                    if (cnt >= 2) { str += 4;  def += 8; }
                    if (cnt >= 4) { atk += 10; hp += 60; }
                }
                case "jade_set" -> {
                    if (cnt >= 2) { def += 12; hp += 50; }
                    if (cnt >= 4) { atk += 16; hp += 80; }
                }
            }
        }
        return new int[]{str, def, atk, hp};
    }

    // ── Getter ───────────────────────────────────────────────
    public String     getName()         { return name; }
    public EquipSlot  getSlot()         { return slot; }
    public ItemRarity getRarity()       { return rarity; }
    public String     getDescription()  { return description; }
    public int        getStrBonus()     { return strBonus; }
    public int        getDexBonus()     { return dexBonus; }
    public int        getIntelBonus()   { return intelBonus; }
    public int        getLukBonus()     { return lukBonus; }
    public int        getAtkBonus()     { return atkBonus; }
    public int        getDefBonus()     { return defBonus; }
    public int        getHpBonus()      { return hpBonus; }
    public int        getMpBonus()      { return mpBonus; }
    public Color      getDisplayColor() { return displayColor; }
    public String     getSetId()        { return setId; }
    public int        getReqLevel()     { return reqLevel; }
}
