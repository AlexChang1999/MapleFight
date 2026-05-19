# MapleFight 開發存檔點
> 最後更新：2026-05-19｜GitHub: https://github.com/AlexChang1999/MapleFight.git

---

## 1. ✅ 已完成（Phase 1–6）

- **Phase 1**：視窗 + 60FPS 遊戲迴圈（`GameWindow`, `GamePanel`, `Main`）
- **Phase 2**：火柴人玩家 + 鍵盤移動（← →）、跳躍（Space）、攻擊（Z）（`Player`, `InputHandler`）
- **Phase 3**：地圖平台 + 從上方落下的碰撞偵測（`GameMap`, `Platform`）
- **Phase 4**：平滑鏡頭 Lerp 卷軸，玩家維持畫面左 1/3（`Camera`）
- **Phase 5**：怪物 AI（追擊、近身攻擊、HP 條、受傷閃紅、死亡擴散動畫）（`Monster`）
- **Phase 6**：底部 HUD（HP/MP/EXP 條）+ S/K/E 鍵切換狀態/技能/裝備面板（`StatusPanel`, `SkillPanel`, `EquipPanel`）

---

## 2. ⏳ 待辦 / 下次繼續（Phase 7–16）

### 🆕 新增功能（Phase 7–12）
| Phase | 任務 | 關鍵檔案 |
|-------|------|---------|
| 7 | 地圖系統：`MapManager`（切換邏輯）+ `VillageMap`（起始村莊） | `core/MapManager.java`, `map/VillageMap.java` |
| 8 | NPC 系統：火柴人 NPC，有名牌、靜待動畫，村莊放 2~3 位 | `entity/NPC.java` |
| 9 | 傳送門：地圖邊界放 `Portal`，碰到自動切換地圖 | `map/Portal.java` |
| 10 | 人物動畫強化：靜待呼吸起伏、走路腰部擺動、攻擊三階段（蓄勢→揮擊→收勢） | 更新 `entity/Player.java` |
| 11 | 裝備系統：`Equipment` 資料類 + `EquipSlot` 枚舉 + 新手預設裝備穿在身上 | `item/Equipment.java`, `item/EquipSlot.java`, 更新 `Player.java` |
| 12 | `GamePanel` 整合：地圖切換、NPC、Portal 渲染 | 更新 `core/GamePanel.java` |

### 原定功能（Phase 13–16）
| Phase | 任務 |
|-------|------|
| 13 | 技能系統（Job 抽象類 + Warrior + 衝刺斬/鐵壁） |
| 14 | 裝備面板完整實作（8 格可點擊） |
| 15 | 升等 + 手動分配 STR/DEX/INT/LUK 視窗 |
| 16 | 轉職系統（Lv10 → 戰士、Lv30 → 勇士，跳出轉職畫面） |

---

## 3. 🐛 已知問題 / 注意事項

- `SkillPanel` 和 `EquipPanel` 目前是**框架佔位**，Phase 13/14 才填入真實資料
- 怪物碰撞偵測用 `prevFeet` 回推，**高速掉落時可能穿台**（Phase 3 簡化版，後續可改用 sweep test）
- `GameMap` 目前是戰鬥地圖，**村莊地圖（VillageMap）Phase 7 才建立**，地圖切換邏輯尚未實作
- 玩家 `Equipment[]` 欄位已預留（註解在 `Player.java`），Phase 11 解開並實作
- 寵物欄位也已預留（`canHavePet`），功能未來再擴充
- `compile_and_run.bat` 已移除中文，用 `dir /s /b *.java` 自動掃描全部 Java 檔

---

## 4. 📁 檔案清單

```
D:\MapleGame\  （GitHub: AlexChang1999/MapleFight，branch: main）
├── compile_and_run.bat          ← 雙擊編譯並執行
├── PROGRESS.md                  ← 本存檔點
├── .gitignore                   ← 排除 out/
└── src/maplestory/
    ├── Main.java
    ├── core/
    │   ├── GameWindow.java      ← JFrame 視窗
    │   ├── GamePanel.java       ← 主迴圈、HUD、面板切換（S/K/E）
    │   └── Camera.java          ← Lerp 平滑鏡頭
    ├── entity/
    │   ├── Player.java          ← 火柴人玩家、移動、跳躍、攻擊、RPG 數值
    │   └── Monster.java         ← 怪物 AI、追擊、受傷、死亡動畫
    ├── input/
    │   └── InputHandler.java    ← ← → Space Z 鍵盤監聽
    ├── map/
    │   ├── GameMap.java         ← 目前唯一地圖（戰鬥場景）
    │   └── Platform.java        ← 平台資料 + 繪製
    └── ui/
        ├── StatusPanel.java     ← S 鍵，完整顯示 STR/DEX/INT/LUK/HP/MP
        ├── SkillPanel.java      ← K 鍵，框架佔位（Phase 13 實作）
        └── EquipPanel.java      ← E 鍵，8 格框架（Phase 14 實作）
```

---

## 5. 🎮 遊戲規格摘要（給下一個 Session 參考）

| 項目 | 內容 |
|------|------|
| 視窗大小 | 800 × 580（遊戲 500 + HUD 80） |
| 地圖寬度 | 2000 px（可卷軸） |
| 操作鍵 | ← → 移動、Space 跳躍、Z 攻擊、S/K/E 開面板 |
| 職業規劃 | 新手 → 戰士(Lv10) → 勇士(Lv30)，架構可擴充 |
| 角色外型 | 純 Java2D 火柴人（無圖片檔） |
| 四維屬性 | STR / DEX / INT / LUK，升等手動分配 |
| 裝備格 | 8 格：頭盔/上衣/下衣/武器/手套/鞋/披風/耳環 |
| 寵物 | 欄位預留，功能未來擴充 |
| 框架 | 純 Java + Swing（不需額外安裝） |
| 開發語言 | Java（使用者：Windows 11，Java v24） |
