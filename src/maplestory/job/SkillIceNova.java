package maplestory.job;

import maplestory.entity.Monster;
import maplestory.entity.Player;

import java.awt.*;
import java.util.List;

/**
 * 法師技能：冰凍術（W 鍵）
 * 以玩家為中心，範圍爆炸冰凍周圍怪物。
 * MP 消耗：30  冷卻：5.0 秒  範圍：100px
 */
public class SkillIceNova extends Skill {

    private static final int    RANGE  = 100;
    private static final double EFFECT_DUR = 0.55;

    private boolean effectActive = false;
    private double  effectTimer  = 0;
    private double  epicenterX, epicenterY;

    public SkillIceNova() {
        super("冰凍術", 30, 5.0);
    }

    @Override
    public void activate(Player player, List<Monster> monsters) {
        epicenterX   = player.getX() + 12;
        epicenterY   = player.getY() + 30;
        effectActive = true;
        effectTimer  = 0;

        int dmg = player.getIntel() * 4 + 30;
        for (Monster m : monsters) {
            if (!m.isAlive()) continue;
            double dx = m.getX() - epicenterX;
            double dy = m.getY() - epicenterY;
            if (Math.sqrt(dx*dx + dy*dy) <= RANGE) {
                m.takeDamage(dmg);
            }
        }
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if (effectActive) {
            effectTimer += dt;
            if (effectTimer >= EFFECT_DUR) effectActive = false;
        }
    }

    @Override
    public void drawEffect(Graphics2D g, maplestory.core.Camera cam) {
        if (!effectActive) return;
        int sx = (int)(epicenterX - cam.getOffsetX());
        int sy = (int)(epicenterY - cam.getOffsetY());
        double t = effectTimer / EFFECT_DUR; // 0→1

        int r = (int)(RANGE * t);
        float alpha = (float)(1.0 - t);

        // 冰環擴散
        g.setColor(new Color(100, 200, 255, (int)(alpha * 150)));
        g.setStroke(new BasicStroke(4f));
        g.drawOval(sx - r, sy - r, r * 2, r * 2);

        // 冰花核心
        g.setColor(new Color(180, 230, 255, (int)(alpha * 200)));
        g.fillOval(sx - 18, sy - 18, 36, 36);
        g.setColor(new Color(220, 245, 255, (int)(alpha * 220)));
        g.fillOval(sx - 10, sy - 10, 20, 20);

        // 冰晶輻射線
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(150, 220, 255, (int)(alpha * 180)));
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            int x1 = sx + (int)(Math.cos(angle) * 12);
            int y1 = sy + (int)(Math.sin(angle) * 12);
            int x2 = sx + (int)(Math.cos(angle) * r);
            int y2 = sy + (int)(Math.sin(angle) * r);
            g.drawLine(x1, y1, x2, y2);
        }
        g.setStroke(new BasicStroke(1f));
    }

    @Override public boolean isEffectActive() { return effectActive; }
}
