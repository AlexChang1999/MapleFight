package maplestory.entity;

import maplestory.audio.SFX;
import maplestory.audio.SoundManager;
import maplestory.core.Camera;
import maplestory.item.Consumable;
import maplestory.item.DropItem;
import maplestory.item.Equipment;
import maplestory.map.BaseMap;
import maplestory.map.Platform;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用怪物類別。
 * 支援三種動物外型：史萊姆、野豬、蝙蝠。
 * 每種類型擁有獨立的 AI 行為與繪製邏輯。
 */
public class Monster {

    // ── 物理常數 ─────────────────────────────────────────────
    private static final double GRAVITY    = 1400;
    private static final double ATTACK_CD  = 1.5;
    private static final int    ATTACK_RANGE = 50;

    // ── 野豬衝刺常數 ─────────────────────────────────────────
    private static final double BOAR_CHARGE_SPEED    = 270;  // 衝刺速度
    private static final double BOAR_CHARGE_DURATION = 0.7;  // 衝刺持續秒
    private static final double BOAR_CHARGE_CD       = 3.0;  // 衝刺冷卻

    // ── 蝙蝠俯衝常數 ─────────────────────────────────────────
    private static final double BAT_DIVE_SPEED   = 260;  // 俯衝速度
    private static final double BAT_DIVE_CD      = 3.5;  // 俯衝冷卻
    private static final int    BAT_DIVE_X_RANGE = 150;  // 觸發俯衝的X距離上限

    // ── 類型與尺寸 ───────────────────────────────────────────
    private final MonsterType type;
    private final int width;
    private final int height;

    // ── 位置與速度 ───────────────────────────────────────────
    private double x, y;
    private final double spawnX; // 重生 X 座標
    private final double spawnY; // 蝙蝠懸停基準高度 / 重生 Y 座標
    private double velY = 0;
    private boolean facingRight = false;

    // ── 數值（從 MonsterType 取得） ──────────────────────────
    private int hp;
    private final int maxHp;
    private final int atk;

    // ── 通用狀態 ─────────────────────────────────────────────
    private boolean alive         = true;
    private boolean hitThisAttack = false;
    private double  attackCooldown = 0;
    private double  hurtTimer      = 0;
    private double  deathTimer     = 0;
    private int     lastDamage     = 0;
    private double  idleTimer      = 0;  // 通用動畫計時
    private double  walkAnim       = 0;  // 走路週期

    // ── 嘲諷狀態（衝擊波技能用） ─────────────────────────────
    private boolean taunted    = false;
    private double  tauntTimer = 0;

    // ── EXP 掉落（一次性讀取） ────────────────────────────────
    private boolean justDied   = false;
    private boolean dropPending = false; // 等待 GamePanel 讀取掉落物

    // ── 重生 ─────────────────────────────────────────────────
    private double  respawnTimer = 0;

    // ── 野豬專屬：衝刺 + 撞牆硬直 ───────────────────────────
    private boolean boarCharging    = false;
    private double  boarChargeTimer = 0;
    private double  boarChargeCd    = 0;
    private double  boarStunTimer   = 0;   // 撞牆後的硬直秒數

    // ── 蝙蝠專屬：俯衝 ───────────────────────────────────────
    private boolean batDiving      = false;
    private boolean batReturning   = false;
    private double  batDiveCd      = 0;
    private double  batDiveTargetY = 0;

    // ── 極地熊專屬：揮擊預備 ─────────────────────────────────
    private double  bearWindupTimer  = 0;     // 預備倒數計時
    private boolean bearWindupActive = false; // 是否正在預備中

    // ─────────────────────────────────────────────────────────
    public Monster(double x, double y, MonsterType type) {
        this.type   = type;
        this.x      = x;
        this.y      = y;
        this.spawnX = x; // 重生座標
        this.spawnY = y; // 蝙蝠懸停目標 / 重生座標
        this.width  = type.width;
        this.height = type.height;
        this.maxHp  = type.maxHp;
        this.hp     = type.maxHp;
        this.atk    = type.atk;
    }

    // ─────────────────────────────────────────────────────────
    // 每幀更新
    // ─────────────────────────────────────────────────────────
    public void update(double dt, BaseMap gameMap, Player player) {
        if (!alive) {
            if (deathTimer > 0) {
                deathTimer -= dt;
            } else if (respawnTimer > 0) {
                respawnTimer -= dt;
                if (respawnTimer <= 0) doRespawn();
            }
            return;
        }

        idleTimer += dt;

        // 計時器倒數
        if (hurtTimer    > 0) hurtTimer    -= dt;
        if (attackCooldown > 0) attackCooldown -= dt;
        if (tauntTimer   > 0) { tauntTimer -= dt; if (tauntTimer <= 0) taunted = false; }
        if (boarStunTimer > 0) boarStunTimer -= dt;
        if (bearWindupTimer > 0) bearWindupTimer -= dt;

        // 依種類執行不同更新
        switch (type) {
            case SLIME, ICE_SLIME              -> updateSlime(dt, gameMap, player);
            case BOAR,  POLAR_BEAR             -> updateBoar (dt, gameMap, player);
            case BAT,   ICE_BAT                -> updateBat  (dt, gameMap, player);
        }

        // 地圖邊界
        if (x < 0) x = 0;
        if (x > gameMap.getMapWidth() - width) x = gameMap.getMapWidth() - width;
    }

