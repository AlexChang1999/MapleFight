package maplestory.entity;

import maplestory.core.Camera;
import maplestory.map.GameMap;
import maplestory.map.Platform;

import java.awt.*;

/**
 * 怪物（火柴人，綠色）
 * 功能：重力下墜、偵測玩家後追擊、近身攻擊、受傷、死亡
 */
public class Monster {

    // ── 碰撞箱 ───────────────────────────────────────────────
    public static final int WIDTH  = 22;
    public static final int HEIGHT = 52;

    // ── 物理常數 ─────────────────────────────────────────────
    private static final double GRAVITY    = 1400;
    private static final double MOVE_SPEED = 70;   // 追擊速度

    // ── AI 常數 ──────────────────────────────────────────────
    private static final int    DETECT_RANGE = 280; // 開始追擊的距離
    private static final int    ATTACK_RANGE = 50;  // 近身攻擊距離
    private static final double ATTACK_CD    = 1.5; // 攻擊冷卻（秒）

    // ── 位置與速度 ───────────────────────────────────────────
    private double x, y;
    private double velY = 0;
    private boolean facingRight = false;

    // ── 數值 ─────────────────────────────────────────────────
    private int hp    = 80;
    private int maxHp = 80;
    private int atk   = 12; // 攻擊力

    // ── 狀態 ─────────────────────────────────────────────────
    private boolean alive         = true;
    private boolean hitThisAttack = false; // 防止一次攻擊算兩次

    // ── 計時器 ───────────────────────────────────────────────
    private double attackCooldown = 0; // 倒計時到 0 才能再攻擊
    private double hurtTimer      = 0; // 受傷紅色閃爍持續時間
    private double deathTimer     = 0; // 死亡後殘留動畫時間

    // ── 傷害數字顯示 ─────────────────────────────────────────
    private int lastDamage = 0;

    // ── 走路動畫 ─────────────────────────────────────────────
    private double walkAnim = 0;

    // ─────────────────────────────────────────────────────────
    public Monster(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // ── 每幀更新 ─────────────────────────────────────────────
    public void update(double dt, GameMap gameMap, Player player) {
        if (!alive) {
            deathTimer -= dt;
            return;
        }

        // 重力
        velY += GRAVITY * dt;
        y    += velY * dt;

        // 平台碰撞
        checkPlatformCollision(gameMap, dt);

        // 邊界限制
        if (x < 0) x = 0;
        if (x > gameMap.getMapWidth() - WIDTH) x = gameMap.getMapWidth() - WIDTH;

        // ── AI：偵測並追擊玩家 ──────────────────────────────
        double distX   = player.getX() - x;
        double distAbs = Math.abs(distX);

        if (distAbs < DETECT_RANGE) {
            // 水平追擊
            if (distX > 0) { x += MOVE_SPEED * dt; facingRight = true;  walkAnim += dt * 6; }
            else            { x -= MOVE_SPEED * dt; facingRight = false; walkAnim += dt * 6; }

            // 近身攻擊
            attackCooldown -= dt;
            if (distAbs < ATTACK_RANGE && attackCooldown <= 0) {
                player.takeDamage(atk);
                attackCooldown = ATTACK_CD;
            }
        }

        // 計時器倒數
        if (hurtTimer > 0) hurtTimer -= dt;
        if (attackCooldown < 0) attackCooldown = 0;
    }

    // ── 平台碰撞 ─────────────────────────────────────────────
    private void checkPlatformCollision(GameMap gameMap, double dt) {
        for (Platform p : gameMap.getPlatforms()) {
            if (x + WIDTH <= p.getX() || x >= p.getX() + p.getWidth()) continue;

            double feet     = y + HEIGHT;
            double prevFeet = feet - velY * dt;

            if (prevFeet <= p.getY() && feet >= p.getY() && velY > 0) {
                y    = p.getY() - HEIGHT;
                velY = 0;
            }
        }
    }

    // ── 受傷 ─────────────────────────────────────────────────
    public void takeDamage(int dmg) {
        hp         -= dmg;
        lastDamage  = dmg;
        hurtTimer   = 0.35;

        if (hp <= 0) {
            hp         = 0;
            alive      = false;
            deathTimer = 1.2; // 死亡後保留 1.2 秒顯示消失動畫
        }
    }

    // ── 繪製（火柴人怪物） ───────────────────────────────────
    public void draw(Graphics2D g, Camera camera) {
        // 死亡後還在 deathTimer 內，畫消失動畫（漸漸變透明）
        if (!alive) {
            if (deathTimer > 0) drawDeathEffect(g, camera);
            return;
        }

        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());
        int cx = sx + WIDTH / 2;

        // 受傷時閃爍紅色，否則是綠色
        Color bodyColor = hurtTimer > 0 ? Color.RED : new Color(60, 200, 80);
        g.setColor(bodyColor);
        g.setStroke(new BasicStroke(2.0f));

        // ── 火柴人本體 ──────────────────────────────────────
        // 頭
        g.drawOval(cx - 9, sy, 18, 18);

        // 身體
        g.drawLine(cx, sy + 18, cx, sy + 36);

        // 手臂
        g.drawLine(cx, sy + 23, cx - 10, sy + 32);
        g.drawLine(cx, sy + 23, cx + 10, sy + 32);

        // 腳（走路動畫）
        int swing = (int)(Math.sin(walkAnim) * 8);
        g.drawLine(cx, sy + 36, cx - 7 - swing, sy + 52);
        g.drawLine(cx, sy + 36, cx + 7 + swing, sy + 52);

        g.setStroke(new BasicStroke(1f));

        // ── 頭上 HP 條 ───────────────────────────────────────
        int barW = 36, barH = 5;
        int barX = cx - barW / 2;
        int barY = sy - 10;

        g.setColor(new Color(80, 0, 0));
        g.fillRect(barX, barY, barW, barH);
        g.setColor(Color.RED);
        g.fillRect(barX, barY, (int)(barW * (double) hp / maxHp), barH);
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barW, barH);

        // ── 受傷數字 ────────────────────────────────────────
        if (hurtTimer > 0) {
            float alpha = (float)(hurtTimer / 0.35); // 淡出效果
            g.setColor(new Color(1f, 1f, 0f, alpha));
            g.setFont(new Font("Arial", Font.BOLD, 15));
            g.drawString("-" + lastDamage, cx - 10, sy - 14);
        }
    }

    /** 死亡消失動畫：畫一個向外擴散的圓圈 */
    private void drawDeathEffect(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX()) + WIDTH / 2;
        int sy = (int)(y - camera.getOffsetY()) + HEIGHT / 2;

        float progress = (float)(1.0 - deathTimer / 1.2); // 0.0 ~ 1.0
        int   radius   = (int)(30 * progress);
        float alpha    = 1.0f - progress;

        g.setColor(new Color(0.8f, 1f, 0.3f, alpha));
        g.setStroke(new BasicStroke(3f));
        g.drawOval(sx - radius, sy - radius, radius * 2, radius * 2);
        g.setStroke(new BasicStroke(1f));
    }

    // ── Getter / Setter ──────────────────────────────────────
    public boolean isAlive()          { return alive; }
    public boolean isHitThisAttack()  { return hitThisAttack; }
    public void setHitThisAttack(boolean v) { hitThisAttack = v; }

    public Rectangle getBoundingBox() {
        return new Rectangle((int) x, (int) y, WIDTH, HEIGHT);
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
