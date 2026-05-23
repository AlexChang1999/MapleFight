<!-- Context: Part of [Development Roadmap](completed-systems.md) -->
<!-- Domain: #game/dev -->

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
| **Feat-3** | Hotbar 可重新綁鍵（HOTBAR_1-5 ActionType、M鍵靜音修正、GamePanel 重構） | keybind/, core/GamePanel |
| **Feat-4** | 按鍵設定面板視覺大改版（3D 鍵效、分類色條、緊湊排版支援 19 個動作） | ui/KeyBindingPanel |
