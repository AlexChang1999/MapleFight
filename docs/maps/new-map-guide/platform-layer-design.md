<!-- Context: Part of [New Map Creation Guide](file-structure-template.md) -->
<!-- Domain: #game/maps -->

### 6-3. 平台層次設計準則

| 層次 | Y 基準（建議） | 平台厚度 | 顏色深度 |
|---|---|---|---|
| 地面 | `gY = GAME_HEIGHT - 40` | `40` | 最亮（草、冰、泥） |
| 中層 | `gY - 110` 到 `gY - 200`（各地圖自訂） | `18` | 中等 |
| 高層 | 中層 - 130 到 - 160 | `18` | 最暗 |
| 裝飾台（城鎮） | `gY - 28` | `12` | 同地面色系 |
