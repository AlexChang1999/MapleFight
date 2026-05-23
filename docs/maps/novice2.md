<!-- Context: Part of [Map Connections](map-connections.md) -->
<!-- Domain: #game/maps -->

### 3-3. 新手森林二區（NoviceMap2 / `"novice2"`）

| 屬性 | 數值 |
|---|---|
| 檔案 | `NoviceMap2.java` |
| `MAP_WIDTH` | `2000` |
| `getMinLevel()` | `1`（左） / `6`（右） |
| 地圖類型 | 戰鬥（怪物：史萊姆 ×2 + 蝙蝠 ×2） |
| 視覺風格 | 林木漸密，光線偏暗 |

**地面**
```
Platform(0, GROUND_Y, 2000, 40)  Color(58,132,48)  // 深草綠
```

**中層浮台（GROUND_Y - 132 / -165 交錯，6 段）**
```
Platform( 148, GROUND_Y-132, 172, 18)  Color(128, 92,50)
Platform( 455, GROUND_Y-165, 188, 18)  Color(104, 72,40)
Platform( 775, GROUND_Y-145, 168, 18)  Color(128, 92,50)
Platform(1075, GROUND_Y-175, 198, 18)  Color(104, 72,40)
Platform(1375, GROUND_Y-150, 182, 18)  Color(128, 92,50)
Platform(1695, GROUND_Y-172, 180, 18)  Color(104, 72,40)
```

**高層浮台（GROUND_Y - 272 / -298 / -312 / -302 / -288，5 段）**
```
Platform( 295, GROUND_Y-272, 152, 18)  Color(92,62,32)
Platform( 638, GROUND_Y-298, 158, 18)  Color(86,58,30)
Platform( 998, GROUND_Y-312, 165, 18)  Color(92,62,32)
Platform(1518, GROUND_Y-302, 170, 18)  Color(86,58,30)
Platform(1838, GROUND_Y-288, 155, 18)  Color(92,62,32)
```

**梯子（地面→中層，3 座）**
```
Ladder(166,  GROUND_Y-132, GROUND_Y)
Ladder(795,  GROUND_Y-145, GROUND_Y)
Ladder(1395, GROUND_Y-150, GROUND_Y)
```

**NPC** 無

**傳送門**
```
左 x=22        → "novice1"  目標(NoviceMap1.MAP_WIDTH-68, GROUND_Y-80)  Lv.1
右 x=MAP_W-68  → "novice3"  目標(70, GROUND_Y-80)                       Lv.6
```

**天空漸層**  `(98,172,238)` → `(172,218,252)`（稍暗藍天，無雲）

**視差層**
- 遠山：parallax 0.20，`Color(98,158,96)`
- 中景樹影：parallax 0.50，`Color(48,115,52, a=145)`
