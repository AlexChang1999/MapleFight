package maplestory.entity;

import maplestory.core.Camera;
import maplestory.item.Equipment;
import maplestory.item.EquipSlot;
import maplestory.map.BaseMap;
import maplestory.map.Platform;

import java.awt.*;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 玩家角色（火柴人）
 * Phase 7-11 更新：
 *   - 接受 BaseMap（支援地圖切換）
 *   - 靜待呼吸動畫
 *   - 走路手腳對向擺動
 *   - 攻擊三階段：蓄勢 → 揮擊 → 收勢
 *   - 裝備系統（8 格，開局穿上預設裝備）
 *   - setPosition()（地圖切換用）
 */
public class Player {

    // ── 碰撞箱 ───────────────────────────────────────────────
    public static final int WIDTH  = 24;
    public static final int HEIGHT = 58;

    // ── 物理常數 ─────────────────────────────────────────────
    private static final double GRAVITY    = 1400;
    private static final double MOVE_SPEED = 190;
    private static final double JUMP_FORCE = -520;

    // ── 攻擊常數 ─────────────────────────────────────────────
    private static final double ATTACK_DURATION  = 0.45; // 完整動畫秒數
    private static final double STRIKE_START     = 0.28; // 揮擊開始比例
    private static final double STRIKE_END       = 0.65; // 揮擊結束比例
    private static final int    ATTACK_RANGE     = 65;
    private static final int    BASE_DAMAGE      = 25;

    // ── 位置 / 速度 ──────────────────────────────────────────
    private double x, y;
    private double velX, velY;

    // ── 移動狀態 ─────────────────────────────────────────────
    private boolean movingLeft, movingRight;
    private boolean onGround    = false;
    private boolean facingRight = true;

    // ── 攻擊狀態 ─────────────────────────────────────────────
    private boolean attacking   = false;
    private double  attackTimer = 0;

    // ── 動畫計時 ─────────────────────────────────────────────
    private double walkAnim = 0; // 走路週期
    private double idleTimer = 0; // 靜待呼吸週期（持續累加）

    // ── RPG 數值 ─────────────────────────────────────────────
    private int level          = 1;
    private int exp            = 0;
    private int expToNextLevel = 100;

    private int str   = 10;
    private int dex   = 4;
    private int intel = 4;
    private int luk   = 4;

    private int hp, maxHp;
    private int mp, maxMp;

    private String jobName = "新手";

    // ── 裝備欄（8 格）────────────────────────────────────────
    private final Map<EquipSlot, Equipment> equipments = new EnumMap<>(EquipSlot.class);

    // ── 寵物欄（預留）────────────────────────────────────────
    private boolean canHavePet = false;

    private final Camera camera;

    // ─────────────────────────────────────────────────────────
    public Player(double x, double y, Camera camera) {
        this.x      = x;
        this.y      = y;
        this.camera = camera;

        // 穿上新手預設裝備
        equipments.put(EquipSlot.TOP,    Equipment.cottonShirt());
        equipments.put(EquipSlot.BOTTOM, Equipment.cottonPants());
        equipments.put(EquipSlot.WEAPON, Equipment.oldSword());
        equipments.put(EquipSlot.GLOVES, Equipment.hempGloves());
        equipments.put(EquipSlot.BOOTS,  Equipment.hempBoots());

        // HP / MP（包含裝備加成）
        recalculateStats();
        this.hp = maxHp;
        this.mp = maxMp;
    }

    /** 重新計算 maxHp / maxMp（換裝備後呼叫） */
    private void recalculateStats() {
        int hpBonus = equipments.values().stream().mapToInt(Equipment::getHpBonus).sum();
        int mpBonus = equipments.values().stream().mapToInt(Equipment::getMpBonus).sum();
        maxHp = 100 + str * 10 + hpBonus;
        maxMp = 30  + intel * 5 + mpBonus;
    }

    // ── 每幀更新（接受 BaseMap，支援地圖切換）───────────────
    public void update(double dt, BaseMap map) {
        idleTimer += dt;

        // 水平移動
        velX = 0;
        if (movingLeft)  { velX = -MOVE_SPEED; facingRight = false; }
        if (movingRight) { velX =  MOVE_SPEED; facingRight = true;  }

        // 走路動畫
        if (velX != 0 && onGround) walkAnim += dt * 8;

        // 重力
        velY += GRAVITY * dt;

        // 套用速度
        x += velX * dt;
        y += velY * dt;

        // 邊界
        if (x < 0) x = 0;
        if (x > map.getMapWidth() - WIDTH) x = map.getMapWidth() - WIDTH;

        // 平台碰撞
        onGround = false;
        for (Platform p : map.getPlatforms()) {
            if (x + WIDTH <= p.getX() || x >= p.getX() + p.getWidth()) continue;
            double feet     = y + HEIGHT;
            double prevFeet = feet - velY * dt;
            if (prevFeet <= p.getY() && feet >= p.getY() && velY > 0) {
                y    = p.getY() - HEIGHT;
                velY = 0;
                onGround = true;
            }
        }

        // 攻擊計時
        if (attacking) {
            attackTimer -= dt;
            if (attackTimer <= 0) attacking = false;
        }
    }

