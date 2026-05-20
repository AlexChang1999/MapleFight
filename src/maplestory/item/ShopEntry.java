package maplestory.item;

import java.util.function.Supplier;

/**
 * 商店單一商品條目。
 *
 * 使用 Supplier 工廠模式，每次購買都產生全新的物件實例，
 * 避免多次購買同一件裝備時共享同一個 Java 物件而產生 bug。
 *
 * 使用範例：
 *   ShopEntry.consumable(Consumable::redPotion, 20)
 *   ShopEntry.equipment(Equipment::noviceHelmet, 150)
 */
public class ShopEntry {

    public enum Kind { CONSUMABLE, EQUIPMENT }

    private final Kind       kind;
    private final int        price;
    private final String     name;
    private final String     desc;
    private final ItemRarity rarity;
    private final Supplier<?> factory;

    private ShopEntry(Kind kind, int price,
                      String name, String desc, ItemRarity rarity,
                      Supplier<?> factory) {
        this.kind    = kind;
        this.price   = price;
        this.name    = name;
        this.desc    = desc;
        this.rarity  = rarity;
        this.factory = factory;
    }

    // ── 工廠方法 ─────────────────────────────────────────────

    public static ShopEntry consumable(Supplier<Consumable> f, int price) {
        Consumable sample = f.get();
        return new ShopEntry(Kind.CONSUMABLE, price,
                             sample.getName(), sample.getDescription(),
                             sample.getRarity(), f);
    }

    public static ShopEntry equipment(Supplier<Equipment> f, int price) {
        Equipment sample = f.get();
        return new ShopEntry(Kind.EQUIPMENT, price,
                             sample.getName(), sample.getDescription(),
                             sample.getRarity(), f);
    }

    // ── 購買時建立新實例 ──────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Consumable createConsumable() {
        return ((Supplier<Consumable>) factory).get();
    }

    @SuppressWarnings("unchecked")
    public Equipment createEquipment() {
        return ((Supplier<Equipment>) factory).get();
    }

    // ── Getter ───────────────────────────────────────────────
    public Kind       getKind()    { return kind; }
    public int        getPrice()   { return price; }
    public String     getName()    { return name; }
    public String     getDesc()    { return desc; }
    public ItemRarity getRarity()  { return rarity; }
}
