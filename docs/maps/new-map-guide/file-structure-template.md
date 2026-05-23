<!-- Context: Part of [New Map Creation Guide](map-manager-integration.md) -->
<!-- Domain: #game/maps -->

### 6-1. 檔案結構模板

```java
package maplestory.map;

import maplestory.core.Camera;
import maplestory.core.GamePanel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 地圖名稱（mapId）
 * 推薦等級：Lv X~Y
 * 怪物：...
 * 特色：...
 *
 * 傳送門：
 *   左側 → 上一張地圖
 *   右側 → 下一張地圖（Lv.X）
 */
public class XxxMap extends BaseMap {

    public static final int MAP_WIDTH = ?;          // 參考下方規格
    private static final int GROUND_Y = GamePanel.GAME_HEIGHT - 40;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Portal>   portals   = new ArrayList<>();
    private final List<Ladder>   ladders   = new ArrayList<>();
    // 城鎮地圖加上：
    // private final List<NPC> npcs = new ArrayList<>();

    public XxxMap() {
        buildPlatforms();
        buildLadders();   // 若有梯子
        buildPortals();
        // buildNPCs();   // 若為城鎮
    }

    private void buildPlatforms() {
        // 1. 地面：一定要一整條
        platforms.add(new Platform(0, GROUND_Y, MAP_WIDTH, 40, groundColor));
        // 2. 中層浮台（厚度固定 18px）
        // 3. 高層浮台（厚度固定 18px）
    }

    private void buildLadders() {
        // topY = 平台 Y，botY = 下方平台 Y（必須完全對齊）
        // x 必須落在目標平台 x 範圍內
    }

    private void buildPortals() {
        portals.add(new Portal(22, GROUND_Y - Portal.HEIGHT,
            "prevMapId", prevMap.MAP_WIDTH - 68, GROUND_Y - 80,
            "回上一區", 1));
        portals.add(new Portal(MAP_WIDTH - 68, GROUND_Y - Portal.HEIGHT,
            "nextMapId", 80, GROUND_Y - 80,
            "下一區(Lv.X)", X));
    }

    @Override public void update(double dt) { /* 靜態地圖 */ }

    @Override
    public void draw(Graphics2D g, Camera camera) {
        drawSky(g);
        drawBackground(g, camera);
        for (Platform p : platforms) p.draw(g, camera);
        for (Ladder   l : ladders)   l.draw(g, camera);
        for (Portal   p : portals)   p.draw(g, camera);
    }

    // drawSky / drawBackground ...

    @Override public List<Platform> getPlatforms() { return platforms; }
    @Override public List<Portal>   getPortals()   { return portals;   }
    @Override public List<Ladder>   getLadders()   { return ladders;   }
    @Override public int            getMapWidth()  { return MAP_WIDTH; }
    @Override public String         getMapId()     { return "xxxId";   }
    @Override public String         getMapName()   { return "地圖名稱"; }
    @Override public int            getMinLevel()  { return X;         }
}
```
