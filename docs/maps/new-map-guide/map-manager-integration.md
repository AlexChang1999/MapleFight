<!-- Context: Part of [New Map Creation Guide](file-structure-template.md) -->
<!-- Domain: #game/maps -->

### 6-6. 新地圖加入 MapManager 的步驟

1. 建立 `XxxMap.java`（繼承 `BaseMap`）
2. 確認 `mapId` 字串唯一（全域搜尋確認無衝突）
3. 在 `MapManager.java`（或對應的地圖工廠）中加入實例化邏輯
4. 確認上一張地圖的右側傳送門 `targetMapId` 正確指向新地圖
5. 確認新地圖左側傳送門目標座標落在上一張地圖的安全地面