    // ── 跳躍 ─────────────────────────────────────────────────
    public void jump() {
        if (onGround) { velY = JUMP_FORCE; onGround = false; }
    }

    // ── 普通攻擊 ─────────────────────────────────────────────
    public void attack() {
        if (!attacking) { attacking = true; attackTimer = ATTACK_DURATION; }
    }

    /** 攻擊進度比例（0.0 = 剛開始，1.0 = 結束） */
    private double attackProgress() {
        return 1.0 - (attackTimer / ATTACK_DURATION);
    }

    /** 只有在揮擊階段才能造成傷害 */
    public boolean isInStrikePhase() {
        if (!attacking) return false;
        double p = attackProgress();
        return p >= STRIKE_START && p <= STRIKE_END;
    }

    /** 偵測揮擊階段內的怪物碰撞 */
    public void checkAttackHits(List<Monster> monsters) {
        if (!isInStrikePhase()) return;
        int atkX = facingRight ? (int) x + WIDTH : (int) x - ATTACK_RANGE;
        Rectangle box = new Rectangle(atkX, (int) y, ATTACK_RANGE, HEIGHT);
        int atkBonus = equipments.containsKey(EquipSlot.WEAPON)
                       ? equipments.get(EquipSlot.WEAPON).getAtkBonus() : 0;
        for (Monster m : monsters) {
            if (!m.isAlive() || m.isHitThisAttack()) continue;
            if (box.intersects(m.getBoundingBox())) {
                m.takeDamage(BASE_DAMAGE + str * 2 + atkBonus);
                m.setHitThisAttack(true);
            }
        }
    }

    public void takeDamage(int dmg) { hp = Math.max(0, hp - dmg); }

    /** 地圖切換時設定新位置 */
    public void setPosition(double nx, double ny) {
        x = nx; y = ny; velX = 0; velY = 0;
    }

    // ── 繪製（火柴人 + 裝備） ────────────────────────────────
    public void draw(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());

        // ── 靜待呼吸偏移 ────────────────────────────────────
        if (velX == 0 && onGround && !attacking) {
            sy += (int)(Math.sin(idleTimer * 1.6) * 2.0);
        }

        int cx = sx + WIDTH / 2;

        // ── 攻擊範圍提示（半透明黃） ─────────────────────────
        if (attacking) {
            g.setColor(new Color(255, 255, 80, 55));
            int atkX = facingRight ? sx + WIDTH : sx - ATTACK_RANGE;
            g.fillRect(atkX, sy + 10, ATTACK_RANGE, HEIGHT - 10);
        }

        g.setStroke(new BasicStroke(2.2f));

        // ── 取得裝備顏色 ────────────────────────────────────
        Color topColor    = equipments.containsKey(EquipSlot.TOP)
                            ? equipments.get(EquipSlot.TOP).getDisplayColor()    : Color.WHITE;
        Color bottomColor = equipments.containsKey(EquipSlot.BOTTOM)
                            ? equipments.get(EquipSlot.BOTTOM).getDisplayColor() : Color.WHITE;
        Color gloveColor  = equipments.containsKey(EquipSlot.GLOVES)
                            ? equipments.get(EquipSlot.GLOVES).getDisplayColor() : Color.WHITE;
        Color bootColor   = equipments.containsKey(EquipSlot.BOOTS)
                            ? equipments.get(EquipSlot.BOOTS).getDisplayColor()  : Color.WHITE;

        // ── 頭 ───────────────────────────────────────────────
        g.setColor(Color.WHITE);
        g.drawOval(cx - 10, sy, 20, 20);

        // ── 身體（上衣色）────────────────────────────────────
        g.setColor(topColor);
        g.drawLine(cx, sy + 20, cx, sy + 40);

        // ── 手臂 + 攻擊三階段 ────────────────────────────────
        drawArms(g, cx, sy, topColor, gloveColor);

        // ── 腳（下褲色，走路對向擺動）────────────────────────
        int legSwing = onGround && !attacking ? (int)(Math.sin(walkAnim) * 10) : 0;
        // 左腳 / 右腳顏色
        g.setColor(bottomColor);
        g.drawLine(cx, sy + 40, cx - 8 - legSwing, sy + 58);
        g.drawLine(cx, sy + 40, cx + 8 + legSwing, sy + 58);

        // ── 靴子（腳尖小矩形）───────────────────────────────
        g.setColor(bootColor);
        g.fillRect(cx - 14 - legSwing, sy + 55, 10, 5);
        g.fillRect(cx + 4  + legSwing, sy + 55, 10, 5);

