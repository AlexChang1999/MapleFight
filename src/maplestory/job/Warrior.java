package maplestory.job;

import maplestory.entity.Player;

/**
 * 劍士職業。
 * 主動技能：突刺（Q）、衝擊波（W）
 * 被動效果：脫戰 5 秒後，每秒回復最大 HP 的 5%
 */
public class Warrior extends Job {

    // 被動回血累積（小數進位用）
    private double hpRegenAccum = 0;

    // ─────────────────────────────────────────────────────────
    public Warrior() {
        skills.add(new SkillThrust());      // index 0 → Q
        skills.add(new SkillShockwave());   // index 1 → W
    }

    @Override public String getDisplayName() { return "劍士"; }
    @Override public String getJobId()       { return "warrior"; }

    /**
     * 被動：脫戰（timeSinceLastCombat >= 5 秒）且 HP 未滿時自動回血。
     * 每秒回復 5% 最大 HP，以累積方式處理小數。
     */
    @Override
    protected void updatePassive(Player player, double dt) {
        boolean outOfCombat = player.getTimeSinceLastCombat() >= 5.0;
        boolean notFullHp   = player.getHp() < player.getMaxHp();

        if (outOfCombat && notFullHp) {
            hpRegenAccum += player.getMaxHp() * 0.05 * dt; // 每秒 5% MaxHP
            int heal = (int) hpRegenAccum;
            if (heal >= 1) {
                player.healHp(heal);
                hpRegenAccum -= heal; // 保留小數
            }
        } else if (!outOfCombat) {
            hpRegenAccum = 0; // 戰鬥中重置累積
        }
    }
}
