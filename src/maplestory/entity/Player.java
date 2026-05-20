package maplestory.entity;

import maplestory.core.Camera;
import maplestory.item.Equipment;
import maplestory.item.EquipSlot;
import maplestory.job.Job;
import maplestory.job.Warrior;
import maplestory.map.BaseMap;
import maplestory.map.Ladder;
import maplestory.map.Platform;

import java.awt.*;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 玩家角色（火柴人）
 * 功能：
 *   - BaseMap 解耦（支援地圖切換 + setPosition）
 *   - 靜待呼吸 / 走路手腳對向擺動
 *   - 雙段連擊：劈砍（Z 奇數次）、突刺（Z 偶數次）
 *   - 裝備系統（8 格，開局穿上預設裝備）
 *   - 職業系統（Job 抽象層，預設劍士）
 *   - 脫戰計時（timeSinceLastCombat）→ 劍士被動回血用
 */
public class Player {

    // ── 碰撞箱（公開給技能類別使用） ─────────────────────────
    public static final int WIDTH  = 24;
    public static final int HEIGHT = 58;

    // ── 物理常數 ─────────────────────────────────────────────
    private static final double GRAVITY    = 1400;
    private static final double MOVE_SPEED = 190;
    private static final double JUMP_FORCE = -520;

    // ── 攻擊常數 ─────────────────────────────────────────────
    private static final double ATTACK_DURATION = 0.42; // 完整動畫秒數
    private static final double STRIKE_START    = 0.28; // 揮擊開始比例
    private static final double STRIKE_END      = 0.65; // 揮擊結束比例
    private static final int    ATTACK_RANGE    = 65;
    private static final int    BASE_DAMAGE     = 25;

    // ── 位置 / 速度 ──────────────────────────────────────────
    private double x, y;
    private double velX, velY;

    // ── 移動狀態 ─────────────────────────────────────────────
    private boolean movingLeft  = false;
    private boolean movingRight = false;
    private boolean onGround    = false;
    private boolean facingRight = true;

    // ── 攻擊狀態 ─────────────────────────────────────────────
    private boolean attacking        = false;
    private double  attackTimer      = 0;
    private int     attackComboIndex = 0; // 0=劈砍, 1=突刺（每次切換）

    // ── 梯子攀爬 ─────────────────────────────────────────────
    private boolean movingUp   = false;
    private boolean movingDown = false;
    private boolean onLadder   = false;
    private static final double CLIMB_SPEED = 115;

    // ── 冰雪緩速 ─────────────────────────────────────────────
    private double slowTimer  = 0;
    private double slowFactor = 1.0; // 1.0=正常, <1.0=減速

    // ── 升級特效 ─────────────────────────────────────────────
    private double levelUpTimer = 0;

    // ── 動畫計時 ─────────────────────────────────────────────
    private double walkAnim  = 0; // 走路週期
    private double idleTimer = 0; // 靜待呼吸週期

    // ── 脫戰計時 ─────────────────────────────────────────────
    /** 距離上次攻擊 / 被攻擊的秒數（劍士被動回血判斷） */
    private double timeSinceLastCombat = 999;

    // ── 角色基本資訊 ─────────────────────────────────────────
    private String name = "新手";   // 玩家取名（顯示在腳下）
    private int    gold = 0;        // 金幣

    // ── RPG 數值 ─────────────────────────────────────────────
    private int level          = 1;
    private int exp            = 0;
    private int expToNextLevel = 100;

    private int str   = 10;
    private int dex   =  4;
    private int intel =  4;
    private int luk   =  4;

    private int hp, maxHp;
    private int mp, maxMp;

    // ── 職業（10 等前為 null，10 等轉劍士）────────────────────
    private Job    job     = null;
    private String jobName = "新手";

    // ── 裝備欄（8 格）────────────────────────────────────────
    private final Map<EquipSlot, Equipment> equipments = new EnumMap<>(EquipSlot.class);

    // ── 寵物欄（預留）────────────────────────────────────────
    @SuppressWarnings("unused")
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

