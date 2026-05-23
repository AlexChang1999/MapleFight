<!-- Context: Part of [Development Roadmap](completed-systems.md) -->
<!-- Domain: #game/dev -->

## 執行順序建議

**Phase 1 → Phase 2 → Phase 6 → Phase 3 → Phase 4 → Phase 5 → Phase 7 → Phase 8**

**理由：**
- Phase 1（存檔）先做，因為之後所有內容都需要持久化
- Phase 2（道具欄）先做，因為商店系統依賴它
- Phase 6（職業選擇）早做，因為目前有硬編碼問題
- Phase 3（商店）依賴 Phase 2 的背包系統
- Phase 4（裝備外觀）是純視覺，可任意時機做
- Phase 5（新手地圖）依賴 Phase 2 的怪物掉落
- Phase 7（地圖模板）確立框架後 Phase 8 才能快速擴充

## 驗證方式
每個 Phase 完成後：
```
find src -name "*.java" | xargs javac -encoding UTF-8 -d out
java -cp out maplestory.Main
```
確認 EXIT:0，並實際運行遊戲測試對應功能。
