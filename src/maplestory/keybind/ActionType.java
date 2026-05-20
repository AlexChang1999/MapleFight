package maplestory.keybind;

import java.awt.Color;

/**
 * 所有可綁定的遊戲動作。
 * 新增功能時只需在此列舉加入新項目，
 * UI 面板會自動顯示，不需修改其他地方。
 *
 * 動作分三類：
 *   GAME_*  → 遊戲內操作（移動、攻擊、技能）
 *   UI_*    → 介面開關
 */
public enum ActionType {

    // ── 移動 ─────────────────────────────────────────────────
    MOVE_LEFT ("向左移動",   Category.GAME,  new Color(100, 160, 255)),
    MOVE_RIGHT("向右移動",   Category.GAME,  new Color(100, 160, 255)),
    JUMP      ("跳躍",       Category.GAME,  new Color( 80, 210, 255)),

    // ── 移動（延伸） ─────────────────────────────────────────
    CLIMB_UP  ("向上攀爬",   Category.GAME,  new Color( 80, 215, 200)),
    CLIMB_DOWN("向下蹲伏",   Category.GAME,  new Color( 80, 215, 200)),

    // ── 戰鬥 ─────────────────────────────────────────────────
    ATTACK    ("普通攻擊",   Category.GAME,  new Color(255, 200, 60)),
    SKILL_0   ("技能：突刺", Category.GAME,  new Color(180,  90, 255)),
    SKILL_1   ("技能：衝擊波", Category.GAME, new Color(140,  80, 230)),

    // ── 介面開關 ─────────────────────────────────────────────
    UI_STATUS ("開啟狀態面板", Category.UI, new Color( 80, 200, 120)),
    UI_SKILL  ("開啟技能面板", Category.UI, new Color( 80, 200, 120)),
    UI_EQUIP  ("開啟裝備面板", Category.UI, new Color( 80, 200, 120)),
    UI_KEYBIND("按鍵設定",    Category.UI, new Color(180, 180, 180));

    // ─────────────────────────────────────────────────────────
    public final String   displayName; // 面板上顯示的名稱
    public final Category category;    // 分類（用於在面板分組）
    public final Color    color;       // 代表色（拖曳 chip 顏色）

    ActionType(String name, Category category, Color color) {
        this.displayName = name;
        this.category    = category;
        this.color       = color;
    }

    // ── 動作分類 ─────────────────────────────────────────────
    public enum Category {
        GAME ("遊戲操作"),
        UI   ("介面開關");

        public final String label;
        Category(String label) { this.label = label; }
    }
}
