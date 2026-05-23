# MapleFight 地圖設計規格書

> 本文件是所有地圖的完整設計基準，新地圖必須以此作為唯一參考。  
> 引擎：純 Java + Swing 2D，邏輯解析度 **800 × 580**（SCREEN_WIDTH × GAME_HEIGHT）。

---

## 1. 全域常數

| 常數 | 數值 | 說明 |
|---|---|---|
| `GamePanel.SCREEN_WIDTH` | `800` | 螢幕寬度（像素） |
| `GamePanel.GAME_HEIGHT` | `580` | 遊戲畫面高度（像素） |
| `groundY` | `GAME_HEIGHT - 40 = 540` | 所有地圖的地面 Y 座標（固定） |
| 玩家 WIDTH | `24` | 碰撞寬度（Player.java） |
| 玩家 HEIGHT | `58` | 碰撞高度（Player.java） |
| `Portal.HEIGHT` | 依 Portal.java | 傳送門高度（建議 40px） |
| `NPC.HEIGHT` | 依 NPC.java | NPC 高度（建議 50px） |

---

## 2. 地圖連線結構

```
新手村(village)
  ├─ 左 → 新手森林一區(novice1)
  └─ 右 → 冒險平原(battle) [Lv.10]

新手森林一區(novice1) ──→ 新手森林二區(novice2) [Lv.3]
新手森林二區(novice2) ──→ 新手森林三區(novice3) [Lv.6]
新手森林三區(novice3) ──→ 前線前哨站(frontier)  [Lv.10]

前線前哨站(frontier)  ──→ 冒險平原(battle)      [Lv.10]
冒險平原(battle)      ──→ 冰原驛站(icepost)      [Lv.15]
冰原驛站(icepost)     ──→ 極地冰原(arctic)       [Lv.15]
極地冰原(arctic)      ──→ 古老森林(forest)        [Lv.20]  ← 待建立
```

---

## 3. 所有現有地圖一覽

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

---

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

---

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

---

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

---

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

---

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

---

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

---

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

---

## 4. BaseMap 介面規格

所有地圖**必須繼承 `BaseMap`** 並實作以下方法：

```java
// 必須實作
public abstract void           update(double dt);
public abstract void           draw(Graphics2D g, Camera camera);
public abstract List<Platform> getPlatforms();
public abstract int            getMapWidth();
public abstract String         getMapId();

// 選擇性覆寫（預設回傳空或 1）
public List<Portal>  getPortals()   // 預設 emptyList
public List<NPC>     getNPCs()      // 預設 emptyList
public List<Ladder>  getLadders()   // 預設 emptyList
public String        getMapName()   // 預設回傳 mapId
public int           getMinLevel()  // 預設 1
```

---

## 5. 碰撞規則（Player.java:240-247，勿修改）

```java
if (x + WIDTH <= p.getX() || x >= p.getX() + p.getWidth()) continue;
double feet     = y + HEIGHT;
double prevFeet = feet - velY * dt;
if (prevFeet <= p.getY() && feet >= p.getY() && velY >= 0) {
    y = p.getY() - HEIGHT; velY = 0; onGround = true;
}
```

**關鍵限制：**
1. **地面不可有缺口**：缺口 > 玩家寬度（24px）會導致玩家掉落重生。所有地面一律用一條 `Platform(0, gY, MAP_WIDTH, 40)` 處理。
2. **梯子 botY 必須等於下方平台 Y**：差距 ≥ 1px 會讓玩家穿台或懸空。
3. **梯子 x 必須在平台範圍內**：`platform.x ≤ ladder.x ≤ platform.x + platform.width`。

---

## 6. 新地圖建立規範

### 6-1. 檔案結構模板

