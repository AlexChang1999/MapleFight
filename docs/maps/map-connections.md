<!-- Context: Part of [Map Design Specifications](global-constants.md) -->
<!-- Domain: #game/maps -->

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
