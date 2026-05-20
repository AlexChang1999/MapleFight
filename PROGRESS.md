# MapleFight 開發存檔點
> 最後更新：2026-05-20｜GitHub: https://github.com/AlexChang1999/MapleFight.git

---

## 1. ✅ 已完成

| Phase | 內容 | 關鍵檔案 |
|-------|------|---------|
| Phase 1–6 | 視窗、角色、地圖、鏡頭、怪物AI、HUD | 各核心檔案 |
| Phase 7–12 | 村莊、NPC、傳送門、動畫、裝備系統 | map/, entity/ |
| 極地冰原 | 梯子、冰怪、極光雪粒子、職業解鎖 | ArcticMap.java, Ladder.java |
| 動物怪物 | 劍士職業、雙段連擊 | Warrior.java, SkillThrust.java |
| 按鍵設定 | 拖曳鍵盤 UI、雙向映射 | KeyBindingPanel.java |
| **Phase 1 (存檔系統)** | 標題畫面、取名、JSON存讀、F5存檔、金幣欄 | TitleScreen.java, SaveManager.java |
| **Phase 0 (基礎修復)** | 等比縮放視窗、ESC暫停選單、新手三關地圖、按鍵設定存檔 | 見下方詳述 |

### Phase 0 詳細（本次完成）

| 項目 | 說明 |
|------|------|
| **等比縮放視窗** | 視窗可自由拖曳縮放，邏輯解析度 800×580 不變，黑邊補齊 letterbox |
| **ESC 暫停選單** | 按 ESC 暫停，選單有：繼續遊戲 / 存檔 / 刪除存檔 / 回主畫面 / 退出遊戲 |
| **新手森林三區** | NoviceMap1（史萊姆）、NoviceMap2（史萊姆+蝙蝠）、NoviceMap3（野豬+蝙蝠） |
| **新手地圖串接** | 村莊左側傳送門 → 一區 → 二區 → 三區 → 冒險平原 |
| **梯子** | 三張新手地圖各自含梯子（每圖 2~4 座） |
| **按鍵設定存檔** | save/keybindings.json，存於 F5/ESC存檔 和關閉按鍵設定面板時 |
| **地圖切換鏡頭** | Camera.snapTo() 切圖時瞬間定位，不 lerp 滑動 |
| **TitleScreen縮放** | 標題畫面也支援等比縮放 |

---

## 2. ⏳ 待辦（Phase 2–7）

### 建議執行順序：2 → 3 → 4 → 5 → 6 → 7

| Phase | 任務 | 關鍵檔案 |
|-------|------|---------|
| **Phase 2** | 道具欄 & 掉落系統 | ItemRarity, Item, Consumable, DropItem, Inventory, InventoryPanel（I鍵）|
| **Phase 3** | NPC 商店系統 | ShopEntry, ShopPanel；NPC 靠近顯示「F互動」；村莊商人開店 |
| **Phase 4** | 裝備外觀疊加在角色身上 | Player.draw() 依裝備槽疊圖形，稀有度決定顏色 |
| **Phase 5** | 職業轉職系統重構 | Mage, Archer + 技能；JobSelectionPanel（Lv10彈出）；移除 Warrior 硬編碼 |
| **Phase 6** | 地圖等級門檻 & EXP指數成長 | BaseMap.getMinLevel()/getMapName()；MapManager 阻擋低等級；EXP=100×1.4^lv |
| **Phase 7** | 多地圖擴充（長期） | 森林/沙漠/火山/深海/天空/城堡/龍族/科幻/神話 |

---

## 3. 🐛 已知問題 / 注意事項

- `SkillPanel` 和 `EquipPanel` 目前是**框架佔位**，Phase 5/4 才填入真實資料
- 怪物死亡後**不會重生**（切換地圖不重置），Phase 2 規劃加入重生機制
- `MapManager` 的怪物列表硬編碼在 `GamePanel`，Phase 6 規劃移入各地圖
- 新手村右側傳送門目前直通**冒險平原**（高等地圖），Phase 6 加等級門檻阻擋
- `SkillPanel` 顯示「戰士第一轉」但職業系統重構後會更新（Phase 5）
- `save/slot1.json` 等已從 git 追蹤中排除（加入 .gitignore）

---

## 4. 📁 檔案清單

