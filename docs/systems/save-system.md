<!-- Context: Part of [Game Specifications](../dev/game-specifications.md) -->
<!-- Domain: #game/systems -->

- `core/SaveManager.java` — JSON 存讀檔（純手寫 JSON，不引入外部 jar）

**存檔 JSON 欄位：** name, level, exp, str/dex/intel/luk, hp, mp, jobId, gold, currentMapId, inventory（三類陣列）, equipments（8 格物件）

| 存檔格式 | JSON（save/slot1~3.json），可記事本開啟 |

| 存檔 | save/slot1~3.json + save/keybindings.json |

| Phase 1 | 標題畫面、取名、JSON存讀、F5存檔、金幣欄 | TitleScreen, SaveManager |
