<!-- Context: Part of [Development Roadmap](completed-features.md) -->
<!-- Domain: #game/dev -->

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
