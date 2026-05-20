package maplestory.job;

import maplestory.core.Camera;
import maplestory.entity.Monster;
import maplestory.entity.Player;

import java.awt.Graphics2D;
import java.util.List;

/**
 * 技能抽象基底類別。
 * 所有主動技能繼承此類別並實作 activate() 與 drawEffect()。
 */
public abstract class Skill {

    protected final String name;
    protected final int    mpCost;
    protected final double maxCooldown;

    // 冷卻倒數（Job.update 每幀遞減）
    protected double currentCooldown = 0;

    // 特效播放狀態
    protected boolean effectActive = false;
    protected double  effectTimer  = 0;

    // ─────────────────────────────────────────────────────────
    public Skill(String name, int mpCost, double maxCooldown) {
        this.name        = name;
        this.mpCost      = mpCost;
        this.maxCooldown = maxCooldown;
    }

    /**
     * 每幀更新：冷卻遞減 + 特效計時。
     * 由 Job.update() 統一呼叫，子類別通常不需覆寫。
     */
    public void update(double dt) {
        if (currentCooldown > 0) currentCooldown -= dt;
        if (effectTimer > 0) {
            effectTimer -= dt;
            if (effectTimer <= 0) effectActive = false;
        }
    }

    /**
     * 施放技能（MP 已由 Player 扣除後才呼叫）。
     * 子類別在這裡計算傷害、套用狀態、設定特效起點。
     */
    public abstract void activate(Player player, List<Monster> monsters);

    /**
     * 繪製技能特效（effectActive 為 true 時由 Job.drawEffects() 呼叫）。
     */
    public abstract void drawEffect(Graphics2D g, Camera camera);

    // ── 工具方法 ─────────────────────────────────────────────
    /** 是否可以使用（冷卻結束且特效已消退） */
    public boolean canUse() { return currentCooldown <= 0 && !effectActive; }

    /** 冷卻比例 0.0（好了）~ 1.0（剛用完） */
    public double getCooldownRatio() {
        return maxCooldown > 0 ? Math.max(0, currentCooldown / maxCooldown) : 0;
    }

    // ── Getter ───────────────────────────────────────────────
    public String  getName()            { return name; }
    public int     getMpCost()          { return mpCost; }
    public double  getMaxCooldown()     { return maxCooldown; }
    public double  getCurrentCooldown() { return currentCooldown; }
    public boolean isEffectActive()     { return effectActive; }
}
