<!-- Context: Part of [Map Design Specifications](map-connections.md) -->
<!-- Domain: #game/maps -->

## 4. BaseMap 介面規格

所有地圖**必須繼承 `BaseMap`** 並實作以下方法：

```java
// 必須實作
public abstract void           update(double dt);
public abstract void           draw(Graphics2D g, Camera camera);
public abstract List<Platform> getPlatforms();
public abstract int            getMapWidth();
public abstract String         getMapId();

// 選擇性覆寫（預設回傳空或 1）
public List<Portal>  getPortals()   // 預設 emptyList
public List<NPC>     getNPCs()      // 預設 emptyList
public List<Ladder>  getLadders()   // 預設 emptyList
public String        getMapName()   // 預設回傳 mapId
public int           getMinLevel()  // 預設 1
```
