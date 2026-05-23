<!-- Context: Part of [Inventory System](inventory-system.md) -->
<!-- Domain: #game/items -->

- `item/DropItem.java`：純展示用掉落品

- `entity/Monster.java` — 加 `rollDrops()` 回傳 `List<Item>`；金幣固定掉、裝備隨機掉（5~20% 機率，稀有度按權重）

**稀有度掉落權重：** COMMON 60% / RARE 25% / EPIC 10% / LEGENDARY 4% / MYTHIC 1%

- `core/GamePanel.java` — `pollJustDied()` 後同時呼叫 `rollDrops()`；HUD 加金幣顯示
