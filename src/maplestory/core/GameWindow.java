package maplestory.core;

import javax.swing.*;
import java.awt.*;

/**
 * 遊戲主視窗。
 * 流程：TitleScreen（選檔）→ GamePanel（遊戲）
 * 使用 CardLayout 在同一視窗內切換畫面，不重新建立 JFrame。
 *
 * 視窗可自由縮放（等比縮放），邏輯解析度保持 800x580。
 */
public class GameWindow extends JFrame {

    private static final String CARD_TITLE = "title";
    private static final String CARD_GAME  = "game";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     cardPanel  = new JPanel(cardLayout);

    private TitleScreen titleScreen;
    private GamePanel   gamePanel;

    // ─────────────────────────────────────────────────────────
    public GameWindow() {
        setTitle("楓之谷 - 火柴人冒險");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);  // 允許使用者自由縮放視窗

        // 最小視窗大小（保持可玩性）
        setMinimumSize(new Dimension(480, 360));

        // 建立標題畫面
        titleScreen = new TitleScreen(this::onStart);
        cardPanel.add(titleScreen, CARD_TITLE);

        add(cardPanel);
        pack();

        // 初始大小：邏輯解析度 800x580 + 視窗標題列高度
        setSize(GamePanel.SCREEN_WIDTH,
                GamePanel.SCREEN_HEIGHT + getInsets().top + getInsets().bottom);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * TitleScreen 選擇存檔後的回呼。
     *
     * @param slot       存檔槽 1~3
     * @param playerName 新遊戲 = 角色名稱，繼續遊戲 = null
     */
    private void onStart(int slot, String playerName) {
        if (gamePanel != null) {
            gamePanel.stopGameLoop();
            cardPanel.remove(gamePanel);
        }
        // 傳入 returnToTitle 回呼，讓 GamePanel 可以呼叫
        gamePanel = new GamePanel(slot, playerName, this::returnToTitle);
        cardPanel.add(gamePanel, CARD_GAME);

        cardLayout.show(cardPanel, CARD_GAME);
        gamePanel.requestFocusInWindow();
        gamePanel.startGameLoop();
    }

    /**
     * 從遊戲內返回標題畫面。
     * 由 GamePanel 的 ESC 選單呼叫。
     */
    public void returnToTitle() {
        if (gamePanel != null) {
            gamePanel.stopGameLoop();
        }
        // 重建標題畫面以刷新存檔摘要
        titleScreen = new TitleScreen(this::onStart);
        cardPanel.add(titleScreen, CARD_TITLE);
        cardLayout.show(cardPanel, CARD_TITLE);
        titleScreen.requestFocusInWindow();
    }
}