    // ── 史萊姆：簡單追擊 + 跳躍感 ───────────────────────────
    private void updateSlime(double dt, BaseMap map, Player player) {
        velY += GRAVITY * dt;
        y    += velY * dt;
        checkPlatformCollision(map, dt);

        double distX   = player.getX() - x;
        double distAbs = Math.abs(distX);
        int    range   = (taunted) ? 9999 : type.detectRange;

        if (distAbs < range) {
            double speed = type.moveSpeed;
            if (distX > 0) { x += speed * dt; facingRight = true;  walkAnim += dt * 5; }
            else            { x -= speed * dt; facingRight = false; walkAnim += dt * 5; }
        }

        tryAttack(player);
    }

    // ── 野豬 / 極地熊：衝刺 AI ──────────────────────────────
    private void updateBoar(double dt, BaseMap map, Player player) {
        velY += GRAVITY * dt;
        y    += velY * dt;
        checkPlatformCollision(map, dt);

        if (boarChargeCd > 0) boarChargeCd -= dt;

        // 硬直中：靜止不動
        if (boarStunTimer > 0) {
            walkAnim = 0;
            tryAttackWithWindup(player);
            return;
        }

        if (boarChargeTimer > 0) {
            // 衝刺中
            double dir = facingRight ? 1 : -1;
            double prevX = x;
            x += BOAR_CHARGE_SPEED * dir * dt;
            walkAnim += dt * 10;
            boarChargeTimer -= dt;

            // 撞到地圖邊界 → 硬直
            boolean hitWall = (x <= 0 && !facingRight)
                           || (x >= map.getMapWidth() - width && facingRight);
            if (hitWall) {
                x = prevX; // 還原到牆前
                boarCharging    = false;
                boarChargeTimer = 0;
                boarChargeCd    = BOAR_CHARGE_CD;
                boarStunTimer   = 0.5; // 撞牆後硬直 0.5 秒
            } else if (boarChargeTimer <= 0) {
                boarCharging = false;
                boarChargeCd = BOAR_CHARGE_CD;
            }
        } else {
            // 普通追擊
            double distX   = player.getX() - x;
            double distAbs = Math.abs(distX);
            int    range   = taunted ? 9999 : type.detectRange;

            if (distAbs < range) {
                // 觸發衝刺（野豬才衝刺；極地熊僅走路追擊）
                if (type == MonsterType.BOAR && boarChargeCd <= 0 && distAbs < type.detectRange * 0.8) {
                    boarCharging    = true;
                    boarChargeTimer = BOAR_CHARGE_DURATION;
                    facingRight     = distX > 0;
                }
                double speed = type.moveSpeed;
                if (distX > 0) { x += speed * dt; facingRight = true;  walkAnim += dt * 6; }
                else            { x -= speed * dt; facingRight = false; walkAnim += dt * 6; }
            }
        }

        tryAttackWithWindup(player);
    }

    /**
     * 帶預備動作的攻擊邏輯（極地熊 0.3s 預備；其他怪物直接攻擊）。
     * 預備邏輯：
     *   接觸玩家 + 冷卻完畢 → 若尚未開始預備，啟動 bearWindupTimer = 0.3s
     *   預備計時中 → return（不打）
     *   預備計時到 0 → 揮擊並重置冷卻
     */
    private void tryAttackWithWindup(Player player) {
        if (attackCooldown > 0) return;
        Rectangle atkBox = new Rectangle((int) x, (int) y, width, height);
        Rectangle plBox  = new Rectangle(
            (int) player.getX(), (int) player.getY(), Player.WIDTH, Player.HEIGHT);

        if (atkBox.intersects(plBox)) {
            if (type == MonsterType.POLAR_BEAR) {
                if (!bearWindupActive) {
                    // 啟動 0.3s 預備
                    bearWindupTimer  = 0.3;
                    bearWindupActive = true;
                    return;
                }
                // 預備倒數中（由 update() 主計時區遞減）
                if (bearWindupTimer > 0) return;
                // 倒數完畢 → 揮擊
                player.takeDamage(atk);
                if (type.iceType) player.applySlow(2.5, 0.5);
                attackCooldown   = ATTACK_CD;
                bearWindupActive = false;
            } else {
                tryAttack(player);
            }
        } else {
            // 脫離攻擊範圍，重置預備狀態
            bearWindupActive = false;
            bearWindupTimer  = 0;
        }
    }

