package maplestory.input;

import maplestory.entity.Player;
import maplestory.keybind.ActionType;
import maplestory.keybind.KeyBindingManager;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 鍵盤輸入處理器。
 * 不再寫死按鍵，改為透過 KeyBindingManager 查詢動作。
 *
 * 只處理「遊戲操作類」動作（Category.GAME）；
 * 「介面開關類」（Category.UI）由 GamePanel 的另一個 KeyAdapter 處理。
 *
 * 待發技能（Q / W）存入 pendingSkill，
 * 由 GamePanel.update() 每幀取出並帶入怪物列表後執行。
 */
public class InputHandler extends KeyAdapter {

    private final Player            player;
    private final KeyBindingManager bindings;

    /** 待執行的技能索引（-1 = 無），GamePanel 每幀 poll 一次 */
    private int pendingSkill = -1;

    // ─────────────────────────────────────────────────────────
    public InputHandler(Player player, KeyBindingManager bindings) {
        this.player   = player;
        this.bindings = bindings;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        ActionType action = bindings.getAction(e.getKeyCode());
        if (action == null) return;

        // 只處理 GAME 類動作（UI 開關由 GamePanel 處理）
        switch (action) {
            case MOVE_LEFT   -> player.setMovingLeft(true);
            case MOVE_RIGHT  -> player.setMovingRight(true);
            case CLIMB_UP    -> player.setMovingUp(true);
            case CLIMB_DOWN  -> player.setMovingDown(true);
            case JUMP        -> player.jump();
            case ATTACK      -> player.attack();
            case SKILL_0     -> pendingSkill = 0;
            case SKILL_1     -> pendingSkill = 1;
            default          -> {}
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        ActionType action = bindings.getAction(e.getKeyCode());
        if (action == null) return;

        // 「放開」只影響持續性動作（移動）
        switch (action) {
            case MOVE_LEFT  -> player.setMovingLeft(false);
            case MOVE_RIGHT -> player.setMovingRight(false);
            case CLIMB_UP   -> player.setMovingUp(false);
            case CLIMB_DOWN -> player.setMovingDown(false);
            default         -> {}
        }
    }

    /** GamePanel 每幀呼叫：取出待發技能索引，並清除（-1 = 無） */
    public int pollPendingSkill() {
        int s = pendingSkill;
        pendingSkill = -1;
        return s;
    }
}
