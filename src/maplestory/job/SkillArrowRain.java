package maplestory.job;

import maplestory.entity.Monster;
import maplestory.entity.Player;

import java.awt.*;
import java.util.List;

/**
 * 弓箭手技能：箭雨（Q 鍵）
 * 在玩家前方落下多支箭，覆蓋 180px 範圍。
 * MP 消耗：25  冷卻：4.0 秒  箭數：6 支
 */
public class SkillArrowRain extends Skill {

    private static final int    ARROW_COUNT = 6;
    private static final int    RANGE_W     = 180;
    private static final double EFFECT_DUR  = 0.7;

    private boolean effectActive = false;
    private double  effectTimer  = 0;
    private double  baseX, baseY;
    private int     faceDir;

    // 箭的 X 位置（隨機分佈在範圍內）
    private final int[] arrowX = new int[ARROW_COUNT];

    public SkillArrowRain() {
        super("箭雨", 25, 4.0);
    }

    @Override
    public void activate(Player player, List<Monster> monsters) {
        effectActive = true;
        effectTimer  = 0;
        baseX        = player.getX() + 12;
        baseY        = player.getY();
        faceDir      = player.isFacingRight() ? 1 : -1;

        // 決定每支箭的落點（前方 20~200px）
        for (int i = 0; i < ARROW_COUNT; i++) {
            arrowX[i] = (int)(baseX + faceDir * (20 + i * (RANGE_W / ARROW_COUNT)));
        }

        // 立即判傷
        int dmg = player.getDex() * 4 + 35;
        for (Monster m : monsters) {
            if (!m.isAlive()) continue;
            double mx = m.getX() + 20;
            double minX = baseX + faceDir * 10;
            double maxX = baseX + faceDir * (RANGE_W + 10);
            if (faceDir > 0 ? (mx >= minX && mx <= maxX)
                            : (mx <= minX && mx >= maxX)) {
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
        double t = effectTimer / EFFECT_DUR;

        for (int i = 0; i < ARROW_COUNT; i++) {
            int sx = (int)(arrowX[i] - cam.getOffsetX());
            // 箭頭從畫面上方 80px 落到地面
            double delay = i * 0.08;
            double phase = Math.max(0, effectTimer - delay) / (EFFECT_DUR * 0.8);
            if (phase <= 0) continue;
            int topY  = (int)(baseY - cam.getOffsetY()) - 80;
            int botY  = (int)(baseY - cam.getOffsetY() + 58);
            int arrowY = topY + (int)((botY - topY) * Math.min(1.0, phase));

            float alpha = (float)(1.0 - Math.max(0, t - 0.7) * 3);
            if (alpha < 0) alpha = 0;

            // 箭身
            g.setColor(new Color(139, 90, 43, (int)(alpha * 220)));
            g.setStroke(new BasicStroke(2f));
            g.drawLine(sx, arrowY - 20, sx, arrowY);
            // 箭頭（三角）
            g.setColor(new Color(180, 180, 190, (int)(alpha * 240)));
            int[] px = {sx, sx - 4, sx + 4};
            int[] py = {arrowY, arrowY - 8, arrowY - 8};
            g.fillPolygon(px, py, 3);
            // 箭尾羽毛
            g.setColor(new Color(200, 220, 255, (int)(alpha * 160)));
            g.drawLine(sx - 3, arrowY - 20, sx, arrowY - 14);
            g.drawLine(sx + 3, arrowY - 20, sx, arrowY - 14);
            g.setStroke(new BasicStroke(1f));
        }
    }

    @Override public boolean isEffectActive() { return effectActive; }
}