```java
package maplestory.map;

import maplestory.core.Camera;
import maplestory.core.GamePanel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 地圖名稱（mapId）
 * 推薦等級：Lv X~Y
 * 怪物：...
 * 特色：...
 *
 * 傳送門：
 *   左側 → 上一張地圖
 *   右側 → 下一張地圖（Lv.X）
 */
public class XxxMap extends BaseMap {

    public static final int MAP_WIDTH = ?;          // 參考下方規格
    private static final int GROUND_Y = GamePanel.GAME_HEIGHT - 40;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Portal>   portals   = new ArrayList<>();
    private final List<Ladder>   ladders   = new ArrayList<>();
    // 城鎮地圖加上：
    // private final List<NPC> npcs = new ArrayList<>();

    public XxxMap() {
        buildPlatforms();
        buildLadders();   // 若有梯子
        buildPortals();
        // buildNPCs();   // 若為城鎮
    }

    private void buildPlatforms() {
        // 1. 地面：一定要一整條
        platforms.add(new Platform(0, GROUND_Y, MAP_WIDTH, 40, groundColor));
        // 2. 中層浮台（厚度固定 18px）
        // 3. 高層浮台（厚度固定 18px）
    }

    private void buildLadders() {
        // topY = 平台 Y，botY = 下方平台 Y（必須完全對齊）
        // x 必須落在目標平台 x 範圍內
    }

    private void buildPortals() {
        portals.add(new Portal(22, GROUND_Y - Portal.HEIGHT,
            "prevMapId", prevMap.MAP_WIDTH - 68, GROUND_Y - 80,
            "回上一區", 1));
        portals.add(new Portal(MAP_WIDTH - 68, GROUND_Y - Portal.HEIGHT,
            "nextMapId", 80, GROUND_Y - 80,
            "下一區(Lv.X)", X));
    }

    @Override public void update(double dt) { /* 靜態地圖 */ }

    @Override
    public void draw(Graphics2D g, Camera camera) {
        drawSky(g);
        drawBackground(g, camera);
        for (Platform p : platforms) p.draw(g, camera);
        for (Ladder   l : ladders)   l.draw(g, camera);
        for (Portal   p : portals)   p.draw(g, camera);
    }

    // drawSky / drawBackground ...

    @Override public List<Platform> getPlatforms() { return platforms; }
    @Override public List<Portal>   getPortals()   { return portals;   }
    @Override public List<Ladder>   getLadders()   { return ladders;   }
    @Override public int            getMapWidth()  { return MAP_WIDTH; }
    @Override public String         getMapId()     { return "xxxId";   }
    @Override public String         getMapName()   { return "地圖名稱"; }
    @Override public int            getMinLevel()  { return X;         }
}
```

### 6-2. MAP_WIDTH 選擇建議

| 地圖類型 | 建議寬度 |
|---|---|
| 城鎮 / 中繼站 | 1600 px |
| 小型戰鬥地圖（初期） | 1800 px |
| 中型戰鬥地圖 | 2000~2200 px |
| 大型戰鬥地圖（後期） | 2400 px |

### 6-3. 平台層次設計準則

| 層次 | Y 基準（建議） | 平台厚度 | 顏色深度 |
|---|---|---|---|
| 地面 | `gY = GAME_HEIGHT - 40` | `40` | 最亮（草、冰、泥） |
| 中層 | `gY - 110` 到 `gY - 200`（各地圖自訂） | `18` | 中等 |
| 高層 | 中層 - 130 到 - 160 | `18` | 最暗 |
| 裝飾台（城鎮） | `gY - 28` | `12` | 同地面色系 |

### 6-4. 視差層比例慣例

| 層次 | parallaxFactor | 說明 |
|---|---|---|
| 星星 / 固定裝飾 | `0.0` | 最遠，不移動 |
| 遠山 / 遠景 | `0.1~0.2` | 緩慢移動 |
| 雲朵 | `0.12~0.15` | 非常緩慢 |
| 中景樹木 / 建築剪影 | `0.4~0.5` | 中速 |
| 地面裝飾 | `1.0`（世界座標） | 與地圖同速 |

### 6-5. 天空色彩主題參考

| 主題 | 頂部色 | 底部色 | 適用地圖 |
|---|---|---|---|
| 晴天 | (118~150, 198~210, 255) | (192~220, 232~240, 255) | 新手森林一區、新手村 |
| 偏暗晴天 | (98,172,238) | (172,218,252) | 新手森林二區 |
| 傍晚橘 | (252,168,88) | (255,208,138) | 新手森林三區 |
| 黃昏橙 | (210,140,70) | (240,190,120) | 前線前哨站 |
| 夜空（冰原） | (5~18, 10~28, 30~68) | (15~38, 35~58, 70~112) | 冰原驛站、極地冰原 |

### 6-6. 新地圖加入 MapManager 的步驟

1. 建立 `XxxMap.java`（繼承 `BaseMap`）
2. 確認 `mapId` 字串唯一（全域搜尋確認無衝突）
3. 在 `MapManager.java`（或對應的地圖工廠）中加入實例化邏輯
4. 確認上一張地圖的右側傳送門 `targetMapId` 正確指向新地圖
5. 確認新地圖左側傳送門目標座標落在上一張地圖的安全地面

---

## 7. 常見 Bug 防範清單

建立新地圖時，完成後自我檢查以下項目：

- [ ] 地面是否為一整條 `Platform(0, gY, MAP_WIDTH, 40, ...)`？（不得分段）
- [ ] 所有梯子的 `botY` 是否與下方平台的 `Y` 值**完全相等**（無差距）？
- [ ] 所有梯子的 `x` 是否落在目標平台的 `[x, x+width]` 範圍內？
- [ ] 左右傳送門的 `targetX / targetY` 是否落在對方地圖的地面上（不會空中重生）？
- [ ] `getMapId()` 回傳的 ID 是否與傳送門中使用的字串一致？
- [ ] 高層浮台是否有足夠的水平空間讓玩家（WIDTH=24）站上？（平台寬度 ≥ 30px 建議值）
- [ ] `getMinLevel()` 是否與傳送門 `minLevel` 設定一致？
