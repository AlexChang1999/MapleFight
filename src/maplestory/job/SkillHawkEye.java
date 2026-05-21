package maplestory.job;

import maplestory.entity.Monster;
import maplestory.entity.Player;

import java.awt.*;
import java.util.List;

/**
 * 弓箭手技能：鷹眼（W 鍵）
 * 暫時提升暴擊率，持續 5 秒，期間攻擊傷害提升 × 1.5。
 * MP 消耗：20  冷卻：12 秒
 */
public class SkillHawkEye extends Skill {

    private static final double BUFF_DUR = 5.0;
    private double buffTimer = 0;

    public SkillHawkEye() {
        super("鷹眼", 20, 12.0);
    }

    @Override
    public void activate(Player player, List<Monster> monsters) {
        buffTimer = BUFF_DUR;
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if (buffTimer > 0) buffTimer -= dt;
    }

    public boolean isBuffActive() { return buffTimer > 0; }
    public double  getBuffRatio() { return isBuffActive() ? 1.5 : 1.0; }
    public double  getBuffTimer() { return buffTimer; }

    @Override
    public void drawEffect(Graphics2D g, maplestory.core.Camera cam) {
        // 鷹眼 buff 顯示在 HUD 由 GamePanel 處理；特效輕微
    }

    @Override public boolean isEffectActive() { return buffTimer > 0; }
}
