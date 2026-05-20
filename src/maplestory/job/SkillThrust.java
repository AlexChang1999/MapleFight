package maplestory.job;

import maplestory.core.Camera;
import maplestory.entity.Monster;
import maplestory.entity.Player;

import java.awt.*;
import java.util.List;

/**
 * 技能一：突刺
 * 用劍向前突刺，對正前方最近的一個目標造成高額傷害。
 * 消耗 15 MP，冷卻 3 秒。
 * 傷害 = 80 + STR × 5
 */
public class SkillThrust extends Skill {

    // 突刺偵測距離（只攻擊正前方 120px 內）
    private static final int THRUST_RANGE = 120;
    private static final double EFFECT_DURATION = 0.3;

    // 特效記錄位置（施放時快照）
    private double effectX;
    private double effectY;
    private boolean thrustFacingRight;

    // ─────────────────────────────────────────────────────────
    public SkillThrust() {
        super("突刺", 15, 3.0);
    }

    @Override
    public void activate(Player player, List<Monster> monsters) {
        boolean fr   = player.isFacingRight();
        int     dmg  = 80 + player.getStr() * 5;

        // 找正前方最近怪物
        Monster target  = null;
        double  minDist = THRUST_RANGE;

        for (Monster m : monsters) {
            if (!m.isAlive()) continue;
            double dx = m.getX() - player.getX();
            // 必須在面對的方向
            if ( fr && dx < 0) continue;
            if (!fr && dx > 0) continue;
            double dist = Math.abs(dx);
            if (dist < minDist) { minDist = dist; target = m; }
        }

        if (target != null) target.takeDamage(dmg);

        // 記錄特效起點（在玩家手前方）
        int handOffX = fr ? Player.WIDTH + 8 : -48;
        effectX          = player.getX() + handOffX;
        effectY          = player.getY() + Player.HEIGHT * 0.38;
        thrustFacingRight = fr;
        effectActive      = true;
        effectTimer       = EFFECT_DURATION;
    }

    @Override
    public void drawEffect(Graphics2D g, Camera camera) {
        int sx = (int)(effectX - camera.getOffsetX());
        int sy = (int)(effectY - camera.getOffsetY());

        // 進度 0=剛施放, 1=結束
        float prog  = 1.0f - (float)(effectTimer / EFFECT_DURATION);
        float alpha = Math.max(0f, 1.0f - prog * 1.2f);

        int dir     = thrustFacingRight ? 1 : -1;
        int lineLen = (int)(70 * (1.0 - prog * 0.25));

        // 主要劍光（亮黃線）
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(1f, 0.95f, 0.3f, alpha));
        g.drawLine(sx, sy, sx + dir * lineLen, sy);

        // 劍光光暈（寬一些的半透明線）
        g.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(1f, 0.85f, 0.1f, alpha * 0.35f));
        g.drawLine(sx, sy, sx + dir * lineLen, sy);

        // 前端爆光點
        g.setColor(new Color(1f, 1f, 0.9f, alpha));
        g.fillOval(sx + dir * (lineLen - 7) - 6, sy - 6, 12, 12);

        // 短橫劍光（十字形點綴）
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(1f, 0.9f, 0.4f, alpha * 0.6f));
        int tip = sx + dir * lineLen;
        g.drawLine(tip, sy - 12, tip, sy + 12);

        g.setStroke(new BasicStroke(1f));
    }
}
