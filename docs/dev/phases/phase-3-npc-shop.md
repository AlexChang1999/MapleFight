<!-- Context: Part of [Development Roadmap](../completed-systems.md) -->
<!-- Domain: #game/dev -->

### Phase 3 — NPC 商店系統

**目標：** 村莊藥水商、鐵匠可互動買賣

**新建檔案：**
- `ui/ShopEntry.java`：(Item + 售價) 的 DTO
- `ui/ShopPanel.java`：左側商店庫存（點擊購買）、右側玩家背包（點擊出售）、底部金幣顯示

**修改檔案：**
- `entity/NPC.java` — 加 `shopType` 字串 + `interactRadius`；玩家靠近時顯示「按 F 互動」提示
- `map/VillageMap.java` — 道具商 NPC 加藥水庫存；鐵匠 NPC 加裝備庫存
- `keybind/ActionType.java` — 加 `INTERACT`（F 鍵）
- `core/GamePanel.java` — 偵測玩家靠近 NPC + F 鍵開啟 ShopPanel

**村莊商品預設：**
- 道具商：HP 藥水小(50G/回100HP)、中(150G/回300HP)、大(400G/回800HP)；MP 藥水小/中/大
- 鐵匠：每批裝備依玩家等級動態刷新（等級區間對應品質）
