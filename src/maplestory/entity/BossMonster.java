package maplestory.entity;

import maplestory.audio.SFX;
import maplestory.audio.SoundManager;
import maplestory.core.Camera;
import maplestory.core.GamePanel;
import maplestory.item.Consumable;
import maplestory.item.DropItem;
import maplestory.item.Equipment;
import maplestory.map.BaseMap;
import maplestory.map.Platform;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Boss 怪物基底類別。
 *
 * 支援兩種 Boss：
 *   GLOFEN  — 荊棘守衛者 葛羅芬（古老森林，Lv20）
 *   PHARAOH — 永恆守墓者 法拉歐（沙漠廢墟，Lv30）
 *
 * 特色：
 *   - 三段 Phase 系統（HP 60%/30% 觸發，技能強化）
 *   - 攻擊碰撞箱列表（近戰 / 拋射物 / 範圍）
 *   - 預警框（紅色虛線框 + 倒數）→ 轉換為攻擊箱
 *   - 進場動畫（2 秒升起）
 *   - Boss 血量條（drawBossHpBar，由 GamePanel 在 HUD 上繪製）
 *   - 封牆（arenaLeft / arenaRight，GamePanel 強制玩家留在戰鬥區域）
 *   - 死亡粒子爆散 + 掉落系統
 */
public class BossMonster {

    // ═══════════════════════════════════════════════════════════
    // Boss 種類
    // ═══════════════════════════════════════════════════════════
    public enum BossType { GLOFEN, PHARAOH }

    // ═══════════════════════════════════════════════════════════
    // 內部資料類別
    // ═══════════════════════════════════════════════════════════

    /** 主動攻擊碰撞箱（動態生成，持續 lifetime 秒後消失） */
    private static class AttackBox {
        double x, y, w, h;
        int    damage;
        double lifetime;
        double velX;      // 拋射物用（>0 向右，<0 向左）
        boolean hitPlayer; // 防止一個碰撞箱重複傷害
        String  tag;       // 視覺顏色分類

