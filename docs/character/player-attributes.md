<!-- Context: Part of [Game Specifications](../dev/game-specifications.md) -->
<!-- Domain: #game/character -->

**存檔 JSON 欄位：** name, level, exp, str/dex/intel/luk, hp, mp, jobId, gold, currentMapId, inventory（三類陣列）, equipments（8 格物件）

- `entity/Player.java` — 加 `name` / `gold` 欄位；`draw()` 在腳下繪製名字（白字黑邊）

- ✅ Player（雙段連擊、等級/EXP、屬性、裝備欄位、冰緩、null-safe job）
