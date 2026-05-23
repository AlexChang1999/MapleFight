<!-- Context: Part of [Development Roadmap](design-decisions.md) -->
<!-- Domain: #game/dev -->

## 已完成系統（不需再做）
- ✅ BaseMap 抽象模板（portal / NPC / ladder 介面）
- ✅ MapManager（3 張地圖：village / battle / arctic，傳送門冷卻）
- ✅ Portal 動畫傳送門 + 碰撞偵測
- ✅ NPC 火柴人 + 呼吸動畫（無商店邏輯）
- ✅ VillageMap（3 棟建築、3 位 NPC、右側傳送門）
- ✅ GameMap（戰鬥平原、3 層平台、左右傳送門）
- ✅ ArcticMap（極地夜空、極光、雪粒子、梯子系統、冰山視差）
- ✅ Ladder（爬梯物理、上下對齊、梯子跳離）
- ✅ Job 抽象類 + Warrior（脫戰回血被動）
- ✅ Skill 抽象類 + SkillThrust + SkillShockwave
- ✅ Player（雙段連擊、等級/EXP、屬性、裝備欄位、冰緩、null-safe job）
- ✅ Monster（6 種動物、AI、冰屬性緩速、pollJustDied EXP 系統）
- ✅ KeyBindingManager + KeyBindingPanel（拖曳改鍵 UI）
- ✅ Equipment / EquipSlot（8 格裝備、屬性加成）
- ✅ GamePanel（arcticMonsters 分離、currentMonsters()、EXP 即時發放）