    // ── 蝙蝠：懸停 → 俯衝 → 返回 三段 AI ────────────────────
    private void updateBat(double dt, BaseMap map, Player player) {
        if (batDiveCd > 0) batDiveCd -= dt;

        double distX   = player.getX() - x;
        double distAbs = Math.abs(distX);
        int    range   = taunted ? 9999 : type.detectRange;

        if (batDiving) {
            // 俯衝中：直線向下 + 繼續水平貼近玩家
            y += BAT_DIVE_SPEED * dt;
            double spd = type.moveSpeed * 1.5;
            if (distX > 0) { x += spd * dt; facingRight = true;  }
            else            { x -= spd * dt; facingRight = false; }

            // 抵達目標高度或飛太遠 → 切換返回
            if (y >= batDiveTargetY || y > spawnY + 500) {
                batDiving    = false;
                batReturning = true;
            }
            tryAttack(player);

        } else if (batReturning) {
            // 返回：彈簧回到出生高度
            velY = (spawnY - y) * 6.0;
            y   += velY * dt;
            // 返回時仍緩慢水平追蹤
            if (distAbs < range) {
                double spd = type.moveSpeed * 0.5;
                if (distX > 0) { x += spd * dt; facingRight = true;  }
                else            { x -= spd * dt; facingRight = false; }
            }
            if (Math.abs(y - spawnY) < 15) {
                batReturning = false;
                batDiveCd    = BAT_DIVE_CD;
            }

        } else {
            // 懸停：彈簧浮動 + 水平追蹤
            double targetY = spawnY + Math.sin(idleTimer * 2.5) * 18;
            velY = (targetY - y) * 5.0;
            y   += velY * dt;

            if (distAbs < range) {
                double spd = type.moveSpeed;
                if (distX > 0) { x += spd * dt; facingRight = true;  walkAnim += dt * 7; }
                else            { x -= spd * dt; facingRight = false; walkAnim += dt * 7; }

                // 觸發俯衝：冷卻完畢且X距離夠近
                if (batDiveCd <= 0 && distAbs < BAT_DIVE_X_RANGE) {
                    batDiving      = true;
                    batDiveTargetY = player.getY(); // 鎖定玩家當前高度
                }
            }
            tryAttack(player);
        }
    }

    /** 共用攻擊邏輯：精確矩形碰撞，冰系額外套用緩速效果。 */
    private void tryAttack(Player player) {
        if (attackCooldown > 0) return;
        Rectangle atkBox = new Rectangle((int) x, (int) y, width, height);
        Rectangle plBox  = new Rectangle(
            (int) player.getX(), (int) player.getY(), Player.WIDTH, Player.HEIGHT);
        if (atkBox.intersects(plBox)) {
            player.takeDamage(atk);
            if (type.iceType) player.applySlow(2.5, 0.5);
            attackCooldown = ATTACK_CD;
        }
    }

