package maplestory.input;

import maplestory.entity.Player;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 負責監聽鍵盤輸入，並把指令傳給玩家。
 * 繼承 KeyAdapter（只需要覆寫需要的方法，不用全部實作）。
 *
 * 按鍵配置：
 *   ← →     移動
 *   Space   跳躍
 *   Z       普通攻擊（交替劈砍 / 突刺）
 *   Q       技能一：突刺（高傷害單體）
 *   W       技能二：衝擊波（範圍嘲諷）
 *
 * 注意：Q / W 技能需要怪物列表才能計算傷害，
 *       因此先存放在 pendingSkill 欄位，
 *       由 GamePanel.update() 在每幀取出並執行。
 */
public class InputHandler extends KeyAdapter {

    private final Player player;

    /**
     * 待執行的技能索引（-1 = 無）。
     * InputHandler 只負責記錄「玩家想用哪個技能」，
     * 實際帶入 monsters 的呼叫由 GamePanel 處理。
     */
    private int pendingSkill = -1;

    public InputHandler(Player player) {
        this.player = player;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT  -> player.setMovingLeft(true);
            case KeyEvent.VK_RIGHT -> player.setMovingRight(true);
            case KeyEvent.VK_SPACE -> player.jump();
            case KeyEvent.VK_Z     -> player.attack();
            case KeyEvent.VK_Q     -> pendingSkill = 0; // 突刺
            case KeyEvent.VK_W     -> pendingSkill = 1; // 衝擊波
        }
    }

    /** GamePanel 每幀呼叫：取出待發技能並清除（-1 = 無） */
    public int pollPendingSkill() {
        int s = pendingSkill;
        pendingSkill = -1;
        return s;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // 只有移動鍵需要「放開」事件，跳躍和攻擊是即時觸發
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT  -> player.setMovingLeft(false);
            case KeyEvent.VK_RIGHT -> player.setMovingRight(false);
        }
    }
}
