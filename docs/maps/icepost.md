<!-- Context: Part of [Map Connections](map-connections.md) -->
<!-- Domain: #game/maps -->

### 3-7. 冰原驛站（IcePostTown / `"icepost"`）

| 屬性 | 數值 |
|---|---|
| 檔案 | `IcePostTown.java` |
| `MAP_WIDTH` | `1600` |
| `getMinLevel()` | `15` |
| 地圖類型 | 城鎮（有 NPC，雪粒子） |
| 視覺風格 | 深藍冰夜、靜態極光色帶 |

**地面**
```
Platform(0, GROUND_Y, 1600, 40)  Color(140,190,220)  // 冰藍
```

**中層冰台**
```
Platform( 420, GROUND_Y-100, 210, 16)  Color(200,225,245)  // snow
Platform( 820, GROUND_Y-115, 190, 16)  Color(200,225,245)
Platform(1160, GROUND_Y-100, 200, 16)  Color(200,225,245)
```

**高層石台**
```
Platform(550, GROUND_Y-200, 155, 16)  Color(110,130,155)  // stone
Platform(960, GROUND_Y-210, 155, 16)  Color(110,130,155)
```

**裝飾小台**
```
Platform(220,  GROUND_Y-28, 55, 12)  Color(160,195,225)
Platform(1020, GROUND_Y-28, 55, 12)  Color(160,195,225)
```

**梯子** 無

**NPC**
| x | 名稱 | 顏色 | 功能 |
|---|---|---|---|
| 560 | 冰原補給商 | (80,160,200) | shop: `"item"` |
| 1050 | 極地鐵匠 | (130,160,190) | shop: `"weapon"` |

**傳送門**
```
左 x=22        → "battle"  目標(GameMap.MAP_WIDTH-68, GROUND_Y-80)  Lv.1
右 x=MAP_W-68  → "arctic"  目標(120, GROUND_Y-90)                   Lv.15
```

**天空漸層**  `(18,28,68)` → `(38,58,112)`（深藍冰夜）

**極光（靜態色帶）**
```
Color(50,180,140, a=40)  y=30  h=60
Color(80,140,200, a=30)  y=70  h=50
```

**雪粒子**
- 數量：40，種子：42
- 速度：18~40，漂移：±6
- 視差：parallax 0.05（幾乎不隨鏡頭移動）

**視差層**
- 冰山輪廓：parallax 0.18，`Color(80,110,160, a=200)`
