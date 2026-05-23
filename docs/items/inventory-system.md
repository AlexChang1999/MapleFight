<!-- Context: Part of [Drop System](drop-system.md) -->
<!-- Domain: #game/items -->

- `item/Inventory.java`：三個 List（consumables / drops / equipments，各最多 40 格）

- `ui/InventoryPanel.java`：按 I 開啟，三頁 Tab；右鍵裝備/使用/賣出

- `keybind/ActionType.java` — 加 `UI_INVENTORY`（I 鍵）

├── Inventory.java     <- findConsumable() 供 Hotbar 使用
