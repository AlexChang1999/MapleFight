<!-- Context: Part of [Development Roadmap](../completed-systems.md) -->
<!-- Domain: #game/dev -->

### Phase 2 — 道具欄 & 掉落系統

**目標：** 怪物掉金幣 + 裝備，玩家有分類背包；HUD 顯示金幣

**新建檔案：**
- `item/ItemRarity.java`（枚舉）：COMMON(白) / RARE(綠) / EPIC(藍) / LEGENDARY(紫) / MYTHIC(橘)
- `item/Item.java`（抽象基底）：id, name, rarity, description
- `item/Consumable.java`：type(HP藥水/MP藥水/捲軸)、healAmount
- `item/DropItem.java`：純展示用掉落品
- `item/Inventory.java`：三個 List（consumables / drops / equipments，各最多 40 格）
- `ui/InventoryPanel.java`：按 I 開啟，三頁 Tab；右鍵裝備/使用/賣出

**修改檔案：**
- `item/Equipment.java` — 加入 `rarity` 欄位；displayColor 按稀有度決定
- `entity/Monster.java` — 加 `rollDrops()` 回傳 `List<Item>`；金幣固定掉、裝備隨機掉（5~20% 機率，稀有度按權重）
- `entity/Player.java` — 加 `Inventory inventory`；`gainGold(int)` 方法
- `keybind/ActionType.java` — 加 `UI_INVENTORY`（I 鍵）
- `core/GamePanel.java` — `pollJustDied()` 後同時呼叫 `rollDrops()`；HUD 加金幣顯示

**稀有度掉落權重：** COMMON 60% / RARE 25% / EPIC 10% / LEGENDARY 4% / MYTHIC 1%
