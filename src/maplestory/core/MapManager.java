package maplestory.core;

import maplestory.entity.Player;
import maplestory.map.BaseMap;
import maplestory.map.GameMap;
import maplestory.map.Portal;
import maplestory.map.VillageMap;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理所有地圖的切換邏輯。
 * GamePanel 透過 MapManager 取得目前地圖，不直接持有地圖參考。
 */
public class MapManager {

    private final Map<String, BaseMap> maps = new HashMap<>();
    private BaseMap currentMap;

    // 傳送門冷卻：進入傳送門後 2 秒內不能再觸發，避免反覆切換
    private double portalCooldown = 0;

    // 供 GamePanel 判斷是否剛切換地圖（需要重置鏡頭等）
    private boolean justSwitched = false;

    public MapManager() {
        maps.put("village", new VillageMap());
        maps.put("battle",  new GameMap());
        currentMap = maps.get("village"); // ← 遊戲從村莊開始
    }

    /** 每幀更新：傳送門動畫 + 碰撞偵測 */
    public void update(double dt, Player player) {
        justSwitched   = false;
        portalCooldown = Math.max(0, portalCooldown - dt);

        // 更新目前地圖內的傳送門動畫
        for (Portal p : currentMap.getPortals()) {
            p.update(dt);
        }

        // 偵測玩家是否踩到傳送門
        if (portalCooldown <= 0) {
            for (Portal p : currentMap.getPortals()) {
                if (p.collidesWith(player)) {
                    switchMap(p.getTargetMapId(),
                              p.getSpawnX(),
                              p.getSpawnY(), player);
                    portalCooldown = 2.0;
                    break;
                }
            }
        }
    }

    /** 切換地圖並設定玩家出生點 */
    public void switchMap(String mapId, double spawnX, double spawnY, Player player) {
        BaseMap target = maps.get(mapId);
        if (target != null && target != currentMap) {
            currentMap   = target;
            player.setPosition(spawnX, spawnY);
            justSwitched = true;
        }
    }

    public BaseMap  getCurrentMap()          { return currentMap; }
    public boolean  isOnMap(String mapId)    { return mapId.equals(currentMap.getMapId()); }
    public boolean  justSwitched()           { return justSwitched; }
}
