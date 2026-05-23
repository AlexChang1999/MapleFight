<!-- Context: Part of [Player Attributes](player-attributes.md) -->
<!-- Domain: #game/character -->

- `entity/Player.java` — EXP 公式改為 `expToNextLevel = (int)(100 * Math.pow(level, 1.5))`（150 等約需 160 萬）

| 地圖節奏 | 每 10 等一個系列地圖，EXP 需求隨等級指數成長 |

| Phase 9 | 地圖等級門檻、EXP 指數公式 | BaseMap, MapManager, Player |
