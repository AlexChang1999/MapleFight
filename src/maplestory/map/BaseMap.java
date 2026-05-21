package maplestory.map;

import maplestory.core.Camera;
import maplestory.entity.NPC;

import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * 所有地圖的抽象基底類別。
 * VillageMap、GameMap 都繼承這個，GamePanel 只跟 BaseMap 打交道。
 */
public abstract class BaseMap {

    /** 每幀更新（傳送門動畫、NPC 更新等） */
    public abstract void update(double dt);

    /** 繪製整張地圖（背景 + 地形 + 裝飾物） */
    public abstract void draw(Graphics2D g, Camera camera);

    /** 供碰撞偵測使用的平台列表 */
    public abstract List<Platform> getPlatforms();

    /** 地圖寬度（像素），供 Camera 限制邊界 */
    public abstract int getMapWidth();

    /** 地圖識別 ID（"village" / "battle"） */
    public abstract String getMapId();

    /** 地圖上的傳送門（預設空，子類覆寫） */
    public List<Portal> getPortals() { return Collections.emptyList(); }

    /** 地圖上的 NPC（預設空，子類覆寫） */
    public List<NPC> getNPCs() { return Collections.emptyList(); }

    /** 地圖上的梯子（預設空，子類覆寫） */
    public List<Ladder> getLadders() { return Collections.emptyList(); }

    /** 地圖顯示名稱（中文），預設回傳 mapId，子類覆寫提供正確名稱 */
    public String getMapName() { return getMapId(); }

    /** 進入此地圖所需最低等級（預設 1，即無限制） */
    public int getMinLevel() { return 1; }
}