        // HP / MP（含裝備加成）
        recalculateStats();
        this.hp = maxHp;
        this.mp = maxMp;
    }

    /** 重新計算 maxHp / maxMp（換裝備後呼叫） */
    private void recalculateStats() {
        int hpBonus = equipments.values().stream().mapToInt(Equipment::getHpBonus).sum();
        int mpBonus = equipments.values().stream().mapToInt(Equipment::getMpBonus).sum();
        maxHp = 100 + str * 10 + hpBonus;
        maxMp =  30 + intel * 5 + mpBonus;
    }

    // ─────────────────────────────────────────────────────────
    // 每幀更新
    // ─────────────────────────────────────────────────────────
    public void update(double dt, BaseMap map) {
        idleTimer           += dt;
        timeSinceLastCombat += dt;
        if (slowTimer    > 0) { slowTimer    -= dt; if (slowTimer    <= 0) slowFactor = 1.0; }
        if (levelUpTimer > 0)   levelUpTimer -= dt;

        // ── 梯子偵測 ─────────────────────────────────────────
        boolean wasOnLadder = onLadder;
        onLadder = false;
        for (Ladder lad : map.getLadders()) {
            if (lad.getZone().intersects(
                    new Rectangle((int) x, (int) y, WIDTH, HEIGHT))) {
                if (wasOnLadder || movingUp || movingDown) {
                    onLadder = true;
                    // 對齊梯子中心 X（平滑插值）
                    double tx = lad.getCenterX() - WIDTH / 2.0;
                    x += (tx - x) * Math.min(1.0, 14 * dt);
                }
                break;
            }
        }

        // ── 位移 ─────────────────────────────────────────────
        if (onLadder) {
            velX = 0; velY = 0;
            if (movingUp)   velY = -CLIMB_SPEED;
            if (movingDown) velY =  CLIMB_SPEED;
            y += velY * dt;
        } else {
            double spd = MOVE_SPEED * slowFactor;
            velX = 0;
            if (movingLeft)  { velX = -spd; facingRight = false; }
            if (movingRight) { velX =  spd; facingRight = true;  }
            if (velX != 0 && onGround) walkAnim += dt * 8;
            velY += GRAVITY * dt;
            x += velX * dt;
            y += velY * dt;
        }

        // ── 邊界 ─────────────────────────────────────────────
        if (x < 0) x = 0;
        if (x > map.getMapWidth() - WIDTH) x = map.getMapWidth() - WIDTH;

        // ── 平台碰撞（爬梯時跳過） ───────────────────────────
        onGround = false;
        if (!onLadder) {
            for (Platform p : map.getPlatforms()) {
                if (x + WIDTH <= p.getX() || x >= p.getX() + p.getWidth()) continue;
                double feet     = y + HEIGHT;
                double prevFeet = feet - velY * dt;
                if (prevFeet <= p.getY() && feet >= p.getY() && velY > 0) {
                    y = p.getY() - HEIGHT; velY = 0; onGround = true;
                }
            }
        }

        // ── 攻擊計時 ─────────────────────────────────────────
        if (attacking) {
            attackTimer -= dt;
            if (attackTimer <= 0) attacking = false;
        }

        // ── 職業更新（被動 + 技能冷卻） ──────────────────────
        if (job != null) job.update(this, dt);
    }

    // ─────────────────────────────────────────────────────────
    // 行動指令
    // ─────────────────────────────────────────────────────────

    public void jump() {
        if (onLadder) {
            onLadder = false; velY = JUMP_FORCE * 0.7; // 從梯子跳離
        } else if (onGround) {
            velY = JUMP_FORCE; onGround = false;
        }
    }

    /**
     * 普通攻擊：每次按 Z 切換連擊段數。
     * attackComboIndex 0 = 劈砍（下劈），1 = 突刺（水平刺）
     */
    public void attack() {
        if (!attacking) {
            attacking        = true;
            attackTimer      = ATTACK_DURATION;
            attackComboIndex = 1 - attackComboIndex; // 0↔1 切換
            timeSinceLastCombat = 0; // 重置脫戰計時
        }
    }

    /** 使用技能（含怪物列表，由 GamePanel 呼叫） */
    public void useSkill(int index, List<Monster> monsters) {
        if (job == null) return;
        job.useSkill(index, this, monsters);
        timeSinceLastCombat = 0;
    }

    /** EXP 獲得 + 自動升級 */
    public void gainExp(int amount) {
        exp += amount;
        while (exp >= expToNextLevel) {
            exp -= expToNextLevel;
            level++;
            expToNextLevel = level * 100;
            str += 2; dex += 1; intel += 1; luk += 1;
            recalculateStats();
            hp = maxHp; mp = maxMp;  // 升級全回
            levelUpTimer = 2.5;
            checkJobUnlock();
        }
    }

    /** 10 等自動轉職劍士 */
    private void checkJobUnlock() {
        if (level >= 10 && job == null) {
            job          = new Warrior();
            jobName      = job.getDisplayName(); // "劍士"
            levelUpTimer = 3.5; // 轉職時延長特效
        }
    }

    /** 受冰系攻擊：套用緩速狀態 */
    public void applySlow(double duration, double factor) {
        slowTimer  = Math.max(slowTimer, duration);
        slowFactor = Math.min(slowFactor, factor);
    }

    /** 攻擊進度比例 0.0（剛開始）~ 1.0（結束） */
    private double attackProgress() {
        return 1.0 - (attackTimer / ATTACK_DURATION);
    }

    /** 只有在揮擊階段（STRIKE_START ~ STRIKE_END）才能傷害怪物 */
    public boolean isInStrikePhase() {
        if (!attacking) return false;
        double p = attackProgress();
        return p >= STRIKE_START && p <= STRIKE_END;
    }

    /** 偵測揮擊階段內的怪物碰撞並造成傷害 */
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
        timeSinceLastCombat = 0; // 命中後重置脫戰計時
    }

    public void takeDamage(int dmg) {
        hp = Math.max(0, hp - dmg);
        timeSinceLastCombat = 0; // 被打也算戰鬥狀態
    }

    /** 消耗 MP，不足時回傳 false */
    public boolean consumeMp(int amount) {
        if (mp < amount) return false;
        mp -= amount;
        return true;
    }

    /** 回復 HP（不超過 maxHp） */
    public void healHp(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    /** 地圖切換時設定新位置 */
    public void setPosition(double nx, double ny) {
        x = nx; y = ny; velX = 0; velY = 0;
    }

    // ─────────────────────────────────────────────────────────
    // 繪製
    // ─────────────────────────────────────────────────────────
    public void draw(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());

        // 靜待呼吸偏移
        if (velX == 0 && onGround && !attacking && !onLadder) {
            sy += (int)(Math.sin(idleTimer * 1.6) * 2.0);
        }

        // 升級 / 轉職光效（金色光環）
        if (levelUpTimer > 0) {
            float alpha = (float) Math.min(1.0, levelUpTimer / 2.5) * 0.65f;
            g.setColor(new Color(1f, 0.9f, 0.25f, alpha));
            g.setStroke(new BasicStroke(3f));
            g.drawOval(sx - 10, sy - 10, WIDTH + 20, HEIGHT + 20);
            g.setStroke(new BasicStroke(1f));
            if (levelUpTimer > 2.5) { // 轉職時顯示文字
                g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
                g.setColor(new Color(1f, 0.95f, 0.3f, alpha));
                g.drawString("✦ 轉職：" + jobName + " ✦", sx - 30, sy - 16);
            }
        }

        // 冰慢光效（藍色光暈）
        if (slowTimer > 0) {
            float a = (float)(slowTimer / 2.5) * 0.3f;
            g.setColor(new Color(0.3f, 0.7f, 1f, Math.min(a, 0.35f)));
            g.fillRoundRect(sx - 4, sy, WIDTH + 8, HEIGHT, 8, 8);
        }

        int cx = sx + WIDTH / 2;

        // 攻擊範圍提示（半透明黃）
        if (attacking) {
            g.setColor(new Color(255, 255, 80, 50));
            int atkX = facingRight ? sx + WIDTH : sx - ATTACK_RANGE;
            g.fillRect(atkX, sy + 10, ATTACK_RANGE, HEIGHT - 10);
        }

        g.setStroke(new BasicStroke(2.2f));

        // 取得裝備顏色
        Color topColor    = equip(EquipSlot.TOP)    != null ? equip(EquipSlot.TOP).getDisplayColor()    : Color.WHITE;
        Color bottomColor = equip(EquipSlot.BOTTOM) != null ? equip(EquipSlot.BOTTOM).getDisplayColor() : Color.WHITE;
        Color gloveColor  = equip(EquipSlot.GLOVES) != null ? equip(EquipSlot.GLOVES).getDisplayColor() : Color.WHITE;
        Color bootColor   = equip(EquipSlot.BOOTS)  != null ? equip(EquipSlot.BOOTS).getDisplayColor()  : Color.WHITE;

        // ── 頭 ───────────────────────────────────────────────
        g.setColor(Color.WHITE);
        g.drawOval(cx - 10, sy, 20, 20);

        // 臉（眼睛）
        int eyeDir = facingRight ? 3 : -3;
        g.setColor(Color.BLACK);
        g.fillOval(cx + eyeDir - 2, sy + 7, 3, 3);

        // ── 身體（上衣色）────────────────────────────────────
        g.setColor(topColor);
        g.drawLine(cx, sy + 20, cx, sy + 40);

        // ── 手臂（含連擊動畫）────────────────────────────────
        drawArms(g, cx, sy, topColor, gloveColor);

        // ── 腳（下褲色，走路對向擺動）────────────────────────
        int legSwing = onGround && !attacking ? (int)(Math.sin(walkAnim) * 10) : 0;
        g.setColor(bottomColor);
        g.drawLine(cx, sy + 40, cx - 8 - legSwing, sy + 58);
        g.drawLine(cx, sy + 40, cx + 8 + legSwing, sy + 58);

        // ── 靴子 ─────────────────────────────────────────────
        g.setColor(bootColor);
        g.fillRect(cx - 14 - legSwing, sy + 55, 10, 5);
        g.fillRect(cx + 4  + legSwing, sy + 55, 10, 5);

        g.setStroke(new BasicStroke(1f));

        // ── 角色名稱（腳下 4px，黑邊白字）────────────────────
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int nameW = fm.stringWidth(name);
        int nameX = cx - nameW / 2;
        int nameY = sy + HEIGHT + 14;
        g.setColor(new Color(0, 0, 0, 180));
        g.drawString(name, nameX - 1, nameY + 1);
        g.drawString(name, nameX + 1, nameY + 1);
        g.setColor(Color.WHITE);
        g.drawString(name, nameX, nameY);
    }

    /**
     * 手臂繪製：依 attackComboIndex 選擇動畫類型。
     *   0 = 劈砍（由上往下斜劈）
     *   1 = 突刺（水平向前刺）
     */
    private void drawArms(Graphics2D g, int cx, int sy,
                          Color topColor, Color gloveColor) {
        if (!attacking) {
            // 普通走路手臂（與腳對向擺動）
            int armSwing = onGround ? (int)(Math.sin(walkAnim + Math.PI) * 8) : 0;
            g.setColor(topColor);
            g.drawLine(cx, sy + 26, cx - 12 - armSwing, sy + 38);
            g.drawLine(cx, sy + 26, cx + 12 + armSwing, sy + 38);
            g.setColor(gloveColor);
            g.fillOval(cx - 14 - armSwing, sy + 36, 6, 6);
            g.fillOval(cx + 10 + armSwing, sy + 36, 6, 6);
            return;
        }

        double prog = attackProgress(); // 0.0 ~ 1.0
        int    dir  = facingRight ? 1 : -1;

        if (attackComboIndex == 0) {
            drawSlashArms(g, cx, sy, dir, prog, topColor, gloveColor);
        } else {
            drawThrustArms(g, cx, sy, dir, prog, topColor, gloveColor);
        }
    }

    /**
     * 劈砍動畫（0 段）：手臂從右後上方斜劈至左前下方。
     * 蓄勢 → 舉高 → 大力下劈 → 收勢
     */
    private void drawSlashArms(Graphics2D g, int cx, int sy,
                                int dir, double prog,
                                Color topColor, Color gloveColor) {
        int frontArmEndX, frontArmEndY;
        int backArmEndX,  backArmEndY;

        if (prog < STRIKE_START) {
            // 蓄勢：前手舉高（右上）
            double t = prog / STRIKE_START;
            frontArmEndX = cx + dir * (int)(-4 + 8 * t);
            frontArmEndY = sy + (int)(30 - 18 * t); // 往上舉
            backArmEndX  = cx - dir * 10;
            backArmEndY  = sy + 34;
        } else if (prog < STRIKE_END) {
            // 揮擊：前手大弧度由高往低前方劈下
            double t = (prog - STRIKE_START) / (STRIKE_END - STRIKE_START);
            frontArmEndX = cx + dir * (int)(4 + 22 * t);
            frontArmEndY = sy + (int)(12 + 34 * t); // 從高到低
            backArmEndX  = cx - dir * 8;
            backArmEndY  = sy + 36;
        } else {
            // 收勢：前手回到腰間
            double t = (prog - STRIKE_END) / (1.0 - STRIKE_END);
            frontArmEndX = cx + dir * (int)(26 - 14 * t);
            frontArmEndY = sy + (int)(46 - 10 * t);
            backArmEndX  = cx - dir * (int)(8 - 3 * t);
            backArmEndY  = sy + 36;
        }

        // 後手
        g.setColor(topColor);
        g.drawLine(cx, sy + 26, backArmEndX, backArmEndY);

        // 前手（揮擊時橘色）
        boolean inStrike = prog >= STRIKE_START && prog <= STRIKE_END;
        Color frontColor = inStrike ? new Color(255, 190, 60) : topColor;
        g.setColor(frontColor);
        g.drawLine(cx, sy + 26, frontArmEndX, frontArmEndY);

        // 劍（斜劈時垂直偏多）
        drawWeapon(g, frontArmEndX, frontArmEndY, dir, 18, -12);

        g.setColor(gloveColor);
        g.fillOval(frontArmEndX - 3, frontArmEndY - 3, 6, 6);
    }

    /**
     * 突刺動畫（1 段）：手臂水平向前伸出，刺出長線。
     * 蓄勢 → 縮手 → 快速水平刺出 → 收勢
     */
    private void drawThrustArms(Graphics2D g, int cx, int sy,
                                 int dir, double prog,
                                 Color topColor, Color gloveColor) {
        int frontArmEndX, frontArmEndY;
        int backArmEndX,  backArmEndY;

        if (prog < STRIKE_START) {
            // 蓄勢：前手往後收
            double t = prog / STRIKE_START;
            frontArmEndX = cx + dir * (int)(12 - 20 * t); // 往後拉
            frontArmEndY = sy + 28;
            backArmEndX  = cx - dir * (int)(8 + 5 * t);
            backArmEndY  = sy + 34;
        } else if (prog < STRIKE_END) {
            // 刺出：手水平射出
            double t = (prog - STRIKE_START) / (STRIKE_END - STRIKE_START);
            frontArmEndX = cx + dir * (int)(-8 + 38 * t); // 快速向前
            frontArmEndY = sy + 28;
            backArmEndX  = cx - dir * 13;
            backArmEndY  = sy + 36;
        } else {
            // 收勢：手臂收回一點
            double t = (prog - STRIKE_END) / (1.0 - STRIKE_END);
            frontArmEndX = cx + dir * (int)(30 - 12 * t);
            frontArmEndY = sy + (int)(28 + 8 * t);
            backArmEndX  = cx - dir * (int)(13 - 4 * t);
            backArmEndY  = sy + 36;
        }

        // 後手
        g.setColor(topColor);
        g.drawLine(cx, sy + 26, backArmEndX, backArmEndY);

        // 前手（揮擊時亮藍白）
        boolean inStrike = prog >= STRIKE_START && prog <= STRIKE_END;
        Color frontColor = inStrike ? new Color(160, 220, 255) : topColor;
        g.setColor(frontColor);
        g.drawLine(cx, sy + 26, frontArmEndX, frontArmEndY);

        // 劍（突刺時水平延伸更長）
        int swordExtX = inStrike ? 28 : 18;
        drawWeapon(g, frontArmEndX, frontArmEndY, dir, swordExtX, -2);

        g.setColor(gloveColor);
        g.fillOval(frontArmEndX - 3, frontArmEndY - 3, 6, 6);
    }

    /** 在手端繪製武器 */
    private void drawWeapon(Graphics2D g, int handX, int handY,
                             int dir, int lenX, int lenY) {
        Equipment w = equip(EquipSlot.WEAPON);
        if (w == null) return;

        g.setColor(w.getDisplayColor());
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(handX, handY,
                   handX + dir * lenX,
                   handY + lenY);

        // 劍柄橫線
        g.setColor(new Color(180, 140, 60));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(handX - 3, handY + 1, handX + 3, handY - 3);

        g.setStroke(new BasicStroke(2.2f));
    }

    // ─────────────────────────────────────────────────────────
    // 取得裝備（null 安全）
    // ─────────────────────────────────────────────────────────
    private Equipment equip(EquipSlot slot) {
        return equipments.getOrDefault(slot, null);
    }

    // ─────────────────────────────────────────────────────────
    // Getter / Setter
    // ─────────────────────────────────────────────────────────
    public void setMovingLeft (boolean v) { movingLeft  = v; }
    public void setMovingRight(boolean v) { movingRight = v; }
    public void setMovingUp   (boolean v) { movingUp    = v; }
    public void setMovingDown (boolean v) { movingDown  = v; }
    public boolean canChangeJob()         { return level >= 10 && job == null; }

    public double  getX()                  { return x; }
    public double  getY()                  { return y; }
    public int     getHp()                 { return hp; }
    public int     getMaxHp()              { return maxHp; }
    public int     getMp()                 { return mp; }
    public int     getMaxMp()              { return maxMp; }
    public int     getLevel()              { return level; }
    public String  getJobName()            { return jobName; }
    public boolean isAttacking()           { return attacking; }
    public boolean isFacingRight()         { return facingRight; }
    public int     getStr()                { return str; }
    public int     getDex()                { return dex; }
    public int     getIntel()              { return intel; }
    public int     getLuk()                { return luk; }
    public int     getExp()                { return exp; }
    public int     getExpToNextLevel()     { return expToNextLevel; }
    public double  getTimeSinceLastCombat(){ return timeSinceLastCombat; }
    public Job     getJob()                { return job; }

    public double getExpRatio() {
        return expToNextLevel > 0 ? (double) exp / expToNextLevel : 0;
    }

    public Map<EquipSlot, Equipment> getEquipments() { return equipments; }

    // ── 角色名稱 & 金幣 ──────────────────────────────────────
    public String getName()          { return name; }
    public void   setName(String n)  { name = (n != null && !n.isBlank()) ? n.trim() : "新手"; }
    public int    getGold()          { return gold; }
    public void   gainGold(int g)    { gold = Math.max(0, gold + g); }
    public boolean spendGold(int g)  {
        if (gold < g) return false;
        gold -= g; return true;
    }

    // ── 完整狀態 setter（存讀檔用）─────────────────────────
    public void setLevel(int v)      { level = v; }
    public void setExp(int v)        { exp = v; }
    public void setExpToNextLevel(int v) { expToNextLevel = v; }
    public void setStr(int v)        { str = v;   recalculateStats(); }
    public void setDex(int v)        { dex = v; }
    public void setIntel(int v)      { intel = v; recalculateStats(); }
    public void setLuk(int v)        { luk = v; }
    public void setHp(int v)         { hp = Math.min(v, maxHp); }
    public void setMp(int v)         { mp = Math.min(v, maxMp); }
    public void setMaxHp(int v)      { maxHp = v; }
    public void setMaxMp(int v)      { maxMp = v; }
    public void setJob(Job j)        { job = j; if (j != null) jobName = j.getDisplayName(); }
    public void setGold(int v)       { gold = Math.max(0, v); }
}
