# MapleFight 開發存檔點
> 最後更新：2026-05-22｜GitHub: https://github.com/AlexChang1999/MapleFight.git

---

## 1. ✅ 已完成

| Phase / Feat | 內容 | 關鍵檔案 |
|---|---|---|
| Phase 1–6 | 視窗、角色、地圖、鏡頭、怪物AI、HUD | 各核心檔案 |
| Phase 7–12 | 村莊、NPC、傳送門、動畫、裝備系統 | map/, entity/ |
| Phase 0 | 等比縮放視窗、ESC暫停選單、新手三關、按鍵設定存檔 | GamePanel, PauseMenu |
| Phase 1 | 標題畫面、取名、JSON存讀、F5存檔、金幣欄 | TitleScreen, SaveManager |
| Phase 2/3/4 | 掉落系統、背包、商店、裝備穿脫 | item/, ui/InventoryPanel |
| Phase 5 | 任務系統、對話系統、商店改版 | quest/, ui/DialoguePanel |
| Phase 6 | Bug修復、中繼村莊(FrontierTown)、傳送卷軸、怪物強化 | map/FrontierTown |
| Phase 5-8 | 角色外觀升級、職業轉職(劍士/法師/弓箭手)、任務擴充、套裝系統 | job/, item/Equipment |
| Phase 9 | 地圖等級門檻、EXP 指數公式 | BaseMap, MapManager, Player |
| Phase 10 | 怪物 AI 升級（野豬衝刺硬直、極熊預備動作）、重生機制 | Monster |
| **Feat-2** | Hotbar 快捷欄（5格，1-5鍵指派/使用） | ui/Hotbar, GamePanel |
| **Feat-1** | BGM & SFX 音效系統補全（frontier/icepost BGM + 全 SFX + M鍵靜音） | audio/BGMTrack, GamePanel |
| **Feat-5** | 裝備面板槽位圖示 + 套裝件數 badge + 懸停浮層 | ui/EquipPanel |

---

## 2. 待辦

### 建議執行順序

| 優先度 | 任務 | 說明 |
|---|---|---|
| 中 | Phase 11+：新地圖擴充 | 古老森林(20-29)、沙漠廢墟(30-39)等新地圖系列 |
| 中 | Boss 怪物系統 | 地圖末端觸發 Boss 戰，多段攻擊、高EXP/稀有掉落 |
| 低 | 更多任務鏈 | 前線前哨站任務、極地冰原任務 |

---

## 3. 已知問題

- `SkillPanel` 目前是框架佔位，職業轉職後未填入真實技能清單
- Hotbar 目前不做持久化（每次開遊戲需重新指派）
- BGM MIDI 音質依賴作業系統 SoundFont，Windows 11 預設效果較佳
- 新手村右側傳送門等級門檻 15（冒險平原），對低等玩家可能偏嚴

---

## 4. 重要檔案清單

```
H:\MapleGame\
├── compile_and_run.bat
├── PROGRESS.md
├── BGM_SPECS.md               <- BGM 情緒定位與旋律規格
├── validated-sniffing-fiddle.md <- 全功能路線圖
└── src/maplestory/
    ├── audio/
    │   ├── BGMTrack.java      <- 6 張地圖 MIDI BGM
    │   ├── SFX.java           <- 12 種 SFX 枚舉
    │   └── SoundManager.java  <- 音效單例（M鍵靜音）
    ├── core/
    │   ├── GamePanel.java     <- 主迴圈、Hotbar、1-5鍵、M鍵靜音
    │   ├── MapManager.java
    │   ├── SaveManager.java
    │   └── TitleScreen.java
    ├── entity/
    │   ├── Monster.java / Player.java / NPC.java
    ├── item/
    │   ├── Inventory.java     <- findConsumable() 供 Hotbar 使用
    │   └── Equipment/Consumable/DropItem/...
    ├── job/
    │   ├── Warrior/Mage/Archer + Skill*.java
    ├── map/
    │   ├── NoviceMap1~3 / VillageMap / FrontierTown
    │   ├── GameMap / IcePostTown / ArcticMap
    │   └── BaseMap / Portal / Ladder / Platform
    ├── quest/
    │   └── Quest / QuestManager
    └── ui/
        ├── EquipPanel.java    <- 槽位圖示 + 套裝badge ★更新
        ├── Hotbar.java        <- 快捷欄 5 格 ★新建
        ├── InventoryPanel.java <- 1-5鍵指派提示 ★更新
        ├── ShopPanel.java     <- SHOP_BUY SFX ★更新
        └── StatusPanel/SkillPanel/KeyBindingPanel/PauseMenu/DialoguePanel
```

---

## 5. 遊戲規格摘要

| 項目 | 內容 |
|---|---|
| 視窗 | 可自由縮放（邏輯 800×580，等比縮放） |
| 存檔 | save/slot1~3.json + save/keybindings.json |
| 地圖流程 | 新手村→新手一/二/三區→前線前哨站/冒險平原→冰原驛站→極地冰原 |
| 操作鍵 | A/D 移動、W/Space 跳躍、↑↓ 爬梯、Z 攻擊、Q/W 技能、1-5 快捷欄 |
| 暫停 | ESC（存檔/刪檔/回主畫面/退出） |
| 面板 | S/K/E/I/B 開狀態/技能/裝備/背包/按鍵設定 |
| 音效 | BGM 6 張地圖、SFX 12 種；M 鍵切換靜音 |
| 職業 | 劍士/法師/弓箭手（Lv10 NPC 轉職，5000G + 30 擊殺） |
| 框架 | 純 Java + Swing（不需額外安裝） |
| Java 版本 | v24（Windows）|
