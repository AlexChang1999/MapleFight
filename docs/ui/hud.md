<!-- Context: Part of [Game Specifications](../dev/game-specifications.md) -->
<!-- Domain: #game/ui -->

- `core/GamePanel.java` — `pollJustDied()` 後同時呼叫 `rollDrops()`；HUD 加金幣顯示

- `core/GamePanel.java` — HUD 地圖名稱改用 `currentMap.getMapName()`

| Phase 1–6 | 視窗、角色、地圖、鏡頭、怪物AI、HUD | 各核心檔案 |

| Phase 1 | 標題畫面、取名、JSON存讀、F5存檔、金幣欄 | TitleScreen, SaveManager |
