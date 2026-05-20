# MapleGame 全功能開發路線圖

## 已完成系統（不需再做）
- ✅ BaseMap 抽象模板（portal / NPC / ladder 介面）
- ✅ MapManager（3 張地圖：village / battle / arctic，傳送門冷卻）
- ✅ Portal 動畫傳送門 + 碰撞偵測
- ✅ NPC 火柴人 + 呼吸動畫（無商店邏輯）
- ✅ VillageMap（3 棟建築、3 位 NPC、右側傳送門）
- ✅ GameMap（戰鬥平原、3 層平台、左右傳送門）
- ✅ ArcticMap（極地夜空、極光、雪粒子、梯子系統、冰山視差）
- ✅ Ladder（爬梯物理、上下對齊、梯子跳離）
- ✅ Job 抽象類 + Warrior（脫戰回血被動）
- ✅ Skill 抽象類 + SkillThrust + SkillShockwave
- ✅ Player（雙段連擊、等級/EXP、屬性、裝備欄位、冰緩、null-safe job）
- ✅ Monster（6 種動物、AI、冰屬性緩速、pollJustDied EXP 系統）
- ✅ KeyBindingManager + KeyBindingPanel（拖曳改鍵 UI）
- ✅ Equipment / EquipSlot（8 格裝備、屬性加成）
- ✅ GamePanel（arcticMonsters 分離、currentMonsters()、EXP 即時發放）

---

## 設計決策記錄

| 項目 | 決定 |
|------|------|
| 存檔格式 | JSON（save/slot1~3.json），可記事本開啟 |
| 職業選擇 | 到達轉職等級 → 彈出選職視窗 + 村莊 NPC 可重複觸發 |
| 裝備外觀 | 分槽位圖形疊加在火柴人身上（武器/頭盔/上衣/下衣/手套/鞋/披風） |
| 稀有度 | 5 階：一般(白) / 稀有(綠) / 史詩(藍) / 傳奇(紫) / 神話(橘) |
| 第一轉職職業 | 劍士 / 法師 / 弓箭手（3 路線，不硬編碼） |
| 新手地圖 | 村莊旁新增新手三關（獨立 3 張地圖，漸進難度） |
| 轉職里程碑 | 10 等→1轉 / 30 等→2轉 / 70 等→3轉 / 120 等→4轉 / 150 等滿級 |
| 地圖節奏 | 每 10 等一個系列地圖，EXP 需求隨等級指數成長 |

---

## 待辦清單（依階段）

---

### Phase 1 — 存檔 & 開始介面（基礎設施，優先做）

**目標：** 玩家能取名、存讀檔，名字顯示在腳下

**新建檔案：**
- `core/TitleScreen.java` — 標題畫面（3 存檔槽 + 取名輸入框）
- `core/SaveManager.java` — JSON 存讀檔（純手寫 JSON，不引入外部 jar）

**修改檔案：**
- `entity/Player.java` — 加 `name` / `gold` 欄位；`draw()` 在腳下繪製名字（白字黑邊）
- `Main.java` / `core/GameWindow.java` — 改為先顯示 TitleScreen，選擇後才進 GamePanel

**存檔 JSON 欄位：** name, level, exp, str/dex/intel/luk, hp, mp, jobId, gold, currentMapId, inventory（三類陣列）, equipments（8 格物件）

---

### Phase 2 — 道具欄 & 掉落系統

**目標：** 怪物掉金幣 + 裝備，玩家有分類背包；HUD 顯示金幣

**新建檔案：**
- `item/ItemRarity.java`（枚舉）：COMMON(白) / RARE(綠) / EPIC(藍) / LEGENDARY(紫) / MYTHIC(橘)
- `item/Item.java`（抽象基底）：id, name, rarity, description
- `item/Consumable.java`：type(HP藥水/MP藥水/捲軸)、healAmount
- `item/DropItem.java`：純展示用掉落品
- `item/Inventory.java`：三個 List（consumables / drops / equipments，各最多 40 格）
- `ui/InventoryPanel.java`：按 I 開啟，三頁 Tab；右鍵裝備/使用/賣出

**修改檔案：**
- `item/Equipment.java` — 加入 `rarity` 欄位；displayColor 按稀有度決定
- `entity/Monster.java` — 加 `rollDrops()` 回傳 `List<Item>`；金幣固定掉、裝備隨機掉（5~20% 機率，稀有度按權重）
- `entity/Player.java` — 加 `Inventory inventory`；`gainGold(int)` 方法
- `keybind/ActionType.java` — 加 `UI_INVENTORY`（I 鍵）
- `core/GamePanel.java` — `pollJustDied()` 後同時呼叫 `rollDrops()`；HUD 加金幣顯示

