<!-- Context: Part of [Development Roadmap](../completed-systems.md) -->
<!-- Domain: #game/dev -->

### Phase 5 — 新手三關地圖

**目標：** 遊戲入門有漸進難度教學關卡，從村莊左側進入

**新建檔案：**
- `map/NoviceMap1.java`（寬 1200px，草地，教學提示文字，NOVICE_SLIME）
- `map/NoviceMap2.java`（寬 1400px，黃昏，2 層平台，NOVICE_MUSHROOM）
- `map/NoviceMap3.java`（寬 1600px，黃昏深，3 層平台＋梯子，NOVICE_BAT）

**新增 MonsterType：**
- NOVICE_SLIME（10HP, 2ATK, 5EXP）
- NOVICE_MUSHROOM（25HP, 5ATK, 12EXP）
- NOVICE_BAT（40HP, 8ATK, 20EXP）

**修改檔案：**
- `map/VillageMap.java` — 左側新增傳送門 → novice1（"新手訓練場"）
- `map/BaseMap.java` — 新增 `getMinLevel()` 和 `getMapName()` 預設方法
- `core/MapManager.java` — 新增 novice1/2/3 + 切換前等級檢查
- `core/GamePanel.java` — 新增 noviceMonsters1/2/3；`currentMonsters()` 擴充
