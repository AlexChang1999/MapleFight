<!-- Context: Part of [Game Specifications](../dev/game-specifications.md) -->
<!-- Domain: #game/ui -->

| **Feat-2** | Hotbar 快捷欄（5格，1-5鍵指派/使用） | ui/Hotbar, GamePanel |

| **Feat-3** | Hotbar 可重新綁鍵（HOTBAR_1-5 ActionType、M鍵靜音修正、GamePanel 重構） | keybind/, core/GamePanel |

├── Hotbar.java        <- 快捷欄 5 格 ★新建

- Hotbar 不做持久化（每次開遊戲需重新指派），但按鍵綁定隨 keybindings.json 保存

| 操作鍵 | A/D 移動、W/Space 跳躍、↑↓ 爬梯、Z 攻擊、Q/W 技能、1-5 快捷欄 |
