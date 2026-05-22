package maplestory.entity;

import java.awt.Color;

/**
 * 角色外觀設定 — 可延伸的楓之谷風格外觀系統
 *
 * 使用方式：
 *   player.setAppearance(Appearance.WARRIOR);      // 套用預設
 *   player.setAppearance(new Appearance(           // 自訂
 *       Appearance.HairStyle.LONG,
 *       new Color(100, 60, 160),                   // 紫髮
 *       Appearance.EyeStyle.BRIGHT,
 *       new Color(150, 80, 220),                   // 紫眼
 *       Appearance.EyebrowStyle.THIN,
 *       new Color(255, 220, 175)));                // 白皙膚色
 *
 * 擴充方式：在各 enum 新增項目，並在 Player 的對應 draw 方法加 case 即可。
 */
public class Appearance {

    // ── 髮型 ──────────────────────────────────────────────────
    public enum HairStyle {
        SPIKY,     // 刺刺頭（楓之谷劍士風，多根向上尖刺）
        SHORT,     // 短刺頭（精幹型，兩根小刺）
        STRAIGHT,  // 直髮蓋額（清秀型，側邊垂髮+瀏海）
        BOWL,      // 西瓜頭（可愛型，碗狀覆蓋）
        LONG       // 長髮（垂肩型，側發延伸至肩膀）
    }

    // ── 眼型 ──────────────────────────────────────────────────
    public enum EyeStyle {
        ROUND,     // 大圓眼（可愛型，大高光）
        BRIGHT,    // 星形亮眼（楓之谷主角風，雙重高光）
        SHARP      // 銳利眼（戰士型，橫向細長）
    }

    // ── 眉型 ──────────────────────────────────────────────────
    public enum EyebrowStyle {
        THICK,     // 粗眉（楓之谷男角經典款）
        ARCHED,    // 拱形眉（優雅弧形）
        FLAT,      // 平眉（中性款）
        THIN       // 細眉（楓之谷女角款）
    }

    // ── 欄位（final，保證不可變）──────────────────────────────
    public final HairStyle     hairStyle;
    public final Color         hairColor;
    public final EyeStyle      eyeStyle;
    public final Color         eyeColor;
    public final EyebrowStyle  eyebrowStyle;
    public final Color         skinColor;

    /** 完整自訂建構子 */
    public Appearance(HairStyle hairStyle,    Color hairColor,
                      EyeStyle eyeStyle,      Color eyeColor,
                      EyebrowStyle eyebrowStyle, Color skinColor) {
        this.hairStyle    = hairStyle;
        this.hairColor    = hairColor;
        this.eyeStyle     = eyeStyle;
        this.eyeColor     = eyeColor;
        this.eyebrowStyle = eyebrowStyle;
        this.skinColor    = skinColor;
    }

    // ── 內建預設外觀（可直接套用）────────────────────────────

    /** 預設新手外觀：刺刺棕髮＋藍色亮眼 */
    public static final Appearance DEFAULT = new Appearance(
        HairStyle.SPIKY,
        new Color(55, 35, 15),    // 深棕黑髮
        EyeStyle.BRIGHT,
        new Color(60, 110, 210),  // 藍色眼睛
        EyebrowStyle.THICK,
        new Color(255, 215, 165)  // 暖膚色
    );

    /** 劍士：短黑髮＋銳利深棕眼 */
    public static final Appearance WARRIOR = new Appearance(
        HairStyle.SHORT,
        new Color(30, 20, 10),    // 黑髮
        EyeStyle.SHARP,
        new Color(60, 40, 20),    // 深棕眼
        EyebrowStyle.THICK,
        new Color(240, 200, 150)  // 略深膚色
    );

    /** 法師：長紫髮＋紫色亮眼 */
    public static final Appearance MAGE = new Appearance(
        HairStyle.LONG,
        new Color(100, 60, 160),  // 紫髮
        EyeStyle.BRIGHT,
        new Color(150, 80, 220),  // 紫眼
        EyebrowStyle.THIN,
        new Color(255, 220, 175)  // 白皙膚色
    );

    /** 弓箭手：直金髮＋綠色圓眼 */
    public static final Appearance BOWMAN = new Appearance(
        HairStyle.STRAIGHT,
        new Color(190, 150, 30),  // 金髮
        EyeStyle.ROUND,
        new Color(50, 130, 60),   // 綠眼
        EyebrowStyle.ARCHED,
        new Color(255, 215, 165)
    );

    /** 盜賊：西瓜頭黑髮＋紅色銳眼 */
    public static final Appearance THIEF = new Appearance(
        HairStyle.BOWL,
        new Color(25, 15, 10),    // 深黑髮
        EyeStyle.SHARP,
        new Color(200, 50, 50),   // 紅眼
        EyebrowStyle.FLAT,
        new Color(245, 205, 155)
    );
}