**稀有度掉落權重：** COMMON 60% / RARE 25% / EPIC 10% / LEGENDARY 4% / MYTHIC 1%

---

### Phase 3 — NPC 商店系統

**目標：** 村莊藥水商、鐵匠可互動買賣

**新建檔案：**
- `ui/ShopEntry.java`：(Item + 售價) 的 DTO
- `ui/ShopPanel.java`：左側商店庫存（點擊購買）、右側玩家背包（點擊出售）、底部金幣顯示

**修改檔案：**
- `entity/NPC.java` — 加 `shopType` 字串 + `interactRadius`；玩家靠近時顯示「按 F 互動」提示
- `map/VillageMap.java` — 道具商 NPC 加藥水庫存；鐵匠 NPC 加裝備庫存
- `keybind/ActionType.java` — 加 `INTERACT`（F 鍵）
- `core/GamePanel.java` — 偵測玩家靠近 NPC + F 鍵開啟 ShopPanel

**村莊商品預設：**
- 道具商：HP 藥水小(50G/回100HP)、中(150G/回300HP)、大(400G/回800HP)；MP 藥水小/中/大
- 鐵匠：每批裝備依玩家等級動態刷新（等級區間對應品質）

---

### Phase 4 — 裝備外觀疊加在角色身上

**目標：** 穿上裝備在火柴人身上可見

**修改檔案：**
- `entity/Player.java draw()` — 依裝備欄位疊加圖形：
  - WEAPON → 右手旁畫短劍/法杖/弓形（依職業判斷）
  - HELMET → 頭頂加半弧頭盔形
  - TOP → 身體矩形改色（裝備 displayColor）
  - BOTTOM → 腿部矩形改色
  - GLOVES → 手末端加小圓
  - BOOTS → 腳末端加底座矩形
  - CAPE → 背後弧形披風
- `item/Equipment.java` — `displayColor` 由稀有度底色 + 裝備類型色相偏移計算

---

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

---

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

---

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

---

### Phase 8 — 多地圖內容擴充（長期）

每 10 等一個系列（每個系列 2~3 張地圖）：

| 等級範圍 | 系列主題 | 特色機制 |
|---------|---------|---------|
| 1–9 | 新手訓練場 (Phase 5 已規劃) | 教學 |
| 10–19 | 冒險平原 (已有) | 草原普通怪 |
| 20–29 | 古老森林 | 植物系怪、毒霧傷害地板 |
| 30–39 | 沙漠廢墟 | 熱帶骷髏、沙塵暴定期減視野 |
| 40–49 | 熔岩火山 | 火焰怪、踩岩漿持續掉血 |
| 50–59 | 極地冰原 (已有) | 冰系怪物、冰緩效果 |
| 60–69 | 深海海底 | 水系怪、水壓（限制跳躍高度） |
| 70–79 | 天空浮島 | 風系鳥族、強風推力 |
| 80–99 | 幽靈城堡 | 亡靈系、詛咒（隨機減屬性） |
| 100–119 | 龍族山脈 | 龍系 Boss、大型怪 |
| 120–149 | 宇宙科幻站 | 機器人、雷射陷阱 |
| 150 | 神話境界 | 神話怪、終局 Boss |

---

## 執行順序建議

**Phase 1 → Phase 2 → Phase 6 → Phase 3 → Phase 4 → Phase 5 → Phase 7 → Phase 8**

**理由：**
- Phase 1（存檔）先做，因為之後所有內容都需要持久化
- Phase 2（道具欄）先做，因為商店系統依賴它
- Phase 6（職業選擇）早做，因為目前有硬編碼問題
- Phase 3（商店）依賴 Phase 2 的背包系統
- Phase 4（裝備外觀）是純視覺，可任意時機做
- Phase 5（新手地圖）依賴 Phase 2 的怪物掉落
- Phase 7（地圖模板）確立框架後 Phase 8 才能快速擴充

## 驗證方式
每個 Phase 完成後：
```
find src -name "*.java" | xargs javac -encoding UTF-8 -d out
java -cp out maplestory.Main
```
確認 EXIT:0，並實際運行遊戲測試對應功能。
