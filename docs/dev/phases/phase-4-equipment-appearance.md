<!-- Context: Part of [Development Roadmap](../completed-systems.md) -->
<!-- Domain: #game/dev -->

### Phase 4 — 裝備外觀疊加在角色身上

**目標：** 穿上裝備在火柴人身上可見

**修改檔案：**
- `entity/Player.java draw()` — 依裝備欄位疊加圖形：
  - WEAPON → 右手旁畫短劍/法杖/弓形（依職業判斷）
  - HELMET → 頭頂加半弧頭盔形
  - TOP → 身體矩形改色（裝備 displayColor）
  - BOTTOM → 腿部矩形改色
  - GLOVES → 手末端加小圓
  - BOOTS → 腳末端加底座矩形
  - CAPE → 背後弧形披風
- `item/Equipment.java` — `displayColor` 由稀有度底色 + 裝備類型色相偏移計算
