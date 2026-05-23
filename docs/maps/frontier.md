<!-- Context: Part of [Map Connections](map-connections.md) -->
<!-- Domain: #game/maps -->

### 3-5. 前線前哨站（FrontierTown / `"frontier"`）

| 屬性 | 數值 |
|---|---|
| 檔案 | `FrontierTown.java` |
| `MAP_WIDTH` | `1600` |
| `getMinLevel()` | `10` |
| 地圖類型 | 城鎮（有 NPC，無怪物） |
| 視覺風格 | 黃昏橙天、石頭哨崗 |

**地面**
```
Platform(0, GROUND_Y, 1600, 40)  Color(115,85,50)  // 泥土褐
```

**浮台**
```
Platform( 580, GROUND_Y-110, 200, 16)  Color(120,110,95)  // stone 中台
Platform( 380, GROUND_Y-190, 160, 16)  Color(100, 90,75)  // stone 高台左
Platform( 780, GROUND_Y-195, 160, 16)  Color(100, 90,75)  // stone 高台右
Platform(1140, GROUND_Y-125, 180, 16)  Color(120,110,95)  // 右側中台
Platform(1300, GROUND_Y-215, 150, 16)  Color(100, 90,75)  // 右側高台
Platform( 200, GROUND_Y-28,   50, 12)  Color(140,110,70)  // 裝飾台
Platform( 900, GROUND_Y-28,   50, 12)  Color(140,110,70)  // 裝飾台
```

**梯子** 無

**NPC**
| x | 名稱 | 顏色 | 功能 |
|---|---|---|---|
| 320 | 前哨長官 | (180,60,50) | dialogue: `"frontier_elder"` |
| 680 | 補給商人 | (60,180,140) | shop: `"item"` |
| 1100 | 武器鍛造師 | (180,130,50) | shop: `"weapon"` |

**傳送門**
```
左 x=22        → "novice3"  目標(NoviceMap3.MAP_WIDTH-68, GROUND_Y-80)  Lv.1
右 x=MAP_W-68  → "battle"   目標(150, GROUND_Y-80)                      Lv.10
```

**天空漸層**  `(210,140,70)` → `(240,190,120)`（黃昏橙）

**視差層**
- 遠山輪廓：parallax 0.20，`Color(120,80,45, a=200)`
- 哨塔剪影：parallax 0.42，`Color(60,45,28, a=185)`
- 旗幟：parallax 同哨塔，`Color(200,50,40, a=160)`
