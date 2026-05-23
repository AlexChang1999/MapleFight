<!-- Context: Part of [Map Connections](map-connections.md) -->
<!-- Domain: #game/maps -->

### 3-6. 冒險平原（GameMap / `"battle"`）

| 屬性 | 數值 |
|---|---|
| 檔案 | `GameMap.java` |
| `MAP_WIDTH` | `2000` |
| `getMinLevel()` | `10` |
| 地圖類型 | 戰鬥（主要戰場） |
| 視覺風格 | 日間草原，多層高台 |

**地面**
```
Platform(0, gY, 2000, 40)  Color(80,140,60)  // 草地綠
```

**中層浮台（gY - 110~200，7 段）**
```
Platform( 150, gY-110, 180, 18)  Color(140,100,55)
Platform( 420, gY-160, 200, 18)  Color(130, 90,50)
Platform( 700, gY-125, 170, 18)  Color(140,100,55)
Platform( 970, gY-180, 210, 18)  Color(130, 90,50)
Platform(1230, gY-135, 190, 18)  Color(140,100,55)
Platform(1500, gY-200, 200, 18)  Color(130, 90,50)
Platform(1760, gY-130, 170, 18)  Color(140,100,55)
```

**高層浮台（gY - 260~340，5 段）**
```
Platform( 300, gY-260, 150, 18)  Color(110,70,35)
Platform( 630, gY-290, 160, 18)  Color(100,65,30)
Platform(1060, gY-320, 175, 18)  Color(110,70,35)
Platform(1380, gY-310, 160, 18)  Color(100,65,30)
Platform(1680, gY-340, 175, 18)  Color(110,70,35)
```

**梯子** 無

**NPC** 無

**傳送門**
```
左 x=30        → "frontier"  目標(FrontierTown.MAP_WIDTH-68, groundY-80)  Lv.1
右 x=MAP_W-68  → "icepost"   目標(80, groundY-80)                         Lv.15
```

**天空漸層**  無明確色帶（用視差山丘覆蓋整個背景）

**視差層**
- 遠山：parallax 0.20，`Color(120,170,110)`，橢圓山
- 近山：parallax 0.40，`Color(90,150,80)`，橢圓山
- 雲朵：parallax 0.15，`Color(255,255,255, a=180)`
