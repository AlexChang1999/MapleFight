package maplestory.keybind;

import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * 按鍵配置管理器（資料層）。
 * 維護「KeyCode → ActionType」的雙向映射。
 *
 * 設計：
 *   - 每個 ActionType 最多對應一個鍵（1 對 1）
 *   - 重新綁定時自動解除舊綁定（不會衝突）
 *   - GamePanel / InputHandler 都透過此物件查詢動作
 *   - 未來新增 ActionType 時，只需在 setDefault() 加一行即可
 */
public class KeyBindingManager {

    // ── 雙向映射 ─────────────────────────────────────────────
    /** keyCode  → 動作（輸入系統用） */
    private final Map<Integer, ActionType>    fwd = new HashMap<>();
    /** 動作     → keyCode（UI 顯示「目前綁定在哪個鍵」用） */
    private final Map<ActionType, Integer>    rev = new EnumMap<>(ActionType.class);

    // ─────────────────────────────────────────────────────────
    public KeyBindingManager() {
        resetToDefault();
    }

    /** 還原預設按鍵配置 */
    public void resetToDefault() {
        fwd.clear();
        rev.clear();

        // ── 移動 ─────────────────────────────────────────────
        bind(KeyEvent.VK_LEFT,  ActionType.MOVE_LEFT);
        bind(KeyEvent.VK_RIGHT, ActionType.MOVE_RIGHT);
        bind(KeyEvent.VK_SPACE, ActionType.JUMP);

        // ── 戰鬥 ─────────────────────────────────────────────
        bind(KeyEvent.VK_UP,   ActionType.CLIMB_UP);
        bind(KeyEvent.VK_DOWN, ActionType.CLIMB_DOWN);
        bind(KeyEvent.VK_Z, ActionType.ATTACK);
        bind(KeyEvent.VK_Q, ActionType.SKILL_0);
        bind(KeyEvent.VK_W, ActionType.SKILL_1);

        // ── 介面 ─────────────────────────────────────────────
        bind(KeyEvent.VK_S, ActionType.UI_STATUS);
        bind(KeyEvent.VK_K, ActionType.UI_SKILL);
        bind(KeyEvent.VK_E, ActionType.UI_EQUIP);
        bind(KeyEvent.VK_B, ActionType.UI_KEYBIND);
    }

    // ── 綁定 / 解綁 ──────────────────────────────────────────

    /**
     * 將 keyCode 綁定到 action。
     * 若 action 已綁到其他鍵，先解除；
     * 若 keyCode 已有其他 action，先解除。
     */
    public void bind(int keyCode, ActionType action) {
        // 解除 action 舊綁定
        Integer oldKey = rev.get(action);
        if (oldKey != null) fwd.remove(oldKey);

        // 解除 keyCode 舊 action
        ActionType oldAction = fwd.get(keyCode);
        if (oldAction != null) rev.remove(oldAction);

        fwd.put(keyCode, action);
        rev.put(action, keyCode);
    }

    /** 解除某個鍵的綁定（從鍵盤上拖走不放回就呼叫此） */
    public void unbind(int keyCode) {
        ActionType action = fwd.remove(keyCode);
        if (action != null) rev.remove(action);
    }

    // ── 查詢 ─────────────────────────────────────────────────

    /** 根據 keyCode 查詢動作（找不到回傳 null） */
    public ActionType getAction(int keyCode) {
        return fwd.get(keyCode);
    }

    /** 查詢某個動作目前綁在哪個鍵（找不到回傳 null） */
    public Integer getKeyFor(ActionType action) {
        return rev.get(action);
    }

    /** 取得所有綁定（唯讀，給 UI 顯示用） */
    public Map<Integer, ActionType> getAll() {
        return Collections.unmodifiableMap(fwd);
    }

    /**
     * 把 keyCode 轉換成顯示用文字。
     * 例：VK_LEFT → "←"，VK_SPACE → "Space"，VK_Q → "Q"
     */
    public static String keyName(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.VK_LEFT  -> "←";
            case KeyEvent.VK_RIGHT -> "→";
            case KeyEvent.VK_UP    -> "↑";
            case KeyEvent.VK_DOWN  -> "↓";
            case KeyEvent.VK_SPACE -> "Space";
            case KeyEvent.VK_ENTER -> "Enter";
            case KeyEvent.VK_BACK_SPACE -> "BS";
            case KeyEvent.VK_SHIFT -> "Shift";
            case KeyEvent.VK_CONTROL -> "Ctrl";
            case KeyEvent.VK_ALT   -> "Alt";
            default -> KeyEvent.getKeyText(keyCode);
        };
    }
}
