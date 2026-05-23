<!-- Context: Part of [Development Roadmap](../completed-systems.md) -->
<!-- Domain: #game/dev -->

### Phase 6 — 職業轉職系統重構（可選職業）

**目標：** 移除硬編碼，劍士/法師/弓箭手三路線可選

**新建檔案：**
- `job/Mage.java` — 被動：每秒回 2% MaxMP
- `job/SkillFireball.java` — 25MP, 5s CD，中距火球 AOE
- `job/SkillFrostNova.java` — 30MP, 10s CD，範圍凍結（減速 70%, 4s）
- `job/Archer.java` — 被動：被攻擊 15% 機率完全閃避
- `job/SkillArrowRain.java` — 20MP, 6s CD，縱向箭雨多段
- `job/SkillEagleEye.java` — 15MP, 4s CD，瞬間暴擊（傷害 ×2.5）
- `ui/JobSelectionPanel.java` — 3 職業卡（名稱/被動說明/技能預覽），點選呼叫 `player.selectJob(Job)`

**修改檔案：**
- `entity/Player.java` — 移除 `checkJobUnlock()` 中 `new Warrior()`；改為設定 `jobChangeAvailable = true`；新增 `selectJob(Job)` 方法
- `map/VillageMap.java` — 村長老人 → 轉職師傅（靠近 F 鍵 → 開啟 JobSelectionPanel，需達到轉職等級）
- `core/GamePanel.java` — 偵測 `player.isJobChangeAvailable()` → 自動彈出 JobSelectionPanel；村莊 NPC 互動也可觸發
