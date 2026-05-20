package maplestory.job;

import maplestory.core.Camera;
import maplestory.entity.Monster;
import maplestory.entity.Player;

import java.awt.*;
import java.util.List;

/**
 * 技能二：衝擊波
 * 用劍擊地，在腳下釋放橢圓形衝擊波。
 * 最多命中 5 個目標，造成微量傷害，並嘲諷命中怪物 3 秒。
 * 消耗 20 MP，冷卻 8 秒。
 * 傷害 = 15 + STR
 */
public class SkillShockwave extends Skill {

    // 橢圓 AoE 半徑
    private static final int    AOE_RADIUS_X   = 150;
    private static final int    AOE_RADIUS_Y   = 60;
    private static final int    MAX_TARGETS    = 5;
    private static final double TAUNT_DURATION = 3.0;
    private static final double EFFECT_DURATION = 0.6;

    // 特效中心（施放時快照）
    private double effectX;
    private double effectY;

    // ─────────────────────────────────────────────────────────
    public SkillShockwave() {
        super("衝擊波", 20, 8.0);
    }

    @Override
    public void activate(Player player, List<Monster> monsters) {
        int    dmg     = 15 + player.getStr();
        double centerX = player.getX() + Player.WIDTH  / 2.0;
        double centerY = player.getY() + Player.HEIGHT;        // 腳底

        // 橢圓範圍內的怪物，最多 MAX_TARGETS 隻
        int hits = 0;
        for (Monster m : monsters) {
            if (!m.isAlive() || hits >= MAX_TARGETS) continue;
            double dx = (m.getX() + m.getWidth()  / 2.0 - centerX) / AOE_RADIUS_X;
            double dy = (m.getY() + m.getHeight() / 2.0 - centerY) / AOE_RADIUS_Y;
            if (dx * dx + dy * dy <= 1.0) {
                m.takeDamage(dmg);
                m.setTaunted(TAUNT_DURATION);
                hits++;
            }
        }

        effectX      = centerX;
        effectY      = centerY;
        effectActive = true;
        effectTimer  = EFFECT_DURATION;
    }

    @Override
    public void drawEffect(Graphics2D g, Camera camera) {
        int sx = (int)(effectX - camera.getOffsetX());
        int sy = (int)(effectY - camera.getOffsetY());

        // 進度 0=剛施放, 1=結束
        float prog  = 1.0f - (float)(effectTimer / EFFECT_DURATION);
        float alpha = Math.max(0f, 1.0f - prog * 0.9f);

        // 外擴橢圓（從小擴大）
        int rX = (int)(AOE_RADIUS_X * (0.2 + 0.8 * prog));
        int rY = (int)(AOE_RADIUS_Y * (0.2 + 0.8 * prog));

        // 填色（半透明紫色）
        g.setColor(new Color(0.65f, 0.35f, 1.0f, alpha * 0.25f));
        g.fillOval(sx - rX, sy - rY, rX * 2, rY * 2);

        // 橢圓邊框（亮紫）
        g.setStroke(new BasicStroke(3.5f));
        g.setColor(new Color(0.8f, 0.6f, 1.0f, alpha));
        g.drawOval(sx - rX, sy - rY, rX * 2, rY * 2);

        // 內圈（亮白，快速收縮）
        int rX2 = (int)(AOE_RADIUS_X * 0.3f * (1.0 - prog));
        int rY2 = (int)(AOE_RADIUS_Y * 0.3f * (1.0 - prog));
        if (rX2 > 2 && rY2 > 2) {
            g.setColor(new Color(1f, 1f, 1f, alpha * 0.8f));
            g.setStroke(new BasicStroke(2f));
            g.drawOval(sx - rX2, sy - rY2, rX2 * 2, rY2 * 2);
        }

        // 地面撞擊火花（施放初期）
        if (prog < 0.3f) {
            float sparkAlpha = 1.0f - prog / 0.3f;
            g.setColor(new Color(1f, 0.9f, 0.4f, sparkAlpha));
            g.fillOval(sx - 8, sy - 8, 16, 16);
        }

        g.setStroke(new BasicStroke(1f));
    }
}
