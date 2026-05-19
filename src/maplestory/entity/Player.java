package maplestory.entity;

import maplestory.core.Camera;
import maplestory.map.GameMap;
import maplestory.map.Platform;

import java.awt.*;
import java.util.List;

/**
 * 玩家角色（火柴人）
 * 負責：移動、跳躍、攻擊、碰撞偵測、繪製
 */
public class Player {

    // ── 碰撞箱尺寸 ───────────────────────────────────────────
    public static final int WIDTH  = 24;
    public static final int HEIGHT = 58;

    // ── 物理常數 ─────────────────────────────────────────────
    private static final double GRAVITY    = 1400;  // 重力加速度（像素/秒²）
    private static final double MOVE_SPEED = 190;   // 水平移動速度
    private static final double JUMP_FORCE = -520;  // 跳躍初速（負 = 向上）

    // ── 攻擊常數 ─────────────────────────────────────────────
    private static final double ATTACK_DURATION = 0.30; // 攻擊動畫持續秒數
    private static final int    ATTACK_RANGE    = 65;   // 攻擊範圍（像素）
    private static final int    BASE_DAMAGE     = 25;   // 基礎傷害

    // ── 位置與速度 ───────────────────────────────────────────
    private double x, y;
    private double velX, velY;

    // ── 移動狀態 ─────────────────────────────────────────────
    private boolean movingLeft, movingRight;
    private boolean onGround   = false;
    private boolean facingRight = true;

    // ── 攻擊狀態 ─────────────────────────────────────────────
    private boolean attacking   = false;
    private double  attackTimer = 0;

    // ── 走路動畫（用位置計算腳的擺動） ───────────────────────
    private double walkAnim = 0;

    // ── RPG 數值 ─────────────────────────────────────────────
    private int level   = 1;
    private int exp     = 0;
    private int expToNextLevel = 100;

    // 四維屬性
    private int str   = 10;  // 力量：影響物理攻擊
    private int dex   = 4;   // 敏捷：（未來影響命中率）
    private int intel = 4;   // 智力：影響 MP 上限
    private int luk   = 4;   // 幸運：（未來影響爆擊率）

    private int hp, maxHp;
    private int mp, maxMp;

    private String jobName = "新手";

    // 寵物欄（預留，Phase 後續再實作）
    private boolean canHavePet = false;
    // private Pet pet = null;

    // 裝備格（8 格，Phase 8 實作）
    // private Equipment[] equipSlots = new Equipment[8];

    private final Camera camera;

    // ─────────────────────────────────────────────────────────
    public Player(double x, double y, Camera camera) {
        this.x      = x;
        this.y      = y;
        this.camera = camera;

        // HP / MP 根據屬性計算
        this.maxHp = 100 + str * 10;
        this.hp    = maxHp;
        this.maxMp = 30  + intel * 5;
        this.mp    = maxMp;
    }

    // ── 每幀更新 ─────────────────────────────────────────────
    public void update(double dt, GameMap gameMap) {

        // 水平移動
        velX = 0;
        if (movingLeft)  { velX = -MOVE_SPEED; facingRight = false; }
        if (movingRight) { velX =  MOVE_SPEED; facingRight = true;  }

        // 走路動畫計數
        if (velX != 0 && onGround) walkAnim += dt * 8;

        // 重力
        velY += GRAVITY * dt;

        // 套用速度
        x += velX * dt;
        y += velY * dt;

        // 邊界限制（不超出地圖左右）
        if (x < 0) x = 0;
        if (x > gameMap.getMapWidth() - WIDTH) x = gameMap.getMapWidth() - WIDTH;

        // 平台碰撞
        onGround = false;
        checkPlatformCollision(gameMap, dt);

        // 攻擊計時
        if (attacking) {
            attackTimer -= dt;
            if (attackTimer <= 0) attacking = false;
        }
    }

    // ── 平台碰撞偵測 ─────────────────────────────────────────
    private void checkPlatformCollision(GameMap gameMap, double dt) {
        for (Platform p : gameMap.getPlatforms()) {
            // 水平範圍內才需要判斷
            if (x + WIDTH <= p.getX() || x >= p.getX() + p.getWidth()) continue;

            double feet     = y + HEIGHT;           // 這一幀腳底
            double prevFeet = feet - velY * dt;     // 上一幀腳底（回推）

            // 從上方越過平台頂部：上一幀在上面，這一幀在下面，且正在下降
            if (prevFeet <= p.getY() && feet >= p.getY() && velY > 0) {
                y    = p.getY() - HEIGHT;
                velY = 0;
                onGround = true;
            }
        }
    }

