package maplestory.quest;

import maplestory.entity.MonsterType;
import maplestory.entity.Player;
import maplestory.item.Consumable;

import java.util.ArrayList;
import java.util.List;

/**
 * 村長老人的三連鎖任務管理器。
 *
 * 任務鏈：
 *   Q0 初出茅廬   - 擊殺史萊姆 ×10  → 500G + 紅色藥水 ×5
 *   Q1 野豬的威脅 - 擊殺野豬  ×5   → 1000G + 橙色藥水 ×3
 *   Q2 冒險者之路 - 到達冒險平原     → 1500G + 萬能藥水 ×2
 */
public class QuestManager {

    // ── 對話資料 DTO ─────────────────────────────────────────────
    public static class DialogueData {
        public final String       npcName;
        public final String       text;
        public final List<String> options;
        public final List<String> actionIds;

        public DialogueData(String npcName, String text) {
            this.npcName   = npcName;
            this.text      = text;
            this.options   = new ArrayList<>();
            this.actionIds = new ArrayList<>();
        }

        public DialogueData addOption(String label, String actionId) {
            options.add(label);
            actionIds.add(actionId);
            return this;
        }
    }

    // ─────────────────────────────────────────────────────────────
    private final Quest[] quests = new Quest[3];

    public QuestManager() {
        quests[0] = new Quest(0, "初出茅廬",
            "村子周邊的史萊姆越來越多了！\n請你幫我消滅 10 隻史萊姆，\n讓村民能夠安心生活。",
            MonsterType.SLIME, 10);

        quests[1] = new Quest(1, "野豬的威脅",
            "更大的威脅出現了……\n野豬群開始衝撞農田，\n請消滅 5 隻野豬來保護村民！",
            MonsterType.BOAR, 5);

        quests[2] = new Quest(2, "冒險者之路",
            "你已是村子的守護英雄！\n前往東邊的冒險平原，\n那裡才是真正考驗冒險者的地方。",
            "battle");
    }

    // ── 事件回呼 ─────────────────────────────────────────────────

    /** 每次怪物死亡時呼叫 */
    public void onMonsterKilled(MonsterType type) {
        for (Quest q : quests) {
            if (q.getState() == Quest.State.IN_PROGRESS) {
                q.onKill(type);
            }
        }
    }

    /** 每次玩家切換到新地圖時呼叫 */
    public void onMapEntered(String mapId) {
        for (Quest q : quests) {
            if (q.getState() == Quest.State.IN_PROGRESS) {
                q.onMapEntered(mapId);
            }
        }
    }

    // ── 任務操作 ─────────────────────────────────────────────────

    public void acceptQuest(int id) {
        if (id < 0 || id >= quests.length) return;
        if (canAcceptQuest(id)) quests[id].accept();
    }

    /** 完成任務並給予獎勵。成功回傳 true。 */
    public boolean completeQuest(int id, Player player) {
        if (id < 0 || id >= quests.length) return false;
        Quest q = quests[id];
        if (!q.isCompletable()) return false;
        q.complete();
        giveRewards(id, player);
        return true;
    }

    /** 放棄任務（重設進度，讓玩家可重新接） */
    public void abandonQuest(int id) {
        if (id < 0 || id >= quests.length) return;
        Quest q = quests[id];
        if (q.getState() == Quest.State.IN_PROGRESS) {
            q.setState(Quest.State.NOT_STARTED);
            q.setProgress(0);
        }
    }

    // ── 對話生成 ─────────────────────────────────────────────────

    /** 根據當前任務狀態，生成村長老人的對話內容。 */
    public DialogueData getElderDialogue() {
        for (int i = 0; i < quests.length; i++) {
            Quest q = quests[i];
            switch (q.getState()) {

                case NOT_STARTED -> {
                    if (canAcceptQuest(i)) {
                        return new DialogueData("村長老人",
                            "任務：【" + q.title + "】\n\n" + q.description)
                            .addOption("接受任務", "accept_" + i)
                            .addOption("再見", "dismiss");
                    }
                }

                case IN_PROGRESS -> {
                    String prog = progressStr(q);
                    if (q.isCompletable()) {
                        return new DialogueData("村長老人",
                            "【" + q.title + "】已完成！" + prog + "\n\n" + getCompleteText(i))
                            .addOption("領取獎勵 " + getRewardHint(i), "complete_" + i)
                            .addOption("稍後再說", "dismiss");
                    } else {
                        return new DialogueData("村長老人",
                            "【" + q.title + "】進行中 " + prog + "\n\n繼續加油！")
                            .addOption("好的", "dismiss")
                            .addOption("放棄任務", "abandon_" + i);
                    }
                }

                case COMPLETED -> { /* 繼續檢查下一個 */ }
            }
        }

        // 全部完成
        return new DialogueData("村長老人",
            "感謝你保護了我們的村子！\n你已是真正的冒險者了，\n你的未來無可限量！")
            .addOption("謝謝您！", "dismiss");
    }

    // ── 輔助方法 ─────────────────────────────────────────────────

    public boolean canAcceptQuest(int id) {
        if (id == 0) return quests[0].getState() == Quest.State.NOT_STARTED;
        return quests[id - 1].getState() == Quest.State.COMPLETED
                && quests[id].getState() == Quest.State.NOT_STARTED;
    }

    private String progressStr(Quest q) {
        if (q.type == Quest.Type.KILL) {
            return "(" + q.getProgress() + "/" + q.targetCount + ")";
        }
        return "";
    }

    private String getCompleteText(int id) {
        return switch (id) {
            case 0 -> "幹得好！史萊姆的威脅解除了！";
            case 1 -> "太厲害了！農田再也不用擔心了！";
            case 2 -> "你真正成為了冒險者！";
            default -> "任務完成！";
        };
    }

    private String getRewardHint(int id) {
        return switch (id) {
            case 0 -> "(500G + 紅藥×5)";
            case 1 -> "(1000G + 橙藥×3)";
            case 2 -> "(1500G + 萬能藥×2)";
            default -> "";
        };
    }

    private void giveRewards(int id, Player player) {
        switch (id) {
            case 0 -> {
                player.gainGold(500);
                for (int i = 0; i < 5; i++) player.getInventory().addConsumable(Consumable.redPotion());
            }
            case 1 -> {
                player.gainGold(1000);
                for (int i = 0; i < 3; i++) player.getInventory().addConsumable(Consumable.orangePotion());
            }
            case 2 -> {
                player.gainGold(1500);
                for (int i = 0; i < 2; i++) player.getInventory().addConsumable(Consumable.elixir());
            }
        }
    }

    // ── 存讀檔輔助 ───────────────────────────────────────────────

    public Quest   getQuest(int id) { return quests[id]; }
    public int     getQuestCount()  { return quests.length; }
}
