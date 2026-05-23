package maplestory.quest;

import maplestory.entity.MonsterType;

/**
 * 單一任務。
 * 支援兩種任務類型：
 *   KILL  - 擊殺指定怪物 N 隻
 *   VISIT - 到達指定地圖
 */
public class Quest {

    public enum State { NOT_STARTED, IN_PROGRESS, COMPLETED }
    public enum Type  { KILL, VISIT, BOSS_KILL }

    public final int         id;
    public final String      title;
    public final String      description;
    public final Type        type;

    // KILL quest 欄位
    public final MonsterType targetMonster; // null for VISIT quests
    public final int         targetCount;

    // VISIT quest 欄位
    public final String      targetMapId;   // null for KILL / BOSS_KILL quests

    // BOSS_KILL quest 欄位
    public final String      bossId;        // null for KILL / VISIT quests

    private State state    = State.NOT_STARTED;
    private int   progress = 0; // 擊殺數 / VISIT 則 0 或 1

    // ── KILL 任務建構子 ───────────────────────────────────────
    public Quest(int id, String title, String desc,
                 MonsterType monster, int count) {
        this.id            = id;
        this.title         = title;
        this.description   = desc;
        this.type          = Type.KILL;
        this.targetMonster = monster;
        this.targetCount   = count;
        this.targetMapId   = null;
        this.bossId        = null;
    }

    // ── VISIT 任務建構子 ──────────────────────────────────────
    public Quest(int id, String title, String desc, String mapId) {
        this.id            = id;
        this.title         = title;
        this.description   = desc;
        this.type          = Type.VISIT;
        this.targetMonster = null;
        this.targetCount   = 1;
        this.targetMapId   = mapId;
        this.bossId        = null;
    }

    // ── BOSS_KILL 任務建構子 ──────────────────────────────────
    public Quest(int id, String title, String desc, String bossId, boolean isBoss) {
        this.id            = id;
        this.title         = title;
        this.description   = desc;
        this.type          = Type.BOSS_KILL;
        this.targetMonster = null;
        this.targetCount   = 1;
        this.targetMapId   = null;
        this.bossId        = bossId;
    }

    // ── 進度更新 ─────────────────────────────────────────────

    /** 擊殺通知。回傳 true 表示任務可以完成了。 */
    public boolean onKill(MonsterType killed) {
        if (state != State.IN_PROGRESS) return false;
        if (type  != Type.KILL)         return false;
        if (killed != targetMonster)    return false;
        if (progress < targetCount) progress++;
        return progress >= targetCount;
    }

    /** Boss 擊殺通知。回傳 true 表示任務可以完成了。 */
    public boolean onBossKilled(String killedBossId) {
        if (state != State.IN_PROGRESS) return false;
        if (type  != Type.BOSS_KILL)    return false;
        if (bossId.equals(killedBossId)) { progress = 1; return true; }
        return false;
    }

    /** 進入地圖通知。回傳 true 表示任務可以完成了。 */
    public boolean onMapEntered(String mapId) {
        if (state != State.IN_PROGRESS) return false;
        if (type  != Type.VISIT)        return false;
        if (targetMapId.equals(mapId))  { progress = 1; return true; }
        return false;
    }

    public boolean isCompletable() {
        return state == State.IN_PROGRESS && progress >= targetCount;
    }

    public void accept()   { if (state == State.NOT_STARTED) state = State.IN_PROGRESS; }
    public void complete() { state = State.COMPLETED; }

    // ── Getter / Setter（存讀檔用） ──────────────────────────
    public State getState()    { return state; }
    public int   getProgress() { return progress; }

    public void setState(State s)  { this.state    = s; }
    public void setProgress(int p) { this.progress = p; }
}