    // ── 跳躍 ─────────────────────────────────────────────────
    public void jump() {
        if (onGround) {
            velY     = JUMP_FORCE;
            onGround = false;
        }
    }

    // ── 普通攻擊 ─────────────────────────────────────────────
    public void attack() {
        if (!attacking) {
            attacking   = true;
            attackTimer = ATTACK_DURATION;
        }
    }

    /** 每幀呼叫：偵測攻擊範圍內的怪物並扣血 */
    public void checkAttackHits(List<Monster> monsters) {
        if (!attacking) return;

        // 攻擊箱：依照面向決定在左邊還是右邊
        int attackX = facingRight
                ? (int) x + WIDTH          // 面右：攻擊箱接在身體右側
                : (int) x - ATTACK_RANGE;  // 面左：攻擊箱接在身體左側

        Rectangle attackBox = new Rectangle(attackX, (int) y, ATTACK_RANGE, HEIGHT);

        for (Monster m : monsters) {
            if (!m.isAlive() || m.isHitThisAttack()) continue;

            if (attackBox.intersects(m.getBoundingBox())) {
                int dmg = BASE_DAMAGE + str * 2; // 力量加成
                m.takeDamage(dmg);
                m.setHitThisAttack(true); // 標記：這次攻擊已打到，不重複算
            }
        }
    }

    // ── 受傷 ─────────────────────────────────────────────────
    public void takeDamage(int dmg) {
        hp = Math.max(0, hp - dmg);
    }

    // ── 繪製（火柴人） ───────────────────────────────────────
    public void draw(Graphics2D g, Camera camera) {
        // 螢幕座標 = 世界座標 - 鏡頭偏移
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());
        int cx = sx + WIDTH / 2; // 水平中心

        // 攻擊時畫半透明攻擊範圍提示
        if (attacking) {
            g.setColor(new Color(255, 255, 100, 70));
            int atkX = facingRight ? sx + WIDTH : sx - ATTACK_RANGE;
            g.fillRect(atkX, sy + 10, ATTACK_RANGE, HEIGHT - 10);
        }

        // ── 火柴人本體 ──────────────────────────────────────
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2.2f));

        // 頭
        g.drawOval(cx - 10, sy, 20, 20);

        // 身體
        g.drawLine(cx, sy + 20, cx, sy + 40);

        // 手臂（攻擊時右手揮出）
        if (attacking) {
            if (facingRight) {
                g.drawLine(cx, sy + 26, cx - 10, sy + 36); // 左手（收）
                g.setColor(new Color(255, 200, 100));       // 揮出的手改色
                g.drawLine(cx, sy + 26, cx + 28, sy + 18); // 右手（揮出）
            } else {
                g.setColor(new Color(255, 200, 100));
                g.drawLine(cx, sy + 26, cx - 28, sy + 18); // 左手（揮出）
                g.setColor(Color.WHITE);
                g.drawLine(cx, sy + 26, cx + 10, sy + 36); // 右手（收）
            }
            g.setColor(Color.WHITE);
        } else {
            g.drawLine(cx, sy + 26, cx - 12, sy + 38); // 左手
            g.drawLine(cx, sy + 26, cx + 12, sy + 38); // 右手
        }

        // 腳（走路時交替擺動）
        int swing = onGround ? (int)(Math.sin(walkAnim) * 10) : 0;
        g.drawLine(cx, sy + 40, cx - 8 - swing, sy + 58); // 左腳
        g.drawLine(cx, sy + 40, cx + 8 + swing, sy + 58); // 右腳

        g.setStroke(new BasicStroke(1f)); // 重置線條粗細
    }

    // ── Getter / Setter ──────────────────────────────────────
    public void setMovingLeft(boolean v)  { movingLeft  = v; }
    public void setMovingRight(boolean v) { movingRight = v; }

    public double getX()       { return x; }
    public double getY()       { return y; }
    public int    getHp()      { return hp; }
    public int    getMaxHp()   { return maxHp; }
    public int    getMp()      { return mp; }
    public int    getMaxMp()   { return maxMp; }
    public int    getLevel()   { return level; }
    public String getJobName() { return jobName; }
    public boolean isAttacking() { return attacking; }

    // 四維數值（StatusPanel 使用）
    public int getStr()   { return str; }
    public int getDex()   { return dex; }
    public int getIntel() { return intel; }
    public int getLuk()   { return luk; }

    // EXP（StatusPanel 使用）
    public int    getExp()            { return exp; }
    public int    getExpToNextLevel() { return expToNextLevel; }

    /** 回傳 EXP 比例（0.0 ~ 1.0），給 HUD 的 EXP 條用 */
    public double getExpRatio() {
        return expToNextLevel > 0 ? (double) exp / expToNextLevel : 0;
    }
}
