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
 *   Z       普通攻擊
 */
public class InputHandler extends KeyAdapter {

    private final Player player;

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
        }
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