```
H:\MapleGame\
├── compile_and_run.bat          ← 雙擊編譯並執行（H 槽）
├── PROGRESS.md                  ← 本存檔點
├── .gitignore                   ← 排除 out/ save/
└── src/maplestory/
    ├── Main.java
    ├── core/
    │   ├── Camera.java          ← Lerp 鏡頭 + snapTo()
    │   ├── GamePanel.java       ← 主迴圈、縮放、ESC選單、怪物分地圖管理
    │   ├── GameWindow.java      ← 可縮放 JFrame，CardLayout
    │   ├── MapManager.java      ← 6 張地圖（village/novice1~3/battle/arctic）
    │   ├── SaveManager.java     ← JSON 存讀刪，3 個槽位
    │   └── TitleScreen.java     ← 標題畫面（縮放支援、刪除確認）
    ├── entity/
    │   ├── Monster.java         ← 多型態怪物AI + ICE屬性
    │   ├── MonsterType.java     ← 列舉（SLIME/BOAR/BAT/ICE_SLIME/POLAR_BEAR/ICE_BAT）
    │   ├── NPC.java             ← 村莊 NPC（靜待動畫、名牌）
    │   └── Player.java          ← 玩家（梯子攀爬、冰緩速、EXP/升等）
    ├── input/
    │   └── InputHandler.java    ← 按鍵綁定查詢
    ├── item/
    │   ├── Equipment.java       ← 裝備資料（8格）
    │   └── EquipSlot.java       ← 裝備格枚舉
    ├── job/
    │   ├── Job.java             ← 職業抽象基底
    │   ├── Skill.java           ← 技能基底
    │   ├── SkillShockwave.java  ← 衝擊波技能
    │   ├── SkillThrust.java     ← 刺擊技能
    │   └── Warrior.java         ← 劍士（第一轉）
    ├── keybind/
    │   ├── ActionType.java      ← 動作枚舉
    │   └── KeyBindingManager.java ← 雙向映射 + 存讀檔
    ├── map/
    │   ├── ArcticMap.java       ← 極地冰原（極光、雪粒子、梯子）
    │   ├── BaseMap.java         ← 抽象基底（getPlatforms/getPortals/getLadders/getNPCs）
    │   ├── GameMap.java         ← 冒險平原（戰鬥地圖）
    │   ├── Ladder.java          ← 梯子物件（碰撞區 + 木頭繪製）
    │   ├── NoviceMap1.java      ← 新手森林一區（晴天，史萊姆）★新
    │   ├── NoviceMap2.java      ← 新手森林二區（林間，史萊姆+蝙蝠）★新
    │   ├── NoviceMap3.java      ← 新手森林三區（傍晚，野豬+蝙蝠）★新
    │   ├── Platform.java        ← 平台物件
    │   ├── Portal.java          ← 傳送門（旋轉光暈動畫）
    │   └── VillageMap.java      ← 新手村（左傳送門→novice1，右→battle）
    └── ui/
        ├── EquipPanel.java      ← 裝備面板（E鍵，佔位）
        ├── KeyBindingPanel.java ← 按鍵設定（B鍵，拖曳綁定）
        ├── PauseMenu.java       ← ESC暫停選單 ★新
        ├── SkillPanel.java      ← 技能面板（K鍵，佔位）
        └── StatusPanel.java     ← 狀態面板（S鍵，完整 STR/DEX/INT/LUK）
```

---

## 5. 🎮 遊戲規格摘要

| 項目 | 內容 |
|------|------|
| 視窗大小 | 可自由縮放（邏輯 800×580，等比縮放，黑邊補齊） |
| 存檔位置 | save/slot1~3.json + save/keybindings.json |
| 地圖流程 | 新手村 → 新手一/二/三區 → 冒險平原 → 極地冰原 |
| 操作鍵 | ← → 移動、Space 跳躍、↑↓ 爬梯、Z 攻擊、Q/W 技能 |
| 暫停 | ESC（可存檔/刪檔/回主畫面/退出） |
| 面板 | S/K/E/B 開狀態/技能/裝備/按鍵設定面板 |
| 職業 | 劍士（Lv10 自動解鎖，未來 Phase 5 改為可選） |
| 框架 | 純 Java + Swing（不需額外安裝） |
| Java 版本 | v24（Windows 11） |
