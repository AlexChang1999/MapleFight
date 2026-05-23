<!-- Context: Part of [Map Design Specifications](basemap-interface.md) -->
<!-- Domain: #game/maps -->

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
