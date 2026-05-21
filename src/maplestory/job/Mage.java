package maplestory.job;

import maplestory.entity.Player;

/**
 * 法師職業。
 * 主動技能：火球術（Q）、冰凍術（W）
 * 被動效果：每 3 秒自動回復最大 MP 的 4%
 */
public class Mage extends Job {

    private double mpRegenAccum = 0;

    public Mage() {
        skills.add(new SkillFireball());   // index 0 → Q
        skills.add(new SkillIceNova());    // index 1 → W
    }

    @Override public String getDisplayName() { return "法師"; }
    @Override public String getJobId()       { return "mage"; }

    /** 被動：每秒自動回復 4% 最大 MP（不論戰鬥狀態） */
    @Override
    protected void updatePassive(Player player, double dt) {
        boolean notFullMp = player.getMp() < player.getMaxMp();
        if (notFullMp) {
            mpRegenAccum += player.getMaxMp() * 0.04 * dt;
            int heal = (int) mpRegenAccum;
            if (heal >= 1) {
                player.healMp(heal);
                mpRegenAccum -= heal;
            }
        } else {
            mpRegenAccum = 0;
        }
    }
}
