package maplestory.job;

import maplestory.core.Camera;
import maplestory.entity.Monster;
import maplestory.entity.Player;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * 職業抽象基底類別。
 * 每個職業持有技能列表，並實作職業被動邏輯。
 * 未來新增職業只需繼承此類別。
 */
public abstract class Job {

    /** 技能列表（最多 2 個主動技能，對應 Q / W 鍵） */
    protected final List<Skill> skills = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    /**
     * 每幀更新：被動邏輯 + 所有技能冷卻遞減。
     * 由 Player.update() 呼叫。
     */
    public void update(Player player, double dt) {
        updatePassive(player, dt);
        for (Skill s : skills) s.update(dt);
    }

    /**
     * 職業被動效果（子類別實作）。
     * 例如：劍士脫戰 5 秒後自動回血。
     */
    protected abstract void updatePassive(Player player, double dt);

    // ─────────────────────────────────────────────────────────
    /**
     * 使用指定索引的技能（Q=0, W=1）。
     * 自動檢查 MP 是否足夠、冷卻是否結束。
     */
    public void useSkill(int index, Player player, List<Monster> monsters) {
        if (index < 0 || index >= skills.size()) return;
        Skill s = skills.get(index);
        if (!s.canUse()) return;
        if (!player.consumeMp(s.getMpCost())) return; // MP 不足

        s.activate(player, monsters);
        s.currentCooldown = s.maxCooldown; // 啟動冷卻
    }

    /**
     * 繪製所有正在播放的技能特效。
     * 由 GamePanel.drawGameArea() 呼叫。
     */
    public void drawEffects(Graphics2D g, Camera camera) {
        for (Skill s : skills) {
            if (s.isEffectActive()) s.drawEffect(g, camera);
        }
    }

    // ── Getter ───────────────────────────────────────────────
    public List<Skill> getSkills() { return skills; }

    /** 取得職業顯示名稱（子類別覆寫） */
    public abstract String getDisplayName();

    /**
     * 取得存檔用的職業 ID（子類別覆寫）。
     * 例："warrior", "mage", "archer"
     */
    public abstract String getJobId();
}
