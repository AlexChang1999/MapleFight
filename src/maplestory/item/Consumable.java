package maplestory.item;

/**
 * 消耗品（回血藥 / 回魔藥）。
 *
 * 使用方式：呼叫 use(player) 即回復 HP / MP，
 * 並從背包中移除（由 Inventory 處理）。
 */
public class Consumable {

    private final String     name;
    private final String     description;
    private final ItemRarity rarity;
    private final int        hpRestore;
    private final int        mpRestore;

    private Consumable(String name, String desc, ItemRarity rarity,
                       int hp, int mp) {
        this.name        = name;
        this.description = desc;
        this.rarity      = rarity;
        this.hpRestore   = hp;
        this.mpRestore   = mp;
    }

    // ── 工廠方法 ─────────────────────────────────────────────

    /** 紅色藥水：回復 80 HP */
    public static Consumable redPotion() {
        return new Consumable("紅色藥水", "回復 80 HP", ItemRarity.COMMON, 80, 0);
    }

    /** 橙色藥水：回復 200 HP */
    public static Consumable orangePotion() {
        return new Consumable("橙色藥水", "回復 200 HP", ItemRarity.UNCOMMON, 200, 0);
    }

    /** 藍色藥水：回復 60 MP */
    public static Consumable bluePotion() {
        return new Consumable("藍色藥水", "回復 60 MP", ItemRarity.COMMON, 0, 60);
    }

    /** 魔法藥水：回復 120 MP */
    public static Consumable manaElixir() {
        return new Consumable("魔法藥水", "回復 120 MP", ItemRarity.UNCOMMON, 0, 120);
    }

    /** 萬能藥水：同時回復 150 HP + 80 MP */
    public static Consumable elixir() {
        return new Consumable("萬能藥水", "回復 150 HP 及 80 MP", ItemRarity.RARE, 150, 80);
    }

    // ── 使用 ─────────────────────────────────────────────────

    /**
     * 對玩家套用回復效果。
     * 回傳使用後產生的通知文字（給 GamePanel 顯示浮動提示）。
     */
    public String apply(maplestory.entity.Player player) {
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

    // ── Getter ───────────────────────────────────────────────
    public String     getName()        { return name; }
    public String     getDescription() { return description; }
    public ItemRarity getRarity()      { return rarity; }
    public int        getHpRestore()   { return hpRestore; }
    public int        getMpRestore()   { return mpRestore; }
}
