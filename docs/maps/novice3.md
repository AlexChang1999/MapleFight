<!-- Context: Part of [Map Connections](map-connections.md) -->
<!-- Domain: #game/maps -->

### 3-4. 新手森林三區（NoviceMap3 / `"novice3"`）

| 屬性 | 數值 |
|---|---|
| 檔案 | `NoviceMap3.java` |
| `MAP_WIDTH` | `2200` |
| `getMinLevel()` | `1`（左） / `10`（右） |
| 地圖類型 | 戰鬥（怪物：野豬 ×2 + 蝙蝠 ×2） |
| 視覺風格 | 傍晚橘色林，四梯 |

**地面**
```
Platform(0, GROUND_Y, 2200, 40)  Color(72,148,50)
```

**中層浮台（7 段，高度 -135 / -170 / -150 / -182 / -155 / -178 / -152）**
```
Platform( 118, GROUND_Y-135, 168, 18)  Color(148,102,54)
Platform( 402, GROUND_Y-170, 188, 18)  Color(112, 78,40)
Platform( 702, GROUND_Y-150, 168, 18)  Color(148,102,54)
Platform(1005, GROUND_Y-182, 195, 18)  Color(112, 78,40)
Platform(1305, GROUND_Y-155, 178, 18)  Color(148,102,54)
Platform(1605, GROUND_Y-178, 182, 18)  Color(112, 78,40)
Platform(1905, GROUND_Y-152, 180, 18)  Color(148,102,54)
```

**高層浮台（6 段，高度 -288 / -312 / -298 / -322 / -308 / -292）**
```
Platform( 242, GROUND_Y-288, 155, 18)  Color(96,66,33)
Platform( 562, GROUND_Y-312, 162, 18)  Color(89,61,31)
Platform( 870, GROUND_Y-298, 170, 18)  Color(96,66,33)
Platform(1162, GROUND_Y-322, 168, 18)  Color(89,61,31)
Platform(1482, GROUND_Y-308, 175, 18)  Color(96,66,33)
Platform(1782, GROUND_Y-292, 162, 18)  Color(89,61,31)
```

**梯子（地面→中層，4 座）**
```
Ladder( 138, GROUND_Y-135, GROUND_Y)
Ladder( 720, GROUND_Y-150, GROUND_Y)
Ladder(1322, GROUND_Y-155, GROUND_Y)
Ladder(1922, GROUND_Y-152, GROUND_Y)
```

**NPC** 無

**傳送門**
```
左 x=22        → "novice2"   目標(NoviceMap2.MAP_WIDTH-68, GROUND_Y-80)  Lv.1
右 x=MAP_W-68  → "frontier"  目標(80, GROUND_Y-80)                       Lv.10
```

**天空漸層**  `(252,168,88)` → `(255,208,138)`（傍晚橘紅）+ 淡橘雲 parallax 0.12

**視差層**
- 夕陽山丘：parallax 0.20，`Color(138,98,58, a=195)`
- 樹木剪影：parallax 0.50，`Color(62,48,32, a=158)`
