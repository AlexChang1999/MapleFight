package maplestory.item;

import java.awt.Color;

/**
 * 道具稀有度。
 * 影響掉落物的顯示顏色與描述文字。
 */
public enum ItemRarity {

    COMMON   ("普通", new Color(210, 210, 210),   8),
    UNCOMMON ("優良", new Color( 80, 220,  80),  30),
    RARE     ("稀有", new Color( 80, 140, 255),  80),
    EPIC     ("史詩", new Color(175,  80, 255), 220),
    LEGENDARY("傳說", new Color(255, 165,  30), 500);

    public final String displayName;
    public final Color  color;
    public final int    sellValue; // 賣給商人的基礎價格

    ItemRarity(String name, Color c, int sellValue) {
        this.displayName = name;
        this.color       = c;
        this.sellValue   = sellValue;
    }
}
