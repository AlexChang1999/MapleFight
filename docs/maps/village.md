<!-- Context: Part of [Map Connections](map-connections.md) -->
<!-- Domain: #game/maps -->

### 3-1. 新手村（VillageMap / `"village"`）

| 屬性 | 數值 |
|---|---|
| 檔案 | `VillageMap.java` |
| `MAP_WIDTH` | `1600` |
| `getMinLevel()` | `1` |
| 地圖類型 | 城鎮（有 NPC，無怪物） |
| 視覺風格 | 溫暖早晨，藍天白雲 |

**地面**
```
Platform(0, groundY, 1600, 40)  Color(80, 145, 60)  // 草地綠
```

**浮台（裝飾台階）**
```
Platform(455, groundY-28, 50, 12)  Color(160,130,90)  // 道具店前
Platform(840, groundY-28, 55, 12)  Color(160,130,90)  // 武器店前
```

**梯子** 無

**NPC**
| x | 名稱 | 顏色 | 功能 |
|---|---|---|---|
| 260 | 村長老人 | (230,150,60) | dialogue: `"elder"` |
| 590 | 道具商人 | (60,200,180) | shop: `"item"` |
| 990 | 武器鐵匠 | (210,80,80) | shop: `"weapon"` |
| 1195 | 劍士師傅 | (180,50,50) | dialogue: `"job_warrior"` |
| 1265 | 法師師傅 | (90,60,200) | dialogue: `"job_mage"` |
| 1335 | 弓手師傅 | (50,170,80) | dialogue: `"job_archer"` |

**傳送門**
```
左 x=22   → "novice1"  目標(MAP_WIDTH-130, groundY-80)  Lv.1
右 x=1510 → "battle"   目標(150, 300)                  Lv.10
```

**天空漸層**  `(150,210,255)` → `(220,240,255)`（早晨藍天）

**視差層**
- 遠山：parallax 0.2，`Color(140,190,130)`
- 近草坡：parallax 0.5，`Color(100,170,80)`
- 花朵裝飾：無視差（世界座標）
- 建築：無視差（世界座標）