        AttackBox(double x, double y, double w, double h,
                  int dmg, double life, double velX, String tag) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.damage = dmg; this.lifetime = life;
            this.velX = velX; this.tag = tag;
        }
    }

    /** 預警框（視覺提示，countdown 後轉為 AttackBox） */
    private static class WarnBox {
        double x, y, w, h;
        double remaining; // 剩餘預警時間（秒）
        int    damage;
        double velX;
        String type;

        WarnBox(double x, double y, double w, double h,
                double remaining, int damage, double velX, String type) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.remaining = remaining; this.damage = damage;
            this.velX = velX; this.type = type;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 物理常數
    // ═══════════════════════════════════════════════════════════
    private static final double GRAVITY = 1400;

    // ═══════════════════════════════════════════════════════════
    // 屬性
    // ═══════════════════════════════════════════════════════════
    private final BossType type;
    private final double   spawnX, spawnY;
    private       double   x, y, velY;
    private       boolean  facingRight = false;

    public final int width, height;

    private final int maxHp;
    private       int hp;
    private final int expReward;

    // ── Phase ─────────────────────────────────────────────────
    private int phase = 1;

    // ── 狀態旗標 ─────────────────────────────────────────────
    private boolean alive       = false; // 未觸發前為 false
    private boolean triggered   = false;
    private boolean entering    = false;
    private double  enterTimer  = 0;
    private static final double ENTER_DURATION = 2.0;

    // ── 計時器 ───────────────────────────────────────────────
    private double idleTimer      = 0;
    private double hurtTimer      = 0;
    private int    lastDamage     = 0;
    private double walkAnim       = 0;

    // ── 技能冷卻 ─────────────────────────────────────────────
    private double skill1Cd = 0;
    private double skill2Cd = 0;
    private double skill3Cd = 0;
    private double skill4Cd = 0;
    private boolean curseCast = false; // Pharaoh 詛咒凝視（只觸發一次）

    // ── Windup（預備動作）────────────────────────────────────
    private double  windupTimer  = 0;
    private boolean windupActive = false;
    private String  pendingSkill = null;

    // ── 攻擊箱列表 ───────────────────────────────────────────
    private final List<AttackBox> hitboxes = new ArrayList<>();
    private final List<WarnBox>   warnings = new ArrayList<>();

    // ── 封牆 ─────────────────────────────────────────────────
    private boolean arenaActive = false;
    private double  arenaLeft;
    private double  arenaRight;

    // ── 觸發 X 座標 ──────────────────────────────────────────
    private final double triggerX;

    // ── 死亡 ─────────────────────────────────────────────────
    private double  deathTimer  = 0;
    private boolean justDied    = false;
    private boolean dropPending = false;

    // ── 死亡粒子 ─────────────────────────────────────────────
    private final List<double[]> deathParticles = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════
    // 建構子
    // ═══════════════════════════════════════════════════════════
    public BossMonster(BossType type, double spawnX, double spawnY) {
        this.type   = type;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.x      = spawnX;
        this.y      = spawnY;

        int w = 0, h = 0, mhp = 0, exp = 0;
        switch (type) {
            case GLOFEN  -> { w = 68;  h = 96;  mhp = 18000; exp = 2400; }
            case PHARAOH -> { w = 72;  h = 108; mhp = 32000; exp = 5500; }
        }
        width = w; height = h; maxHp = mhp; expReward = exp;
        hp       = maxHp;
        triggerX = spawnX - 480; // 玩家進入 Boss 左方 480px 觸發
    }

    // ═══════════════════════════════════════════════════════════
    // 每幀更新
    // ═══════════════════════════════════════════════════════════
    public void update(double dt, BaseMap map, Player player) {

        // ── 觸發判定 ─────────────────────────────────────────
        if (!triggered && player.getX() >= triggerX) {
            triggered   = true;
            alive       = true;
            entering    = true;
            enterTimer  = ENTER_DURATION;
            arenaActive = true;
            arenaLeft   = triggerX - 60;
            arenaRight  = spawnX + width + 120;
            SoundManager.get().playSFX(SFX.MONSTER_DEATH); // 進場衝擊音效
        }

        if (!triggered) return;

        if (!alive) {
            if (deathTimer > 0) {
                deathTimer -= dt;
                updateDeathParticles(dt);
            }
            return;
        }

        // ── 進場動畫（2 秒）──────────────────────────────────
        if (entering) {
            enterTimer -= dt;
            if (enterTimer <= 0) {
                entering = false;
            }
            return; // 進場中不執行 AI
        }

        idleTimer += dt;
        if (hurtTimer  > 0) hurtTimer  -= dt;
        if (skill1Cd   > 0) skill1Cd   -= dt;
        if (skill2Cd   > 0) skill2Cd   -= dt;
        if (skill3Cd   > 0) skill3Cd   -= dt;
        if (skill4Cd   > 0) skill4Cd   -= dt;

        // ── Phase 切換 ───────────────────────────────────────
        double hpRatio = (double) hp / maxHp;
        int newPhase = hpRatio > 0.60 ? 1 : hpRatio > 0.30 ? 2 : 3;
        if (newPhase != phase) {
            phase = newPhase;
            // Phase 切換：短暫停頓感
            skill1Cd = Math.max(skill1Cd, 1.8);
        }

        // ── 攻擊箱更新 ───────────────────────────────────────
        updateAttackBoxes(dt, player);
        updateWarnings(dt);

        // ── Windup 中：不移動，等倒數完成後釋放技能 ──────────
        if (windupActive) {
            windupTimer -= dt;
            if (windupTimer <= 0) {
                windupActive = false;
                executeSkill(pendingSkill, player, map);
                pendingSkill = null;
            }
            return;
        }

        // ── Boss AI ──────────────────────────────────────────
        switch (type) {
            case GLOFEN  -> updateGlofen (dt, map, player);
            case PHARAOH -> updatePharaoh(dt, map, player);
        }

        // 地圖邊界
        if (x < 0) x = 0;
        if (x > map.getMapWidth() - width) x = map.getMapWidth() - width;
    }

    // ═══════════════════════════════════════════════════════════
    // 荊棘守衛者 AI
    // ═══════════════════════════════════════════════════════════
    private void updateGlofen(double dt, BaseMap map, Player player) {
        velY += GRAVITY * dt;
        y    += velY * dt;
        checkPlatform(map, dt);

        double playerCX = player.getX() + Player.WIDTH / 2.0;
        double selfCX   = x + width / 2.0;
        double distX    = playerCX - selfCX;
        double distAbs  = Math.abs(distX);
        facingRight = distX > 0;

        double speed = switch (phase) {
            case 1 -> 90;
            case 2 -> 117;
            default -> 220; // Phase 3：狂暴衝刺
        };
        if (distX > 0) { x += speed * dt; walkAnim += dt * 5; }
        else           { x -= speed * dt; walkAnim += dt * 5; }

        // 技能決策
        double s1Cd = phase == 1 ? 2.5 : phase == 2 ? 1.5 : 0.8;
        if (skill1Cd <= 0 && distAbs < 110) {
            startWindup("s1", 0.4);
            skill1Cd = s1Cd;
        }
        if (skill2Cd <= 0 && distAbs >= 150 && distAbs <= 320) {
            startWindup("s2", 0.3);
            skill2Cd = phase == 1 ? 4.0 : 2.5;
        }
        if (phase >= 2 && skill3Cd <= 0 && distAbs < 180) {
            startWindup("s3", 0.8);
            skill3Cd = phase == 2 ? 6.0 : 3.0;
        }
        if (phase == 3 && skill4Cd <= 0) {
            startWindup("s4", 0.5);
            skill4Cd = 8.0;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 永恆守墓者 AI
    // ═══════════════════════════════════════════════════════════
    private void updatePharaoh(double dt, BaseMap map, Player player) {
        velY += GRAVITY * dt;
        y    += velY * dt;
        checkPlatform(map, dt);

        double playerCX = player.getX() + Player.WIDTH / 2.0;
        double selfCX   = x + width / 2.0;
        double distX    = playerCX - selfCX;
        double distAbs  = Math.abs(distX);
        facingRight = distX > 0;

        double speed = switch (phase) {
            case 1 -> 72;
            case 2 -> 132;
            default -> 50; // Phase 3：定點召喚型
        };
        if (distX > 0) { x += speed * dt; walkAnim += dt * 4; }
        else           { x -= speed * dt; walkAnim += dt * 4; }

        // 技能決策
        if (skill1Cd <= 0 && distAbs < 130) {
            startWindup("s1", 0.4);
            skill1Cd = phase == 1 ? 2.5 : 1.8;
        }
        if (skill2Cd <= 0 && distAbs >= 180 && distAbs <= 420) {
            startWindup("s2", 0.3);
            skill2Cd = phase == 1 ? 5.0 : 3.0;
        }
        if (phase >= 2 && skill3Cd <= 0 && distAbs < 300) {
            startWindup("s3", 0.5);
            skill3Cd = phase == 2 ? 7.0 : 4.0;
        }
        if (phase == 3 && skill4Cd <= 0) {
            startWindup("s4", 0.6);
            skill4Cd = 5.0;
        }
        // 詛咒凝視（一次性，HP < 15%）
        if (phase == 3 && !curseCast && (double) hp / maxHp < 0.15) {
            curseCast = true;
            player.applyAtkDebuff(10.0, 0.75);
            hurtTimer = 1.5; // 眼部強光閃爍
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Windup 啟動
    // ═══════════════════════════════════════════════════════════
    private void startWindup(String skill, double duration) {
        if (windupActive) return;
        windupActive = true;
        windupTimer  = duration;
        pendingSkill = skill;
    }

    // ═══════════════════════════════════════════════════════════
    // 技能執行（Windup 結束後呼叫）
    // ═══════════════════════════════════════════════════════════
    private void executeSkill(String skill, Player player, BaseMap map) {
        double cx  = x + width  / 2.0;
        double cy  = y + height / 2.0;
        double gY  = y + height;       // Boss 腳底 Y

        switch (type) {
            case GLOFEN  -> execGlofen (skill, cx, cy, gY, player);
            case PHARAOH -> execPharaoh(skill, cx, cy, gY, player);
        }
    }

    private void execGlofen(String skill, double cx, double cy, double gY, Player player) {
        switch (skill) {
            case "s1" -> { // 荊棘爪擊（近身）
                double bx = facingRight ? x + width : x - 120;
                int dmg = 65;
                hitboxes.add(new AttackBox(bx, y, 120, height, dmg, 0.30, 0, "claw"));
                if (phase == 3) { // Phase 3 連段第 2 擊（稍遠碰撞箱）
                    hitboxes.add(new AttackBox(
                        facingRight ? x + width - 10 : x - 140, y,
                        140, height, dmg, 0.55, 0, "claw"));
                }
                SoundManager.get().playSFX(SFX.ATTACK);
            }
            case "s2" -> { // 藤蔓射擊（水平拋射物）
                double velX = facingRight ? 350 : -350;
                hitboxes.add(new AttackBox(cx - 8, cy - 6, 20, 12, 55, 3.5, velX, "vine"));
            }
            case "s3" -> { // 荊棘圈（8 方向刺爆）
                int radius = phase == 2 ? 120 : 160;
                for (int i = 0; i < 8; i++) {
                    double angle = Math.PI * 2 / 8 * i;
                    double tx = cx + Math.cos(angle) * radius - 10;
                    double ty = cy + Math.sin(angle) * radius - 10;
                    hitboxes.add(new AttackBox(tx, ty, 20, 20, 100, 0.40, 0, "thorn"));
                }
                SoundManager.get().playSFX(SFX.SKILL_SHOCKWAVE);
            }
            case "s4" -> { // 根系爆發（玩家位置 3 根荊棘柱，1s 預警）
                double playerCX = player.getX() + Player.WIDTH / 2.0;
                for (int i = -1; i <= 1; i++) {
                    double wx = playerCX + i * 55 - 15;
                    warnings.add(new WarnBox(wx, gY - 160, 30, 160, 1.0, 180, 0, "column"));
                }
            }
        }
    }

    private void execPharaoh(String skill, double cx, double cy, double gY, Player player) {
        switch (skill) {
            case "s1" -> { // 彎刀橫斬（寬扇形）
                double bx = facingRight ? x + width - 20 : x - 150;
                hitboxes.add(new AttackBox(bx, y - 10, 150, height + 20, 90, 0.35, 0, "slash"));
                if (phase >= 2) x += facingRight ? 80 : -80; // 衝刺追加
                SoundManager.get().playSFX(SFX.ATTACK);
            }
            case "s2" -> { // 連枷投擲（落點預警）
                double playerCX = player.getX() + Player.WIDTH / 2.0;
                double playerGY = player.getY() + Player.HEIGHT;
                warnings.add(new WarnBox(playerCX - 24, playerGY - 70, 48, 70, 0.8, 80, 0, "flail"));
            }
            case "s3" -> { // 沙暴衝擊波（地板雙向延伸）
                double waveY = gY - 55;
                // 向右
                hitboxes.add(new AttackBox(cx,       waveY, 400, 55, 140, 1.2,  300, "wave"));
                // 向左
                hitboxes.add(new AttackBox(cx - 400, waveY, 400, 55, 140, 1.2, -300, "wave"));
                SoundManager.get().playSFX(SFX.SKILL_SHOCKWAVE);
            }
            case "s4" -> { // 沙柱召喚（3 根，1s 預警）
                double playerCX = player.getX() + Player.WIDTH / 2.0;
                for (int i = 0; i < 3; i++) {
                    double wx = playerCX + (i - 1) * 55 - 15;
                    warnings.add(new WarnBox(wx, gY - 200, 30, 200, 1.0, 200, 0, "column"));
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 攻擊箱 / 預警框 更新
    // ═══════════════════════════════════════════════════════════
    private void updateAttackBoxes(double dt, Player player) {
        Rectangle plBox = new Rectangle(
            (int) player.getX(), (int) player.getY(), Player.WIDTH, Player.HEIGHT);

        hitboxes.removeIf(box -> {
            box.lifetime -= dt;
            box.x        += box.velX * dt;
            if (box.x < -500 || box.x > 6000) return true;

            if (!box.hitPlayer) {
                Rectangle r = new Rectangle(
                    (int) box.x, (int) box.y, (int) box.w, (int) box.h);
                if (r.intersects(plBox)) {
                    player.takeDamage(box.damage);
                    box.hitPlayer = true;
                }
            }
            return box.lifetime <= 0;
        });
    }

    private void updateWarnings(double dt) {
        List<WarnBox> toConvert = new ArrayList<>();
        warnings.removeIf(w -> {
            w.remaining -= dt;
            if (w.remaining <= 0) { toConvert.add(w); return true; }
            return false;
        });
        for (WarnBox w : toConvert) {
            hitboxes.add(new AttackBox(w.x, w.y, w.w, w.h,
                                       w.damage, 0.50, w.velX, w.type));
        }
    }

    // ── 平台碰撞 ─────────────────────────────────────────────
    private void checkPlatform(BaseMap map, double dt) {
        for (Platform p : map.getPlatforms()) {
            if (x + width <= p.getX() || x >= p.getX() + p.getWidth()) continue;
            double feet     = y + height;
            double prevFeet = feet - velY * dt;
            if (prevFeet <= p.getY() && feet >= p.getY() && velY > 0) {
                y    = p.getY() - height;
                velY = 0;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 受傷
    // ═══════════════════════════════════════════════════════════
    public void takeDamage(int dmg) {
        if (!alive || entering) return;
        hp        -= dmg;
        lastDamage = dmg;
        hurtTimer  = 0.22;
        if (hp <= 0) {
            hp          = 0;
            alive       = false;
            deathTimer  = 3.0;
            justDied    = true;
            dropPending = true;
            arenaActive = false;
            hitboxes.clear();
            warnings.clear();
            initDeathParticles();
            SoundManager.get().playSFX(SFX.MONSTER_DEATH);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 死亡粒子
    // ═══════════════════════════════════════════════════════════
    private void initDeathParticles() {
        deathParticles.clear();
        for (int i = 0; i < 50; i++) {
            deathParticles.add(new double[]{
                x + Math.random() * width,          // px
                y + Math.random() * height,          // py
                (Math.random() - 0.5) * 240,        // velX
                -80 - Math.random() * 220,           // velY
                1.8 + Math.random() * 1.5,           // life
                4  + Math.random() * 8               // size
            });
        }
    }

    private void updateDeathParticles(double dt) {
        deathParticles.removeIf(p -> {
            p[0] += p[2] * dt;
            p[1] += p[3] * dt;
            p[3] += 700 * dt; // 重力
            p[4] -= dt;
            return p[4] <= 0;
        });
    }

    // ═══════════════════════════════════════════════════════════
    // 掉落物
    // ═══════════════════════════════════════════════════════════
    public List<DropItem> rollDrops() {
        List<DropItem> list = new ArrayList<>();
        double cx = x + width / 2.0;
        double cy = y;

        // 金幣（固定掉落）
        int gold = switch (type) {
            case GLOFEN  -> 180 + (int)(Math.random() * 141);
            case PHARAOH -> 400 + (int)(Math.random() * 251);
        };
        list.add(DropItem.gold(cx,      cy, gold));
        list.add(DropItem.gold(cx + 24, cy, 15 + (int)(Math.random() * 40)));

        // 固定掉落：萬能藥水
        list.add(DropItem.consumable(cx - 20, cy, Consumable.elixir()));

        // 稀有裝備掉落
        switch (type) {
            case GLOFEN  -> rollGlofenDrops (list, cx, cy);
            case PHARAOH -> rollPharaohDrops(list, cx, cy);
        }
        return list;
    }

    private void rollGlofenDrops(List<DropItem> list, double cx, double cy) {
        double r = Math.random();
        if      (r < 0.08)        list.add(DropItem.equipment(cx,      cy, Equipment.forestCoreAmulet()));
        else if (r < 0.08 + 0.05) list.add(DropItem.equipment(cx + 12, cy, Equipment.vineWristguard()));
        if (Math.random() < 0.15) list.add(DropItem.consumable(cx - 14, cy, Consumable.ancientForestEssence()));
    }

    private void rollPharaohDrops(List<DropItem> list, double cx, double cy) {
        double r = Math.random();
        if      (r < 0.06)        list.add(DropItem.equipment(cx,      cy, Equipment.pharaohCurvedSword()));
        else if (r < 0.06 + 0.04) list.add(DropItem.equipment(cx + 12, cy, Equipment.scarabAmulet()));
        if (Math.random() < 0.10) list.add(DropItem.consumable(cx - 14, cy, Consumable.pharaohBandage()));
    }

    // ═══════════════════════════════════════════════════════════
    // 繪製
    // ═══════════════════════════════════════════════════════════
    public void draw(Graphics2D g, Camera camera) {
        if (!triggered) return;
        double camX = camera.getOffsetX();
        double camY = camera.getOffsetY();

        // 預警框（橙色虛線閃爍）
        for (WarnBox w : warnings) {
            float alpha = (float) Math.min(1.0, 0.35 + 0.65 * (1.0 - w.remaining / 1.5));
            g.setColor(new Color(1f, 0.4f, 0f, alpha));
            float[] dash = {8f, 5f};
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                                        BasicStroke.JOIN_ROUND, 1f, dash,
                                        (float)(idleTimer * 20 % 13)));
            g.drawRect((int)(w.x - camX), (int)(w.y - camY), (int) w.w, (int) w.h);
            // 填充（半透明）
            g.setColor(new Color(1f, 0.4f, 0f, alpha * 0.18f));
            g.fillRect((int)(w.x - camX), (int)(w.y - camY), (int) w.w, (int) w.h);
            g.setStroke(new BasicStroke(1f));
        }

        // 攻擊碰撞箱（半透明，用於可視化）
        for (AttackBox box : hitboxes) {
            Color c = switch (box.tag) {
                case "claw", "slash" -> new Color(255,  80,  80, 55);
                case "vine", "flail" -> new Color( 80, 220,  80, 55);
                case "wave"          -> new Color(240, 200,  60, 65);
                case "thorn"         -> new Color(120,  80, 220, 65);
                case "column"        -> new Color(220, 160,  30, 65);
                default              -> new Color(200, 200, 200, 40);
            };
            g.setColor(c);
            g.fillRect((int)(box.x - camX), (int)(box.y - camY),
                       (int) box.w, (int) box.h);
        }

        // 死亡演出
        if (!alive) {
            drawDeathEffect(g, camera);
            return;
        }

        int sx = (int)(x - camX);
        int sy = (int)(y - camY);

        // 進場動畫（從地面升起）
        if (entering) {
            double progress = 1.0 - (enterTimer / ENTER_DURATION);
            // 前 1 秒：地面震裂；後 1 秒：Boss 升起
            drawEnterAnimation(g, sx, sy, progress, camY);
            return;
        }

        // Boss 本體
        switch (type) {
            case GLOFEN  -> drawGlofen (g, sx, sy);
            case PHARAOH -> drawPharaoh(g, sx, sy);
        }

        drawHpBarLocal(g, camera);
        drawDamageFloat(g, camera);
    }

    // ── 全局 Boss 血量條（由 GamePanel 在遊戲區頂部繪製）─────
    public void drawBossHpBar(Graphics2D g) {
        if (!triggered || !alive || entering) return;

        int barW = 320, barH = 14;
        int barX = (GamePanel.SCREEN_WIDTH - barW) / 2;
        int barY = 8;

        // 名稱
        String bossName = switch (type) {
            case GLOFEN  -> "【Boss】荊棘守衛者 · 葛羅芬";
            case PHARAOH -> "【Boss】永恆守墓者 · 法拉歐";
        };
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(255, 200, 60));
        g.drawString(bossName,
                     (GamePanel.SCREEN_WIDTH - fm.stringWidth(bossName)) / 2,
                     barY + barH + 14);

        // 血量條底板
        g.setColor(new Color(30, 0, 0, 200));
        g.fillRect(barX - 2, barY - 2, barW + 4, barH + 4);
        g.setColor(new Color(50, 0, 0));
        g.fillRect(barX, barY, barW, barH);

        // 填充
        int fillW = (int)(barW * (double) hp / maxHp);
        Color barColor = switch (phase) {
            case 1 -> new Color(200,  60,  60);
            case 2 -> new Color(220, 120,  30);
            default -> new Color(190,  30, 190);
        };
        g.setColor(barColor);
        g.fillRect(barX, barY, fillW, barH);

        // 光澤
        g.setColor(new Color(255, 255, 255, 50));
        g.fillRect(barX, barY, fillW, barH / 2);

        // 邊框
        g.setColor(new Color(180, 180, 180));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect(barX, barY, barW, barH);
        g.setStroke(new BasicStroke(1f));

        // HP 數字
        String hpStr = hp + " / " + maxHp;
        g.setFont(new Font("Arial", Font.BOLD, 9));
        g.setColor(Color.WHITE);
        g.drawString(hpStr,
                     barX + (barW - g.getFontMetrics().stringWidth(hpStr)) / 2,
                     barY + 10);

        // Phase 指示點
        g.setFont(new Font("Arial", Font.BOLD, 9));
        g.setColor(barColor.brighter());
        g.drawString("Phase " + phase, barX + barW + 6, barY + 10);
    }

    // ═══════════════════════════════════════════════════════════
    // 葛羅芬 繪製（樹妖）
    // ═══════════════════════════════════════════════════════════
    private void drawGlofen(Graphics2D g, int sx, int sy) {
        int cx = sx + width / 2;

        Color coreColor = switch (phase) {
            case 1 -> new Color(60,  220, 80);
            case 2 -> new Color(180, 220, 30);
            default -> new Color(230, 40,  40);
        };
        Color bodyColor = hurtTimer > 0 ? new Color(255, 120, 60)
                        : new Color(80, 50, 25);

        g.setStroke(new BasicStroke(1.5f));

        // ── 軀幹（古木橢圓）
        g.setColor(bodyColor);
        g.fillOval(sx + 10, sy + 30, width - 20, height - 30);
        g.setColor(bodyColor.darker());
        g.setStroke(new BasicStroke(2f));
        g.drawOval(sx + 10, sy + 30, width - 20, height - 30);

        // 藤蔓紋路
        g.setColor(new Color(50, 90, 20, 170));
        g.setStroke(new BasicStroke(2f));
        g.drawArc(sx + 15, sy + 42, 22, 36,   0, 200);
        g.drawArc(sx + 30, sy + 52, 18, 30,  20, 200);
        g.drawArc(sx + 18, sy + 68, 26, 26, -20, 180);

        // ── 頭部
        g.setColor(bodyColor);
        g.setStroke(new BasicStroke(2f));
        g.fillOval(sx + 14, sy + 2, width - 28, 42);
        g.setColor(bodyColor.darker());
        g.drawOval(sx + 14, sy + 2, width - 28, 42);

        // ── 胸口核心水晶（脈動）
        float pulse = (float)(0.6 + 0.4 * Math.sin(idleTimer * 4.5));
        g.setColor(new Color(coreColor.getRed(), coreColor.getGreen(),
                             coreColor.getBlue(), (int)(210 * pulse)));
        g.fillOval(cx - 11, sy + 46, 22, 22);
        g.setColor(coreColor.brighter());
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(cx - 11, sy + 46, 22, 22);
        // 光暈
        g.setColor(new Color(coreColor.getRed(), coreColor.getGreen(),
                             coreColor.getBlue(), (int)(55 * pulse)));
        g.fillOval(cx - 20, sy + 37, 40, 40);

        // ── 眼睛（Phase 3 轉血紅）
        Color eyeColor = phase >= 3 ? new Color(255, 30, 30) : new Color(255, 165, 0);
        g.setColor(eyeColor);
        g.fillOval(cx - 14, sy + 10, 10, 12);
        g.fillOval(cx + 4,  sy + 10, 10, 12);
        g.setColor(Color.BLACK);
        g.fillOval(cx - 11, sy + 14,  5,  5);
        g.fillOval(cx +  7, sy + 14,  5,  5);

        // ── 左臂（藤蔓爪）
        g.setColor(new Color(55, 100, 20));
        g.setStroke(new BasicStroke(5f));
        int armSwing = (int)(Math.sin(walkAnim) * 7);
        g.drawLine(sx + 12, sy + 52, sx - 6,  sy + 63 + armSwing);
        g.drawLine(sx - 6,  sy + 63 + armSwing, sx - 16, sy + 75 + armSwing);
        // 爪刺
        g.setColor(new Color(130, 75, 20));
        g.setStroke(new BasicStroke(2.5f));
        g.drawLine(sx - 16, sy + 75, sx - 27, sy + 68);
        g.drawLine(sx - 16, sy + 77, sx - 28, sy + 81);
        g.drawLine(sx - 16, sy + 79, sx - 25, sy + 88);

        // ── 右臂（藤蔓爪）
        g.setColor(new Color(55, 100, 20));
        g.setStroke(new BasicStroke(5f));
        int rx = sx + width;
        g.drawLine(rx - 12, sy + 52, rx + 6,  sy + 63 - armSwing);
        g.drawLine(rx + 6,  sy + 63 - armSwing, rx + 16, sy + 75 - armSwing);
        g.setColor(new Color(130, 75, 20));
        g.setStroke(new BasicStroke(2.5f));
        int rax = rx + 16;
        g.drawLine(rax, sy + 75, rax + 11, sy + 68);
        g.drawLine(rax, sy + 77, rax + 12, sy + 81);
        g.drawLine(rax, sy + 79, rax + 10, sy + 88);

        // ── 樹根腳
        g.setColor(new Color(60, 40, 15));
        g.setStroke(new BasicStroke(4f));
        g.drawLine(cx - 15, sy + height, cx - 26, sy + height + 10);
        g.drawLine(cx - 5,  sy + height, cx - 7,  sy + height + 12);
        g.drawLine(cx + 8,  sy + height, cx + 10, sy + height + 12);
        g.drawLine(cx + 20, sy + height, cx + 30, sy + height + 10);

        // ── Windup 發光（預備動作提示）
        if (windupActive) {
            float glow = (float)(0.5 + 0.5 * Math.sin(idleTimer * 30));
            g.setColor(new Color(coreColor.getRed(), coreColor.getGreen(),
                                 coreColor.getBlue(), (int)(200 * glow)));
            g.setStroke(new BasicStroke(3f));
            int wax = facingRight ? rax : sx - 16;
            g.drawLine(wax - 5, sy + 58, wax + (facingRight ? 18 : -18), sy + 48);
        }
        g.setStroke(new BasicStroke(1f));
    }

    // ═══════════════════════════════════════════════════════════
    // 法拉歐 繪製（木乃伊法老）
    // ═══════════════════════════════════════════════════════════
    private void drawPharaoh(Graphics2D g, int sx, int sy) {
        int cx = sx + width / 2;
        int dir = facingRight ? 1 : -1;

        Color bodyBase  = hurtTimer > 0 ? new Color(255, 180, 80)
                        : new Color(210, 180, 100);
        Color accentCol = new Color(150, 110, 40);

        g.setStroke(new BasicStroke(1.5f));

        // ── 軀幹（帶橫向繃帶紋路）
        g.setColor(bodyBase);
        g.fillRoundRect(sx + 12, sy + 32, width - 24, height - 42, 8, 8);
        g.setColor(accentCol);
        g.setStroke(new BasicStroke(1f));
        for (int ly = sy + 40; ly < sy + height - 30; ly += 10) {
            g.drawLine(sx + 13, ly, sx + width - 13, ly);
        }

        // ── 頭部
        g.setColor(bodyBase);
        g.setStroke(new BasicStroke(1.5f));
        g.fillOval(sx + 8, sy + 4, width - 16, 34);
        g.setColor(accentCol);
        g.drawOval(sx + 8, sy + 4, width - 16, 34);

        // ── 雙冠（尖白冠 + 紅冠帶）
        int[] crownX = {cx - 13, cx + 13, cx + 8, cx, cx - 8};
        int[] crownY = {sy + 6,  sy + 6,  sy - 18, sy - 34, sy - 18};
        g.setColor(new Color(240, 238, 220));
        g.fillPolygon(crownX, crownY, 5);
        g.setColor(accentCol);
        g.drawPolygon(crownX, crownY, 5);
        g.setColor(new Color(175, 35, 35));
        g.fillArc(sx + 6, sy + 2, width - 12, 18, 0, 180);

        // ── 眼睛（聖甲蟲藍 → Phase 3 橙紅）
        Color eyeC = phase >= 3 ? new Color(255, 100, 20) : new Color(55, 155, 255);
        g.setColor(eyeC);
        g.fillOval(cx - 14, sy + 14, 10, 12);
        g.fillOval(cx +  4, sy + 14, 10, 12);
        // 眼睛脈動光暈
        float pulse = (float)(0.5 + 0.5 * Math.sin(idleTimer * 3));
        g.setColor(new Color(eyeC.getRed(), eyeC.getGreen(), eyeC.getBlue(), (int)(80 * pulse)));
        g.fillOval(cx - 20, sy + 8, 22, 22);
        g.fillOval(cx -  2, sy + 8, 22, 22);

        // ── 彎刀（右手）
        int wepX = facingRight ? sx + width - 2 : sx - 28;
        g.setColor(new Color(200, 185, 55));
        g.setStroke(new BasicStroke(3f));
        g.drawArc(wepX, sy + 36, 32, 44, facingRight ? 0 : 150, 150);
        // 刀柄
        g.setColor(new Color(160, 130, 40));
        g.setStroke(new BasicStroke(4f));
        g.drawLine(wepX + (facingRight ? 0 : 32), sy + 44,
                   wepX + (facingRight ? 0 : 32), sy + 56);

        // ── 連枷（左手，甩動）
        int flaX = facingRight ? sx - 8 : sx + width - 8;
        double swing = Math.sin(walkAnim * 1.5) * 14;
        g.setColor(new Color(140, 120, 50));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(flaX + 8, sy + 46, (int)(flaX + 6 + swing), sy + 72);
        g.setColor(new Color(165, 55, 20));
        g.fillOval((int)(flaX + swing) - 1, sy + 67, 16, 16);
        // 連枷尖刺
        g.setColor(new Color(185, 75, 30));
        g.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 4; i++) {
            double a = Math.PI / 4 * i;
            int tx = (int)(flaX + swing + 7 + Math.cos(a) * 11);
            int ty = (int)(sy + 75 + Math.sin(a) * 11);
            g.drawLine((int)(flaX + swing + 7), sy + 75, tx, ty);
        }

        // ── 腿柱
        g.setColor(bodyBase);
        g.setStroke(new BasicStroke(10f));
        g.drawLine(cx - 12, sy + height - 20, cx - 12, sy + height);
        g.drawLine(cx + 12, sy + height - 20, cx + 12, sy + height);

        // Phase 2+ 繃帶散落粒子
        if (phase >= 2) {
            g.setColor(new Color(200, 170, 100, 90));
            g.setStroke(new BasicStroke(1f));
            for (int i = 0; i < 6; i++) {
                int px = sx + (int)(Math.random() * width);
                int py = sy + (int)(Math.random() * height);
                g.fillRect(px, py, 3, 2);
            }
        }

        g.setStroke(new BasicStroke(1f));
    }

    // ── 頭上血量條 ────────────────────────────────────────────
    private void drawHpBarLocal(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());
        int cx = sx + width / 2;

        int barW = 100, barH = 7;
        int barX = cx - barW / 2;
        int barY = sy - 16;

        g.setColor(new Color(40, 0, 0));
        g.fillRect(barX, barY, barW, barH);
        Color barFill = switch (phase) {
            case 1 -> new Color(200,  60,  60);
            case 2 -> new Color(220, 120,  30);
            default -> new Color(200,  30, 200);
        };
        g.setColor(barFill);
        g.fillRect(barX, barY, (int)(barW * (double) hp / maxHp), barH);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect(barX, barY, barW, barH);
        g.setStroke(new BasicStroke(1f));

        String name = type == BossType.GLOFEN ? "葛羅芬" : "法拉歐";
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 9));
        g.setColor(new Color(255, 200, 60));
        g.drawString(name, barX, barY - 2);
    }

    // ── 傷害飄字 ─────────────────────────────────────────────
    private void drawDamageFloat(Graphics2D g, Camera camera) {
        if (hurtTimer <= 0) return;
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());
        int cx = sx + width / 2;

        float alpha = (float)(hurtTimer / 0.22);
        g.setColor(new Color(1f, 1f, 0.2f, Math.min(1f, alpha)));
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("-" + lastDamage, cx - 18, sy - 20);
    }

    // ── 進場動畫 ─────────────────────────────────────────────
    private void drawEnterAnimation(Graphics2D g, int sx, int sy,
                                    double progress, double camY) {
        int groundSY = (int)(spawnY + height - camY);

        // 地面震裂（碎石粒子）
        g.setColor(new Color(100, 70, 30, (int)(180 * (1 - progress))));
        g.setStroke(new BasicStroke(2.5f));
        g.drawLine(sx,         groundSY, sx - 28,       groundSY + 14);
        g.drawLine(sx + width, groundSY, sx + width + 22, groundSY + 12);
        g.drawLine(sx + 20,    groundSY, sx + 15,       groundSY + 18);
        g.setStroke(new BasicStroke(1f));

        // 後半（progress > 0.5）：Boss 升起
        if (progress > 0.5) {
            double riseP   = (progress - 0.5) / 0.5; // 0→1
            int    drawSY  = groundSY - (int)(height * riseP);

            // 裁剪：只顯示 groundSY 以上的部分
            Shape oldClip = g.getClip();
            g.setClip(0, 0, GamePanel.SCREEN_WIDTH, groundSY + 10);
            switch (type) {
                case GLOFEN  -> drawGlofen (g, sx, drawSY);
                case PHARAOH -> drawPharaoh(g, sx, drawSY);
            }
            g.setClip(oldClip);
        }
    }

    // ── 死亡特效 ─────────────────────────────────────────────
    private void drawDeathEffect(Graphics2D g, Camera camera) {
        double camX = camera.getOffsetX(), camY = camera.getOffsetY();

        Color pcol = type == BossType.GLOFEN
                   ? new Color(60,  210, 60)
                   : new Color(200, 170, 70);

        for (double[] p : deathParticles) {
            float alpha = (float)(p[4] / 3.0) * 0.9f;
            g.setColor(new Color(pcol.getRed(), pcol.getGreen(), pcol.getBlue(),
                                 (int)(alpha * 230)));
            int px = (int)(p[0] - camX);
            int py = (int)(p[1] - camY);
            int ps = (int) p[5];
            g.fillOval(px - ps / 2, py - ps / 2, ps, ps);
        }

        // 爆散圓環（死亡後 1.5s 內）
        if (deathTimer > 1.5) {
            float prog   = (float)((3.0 - deathTimer) / 1.5);
            int   radius = (int)(100 * prog);
            float alpha  = 1.0f - prog;
            int   cx     = (int)(x + width  / 2 - camX);
            int   cy2    = (int)(y + height / 2 - camY);
            g.setColor(new Color(1f, 0.85f, 0.3f, alpha));
            g.setStroke(new BasicStroke(5f));
            g.drawOval(cx - radius, cy2 - radius, radius * 2, radius * 2);
            g.setStroke(new BasicStroke(1f));
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Getter / 公開方法
    // ═══════════════════════════════════════════════════════════

    public boolean pollJustDied() {
        if (justDied) { justDied = false; return true; }
        return false;
    }

    public boolean pollDropPending() {
        if (dropPending) { dropPending = false; return true; }
        return false;
    }

    public int       getExpReward()    { return expReward; }
    public boolean   isAlive()         { return alive; }
    public boolean   isTriggered()     { return triggered; }
    public boolean   isEntering()      { return entering; }
    public double    getX()            { return x; }
    public double    getY()            { return y; }

    /** Boss 本體碰撞箱（玩家攻擊判定用） */
    public Rectangle getBoundingBox() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    /** 封牆：Boss 存活且已觸發時啟用，限制玩家移動範圍 */
    public boolean isArenaActive() { return arenaActive; }
    public double  getArenaLeft()  { return arenaLeft;   }
    public double  getArenaRight() { return arenaRight;  }
}
