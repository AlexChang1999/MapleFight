<!-- Context: Part of [New Map Creation Guide](file-structure-template.md) -->
<!-- Domain: #game/maps -->

### 6-4. 視差層比例慣例

| 層次 | parallaxFactor | 說明 |
|---|---|---|
| 星星 / 固定裝飾 | `0.0` | 最遠，不移動 |
| 遠山 / 遠景 | `0.1~0.2` | 緩慢移動 |
| 雲朵 | `0.12~0.15` | 非常緩慢 |
| 中景樹木 / 建築剪影 | `0.4~0.5` | 中速 |
| 地面裝飾 | `1.0`（世界座標） | 與地圖同速 |
