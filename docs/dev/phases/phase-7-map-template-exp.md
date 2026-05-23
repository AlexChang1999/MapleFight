<!-- Context: Part of [Development Roadmap](../completed-systems.md) -->
<!-- Domain: #game/dev -->

### Phase 7 — 地圖模板規範 & EXP 指數成長

**目標：** 快速建立新地圖的框架 + 指數 EXP 公式

**修改檔案：**
- `map/BaseMap.java` — 新增 `getMinLevel()` (預設 -1)、`getMapName()` 抽象方法
- `core/MapManager.java` — 新增 `registerMap()`；傳送門切換前判斷 `getMinLevel()`，不足則顯示等級不足提示
- `entity/Player.java` — EXP 公式改為 `expToNextLevel = (int)(100 * Math.pow(level, 1.5))`（150 等約需 160 萬）
- `core/GamePanel.java` — HUD 地圖名稱改用 `currentMap.getMapName()`

**地圖模板建議 Constructor 結構（文件規範）：**
```
buildGround()    ← 地面層平台
buildPlatforms() ← 中高層浮台
buildLadders()   ← 梯子（可選）
buildPortals()   ← 傳送門（左=回、右=進）
buildNPCs()      ← NPC（可選）
initParticles()  ← 粒子特效（可選）
```
