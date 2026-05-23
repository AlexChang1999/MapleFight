<!-- Context: Part of [Job System Overview](../jobs/job-system-overview.md) -->
<!-- Domain: #game/ui -->

- `ui/JobSelectionPanel.java` — 3 職業卡（名稱/被動說明/技能預覽），點選呼叫 `player.selectJob(Job)`

- `core/GamePanel.java` — 偵測 `player.isJobChangeAvailable()` → 自動彈出 JobSelectionPanel；村莊 NPC 互動也可觸發

- `map/VillageMap.java` — 村長老人 → 轉職師傅（靠近 F 鍵 → 開啟 JobSelectionPanel，需達到轉職等級）
