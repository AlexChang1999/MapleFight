package maplestory.job;

import maplestory.entity.Monster;
import maplestory.entity.Player;

import java.awt.*;
import java.util.List;

/**
 * 法師技能：火球術（Q 鍵）
 * 向前方發射火球，命中第一隻怪物造成傷害 + 燃燒（持續傷害）。
 * MP 消耗：20  冷卻：2.5 秒
 */
public class SkillFireball extends Skill {

    // 飛行火球狀態
    private boolean flying     = false;
    private double  ballX, ballY;
    private double  velX;
    private int     direction;
    private double  flyTimer   = 0;
    private static final double FLY_SPEED  = 380;
    private static final double FLY_MAX    = 1.8; // 最長飛行時間（秒）

    public SkillFireball() {
        super("火球術", 20, 2.5);
    }

    @Override
    public void activate(Player player, List<Monster> monsters) {
        flying    = true;
        ballX     = player.getX() + (player.isFacingRight() ? 24 : -12);
        ballY     = player.getY() + 18;
        direction = player.isFacingRight() ? 1 : -1;
        velX      = direction * FLY_SPEED;
        flyTimer  = 0;
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if (!flying) return;

        flyTimer += dt;
        ballX    += velX * dt;

        if (flyTimer >= FLY_MAX) { flying = false; return; }

        // 動態需要怪物列表 — 透過 GamePanel 的 checkAttackHits 代替
        // 此處簡化：flyTimer 超過就消失（真正碰撞由 GamePanel 外部驅動）
    }

    /** GamePanel 每幀呼叫取得火球位置以做碰撞判定 */
    public boolean isFlying()  { return flying; }
    public double  getBallX()  { return ballX;  }
    public double  getBallY()  { return ballY;  }
    public void    stop()      { flying = false; }

    /** 火球命中後傷害 = 玩家 INT×5 + 基礎 45 */
    public static int calcDamage(Player p) { return p.getIntel() * 5 + 45; }

    @Override
    public void drawEffect(java.awt.Graphics2D g, maplestory.core.Camera cam) {
        if (!flying) return;
        int sx = (int)(ballX - cam.getOffsetX());
        int sy = (int)(ballY - cam.getOffsetY());

        // 外焰（橙色光暈）
        g.setColor(new Color(255, 120, 20, 100));
        g.fillOval(sx - 12, sy - 12, 24, 24);

        // 火球核心
        g.setColor(new Color(255, 200, 60));
        g.fillOval(sx - 7, sy - 7, 14, 14);

        // 火芯（白）
        g.setColor(new Color(255, 255, 200));
        g.fillOval(sx - 3, sy - 3, 6, 6);

        // 尾焰粒子
        double elapsed = flyTimer;
        for (int i = 0; i < 4; i++) {
            int px = sx - direction * (8 + i * 6);
            int py = sy + (int)(Math.sin(elapsed * 12 + i) * 3);
            int r  = 5 - i;
            g.setColor(new Color(255, 80 + i * 30, 0, 180 - i * 40));
            g.fillOval(px - r, py - r, r * 2, r * 2);
        }
    }

    @Override public boolean isEffectActive() { return flying; }
}
