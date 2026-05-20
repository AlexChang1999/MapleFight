package maplestory.core;

import javax.swing.*;
import java.awt.*;

/**
 * 遊戲主視窗。
 * 流程：TitleScreen（選檔）→ GamePanel（遊戲）
 * 使用 CardLayout 在同一視窗內切換畫面，不重新建立 JFrame。
 */
public class GameWindow extends JFrame {

    private static final String CARD_TITLE = "title";
    private static final String CARD_GAME  = "game";

    private final CardLayout    cardLayout = new CardLayout();
    private final JPanel        cardPanel  = new JPanel(cardLayout);

    private TitleScreen titleScreen;
    private GamePanel   gamePanel;

    // ─────────────────────────────────────────────────────────
    public GameWindow() {
        setTitle("楓之谷 - 火柴人冒險");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // 先建立標題畫面
        titleScreen = new TitleScreen(this::onStart);
        cardPanel.add(titleScreen, CARD_TITLE);

        // GamePanel 延後到「開始」時才建立（因為需要知道存檔槽）
        add(cardPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // 預先把視窗大小固定好（避免切換後閃爍）
        setSize(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT + getInsets().top + getInsets().bottom);
        setLocationRelativeTo(null);
    }

    /**
     * TitleScreen 選擇存檔後的回呼。
     * @param slot       存檔槽 1~3
     * @param playerName 新遊戲 = 角色名稱，繼續遊戲 = null
     */
    private void onStart(int slot, String playerName) {
        // 建立或重建 GamePanel
        if (gamePanel != null) {
            cardPanel.remove(gamePanel);
        }
        gamePanel = new GamePanel(slot, playerName);  // 傳入存檔資訊
        cardPanel.add(gamePanel, CARD_GAME);

        // 切換到遊戲畫面
        cardLayout.show(cardPanel, CARD_GAME);
        gamePanel.requestFocusInWindow();
        gamePanel.startGameLoop();
    }

    /**
     * 從遊戲內返回標題畫面（存檔後）。
     * 目前未使用，預留給「返回主選單」功能。
     */
    public void returnToTitle() {
        if (gamePanel != null) {
            gamePanel.stopGameLoop();
        }
        // 刷新存檔摘要後顯示標題
        titleScreen = new TitleScreen(this::onStart);
        cardPanel.add(titleScreen, CARD_TITLE);
        cardLayout.show(cardPanel, CARD_TITLE);
        titleScreen.requestFocusInWindow();
    }
}
