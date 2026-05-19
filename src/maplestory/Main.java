package maplestory;

import maplestory.core.GameWindow;

public class Main {
    public static void main(String[] args) {
        // 在 Swing 的執行緒上啟動視窗（避免執行緒衝突）
        javax.swing.SwingUtilities.invokeLater(() -> new GameWindow());
    }
}
