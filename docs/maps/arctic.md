<!-- Context: Part of [Map Connections](map-connections.md) -->
<!-- Domain: #game/maps -->

### 3-8. 極地冰原（ArcticMap / `"arctic"`）

| 屬性 | 數值 |
|---|---|
| 檔案 | `ArcticMap.java` |
| `MAP_WIDTH` | `2400` |
| `getMinLevel()` | `15` |
| 地圖類型 | 戰鬥（冰系怪物：冰晶史萊姆、極地熊、冰蝠） |
| 視覺風格 | 極夜、動態極光、雪粒子系統 |

**Y 軸基準**
```java
int gY   = GamePanel.GAME_HEIGHT - 40;   // 地面 = 540
int midY = gY - 130;                      // 中層 = 410
int hiY  = gY - 270;                      // 高層 = 270
```

**地面（完整單條）**
```
Platform(0, gY, 2400, 40)  Color(160,210,240)  // iceGround
```

**中層浮台（midY / midY-30 交錯，8 段）**
```
Platform( 200, midY,      180, 18)  Color(100,170,220)  // iceMid
Platform( 480, midY-30,   180, 18)
Platform( 780, midY,      200, 18)
Platform(1060, midY-30,   170, 18)
Platform(1320, midY,      200, 18)
Platform(1620, midY-30,   180, 18)
Platform(1900, midY,      200, 18)
Platform(2160, midY-30,   200, 18)
```

**高層浮台（hiY / hiY-30 交錯，6 段）**
```
Platform( 350, hiY,      160, 18)  Color(65,130,190)  // iceHigh
Platform( 680, hiY-30,   160, 18)
Platform(1000, hiY,      170, 18)
Platform(1380, hiY-30,   160, 18)
Platform(1750, hiY,      170, 18)
Platform(2100, hiY-30,   200, 18)
```

**梯子**  
顏色：柱 `Color(130,195,240)`，橫檔 `Color(100,170,220)`

```
// 地面 → 中層（4 座）
Ladder( 330, midY,      gY,        ...)  // 對應中層 [200,380] midY
Ladder( 900, midY,      gY,        ...)  // 對應中層 [780,980] midY
Ladder(1450, midY,      gY,        ...)  // 對應中層 [1320,1520] midY
Ladder(2050, midY,      gY,        ...)  // 對應中層 [1900,2100] midY

// 中層 → 高層（4 座）——botY 必須與下方平台 Y 完全吻合
Ladder( 510, hiY, midY-30, ...)  // ✅ x=510 在 [480,660]，平台 Y=midY-30
Ladder(1060, hiY, midY-30, ...)  // ✅ x=1060 在 [1060,1230]，平台 Y=midY-30
Ladder(1660, hiY, midY-30, ...)  // ✅ x=1660 在 [1620,1800]，平台 Y=midY-30
Ladder(2180, hiY, midY-30, ...)  // ✅ x=2180 在 [2160,2360]，平台 Y=midY-30
```

**NPC** 無

**傳送門**
```
左 x=30        → "icepost"  目標(IcePostTown.MAP_WIDTH-68, gY-80)  Lv.1
右 x=MAP_W-68  → "forest"   目標(60, gY-80)                        Lv.20
```

**天空**  
- 夜空漸層：`(5,10,30)` → `(15,35,70)`  
- 星星：20 顆，固定位置（無視差，最遠層）  
- 極光（動態，3 條正弦波帶）：
  ```
  Color(30,200,120, a=45)  freq=0.008  amp=30  yBase=80   parallax=0.1
  Color(20,180,200, a=35)  freq=0.006  amp=25  yBase=120
  Color(120,60,220, a=30)  freq=0.010  amp=20  yBase=100
  ```
- 遠景冰山：parallax 0.2，`Color(30,70,120, a=200)`  
- 近景冰山：parallax 0.5，`Color(20,55,100, a=220)`

**雪粒子**
- 數量：80，種子：42
- 下落速度：40~100，大小：1~3px，漂移：±10
- 視差：直接對應世界座標（不縮減）