        g.setStroke(new BasicStroke(1f));
    }

    /**
     * 手臂繪製（含三階段攻擊動畫）
     * 蓄勢(0~28%)→揮擊(28~65%)→收勢(65~100%)
     */
    private void drawArms(Graphics2D g, int cx, int sy,
                           Color topColor, Color gloveColor) {
        if (!attacking) {
            // ── 普通走路手臂（與腳對向擺動）────────────────
            int armSwing = onGround ? (int)(Math.sin(walkAnim + Math.PI) * 8) : 0;
            g.setColor(topColor);
            g.drawLine(cx, sy + 26, cx - 12 - armSwing, sy + 38);
            g.drawLine(cx, sy + 26, cx + 12 + armSwing, sy + 38);
            // 手套點
            g.setColor(gloveColor);
            g.fillOval(cx - 14 - armSwing, sy + 36, 6, 6);
            g.fillOval(cx + 10 + armSwing, sy + 36, 6, 6);
            return;
        }

        double prog = attackProgress(); // 0.0 ~ 1.0

        // 方向因子：面右 +1，面左 -1
        int dir = facingRight ? 1 : -1;

        int frontArmEndX, frontArmEndY; // 揮擊手（前手）終點
        int backArmEndX,  backArmEndY;  // 後手終點

        if (prog < STRIKE_START) {
            // ── 蓄勢：前手收回，後手往後拉 ─────────────────
            double t = prog / STRIKE_START; // 0→1
            frontArmEndX = cx + dir * (int)(-5 - 10 * t);
            frontArmEndY = sy + 30 + (int)(5 * t);
            backArmEndX  = cx - dir * (int)(8 + 6 * t);
            backArmEndY  = sy + 34;
        } else if (prog < STRIKE_END) {
            // ── 揮擊：前手快速甩出 ──────────────────────────
            double t = (prog - STRIKE_START) / (STRIKE_END - STRIKE_START); // 0→1
            frontArmEndX = cx + dir * (int)(15 + 18 * t);
            frontArmEndY = sy + (int)(30 - 12 * t);
            backArmEndX  = cx - dir * (int)(10);
            backArmEndY  = sy + 36;
        } else {
            // ── 收勢：前手緩緩收回 ──────────────────────────
            double t = (prog - STRIKE_END) / (1.0 - STRIKE_END); // 0→1
            frontArmEndX = cx + dir * (int)(33 - 20 * t);
            frontArmEndY = sy + (int)(18 + 15 * t);
            backArmEndX  = cx - dir * (int)(10 - 3 * t);
            backArmEndY  = sy + 36;
        }

        // 畫後手（上衣色）
        g.setColor(topColor);
        g.drawLine(cx, sy + 26, backArmEndX, backArmEndY);

        // 畫前手（揮擊色：亮橘色）
        Color strikeColor = (prog >= STRIKE_START && prog <= STRIKE_END)
                            ? new Color(255, 200, 80) : topColor;
        g.setColor(strikeColor);
        g.drawLine(cx, sy + 26, frontArmEndX, frontArmEndY);

        // ── 武器（短劍）附在前手端 ──────────────────────────
        if (equipments.containsKey(EquipSlot.WEAPON)) {
            Equipment w = equipments.get(EquipSlot.WEAPON);
            g.setColor(w.getDisplayColor());
            g.setStroke(new BasicStroke(3f));
            int swordLen = 22;
            g.drawLine(frontArmEndX, frontArmEndY,
                       frontArmEndX + dir * swordLen,
                       frontArmEndY - 8);
            // 劍柄（橫線）
            g.setColor(new Color(180, 140, 60));
            g.setStroke(new BasicStroke(2f));
            g.drawLine(frontArmEndX - 3, frontArmEndY + 1,
                       frontArmEndX + 3, frontArmEndY - 3);
            g.setStroke(new BasicStroke(2.2f));
        }

        // 手套
        g.setColor(gloveColor);
        g.fillOval(frontArmEndX - 3, frontArmEndY - 3, 6, 6);
    }

    // ── Getter / Setter ──────────────────────────────────────
    public void setMovingLeft(boolean v)  { movingLeft  = v; }
    public void setMovingRight(boolean v) { movingRight = v; }

    public double  getX()             { return x; }
    public double  getY()             { return y; }
    public int     getHp()            { return hp; }
    public int     getMaxHp()         { return maxHp; }
    public int     getMp()            { return mp; }
    public int     getMaxMp()         { return maxMp; }
    public int     getLevel()         { return level; }
    public String  getJobName()       { return jobName; }
    public boolean isAttacking()      { return attacking; }
    public int     getStr()           { return str; }
    public int     getDex()           { return dex; }
    public int     getIntel()         { return intel; }
    public int     getLuk()           { return luk; }
    public int     getExp()           { return exp; }
    public int     getExpToNextLevel(){ return expToNextLevel; }

    public double getExpRatio() {
        return expToNextLevel > 0 ? (double) exp / expToNextLevel : 0;
    }

    public Map<EquipSlot, Equipment> getEquipments() { return equipments; }
}
