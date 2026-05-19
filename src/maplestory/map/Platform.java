package maplestory.map;

import maplestory.core.Camera;

import java.awt.*;

/**
 * 代表地圖上的一塊平台（地板或浮空台）。
 * 玩家和怪物只能從上方落到平台上。
 */
public class Platform {

    private final int x, y, width, height;
    private final Color baseColor;

    public Platform(int x, int y, int width, int height, Color baseColor) {
        this.x         = x;
        this.y         = y;
        this.width     = width;
        this.height    = height;
        this.baseColor = baseColor;
    }

    public void draw(Graphics2D g, Camera camera) {
        // 世界座標轉螢幕座標
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());

        // 平台主體
        g.setColor(baseColor);
        g.fillRect(sx, sy, width, height);

        // 上緣高亮（讓平台看起來有厚度感）
        g.setColor(baseColor.brighter().brighter());
        g.fillRect(sx, sy, width, 5);

        // 下緣暗面
        g.setColor(baseColor.darker());
        g.fillRect(sx, sy + height - 4, width, 4);
    }

    // Getter（供碰撞偵測使用）
    public int getX()      { return x; }
    public int getY()      { return y; }
    public int getWidth()  { return width; }
    public int getHeight() { return height; }
}
