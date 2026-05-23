<!-- Context: Part of [Development Roadmap](../completed-systems.md) -->
<!-- Domain: #game/dev -->

### Phase 1 — 存檔 & 開始介面（基礎設施，優先做）

**目標：** 玩家能取名、存讀檔，名字顯示在腳下

**新建檔案：**
- `core/TitleScreen.java` — 標題畫面（3 存檔槽 + 取名輸入框）
- `core/SaveManager.java` — JSON 存讀檔（純手寫 JSON，不引入外部 jar）

**修改檔案：**
- `entity/Player.java` — 加 `name` / `gold` 欄位；`draw()` 在腳下繪製名字（白字黑邊）
- `Main.java` / `core/GameWindow.java` — 改為先顯示 TitleScreen，選擇後才進 GamePanel

**存檔 JSON 欄位：** name, level, exp, str/dex/intel/luk, hp, mp, jobId, gold, currentMapId, inventory（三類陣列）, equipments（8 格物件）
