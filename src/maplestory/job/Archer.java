package maplestory.job;

import maplestory.entity.Player;

/**
 * 弓箭手職業。
 * 主動技能：箭雨（Q）、鷹眼（W）
 * 被動效果：10% 機率閃避攻擊（在 Player.takeDamage 前判定）
 *           + 鷹眼 buff 期間攻擊傷害 ×1.5
 */
public class Archer extends Job {

    @Override public String getDisplayName() { return "弓箭手"; }
    @Override public String getJobId()       { return "archer"; }

    public Archer() {
        skills.add(new SkillArrowRain()); // index 0 → Q
        skills.add(new SkillHawkEye());   // index 1 → W
    }

    /**
     * 被動閃避：10% 機率。
     * 在 Player.takeDamage() 中呼叫 archer.tryDodge() 決定是否免傷。
     */
    public boolean tryDodge() {
        return Math.random() < 0.10;
    }

    /** 鷹眼 buff 期間的傷害倍率（由 Player.checkAttackHits 乘算） */
    public double getDamageMultiplier() {
        SkillHawkEye hawk = getHawkEye();
        return hawk != null ? hawk.getBuffRatio() : 1.0;
    }

    public SkillHawkEye getHawkEye() {
        for (Skill s : skills) {
            if (s instanceof SkillHawkEye h) return h;
        }
        return null;
    }

    @Override
    protected void updatePassive(Player player, double dt) {
        // 鷹眼計時由 SkillHawkEye.update() 自行遞減，無需此處處理
    }
}
