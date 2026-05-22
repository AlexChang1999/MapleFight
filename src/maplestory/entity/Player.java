package maplestory.entity;

import maplestory.audio.SFX;
import maplestory.audio.SoundManager;
import maplestory.core.Camera;
import maplestory.item.Equipment;
import maplestory.item.EquipSlot;
import maplestory.item.Inventory;
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
    private double spawnX, spawnY;

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
    private boolean movingUp           = false;
    private boolean movingDown         = false;
    private boolean onLadder           = false;
    private Ladder  currentLadder      = null;
    private double  ladderExitCooldown = 0; // 離梯後短暫禁止重新進入梯子
    private static final double CLIMB_SPEED = 115;

    // ── 冰雪緩速 ─────────────────────────────────────────────
    private double slowTimer  = 0;
    private double slowFactor = 1.0; // 1.0=正常, <1.0=減速

    // ── 升級特效 ─────────────────────────────────────────────
    private double levelUpTimer = 0;

    // ── 受傷表情計時 ─────────────────────────────────────────
    private double hurtTimer = 0;

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

    // ── 背包 ─────────────────────────────────────────────────
    private final Inventory inventory = new Inventory();

    // ── 寵物欄（預留）────────────────────────────────────────
    @SuppressWarnings("unused")
    private boolean canHavePet = false;

    private final Camera camera;

    // ─────────────────────────────────────────────────────────
    public Player(double x, double y, Camera camera) {
        this.x      = x;
        this.y      = y;
        this.spawnX = x;
        this.spawnY = y;
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

    /** 重新計算 maxHp / maxMp（換裝備後呼叫），含套裝加成 */
    private void recalculateStats() {
        int hpBonus  = equipments.values().stream().mapToInt(Equipment::getHpBonus).sum();
        int mpBonus  = equipments.values().stream().mapToInt(Equipment::getMpBonus).sum();
        // 套裝加成 {str, def, atk, hp}
        int[] setB   = Equipment.calcSetBonus(equipments.values());
        maxHp = 100 + str * 10 + hpBonus + setB[3]; // setB[3] = 套裝 HP 加成
        maxMp =  30 + intel * 5 + mpBonus;
    }

    /** 取得套裝攻擊加成（Player.checkAttackHits 中使用） */
    public int getSetAtkBonus() {
        int[] b = Equipment.calcSetBonus(equipments.values());
        return b[2];
    }

    // ─────────────────────────────────────────────────────────
    // 每幀更新
    // ─────────────────────────────────────────────────────────
    public void update(double dt, BaseMap map) {
        idleTimer           += dt;
        timeSinceLastCombat += dt;
        if (slowTimer          > 0) { slowTimer    -= dt; if (slowTimer    <= 0) slowFactor = 1.0; }
        if (levelUpTimer       > 0)   levelUpTimer -= dt;
        if (hurtTimer          > 0)   hurtTimer    -= dt;
        if (ladderExitCooldown > 0)   ladderExitCooldown -= dt;

        // ── 梯子偵測 ─────────────────────────────────────────
        boolean wasOnLadder = onLadder;
        onLadder = false;
        for (Ladder lad : map.getLadders()) {
            if (lad.getZone().intersects(
                    new Rectangle((int) x, (int) y, WIDTH, HEIGHT))) {
                // 離梯冷卻期間禁止重新進入，避免踏上頂端平台後被瞬間拉回梯子
                if (ladderExitCooldown <= 0 && (wasOnLadder || movingUp || movingDown)) {
                    onLadder      = true;
                    currentLadder = lad;
                    // 對齊梯子中心 X（平滑插值）
                    double tx = lad.getCenterX() - WIDTH / 2.0;
                    x += (tx - x) * Math.min(1.0, 14 * dt);
                }
                break;
            }
        }
        if (!onLadder) currentLadder = null;

        // ── 位移 ─────────────────────────────────────────────
        if (onLadder) {
            velX = 0; velY = 0;
            if (movingUp)   velY = -CLIMB_SPEED;
            if (movingDown) velY =  CLIMB_SPEED;
            y += velY * dt;

            // 抵達梯子頂端 → 踏上平台
            if (currentLadder != null && y + HEIGHT <= currentLadder.getTopY()) {
                y                  = currentLadder.getTopY() - HEIGHT;
                onLadder           = false;
                velY               = 0;
                onGround           = true;
                ladderExitCooldown = 0.3; // 防止重新被梯子捕捉
                currentLadder      = null;
            }
            // 抵達梯子底端 → 落地離梯
            else if (currentLadder != null && y + HEIGHT >= currentLadder.getBotY()) {
                y                  = currentLadder.getBotY() - HEIGHT;
                onLadder           = false;
                velY               = 0;
                ladderExitCooldown = 0.3; // 防止立即被梯子重新捕捉
                currentLadder      = null;
            }
        } else {
            // 剛脫離梯子且正在上升 → 清除上升慣性，避免飄移
            if (wasOnLadder && velY < 0) velY = 0;

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
        // 掉出地圖下方 → 回到出生點（700 = GAME_HEIGHT 500 + 緩衝 200）
        if (y > 700) {
            x = spawnX; y = spawnY; velX = 0; velY = 0;
        }

        // ── 平台碰撞（爬梯時跳過） ───────────────────────────
        onGround = false;
        if (!onLadder) {
            for (Platform p : map.getPlatforms()) {
                if (x + WIDTH <= p.getX() || x >= p.getX() + p.getWidth()) continue;
                double feet     = y + HEIGHT;
                double prevFeet = feet - velY * dt;
                // velY >= 0：涵蓋梯子頂端踏台時 velY=0 的特殊情況
                if (prevFeet <= p.getY() && feet >= p.getY() && velY >= 0) {
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
            onLadder = false; velY = JUMP_FORCE * 0.7;
            SoundManager.get().playSFX(SFX.JUMP);
        } else if (onGround) {
            velY = JUMP_FORCE; onGround = false;
            SoundManager.get().playSFX(SFX.JUMP);
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
            attackComboIndex = 1 - attackComboIndex;
            timeSinceLastCombat = 0;
            SoundManager.get().playSFX(SFX.ATTACK);
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
            expToNextLevel = (int)(100 * Math.pow(level, 1.5));
            str += 2; dex += 1; intel += 1; luk += 1;
            recalculateStats();
            hp = maxHp; mp = maxMp;
            levelUpTimer = 2.5;
            SoundManager.get().playSFX(SFX.LEVEL_UP);
            checkJobUnlock();
        }
    }

    /**
     * 升級時的職業解鎖提示（不再自動轉劍士）。
     * 玩家需要到轉職所 NPC 手動選職業。
     */
    private void checkJobUnlock() {
        // Lv.10 以上且未轉職：在 HUD 顯示「可轉職」提示
        // 實際轉職由 VillageMap 的轉職所 NPC dialogue 觸發，
        // 透過 GamePanel.handleDialogueConfirm("job_warrior"/"job_mage"/"job_archer") 完成。
    }

    /**
     * 手動設定職業（由 GamePanel 在轉職對話確認後呼叫）。
     * @param newJob 新職業實例（Warrior / Mage / Archer）
     */
    public void changeJob(maplestory.job.Job newJob) {
        if (job != null) return; // 已轉職，不能再轉
        job          = newJob;
        jobName      = newJob.getDisplayName();
        levelUpTimer = 3.5;
    }

    /** 是否達到轉職條件（Lv.10 以上且尚未轉職） */
    public boolean canChangeJob() { return level >= 10 && job == null; }

    /** 總擊殺數（轉職任務計數用） */
    private int totalMonstersKilled = 0;
    public void addKill()           { totalMonstersKilled++; }
    public int  getTotalKills()     { return totalMonstersKilled; }

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
        int setAtk   = getSetAtkBonus();

        // 弓箭手鷹眼 buff 倍率
        double hawkMult = 1.0;
        if (job instanceof maplestory.job.Archer a) hawkMult = a.getDamageMultiplier();

        for (Monster m : monsters) {
            if (!m.isAlive() || m.isHitThisAttack()) continue;
            if (box.intersects(m.getBoundingBox())) {
                int dmg = (int)((BASE_DAMAGE + str * 2 + atkBonus + setAtk) * hawkMult);
                m.takeDamage(dmg);
                m.setHitThisAttack(true);
            }
        }
        timeSinceLastCombat = 0; // 命中後重置脫戰計時
    }

    public void takeDamage(int dmg) {
        // 弓箭手被動閃避 10%
        if (job instanceof maplestory.job.Archer a && a.tryDodge()) return;
        hp = Math.max(0, hp - dmg);
        timeSinceLastCombat = 0;
        hurtTimer = 0.5;
        SoundManager.get().playSFX(SFX.HURT);
    }

    /** 消耗 MP，不足時回傳 false */
    public boolean consumeMp(int amount) {
        if (mp < amount) return false;
        mp -= amount;
        return true;
    }

    /**
     * 回復 HP（不超過 maxHp）。
     * @return 實際回復量（可能因滿血而少於 amount）
     */
    public int healHp(int amount) {
        int before = hp;
        hp = Math.min(maxHp, hp + amount);
        return hp - before;
    }

    /**
     * 回復 MP（不超過 maxMp）。
     * @return 實際回復量
     */
    public int healMp(int amount) {
        int before = mp;
        mp = Math.min(maxMp, mp + amount);
        return mp - before;
    }

    /**
     * 從背包裝備欄位 index 取出並穿上裝備。
     * 若該 slot 已有裝備，舊裝備放回背包（因為剛騰出一格，必定成功）。
     * @return true 若成功裝備
     */
    public boolean equipFromInventory(int invIndex) {
        Equipment e = inventory.removeEquipment(invIndex); // 從背包移除（騰出一格）
        if (e == null) return false;
        Equipment old = equipments.put(e.getSlot(), e);   // 穿上，取回舊裝備
        if (old != null) inventory.addEquipment(old);      // 舊裝備放回背包（必定成功）
        recalculateStats();
        SoundManager.get().playSFX(SFX.EQUIP);
        return true;
    }

    /**
     * 脫下指定欄位的裝備，放回背包。
     * @return true 若成功脫裝（背包有空間）
     */
    public boolean unequip(EquipSlot slot) {
        Equipment e = equipments.get(slot);
        if (e == null) return false;
        if (!inventory.addEquipment(e)) return false; // 背包滿，無法脫裝
        equipments.remove(slot);
        recalculateStats();
        return true;
    }

    /** 地圖切換時設定新位置，同步更新出生點 */
    public void setPosition(double nx, double ny) {
        x = nx; y = ny; velX = 0; velY = 0;
        spawnX = nx; spawnY = ny;
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
            if (levelUpTimer > 2.5) {
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

        // ── 裝備參照 ─────────────────────────────────────────
        Equipment topEq     = equip(EquipSlot.TOP);
        Equipment bottomEq  = equip(EquipSlot.BOTTOM);
        Equipment helmetEq  = equip(EquipSlot.HELMET);
        Equipment gloveEq   = equip(EquipSlot.GLOVES);
        Equipment bootEq    = equip(EquipSlot.BOOTS);
        Equipment capeEq    = equip(EquipSlot.CAPE);
        Equipment earringEq = equip(EquipSlot.EARRING);

        Color topColor    = topEq    != null ? topEq.getDisplayColor()    : new Color(190, 190, 210);
        Color bottomColor = bottomEq != null ? bottomEq.getDisplayColor() : new Color(130, 130, 170);
        Color gloveColor  = gloveEq  != null ? gloveEq.getDisplayColor()  : new Color(210, 180, 130);
        Color bootColor   = bootEq   != null ? bootEq.getDisplayColor()   : new Color(150, 110, 75);

        int legSwing = onGround && !attacking ? (int)(Math.sin(walkAnim) * 10) : 0;

        g.setStroke(new BasicStroke(2.2f));

        // ── 披風（在身體後方，最先畫）────────────────────────
        if (capeEq != null) drawCape(g, cx, sy, capeEq.getDisplayColor());

        // ── 身體（上衣，填色圓角矩形）────────────────────────
        g.setColor(topColor);
        g.fillRoundRect(cx - 8, sy + 20, 16, 20, 4, 4);
        g.setColor(topColor.darker());
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(cx - 8, sy + 20, 16, 20, 4, 4);
        g.setStroke(new BasicStroke(2.2f));

        // ── 手臂（含連擊動畫）────────────────────────────────
        drawArms(g, cx, sy, topColor, gloveColor);

        // ── 腳（下褲色，走路對向擺動，有厚度）───────────────
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(bottomColor);
        g.drawLine(cx - 2, sy + 40, cx - 8 - legSwing, sy + 56);
        g.drawLine(cx + 2, sy + 40, cx + 8 + legSwing, sy + 56);
        g.setStroke(new BasicStroke(2.2f));

        // ── 靴子（梯形填色）──────────────────────────────────
        boolean leftToe  = !facingRight; // 鞋尖方向
        boolean rightToe = facingRight;
        drawBoot(g, cx - 8 - legSwing, sy + 56, bootColor, leftToe);
        drawBoot(g, cx + 8 + legSwing, sy + 56, bootColor, rightToe);

        // ── 頭（實心圓，膚色）────────────────────────────────
        Color skinColor = new Color(255, 215, 165);
        g.setColor(skinColor);
        g.fillOval(cx - 10, sy, 20, 20);
        g.setColor(new Color(210, 160, 110));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(cx - 10, sy, 20, 20);
        g.setStroke(new BasicStroke(2.2f));

        // ── 頭盔（蓋在頭頂）──────────────────────────────────
        if (helmetEq != null) drawHelmet(g, cx, sy, helmetEq.getDisplayColor());

        // ── 耳環（頭側小圓）──────────────────────────────────
        if (earringEq != null) {
            int earX = facingRight ? cx + 9 : cx - 9;
            g.setColor(earringEq.getDisplayColor());
            g.fillOval(earX - 3, sy + 12, 6, 6);
            g.setColor(earringEq.getDisplayColor().darker());
            g.setStroke(new BasicStroke(1f));
            g.drawOval(earX - 3, sy + 12, 6, 6);
            g.setStroke(new BasicStroke(2.2f));
        }

        // ── 臉（眼白 + 瞳孔 + 嘴）───────────────────────────
        int eyeDir = facingRight ? 4 : -4;
        g.setStroke(new BasicStroke(1.5f));

        if (hurtTimer > 0) {
            // 受傷：X 字眼睛 + 下彎嘴 + 眉毛下垂
            g.setColor(Color.BLACK);
            int ex = cx + eyeDir - 1, ey = sy + 6;
            g.drawLine(ex - 2, ey,     ex + 2, ey + 4);
            g.drawLine(ex - 2, ey + 4, ex + 2, ey);
            g.setColor(new Color(180, 30, 30));
            g.drawArc(cx - 4, sy + 13, 8, 5, 0, -180);
            g.setColor(Color.BLACK);
            if (facingRight) g.drawLine(cx + eyeDir - 3, sy + 4, cx + eyeDir + 1, sy + 6);
            else             g.drawLine(cx + eyeDir - 1, sy + 4, cx + eyeDir + 3, sy + 6);
        } else if (attacking) {
            // 攻擊：眼神銳利（縮瞳 + 眉毛下斜）
            g.setColor(Color.WHITE);
            g.fillOval(cx + eyeDir - 3, sy + 7, 6, 5);
            g.setColor(new Color(40, 25, 10));
            int pupilAtkX = facingRight ? cx + eyeDir - 1 : cx + eyeDir - 2;
            g.fillOval(pupilAtkX, sy + 8, 3, 3);
            g.setColor(Color.BLACK);
            if (facingRight) g.drawLine(cx + eyeDir - 4, sy + 5, cx + eyeDir + 2, sy + 7);
            else             g.drawLine(cx + eyeDir - 2, sy + 7, cx + eyeDir + 4, sy + 5);
            g.setColor(new Color(80, 40, 20));
            g.drawLine(cx - 3, sy + 14, cx + 3, sy + 14);
        } else {
            // 正常：眼白 + 深色瞳孔 + 高光 + 微笑
            g.setColor(Color.WHITE);
            g.fillOval(cx + eyeDir - 3, sy + 7, 6, 6);
            g.setColor(new Color(40, 25, 10));
            int pupilOffX = facingRight ? 1 : -1;
            g.fillOval(cx + eyeDir - 2 + pupilOffX, sy + 8, 3, 4);
            g.setColor(new Color(255, 255, 255, 180));
            g.fillOval(cx + eyeDir - 1 + pupilOffX, sy + 8, 1, 1);
            g.setColor(new Color(130, 65, 45));
            g.drawArc(cx - 4, sy + 12, 8, 5, 0, 180);
        }
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

    /** 靴子：梯形填色（鞋尖依面向延伸） */
    private void drawBoot(Graphics2D g, int footX, int footY, Color color, boolean toeRight) {
        int toe = toeRight ? 3 : -3;
        int[] bx = {footX - 4 + toe, footX + 8 + toe, footX + 8, footX - 4};
        int[] by = {footY,            footY,            footY + 7, footY + 7};
        g.setColor(color);
        g.fillPolygon(bx, by, 4);
        g.setColor(color.darker());
        g.setStroke(new BasicStroke(1f));
        g.drawPolygon(bx, by, 4);
        g.setStroke(new BasicStroke(2.2f));
    }

    /** 頭盔：弧形蓋住頭頂 + 兩側護頰 */
    private void drawHelmet(Graphics2D g, int cx, int sy, Color color) {
        g.setColor(color);
        g.fillArc(cx - 13, sy - 5, 26, 26, 8, 164);
        g.setColor(color.darker());
        g.setStroke(new BasicStroke(1.5f));
        g.drawArc(cx - 13, sy - 5, 26, 26, 8, 164);
        // 護頰
        g.setColor(color);
        g.fillRect(cx - 13, sy + 6, 4, 8);
        g.fillRect(cx + 9,  sy + 6, 4, 8);
        g.setColor(color.darker());
        g.drawRect(cx - 13, sy + 6, 4, 8);
        g.drawRect(cx + 9,  sy + 6, 4, 8);
        g.setStroke(new BasicStroke(2.2f));
    }

    /** 披風：背後飄逸三角形 */
    private void drawCape(Graphics2D g, int cx, int sy, Color color) {
        int d = facingRight ? -1 : 1;
        int[] px = {cx + d * 2, cx + d * 22, cx + d * 16};
        int[] py = {sy + 22,    sy + 28,      sy + 46};
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 215));
        g.fillPolygon(px, py, 3);
        g.setColor(color.darker());
        g.setStroke(new BasicStroke(1f));
        g.drawPolygon(px, py, 3);
        g.setStroke(new BasicStroke(2.2f));
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

    /** 在手端繪製武器（填色幾何形狀） */
    private void drawWeapon(Graphics2D g, int handX, int handY,
                             int dir, int lenX, int lenY) {
        Equipment w = equip(EquipSlot.WEAPON);
        if (w == null) return;

        Color wColor = w.getDisplayColor();
        int tipX = handX + dir * lenX;
        int tipY = handY + lenY;

        // 劍身（填色三角形，有厚度）
        int[] bx = {handX + dir * 3, handX - dir * 2, tipX};
        int[] by = {handY - 2, handY + 3, tipY};
        g.setColor(wColor);
        g.fillPolygon(bx, by, 3);
        // 劍刃高光
        g.setColor(wColor.brighter());
        g.setStroke(new BasicStroke(1f));
        g.drawLine(handX + dir * 3, handY - 2, tipX, tipY);

        // 護手（十字形，金色）
        g.setColor(new Color(210, 170, 55));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(handX - 5, handY + 1, handX + 5, handY + 1);

        // 劍柄（深棕色）
        g.setColor(new Color(115, 72, 35));
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(handX, handY + 1, handX - dir * 7, handY + 3);

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
    public Inventory getInventory() { return inventory; }

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