    // ── 平台碰撞（蝙蝠不用） ─────────────────────────────────
    private void checkPlatformCollision(BaseMap map, double dt) {
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

    // ─────────────────────────────────────────────────────────
    // 受傷 / 嘲諷
    // ─────────────────────────────────────────────────────────
    public void takeDamage(int dmg) {
        hp        -= dmg;
        lastDamage = dmg;
        hurtTimer  = 0.35;
        if (hp <= 0) {
            hp           = 0;
            alive        = false;
            deathTimer   = 1.2;
            respawnTimer = type.respawnTime;
            justDied     = true;
            dropPending  = true;
            SoundManager.get().playSFX(SFX.MONSTER_DEATH);
        }
    }

    public void setTaunted(double duration) {
        taunted    = true;
        tauntTimer = duration;
    }

    // ─────────────────────────────────────────────────────────
    // 繪製
    // ─────────────────────────────────────────────────────────
    public void draw(Graphics2D g, Camera camera) {
        if (!alive) {
            if (deathTimer > 0) drawDeathEffect(g, camera);
            return;
        }

        switch (type) {
            case SLIME      -> drawSlime(g, camera);
            case BOAR       -> drawBoar (g, camera);
            case BAT        -> drawBat  (g, camera);
            case ICE_SLIME  -> drawIceSlime (g, camera);
            case POLAR_BEAR -> drawPolarBear(g, camera);
            case ICE_BAT    -> drawIceBat   (g, camera);
        }

        drawHpBar      (g, camera);
        drawDamageFloat(g, camera);
    }

    // ── 史萊姆 ───────────────────────────────────────────────
    private void drawSlime(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());

        // 移動時壓扁（水平拉伸、垂直縮短）
        double squish  = Math.sin(walkAnim * 2) * 0.12; // -0.12 ~ +0.12
        int drawW = (int)(width  * (1.0 + squish));
        int drawH = (int)(height * (1.0 - squish * 0.7));
        int bx    = sx + (width - drawW) / 2; // 保持中心

        Color bodyColor = hurtTimer > 0 ? new Color(255, 80, 80) : new Color(60, 200, 90);

        // 身體（大橢圓）
        g.setColor(bodyColor);
        g.fillOval(bx, sy + (height - drawH), drawW, drawH);

        // 深色輪廓
        g.setColor(bodyColor.darker());
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(bx, sy + (height - drawH), drawW, drawH);

        // 高光
        g.setColor(new Color(255, 255, 255, 120));
        g.fillOval(bx + drawW / 5, sy + (height - drawH) + 3, drawW / 4, drawH / 5);

        int cx = sx + width / 2;
        int eyeY = sy + (height - drawH) + drawH / 3;

        // ── 眼睛（楓之谷 kawaii 風：大眼白 + 彩色虹膜 + 雙重高光）
        int pupilOff = facingRight ? 2 : -2;
        // 左眼
        g.setColor(Color.WHITE);
        g.fillOval(cx - 10, eyeY, 9, 10);
        g.setColor(new Color(40, 160, 70));          // 綠色虹膜
        g.fillOval(cx - 9,  eyeY + 1, 6,  8);
        g.setColor(new Color(15, 10, 5));            // 瞳孔
        g.fillOval(cx - 8 + pupilOff, eyeY + 2, 4, 6);
        g.setColor(new Color(255, 255, 255, 220));   // 大高光
        g.fillOval(cx - 7 + pupilOff, eyeY + 2, 3, 3);
        g.fillOval(cx - 9 + pupilOff, eyeY + 6, 2, 2); // 小高光
        g.setColor(new Color(20, 10, 5));
        g.setStroke(new BasicStroke(1f));
        g.drawOval(cx - 10, eyeY, 9, 10);           // 眼線

        // 右眼
        g.setColor(Color.WHITE);
        g.fillOval(cx + 1, eyeY, 9, 10);
        g.setColor(new Color(40, 160, 70));
        g.fillOval(cx + 2, eyeY + 1, 6,  8);
        g.setColor(new Color(15, 10, 5));
        g.fillOval(cx + 3 + pupilOff, eyeY + 2, 4, 6);
        g.setColor(new Color(255, 255, 255, 220));
        g.fillOval(cx + 4 + pupilOff, eyeY + 2, 3, 3);
        g.fillOval(cx + 2 + pupilOff, eyeY + 6, 2, 2);
        g.setColor(new Color(20, 10, 5));
        g.drawOval(cx + 1, eyeY, 9, 10);

        // 嘴巴（寬笑，更有表情）
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(30, 15, 5));
        g.drawArc(cx - 6, eyeY + 9, 12, 7, 200, 140);
        g.setStroke(new BasicStroke(1f));
    }

    // ── 野豬 ─────────────────────────────────────────────────
    private void drawBoar(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());
        int cx = sx + width / 2;

        Color bodyColor = hurtTimer > 0 ? new Color(255, 100, 80)
                : boarCharging         ? new Color(210, 80, 60)   // 衝刺時偏紅
                : new Color(145, 90, 50);

        g.setStroke(new BasicStroke(1.5f));
        int dir = facingRight ? 1 : -1;

        // 身體（橢圓）
        g.setColor(bodyColor);
        g.fillOval(sx + 4, sy + 10, width - 8, height - 14);
        g.setColor(bodyColor.darker());
        g.drawOval(sx + 4, sy + 10, width - 8, height - 14);

        // 頭（前端小橢圓）
        int headX = facingRight ? sx + width - 14 : sx;
        g.setColor(bodyColor);
        g.fillOval(headX, sy + 6, 18, 16);
        g.setColor(bodyColor.darker());
        g.drawOval(headX, sy + 6, 18, 16);

        // 耳朵
        int earX = facingRight ? sx + width - 10 : sx + 4;
        g.setColor(new Color(180, 110, 70));
        g.fillOval(earX - dir * 2, sy + 2, 7, 8);
        g.fillOval(earX + dir * 6, sy + 2, 7, 8);

        // 鼻孔（粉紅色）
        int snoutX = facingRight ? sx + width - 5 : sx - 4;
        g.setColor(new Color(220, 150, 130));
        g.fillOval(snoutX, sy + 12, 10, 7);
        g.setColor(new Color(160, 80, 70));
        g.fillOval(snoutX + 1, sy + 13, 3, 3);
        g.fillOval(snoutX + 6, sy + 13, 3, 3);

        // 獠牙（白色短線）
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2f));
        int tuskX = facingRight ? sx + width - 2 : sx + 2;
        g.drawLine(tuskX, sy + 17, tuskX + dir * 5, sy + 22);
        g.drawLine(tuskX, sy + 19, tuskX + dir * 4, sy + 24);

        // 眼睛
        int eyeX = facingRight ? sx + width - 12 : sx + 5;
        g.setColor(Color.WHITE);
        g.fillOval(eyeX, sy + 7, 7, 7);
        g.setColor(new Color(60, 30, 10));
        g.fillOval(eyeX + (facingRight ? 2 : 1), sy + 9, 4, 4);

        // 4 條腿（走路動畫）
        int swing = (int)(Math.sin(walkAnim) * 5);
        g.setColor(new Color(120, 75, 40));
        g.setStroke(new BasicStroke(3f));
        int legY = sy + height - 6;
        // 前腿
        int fl = facingRight ? sx + width - 12 : sx + 4;
        g.drawLine(fl,      legY - 8, fl,           legY + swing);
        g.drawLine(fl + 5,  legY - 8, fl + 5,       legY - swing);
        // 後腿
        int bl = facingRight ? sx + 5 : sx + width - 14;
        g.drawLine(bl,      legY - 8, bl,           legY - swing);
        g.drawLine(bl + 5,  legY - 8, bl + 5,       legY + swing);

        // 捲尾巴
        int tailX = facingRight ? sx + 4 : sx + width - 8;
        g.setColor(bodyColor);
        g.setStroke(new BasicStroke(2f));
        g.drawArc(tailX - 4, sy + 12, 10, 10, 0, 280);

        g.setStroke(new BasicStroke(1f));
    }

    // ── 蝙蝠 ─────────────────────────────────────────────────
    private void drawBat(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());
        int cx = sx + width / 2;
        int cy = sy + height / 2;

        Color bodyColor = hurtTimer > 0 ? new Color(255, 80, 80) : new Color(80, 45, 110);

        // 翅膀搧動角度
        double wingAngle = Math.sin(idleTimer * 8) * 0.4; // 快速拍翅
        int wingSway = (int)(wingAngle * 14);

        // 左翅（三角形）
        int[] lWingX = { cx, cx - 8, cx - width };
        int[] lWingY = { cy, cy - 10 + wingSway, cy - 6 + wingSway * 2 };
        g.setColor(new Color(60, 30, 85, 220));
        g.fillPolygon(lWingX, lWingY, 3);
        g.setColor(new Color(90, 55, 120));
        g.drawPolygon(lWingX, lWingY, 3);

        // 右翅（三角形）
        int[] rWingX = { cx, cx + 8, cx + width };
        int[] rWingY = { cy, cy - 10 + wingSway, cy - 6 + wingSway * 2 };
        g.setColor(new Color(60, 30, 85, 220));
        g.fillPolygon(rWingX, rWingY, 3);
        g.setColor(new Color(90, 55, 120));
        g.drawPolygon(rWingX, rWingY, 3);

        // 身體（深紫橢圓）
        g.setColor(bodyColor);
        g.fillOval(cx - 10, cy - 10, 20, 18);
        g.setColor(bodyColor.darker());
        g.drawOval(cx - 10, cy - 10, 20, 18);

        // 耳朵（尖三角）
        int[] lEarX = { cx - 8, cx - 4, cx - 12 };
        int[] lEarY = { cy - 9, cy - 18, cy - 16 };
        g.setColor(bodyColor);
        g.fillPolygon(lEarX, lEarY, 3);
        int[] rEarX = { cx + 8, cx + 4, cx + 12 };
        int[] rEarY = { cy - 9, cy - 18, cy - 16 };
        g.fillPolygon(rEarX, rEarY, 3);

        // 紅色發光眼睛
        g.setColor(new Color(255, 50, 50, 200));
        g.fillOval(cx - 7, cy - 6, 5, 5);
        g.fillOval(cx + 2, cy - 6, 5, 5);
        // 眼睛光暈
        g.setColor(new Color(255, 100, 100, 80));
        g.fillOval(cx - 9, cy - 8, 9, 9);
        g.fillOval(cx,     cy - 8, 9, 9);

        // 嘴（小牙）
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(cx - 3, cy + 5, cx - 3, cy + 8);
        g.drawLine(cx,     cy + 5, cx,     cy + 8);
        g.drawLine(cx + 3, cy + 5, cx + 3, cy + 8);
        g.setStroke(new BasicStroke(1f));
    }

    // ── HP 條 ─────────────────────────────────────────────────
    private void drawHpBar(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());
        int cx = sx + width / 2;

        int barW = 38, barH = 5;
        int barX = cx - barW / 2;
        int barY = sy - 10;

        g.setColor(new Color(60, 0, 0));
        g.fillRect(barX, barY, barW, barH);
        g.setColor(new Color(220, 50, 50));
        g.fillRect(barX, barY, (int)(barW * (double) hp / maxHp), barH);
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barW, barH);

        // 嘲諷標記（橘色驚嘆號）
        if (taunted) {
            g.setColor(new Color(255, 160, 30));
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("!", cx - 3, barY - 2);
        }
    }

    // ── 傷害飄字 ─────────────────────────────────────────────
    private void drawDamageFloat(Graphics2D g, Camera camera) {
        if (hurtTimer <= 0) return;
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());
        int cx = sx + width / 2;

        float alpha = (float)(hurtTimer / 0.35);
        g.setColor(new Color(1f, 1f, 0f, alpha));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("-" + lastDamage, cx - 10, sy - 14);
    }

    // ── 冰晶史萊姆 ───────────────────────────────────────────
    private void drawIceSlime(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());
        double squish = Math.sin(walkAnim * 2) * 0.1;
        int drawW = (int)(width  * (1.0 + squish));
        int drawH = (int)(height * (1.0 - squish * 0.6));
        int bx    = sx + (width - drawW) / 2;
        int bodyTop = sy + (height - drawH);

        Color bodyColor = hurtTimer > 0 ? new Color(255, 120, 120)
                        : new Color(140, 210, 255); // 冰藍色

        // 身體
        g.setColor(new Color(bodyColor.getRed(), bodyColor.getGreen(),
                             bodyColor.getBlue(), 200));
        g.fillOval(bx, bodyTop, drawW, drawH);
        g.setColor(bodyColor);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(bx, bodyTop, drawW, drawH);

        // 冰晶高光
        g.setColor(new Color(220, 240, 255, 180));
        g.fillOval(bx + drawW/5, bodyTop + 3, drawW/3, drawH/4);

        int cx = sx + width / 2;
        int eyeY = bodyTop + drawH / 3;

        // 眼睛（冰藍 kawaii 風：藍色虹膜 + 冰晶高光）
        int icePupilOff = facingRight ? 2 : -2;
        // 左眼
        g.setColor(new Color(220, 240, 255));
        g.fillOval(cx - 10, eyeY, 9, 10);
        g.setColor(new Color(80, 130, 220));         // 冰藍虹膜
        g.fillOval(cx - 9,  eyeY + 1, 6, 8);
        g.setColor(new Color(40, 30, 120));          // 深藍瞳孔
        g.fillOval(cx - 8 + icePupilOff, eyeY + 2, 4, 6);
        g.setColor(new Color(255, 255, 255, 230));   // 大高光
        g.fillOval(cx - 7 + icePupilOff, eyeY + 2, 3, 3);
        g.fillOval(cx - 9 + icePupilOff, eyeY + 6, 2, 2);
        g.setColor(new Color(100, 160, 255, 150));   // 冰藍眼線
        g.setStroke(new BasicStroke(1f));
        g.drawOval(cx - 10, eyeY, 9, 10);
        // 右眼
        g.setColor(new Color(220, 240, 255));
        g.fillOval(cx + 1, eyeY, 9, 10);
        g.setColor(new Color(80, 130, 220));
        g.fillOval(cx + 2, eyeY + 1, 6, 8);
        g.setColor(new Color(40, 30, 120));
        g.fillOval(cx + 3 + icePupilOff, eyeY + 2, 4, 6);
        g.setColor(new Color(255, 255, 255, 230));
        g.fillOval(cx + 4 + icePupilOff, eyeY + 2, 3, 3);
        g.fillOval(cx + 2 + icePupilOff, eyeY + 6, 2, 2);
        g.setColor(new Color(100, 160, 255, 150));
        g.drawOval(cx + 1, eyeY, 9, 10);

        // 冰刺（頂部小三角）
        g.setColor(new Color(180, 230, 255));
        int[] iceX = {cx - 4, cx, cx + 4};
        int[] iceY = {bodyTop, bodyTop - 8, bodyTop};
        g.fillPolygon(iceX, iceY, 3);
        int[] iceX2 = {cx + 6, cx + 10, cx + 14};
        int[] iceY2 = {bodyTop + 2, bodyTop - 5, bodyTop + 2};
        g.fillPolygon(iceX2, iceY2, 3);
        g.setStroke(new BasicStroke(1f));
    }

    // ── 極地熊 ───────────────────────────────────────────────
    private void drawPolarBear(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());
        int cx = sx + width / 2;
        int dir = facingRight ? 1 : -1;

        Color bodyColor = hurtTimer > 0 ? new Color(255, 150, 150)
                        : new Color(235, 240, 245); // 白/淺灰

        g.setStroke(new BasicStroke(1.5f));

        // 身體（大橢圓）
        g.setColor(bodyColor);
        g.fillOval(sx + 2, sy + 12, width - 4, height - 16);
        g.setColor(new Color(200, 210, 220));
        g.drawOval(sx + 2, sy + 12, width - 4, height - 16);

        // 頭（前端圓）
        int headX = facingRight ? sx + width - 16 : sx - 2;
        g.setColor(bodyColor);
        g.fillOval(headX, sy + 4, 22, 22);
        g.setColor(new Color(200, 210, 220));
        g.drawOval(headX, sy + 4, 22, 22);

        // 耳朵（半圓）
        int earBase = facingRight ? headX + 14 : headX + 2;
        g.setColor(bodyColor);
        g.fillOval(earBase,     sy,     10, 10);
        g.fillOval(earBase - 8, sy + 1, 10, 10);
        g.setColor(new Color(220, 170, 160));
        g.fillOval(earBase + 2, sy + 2, 6, 6);

        // 眼睛（小黑點）
        int eyeX = facingRight ? headX + 14 : headX + 3;
        g.setColor(Color.BLACK);
        g.fillOval(eyeX, sy + 9, 5, 5);
        // 眼睛高光
        g.setColor(Color.WHITE);
        g.fillOval(eyeX + 1, sy + 9, 2, 2);

        // 鼻子
        int noseX = facingRight ? headX + 18 : headX - 2;
        g.setColor(new Color(60, 40, 30));
        g.fillOval(noseX, sy + 15, 6, 5);

        // 4 條腿
        int swing = (int)(Math.sin(walkAnim) * 6);
        g.setColor(bodyColor);
        g.setStroke(new BasicStroke(5f));
        int legY = sy + height - 4;
        int fl = facingRight ? sx + width - 14 : sx + 4;
        int bl = facingRight ? sx + 6          : sx + width - 18;
        g.drawLine(fl,     legY - 12, fl,     legY + swing);
        g.drawLine(fl + 7, legY - 10, fl + 7, legY - swing);
        g.drawLine(bl,     legY - 12, bl,     legY - swing);
        g.drawLine(bl + 7, legY - 10, bl + 7, legY + swing);

        // 尾巴（小圓）
        int tailX = facingRight ? sx + 3 : sx + width - 10;
        g.setColor(bodyColor);
        g.setStroke(new BasicStroke(1f));
        g.fillOval(tailX, sy + 20, 10, 10);
        g.setColor(new Color(200, 210, 220));
        g.drawOval(tailX, sy + 20, 10, 10);

        // 揮擊預備動畫（爪子發光 + 震動提示）
        if (bearWindupActive && bearWindupTimer > 0) {
            float pulse = (float)(0.5 + 0.5 * Math.sin(idleTimer * 30));
            int glow    = (int)(150 + 105 * pulse);
            g.setColor(new Color(255, 120, 40, glow));
            g.setStroke(new BasicStroke(3f));
            int pawX = facingRight ? sx + width + 2 : sx - 8;
            g.drawLine(pawX, sy + 14, pawX + dir * 14, sy + 10);
            g.drawLine(pawX, sy + 19, pawX + dir * 16, sy + 18);
            g.drawLine(pawX, sy + 24, pawX + dir * 14, sy + 28);
            g.setColor(new Color(255, 200, 80, (int)(glow * 0.5)));
            g.fillOval(pawX + dir * 6 - 6, sy + 8, 12, 12);
        }
        // 冰爪命中效果
        if (hurtTimer > 0.25f) {
            g.setColor(new Color(180, 230, 255, 150));
            g.setStroke(new BasicStroke(2f));
            int pawX = facingRight ? sx + width + 2 : sx - 8;
            g.drawLine(pawX, sy + 18, pawX + dir * 10, sy + 14);
            g.drawLine(pawX, sy + 22, pawX + dir * 12, sy + 20);
            g.drawLine(pawX, sy + 26, pawX + dir * 10, sy + 28);
        }
        g.setStroke(new BasicStroke(1f));
    }

    // ── 冰蝠 ─────────────────────────────────────────────────
    private void drawIceBat(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX());
        int sy = (int)(y - camera.getOffsetY());
        int cx = sx + width / 2;
        int cy = sy + height / 2;

        Color bodyColor = hurtTimer > 0 ? new Color(255, 130, 130)
                        : new Color(150, 200, 240); // 冰藍

        // 翅膀搧動
        double wingAngle = Math.sin(idleTimer * 9) * 0.45;
        int wingSway = (int)(wingAngle * 14);

        // 翅膀（冰藍色半透明）
        int[] lWX = {cx, cx - 8, cx - width};
        int[] lWY = {cy, cy - 10 + wingSway, cy - 5 + wingSway * 2};
        g.setColor(new Color(140, 200, 255, 190));
        g.fillPolygon(lWX, lWY, 3);
        g.setColor(new Color(180, 220, 255));
        g.drawPolygon(lWX, lWY, 3);

        int[] rWX = {cx, cx + 8, cx + width};
        int[] rWY = {cy, cy - 10 + wingSway, cy - 5 + wingSway * 2};
        g.setColor(new Color(140, 200, 255, 190));
        g.fillPolygon(rWX, rWY, 3);
        g.setColor(new Color(180, 220, 255));
        g.drawPolygon(rWX, rWY, 3);

        // 身體
        g.setColor(bodyColor);
        g.fillOval(cx - 10, cy - 10, 20, 18);
        g.setColor(new Color(100, 170, 220));
        g.drawOval(cx - 10, cy - 10, 20, 18);

        // 耳朵（冰刺狀尖耳）
        int[] lEarX = {cx - 8, cx - 4, cx - 14};
        int[] lEarY = {cy - 9, cy - 20, cy - 18};
        g.setColor(bodyColor);
        g.fillPolygon(lEarX, lEarY, 3);
        int[] rEarX = {cx + 8, cx + 4, cx + 14};
        int[] rEarY = {cy - 9, cy - 20, cy - 18};
        g.fillPolygon(rEarX, rEarY, 3);

        // 冰晶眼睛（白藍色）
        g.setColor(new Color(200, 240, 255));
        g.fillOval(cx - 7, cy - 6, 5, 5);
        g.fillOval(cx + 2, cy - 6, 5, 5);
        g.setColor(new Color(50, 120, 200));
        g.fillOval(cx - 6, cy - 5, 3, 3);
        g.fillOval(cx + 3, cy - 5, 3, 3);

        // 冰霧噴吐效果（間歇性）
        if ((int)(idleTimer * 2) % 3 == 0) {
            g.setColor(new Color(200, 235, 255, 90));
            g.fillOval(cx - 8, cy + 5, 16, 8);
        }
        g.setStroke(new BasicStroke(1f));
    }

    // ── 死亡特效 ─────────────────────────────────────────────
    private void drawDeathEffect(Graphics2D g, Camera camera) {
        int sx = (int)(x - camera.getOffsetX()) + width  / 2;
        int sy = (int)(y - camera.getOffsetY()) + height / 2;

        float progress = (float)(1.0 - deathTimer / 1.2);
        int   radius   = (int)(32 * progress);
        float alpha    = 1.0f - progress;

        // 種類特色顏色
        Color c = switch (type) {
            case SLIME      -> new Color(0.3f, 1f,   0.4f, alpha);
            case BOAR       -> new Color(0.9f, 0.6f, 0.2f, alpha);
            case BAT        -> new Color(0.7f, 0.3f, 1.0f, alpha);
            case ICE_SLIME  -> new Color(0.6f, 0.9f, 1.0f, alpha);
            case POLAR_BEAR -> new Color(0.9f, 0.95f,1.0f, alpha);
            case ICE_BAT    -> new Color(0.5f, 0.85f,1.0f, alpha);
        };
        g.setColor(c);
        g.setStroke(new BasicStroke(3f));
        g.drawOval(sx - radius, sy - radius, radius * 2, radius * 2);
        g.setStroke(new BasicStroke(1f));
    }

    // ── 重生 ─────────────────────────────────────────────────
    private void doRespawn() {
        x            = spawnX;
        y            = spawnY;
        hp           = maxHp;
        alive        = true;
        velY         = 0;
        hurtTimer    = 0;
        attackCooldown  = 0;
        boarCharging    = false;
        boarChargeTimer = 0;
        boarChargeCd    = 0;
        boarStunTimer   = 0;
        batDiving       = false;
        batReturning    = false;
        batDiveCd       = 0;
        bearWindupTimer  = 0;
        bearWindupActive = false;
        taunted      = false;
        tauntTimer   = 0;
        justDied     = false;
        dropPending  = false;
    }

    /**
     * 強制立即重生（地圖重新進入時使用）。
     * 若怪物已存活，只重置狀態；若已死亡，立刻復活。
     */
    public void forceRespawn() {
        respawnTimer = 0;
        deathTimer   = 0;
        doRespawn();
    }

    // ── 掉落物生成 ───────────────────────────────────────────

    /**
     * 依怪物種類隨機生成掉落物清單。
     * 每次死亡呼叫一次；GamePanel 透過 pollDropPending() 確認後呼叫此方法。
     */
    public List<DropItem> rollDrops() {
        List<DropItem> list = new ArrayList<>();
        double cx = x + width / 2.0;
        double cy = y;

        // 金幣：固定掉落（exp 的 50%~150%）
        int gold = (int)(type.expReward * (0.5 + Math.random()));
        list.add(DropItem.gold(cx, cy, gold));

        // 消耗品掉落機率（依種類）
        double hpPot = 0, mpPot = 0;
        switch (type) {
            case SLIME      -> { hpPot = 0.35; mpPot = 0.15; }
            case BOAR       -> { hpPot = 0.45; mpPot = 0.10; }
            case BAT        -> { hpPot = 0.20; mpPot = 0.30; }
            case ICE_SLIME  -> { hpPot = 0.30; mpPot = 0.25; }
            case POLAR_BEAR -> { hpPot = 0.50; mpPot = 0.15; }
            case ICE_BAT    -> { hpPot = 0.20; mpPot = 0.35; }
        }
        if (Math.random() < hpPot) {
            Consumable c = Math.random() < 0.25
                    ? Consumable.orangePotion()
                    : Consumable.redPotion();
            list.add(DropItem.consumable(cx + (Math.random() - 0.5) * 20, cy, c));
        }
        if (Math.random() < mpPot) {
            Consumable c = Math.random() < 0.2
                    ? Consumable.manaElixir()
                    : Consumable.bluePotion();
            list.add(DropItem.consumable(cx + (Math.random() - 0.5) * 20, cy, c));
        }

        // 裝備掉落（低機率，依種類決定種類）
        double eqRate = switch (type) {
            case SLIME, BAT   -> 0.05;
            case BOAR         -> 0.08;
            case ICE_SLIME, ICE_BAT -> 0.06;
            case POLAR_BEAR   -> 0.12;
        };
        if (Math.random() < eqRate) {
            Equipment eq = rollEquipment();
            if (eq != null) list.add(DropItem.equipment(cx, cy, eq));
        }

        return list;
    }

    private Equipment rollEquipment() {
        double r = Math.random();
        return switch (type) {
            case SLIME, ICE_SLIME -> r < 0.5 ? Equipment.noviceHelmet()   : Equipment.hempBoots();
            case BOAR             -> r < 0.5 ? Equipment.leatherTop()      : Equipment.noviceHelmet();
            case BAT, ICE_BAT     -> r < 0.5 ? Equipment.magicEarring()    : Equipment.hempGloves();
            case POLAR_BEAR       -> r < 0.4 ? Equipment.reinforcedSword() :
                                     r < 0.7 ? Equipment.steelCape()       : Equipment.leatherTop();
        };
    }

    // ── Getter / Setter ──────────────────────────────────────
    /**
     * 讀取並清除「剛死亡」旗標（每死只觸發一次，GamePanel 用來給玩家 EXP）。
     */
    public boolean pollJustDied() {
        if (justDied) { justDied = false; return true; }
        return false;
    }

    /** 讀取並清除「待掉落」旗標（每死只觸發一次，GamePanel 呼叫 rollDrops()）。 */
    public boolean pollDropPending() {
        if (dropPending) { dropPending = false; return true; }
        return false;
    }
    public int     getExpReward()  { return type.expReward; }
    public boolean isAlive()         { return alive; }
    public boolean   isHitThisAttack() { return hitThisAttack; }
    public void      setHitThisAttack(boolean v) { hitThisAttack = v; }
    public double    getX()            { return x; }
    public double    getY()            { return y; }
    public int       getWidth()        { return width; }
    public int       getHeight()       { return height; }
    public MonsterType getType()       { return type; }

    public Rectangle getBoundingBox() {
        return new Rectangle((int) x, (int) y, width, height);
    }
}
