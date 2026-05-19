package maplestory.core;

import javax.swing.JFrame;

public class GameWindow extends JFrame {

    public GameWindow() {
        GamePanel gamePanel = new GamePanel();

        add(gamePanel);
        setTitle("楓之谷 - 火柴人冒險");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();                        // 讓視窗自動配合 GamePanel 大小
        setLocationRelativeTo(null);   // 視窗置中
        setVisible(true);

        gamePanel.startGameLoop();     // 視窗顯示後再啟動遊戲迴圈
    }
}
