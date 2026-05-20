package maplestory.item;

import maplestory.core.Camera;
import maplestory.entity.Player;

import java.awt.*;

/**
 * 地圖上的掉落物。
 * 怪物死亡後生成，以拋物弧線落地，30 秒後消失。
 * 玩家走近（40 px 內）自動撿取。
 *
 * 可裝載：金幣（int goldAmount）、消耗品（Consumable）、裝備（Equipment）。
 */
public class DropItem {

    // ── 物理常數 ─────────────────────────────────────────────
    private static final double GRAVITY      = 900;
    private static final double GROUND_Y     = maplestory.core.GamePanel.GAME_HEIGHT - 40 - 10;
    private static final double PICKUP_RANGE = 42;  // 自動撿取半徑（px）
    private static final double LIFETIME     = 30.0;// 存在秒數

    // ── 類型 ─────────────────────────────────────────────────
    public enum Type { GOLD, CONSUMABLE, EQUIPMENT }

    private final Type       type;
    private final int        goldAmount;
    private final Consumable consumable;
    private final Equipment  equipment;

    // ── 顯示資訊（快取） ──────────────────────────────────────
    private final String label;
    private final Color  labelColor;

    // ── 物理狀態 ─────────────────────────────────────────────
    private double x, y;
    private double velX, velY;
    private boolean landed  = false;
    private boolean pickedUp = false;
    private double  lifeTimer = LIFETIME;

    // ── 視覺動畫 ─────────────────────────────────────────────
    private double animTimer = 0;

    // ─────────────────────────────────────────────────────────
    // 建構子（私有，透過工廠方法建立）
    // ─────────────────────────────────────────────────────────

    private DropItem(Type type, int goldAmount, Consumable consumable,
                     Equipment equipment, String label, Color color,
                     double x, double y) {
        this.type       = type;
        this.goldAmount = goldAmount;
        this.consumable = consumable;
        this.equipment  = equipment;
        this.label      = label;
        this.labelColor = color;
        this.x          = x;
        this.y          = y;

        // 隨機拋出方向
        this.velX = (Math.random() - 0.5) * 80;
        this.velY = -(180 + Math.random() * 120);
    }

    // ── 工廠方法 ─────────────────────────────────────────────

    public static DropItem gold(double x, double y, int amount) {
        return new DropItem(Type.GOLD, amount, null, null,
                            "G " + amount,
                            new Color(255, 215, 0),
                            x, y);
    }

    public static DropItem consumable(double x, double y, Consumable c) {
        return new DropItem(Type.CONSUMABLE, 0, c, null,
                            c.getName(),
                            c.getRarity().color,
                            x, y);
    }

    public static DropItem equipment(double x, double y, Equipment e) {
        return new DropItem(Type.EQUIPMENT, 0, null, e,
                            e.getName(),
                            e.getRarity().color,
                            x, y);
    }

    // ── 更新 ─────────────────────────────────────────────────

    public void update(double dt) {
        if (pickedUp) return;
        lifeTimer -= dt;
        animTimer += dt;

        if (!landed) {
            velY += GRAVITY * dt;
            x    += velX * dt;
            y    += velY * dt;

            if (y >= GROUND_Y) {
                y      = GROUND_Y;
                velX   = 0;
                velY   = 0;
                landed = true;
            }
        }
    }

    // ── 繪製 ─────────────────────────────────────────────────

    public void draw(Graphics2D g, Camera camera) {
        if (pickedUp) return;

        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());

        // 快要消失時閃爍
        if (lifeTimer < 5.0) {
            if ((int)(lifeTimer * 6) % 2 == 0) return;
        }

        // 道具本體（小正方形，顏色依稀有度）
        int size = 12;
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(sx - size / 2 + 1, sy - size / 2 + 1, size, size); // 陰影
        g.setColor(labelColor);
        g.fillRect(sx - size / 2, sy - size / 2, size, size);
        g.setColor(labelColor.brighter());
        g.drawRect(sx - size / 2, sy - size / 2, size, size);

        // 圖示（金幣和消耗品有不同標記）
        g.setFont(new Font("Arial", Font.BOLD, 8));
        g.setColor(Color.WHITE);
        String icon = switch (type) {
            case GOLD       -> "G";
            case CONSUMABLE -> consumable.getHpRestore() > 0 ? "H" : "M";
            case EQUIPMENT  -> "E";
        };
        FontMetrics fm = g.getFontMetrics();
        g.drawString(icon, sx - fm.stringWidth(icon) / 2, sy + 3);

        // 上方標籤（附近時顯示）
        float bob = (float)(Math.sin(animTimer * 3.0) * 2);
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 10));
        g.setColor(new Color(labelColor.getRed(), labelColor.getGreen(),
                             labelColor.getBlue(), 220));
        FontMetrics fm2 = g.getFontMetrics();
        g.drawString(label, sx - fm2.stringWidth(label) / 2,
                     sy - size / 2 - 4 + (int) bob);
    }

    // ── 撿取 ─────────────────────────────────────────────────

    /**
     * 偵測玩家是否在撿取範圍內。
     * 若在範圍且已落地，回傳 true 並標記為已撿取。
     */
    public boolean tryPickup(Player player) {
        if (pickedUp || !landed) return false;
        if (lifeTimer <= 0) { pickedUp = true; return false; } // 過期

        double dx = (player.getX() + Player.WIDTH / 2.0) - x;
        double dy = (player.getY() + Player.HEIGHT / 2.0) - y;
        if (Math.abs(dx) < PICKUP_RANGE && Math.abs(dy) < PICKUP_RANGE * 1.5) {
            pickedUp = true;
            return true;
        }
        return false;
    }

    // ── 狀態 ─────────────────────────────────────────────────
    public boolean isExpired()  { return lifeTimer <= 0 || pickedUp; }
    public boolean isPickedUp() { return pickedUp; }

    // ── Getter ───────────────────────────────────────────────
    public Type       getType()       { return type; }
    public int        getGoldAmount() { return goldAmount; }
    public Consumable getConsumable() { return consumable; }
    public Equipment  getEquipment()  { return equipment; }
    public String     getLabel()      { return label; }
    public Color      getLabelColor() { return labelColor; }
}
