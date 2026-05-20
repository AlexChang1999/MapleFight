package maplestory.core;

/**
 * 鏡頭：決定「世界座標」要偏移多少才能顯示在螢幕上。
 *
 * 使用方式：
 *   螢幕 X = 世界 X - camera.getOffsetX()
 *   螢幕 Y = 世界 Y - camera.getOffsetY()
 *
 * 鏡頭會平滑（lerp）跟隨玩家，不會瞬間跳動。
 */
public class Camera {

    private double offsetX = 0;
    private double offsetY = 0;

    private final int screenWidth;
    private final int screenHeight;

    // 鏡頭跟隨速度（0.0 ~ 1.0，越大越快，1.0 = 瞬間跟上）
    private static final double LERP_SPEED = 0.12;

    public Camera(int screenWidth, int screenHeight) {
        this.screenWidth  = screenWidth;
        this.screenHeight = screenHeight;
    }

    /**
     * 每幀更新鏡頭位置。
     *
     * @param playerX  玩家世界 X 座標
     * @param playerY  玩家世界 Y 座標
     * @param mapWidth 地圖總寬度（用來限制右邊界）
     */
    public void update(double playerX, double playerY, int mapWidth) {
        // 讓玩家出現在畫面橫向 1/3 處（偏左，看到更多前方畫面）
        double targetX = playerX - screenWidth / 3.0;
        // 讓玩家出現在畫面縱向 55% 處（略低於中間）
        double targetY = playerY - screenHeight * 0.55;

        // 線性插值（Lerp）：每幀慢慢靠近目標，產生平滑效果
        offsetX += (targetX - offsetX) * LERP_SPEED;
        offsetY += (targetY - offsetY) * LERP_SPEED;

        // 邊界限制：鏡頭不超出地圖範圍
        if (offsetX < 0)                         offsetX = 0;
        if (offsetX > mapWidth - screenWidth)    offsetX = mapWidth - screenWidth;
        if (offsetY < 0)                         offsetY = 0;
    }

    /**
     * 瞬間跳到目標位置（地圖切換時呼叫，避免 lerp 滑動）。
     */
    public void snapTo(double playerX, double playerY, int mapWidth) {
        offsetX = playerX - screenWidth / 3.0;
        offsetY = playerY - screenHeight * 0.55;
        if (offsetX < 0)                      offsetX = 0;
        if (offsetX > mapWidth - screenWidth) offsetX = mapWidth - screenWidth;
        if (offsetY < 0)                      offsetY = 0;
    }

    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
}
