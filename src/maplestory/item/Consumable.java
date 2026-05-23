package maplestory.item;

/**
 * 消耗品（回血藥 / 回魔藥 / 傳送卷軸）。
 *
 * 傳送卷軸：teleportMapId != null，apply() 回傳 "TELEPORT:<mapId>"，
 * 由 GamePanel 解析並執行地圖切換。
 */
public class Consumable {

    private final String     name;
    private final String     description;
    private final ItemRarity rarity;
    private final int        hpRestore;
    private final int        mpRestore;
    private final String     teleportMapId; // null = 非卷軸
    private int              quantity = 1;  // 這疊的數量，最多 200

    private Consumable(String name, String desc, ItemRarity rarity,
                       int hp, int mp, String teleportMapId) {
        this.name          = name;
        this.description   = desc;
        this.rarity        = rarity;
        this.hpRestore     = hp;
        this.mpRestore     = mp;
        this.teleportMapId = teleportMapId;
    }

    // ── 工廠方法（藥水）────────────────────────────────────

    /** 紅色藥水：回復 80 HP */
    public static Consumable redPotion() {
        return new Consumable("紅色藥水", "回復 80 HP", ItemRarity.COMMON, 80, 0, null);
    }

    /** 橙色藥水：回復 200 HP */
    public static Consumable orangePotion() {
        return new Consumable("橙色藥水", "回復 200 HP", ItemRarity.UNCOMMON, 200, 0, null);
    }

    /** 藍色藥水：回復 60 MP */
    public static Consumable bluePotion() {
        return new Consumable("藍色藥水", "回復 60 MP", ItemRarity.COMMON, 0, 60, null);
    }

    /** 魔法藥水：回復 120 MP */
    public static Consumable manaElixir() {
        return new Consumable("魔法藥水", "回復 120 MP", ItemRarity.UNCOMMON, 0, 120, null);
    }

    /** 萬能藥水：同時回復 150 HP + 80 MP */
    public static Consumable elixir() {
        return new Consumable("萬能藥水", "回復 150 HP 及 80 MP", ItemRarity.RARE, 150, 80, null);
    }

    // ── 工廠方法（傳送卷軸）────────────────────────────────

    /** 回新手村卷軸：傳送至新手村 */
    public static Consumable returnToVillage() {
        return new Consumable("回新手村卷軸", "傳送回新手村", ItemRarity.UNCOMMON, 0, 0, "village");
    }

    /** 回前線前哨站卷軸 */
    public static Consumable returnToFrontier() {
        return new Consumable("回前哨站卷軸", "傳送回前線前哨站", ItemRarity.UNCOMMON, 0, 0, "frontier");
    }

    /** 回冒險平原卷軸 */
    public static Consumable returnToBattle() {
        return new Consumable("回冒險平原卷軸", "傳送回冒險平原", ItemRarity.RARE, 0, 0, "battle");
    }


    /**
     * 回家卷軸：自動偵測所在區域，傳送回對應村莊中央。
     * novice1/2/3 → 新手村，battle → 前哨站，arctic → 冰原驛站。
     * teleportMapId = "HOME"，由 GamePanel 動態解析目標地圖。
     */
    public static Consumable returnHome() {
        return new Consumable("回家卷軸", "傳送回所在區域的村莊中央", ItemRarity.UNCOMMON, 0, 0, "HOME");
    }

    /** 新手村村莊卷軸：傳送至新手村地圖正中央 */
    public static Consumable villageScrollVillage() {
        return new Consumable("新手村村莊卷軸", "傳送至新手村正中央", ItemRarity.RARE, 0, 0, "VCENTER:village");
    }

    /** 前哨站村莊卷軸：傳送至前線前哨站地圖正中央 */
    public static Consumable villageScrollFrontier() {
        return new Consumable("前哨站村莊卷軸", "傳送至前線前哨站正中央", ItemRarity.RARE, 0, 0, "VCENTER:frontier");
    }

    /** 冰原驛站村莊卷軸：傳送至冰原驛站地圖正中央 */
    public static Consumable villageScrollIcePost() {
        return new Consumable("冰原驛站村莊卷軸", "傳送至冰原驛站正中央", ItemRarity.RARE, 0, 0, "VCENTER:icepost");
    }
    // ── Boss 專屬消耗品 ───────────────────────────────────────

    /** 古老森林精華：回復 60% 最大 HP（葛羅芬掉落） */
    public static Consumable ancientForestEssence() {
        return new Consumable("古老森林精華",
            "回復大量 HP（依最大 HP 60% 計算）",
            ItemRarity.EPIC, -1, 0, null); // hp=-1 代表百分比回復（apply 中特判）
    }

    /** 沙漠法老繃帶：回復 80% 最大 HP 並解除緩速狀態（法拉歐掉落） */
    public static Consumable pharaohBandage() {
        return new Consumable("沙漠法老繃帶",
            "回復大量 HP 並解除所有負面狀態",
            ItemRarity.EPIC, -2, 0, null); // hp=-2 代表 80% + 解除 debuff
    }
    // ── 使用 ─────────────────────────────────────────────────

    /**
     * 對玩家套用效果。
     * - 一般藥水：回傳回復提示文字（如 "+80 HP"）
     * - 傳送卷軸：回傳 "TELEPORT:<mapId>"，由 GamePanel 解析執行傳送
     */
    public String apply(maplestory.entity.Player player) {
        if (teleportMapId != null) {
            return "TELEPORT:" + teleportMapId;
        }
        // Boss 專屬：百分比回復
        if (hpRestore == -1) { // 古老森林精華：60% 最大 HP
            int heal = (int)(player.getMaxHp() * 0.60);
            int actual = player.healHp(heal);
            return actual > 0 ? "+" + actual + " HP（森林精華）" : "HP 已滿";
        }
        if (hpRestore == -2) { // 沙漠法老繃帶：80% 最大 HP + 解除 debuff
            int heal = (int)(player.getMaxHp() * 0.80);
            int actual = player.healHp(heal);
            player.clearDebuffs();
            return actual > 0 ? "+" + actual + " HP + 解除異常狀態" : "HP 已滿（異常狀態已解除）";
        }
        StringBuilder sb = new StringBuilder();
        if (hpRestore > 0) {
            int actual = player.healHp(hpRestore);
            if (actual > 0) sb.append("+").append(actual).append(" HP ");
        }
        if (mpRestore > 0) {
            int actual = player.healMp(mpRestore);
            if (actual > 0) sb.append("+").append(actual).append(" MP");
        }
        return sb.toString().trim();
    }

    // ── Getter / Setter（數量管理）───────────────────────────
    public String     getName()          { return name; }
    public String     getDescription()   { return description; }
    public ItemRarity getRarity()        { return rarity; }
    public int        getHpRestore()     { return hpRestore; }
    public int        getMpRestore()     { return mpRestore; }
    public String     getTeleportMapId() { return teleportMapId; }
    public boolean    isTeleportScroll() { return teleportMapId != null; }

    public int  getQuantity()          { return quantity; }
    public void addQuantity(int delta) { this.quantity += delta; }
    public void decrement()            { if (quantity > 0) quantity--; }
}
