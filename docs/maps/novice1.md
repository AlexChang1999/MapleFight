<!-- Context: Part of [Map Connections](map-connections.md) -->
<!-- Domain: #game/maps -->

### 3-2. 新手森林一區（NoviceMap1 / `"novice1"`）

| 屬性 | 數值 |
|---|---|
| 檔案 | `NoviceMap1.java` |
| `MAP_WIDTH` | `1800` |
| `getMinLevel()` | `1` |
| 地圖類型 | 戰鬥（怪物：史萊姆 ×3） |
| 視覺風格 | 明亮晴天森林 |

**地面**
```
Platform(0, GROUND_Y, 1800, 40)  Color(70,155,55)  // 鮮草綠
```

**中層浮台（GROUND_Y - 118 / -155 交錯）**
```
Platform( 200, GROUND_Y-118, 165, 18)  Color(145,102,56)  // wood
Platform( 510, GROUND_Y-155, 175, 18)  Color(118, 82,44)  // dkWood
Platform( 870, GROUND_Y-132, 158, 18)  Color(145,102,56)
Platform(1190, GROUND_Y-162, 170, 18)  Color(118, 82,44)
Platform(1540, GROUND_Y-128, 162, 18)  Color(145,102,56)
```

**高層浮台（GROUND_Y - 252 / -272 / -282 / -265）**
```
Platform( 345, GROUND_Y-252, 142, 18)  Color(108,74,36)
Platform( 710, GROUND_Y-272, 148, 18)  Color( 98,66,32)
Platform(1040, GROUND_Y-282, 155, 18)  Color(108,74,36)
Platform(1380, GROUND_Y-265, 148, 18)  Color( 98,66,32)
```

**梯子（地面→中層）**
```
Ladder(218, GROUND_Y-118, GROUND_Y)
Ladder(888, GROUND_Y-132, GROUND_Y)
```

**NPC** 無

**傳送門**
```
左 x=22        → "village"  目標(80, GROUND_Y-80)               Lv.1
右 x=MAP_W-68  → "novice2"  目標(70, GROUND_Y-80)               Lv.3
```

**天空漸層**  `(118,198,255)` → `(192,232,255)`（晴天藍）+ 白雲 parallax 0.15

**視差層**
- 遠山：parallax 0.20，`Color(132,188,118)`
- 近草坡：parallax 0.45，`Color(88,155,75, a=165)`
