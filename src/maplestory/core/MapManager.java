package maplestory.core;

import maplestory.audio.SFX;
import maplestory.audio.SoundManager;
import maplestory.entity.Player;
import maplestory.map.ArcticMap;
import maplestory.map.BaseMap;
import maplestory.map.FrontierTown;
import maplestory.map.GameMap;
import maplestory.map.IcePostTown;
import maplestory.map.NoviceMap1;
import maplestory.map.NoviceMap2;
import maplestory.map.NoviceMap3;
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

    // 等級不足被擋下時的通知文字（由 GamePanel 讀取顯示）
    private String  levelBlockedNotice  = null;
    private double  levelBlockedTimer   = 0;

    public MapManager() {
        maps.put("village",  new VillageMap());
        maps.put("novice1",  new NoviceMap1());
        maps.put("novice2",  new NoviceMap2());
        maps.put("novice3",  new NoviceMap3());
        maps.put("frontier", new FrontierTown());
        maps.put("battle",   new GameMap());
        maps.put("icepost",  new IcePostTown());
        maps.put("arctic",   new ArcticMap());
        currentMap = maps.get("village");
        SoundManager.get().playBGM("village");
    }

    /** 每幀更新：傳送門動畫 + 碰撞偵測 */
    public void update(double dt, Player player) {
        justSwitched   = false;
        portalCooldown = Math.max(0, portalCooldown - dt);

        // 更新目前地圖內的傳送門動畫
        for (Portal p : currentMap.getPortals()) {
            p.update(dt);
        }

        // 等級封鎖通知計時
        if (levelBlockedTimer > 0) levelBlockedTimer -= dt;
        if (levelBlockedTimer <= 0) levelBlockedNotice = null;

        // 偵測玩家是否踩到傳送門
        if (portalCooldown <= 0) {
            for (Portal p : currentMap.getPortals()) {
                if (p.collidesWith(player)) {
                    if (player.getLevel() < p.getMinLevel()) {
                        // 等級不足：顯示提示，不切換
                        levelBlockedNotice = "等級不足！需要 Lv." + p.getMinLevel()
                                           + "（目前 Lv." + player.getLevel() + "）";
                        levelBlockedTimer  = 2.5;
                        portalCooldown     = 1.5; // 短冷卻避免重複觸發
                    } else {
                        switchMap(p.getTargetMapId(),
                                  p.getSpawnX(),
                                  p.getSpawnY(), player);
                        portalCooldown = 2.0;
                    }
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
            SoundManager.get().playSFX(SFX.PORTAL);
            SoundManager.get().playBGM(mapId);
        }
    }


    /** 根據當前地圖 ID 回傳對應的村莊 mapId（回家卷軸使用） */
    public String getHomeMapId(String currentMapId) {
        return switch (currentMapId) {
            case "novice1", "novice2", "novice3" -> "village";
            case "battle"                        -> "frontier";
            case "arctic"                        -> "icepost";
            default                              -> currentMapId; // 已在村莊
        };
    }

    /** 取得指定地圖的寬度（村莊卷軸計算中央出生點用） */
    public int getMapWidth(String mapId) {
        BaseMap m = maps.get(mapId);
        return m != null ? m.getMapWidth() : 800;
    }
    public BaseMap  getCurrentMap()          { return currentMap; }
    public boolean  isOnMap(String mapId)    { return mapId.equals(currentMap.getMapId()); }
    public boolean  justSwitched()           { return justSwitched; }

    /** 若等級不足被擋下，回傳通知文字；否則回傳 null */
    public String   getLevelBlockedNotice()  { return levelBlockedTimer > 0 ? levelBlockedNotice : null; }
}
