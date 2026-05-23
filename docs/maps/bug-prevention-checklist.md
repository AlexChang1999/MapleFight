<!-- Context: Part of [New Map Creation Guide](new-map-guide/file-structure-template.md) -->
<!-- Domain: #game/maps -->

## 7. 常見 Bug 防範清單

建立新地圖時，完成後自我檢查以下項目：

- [ ] 地面是否為一整條 `Platform(0, gY, MAP_WIDTH, 40, ...)`？（不得分段）
- [ ] 所有梯子的 `botY` 是否與下方平台的 `Y` 值**完全相等**（無差距）？
- [ ] 所有梯子的 `x` 是否落在目標平台的 `[x, x+width]` 範圍內？
- [ ] 左右傳送門的 `targetX / targetY` 是否落在對方地圖的地面上（不會空中重生）？
- [ ] `getMapId()` 回傳的 ID 是否與傳送門中使用的字串一致？
- [ ] 高層浮台是否有足夠的水平空間讓玩家（WIDTH=24）站上？（平台寬度 ≥ 30px 建議值）
- [ ] `getMinLevel()` 是否與傳送門 `minLevel` 設定一致？
