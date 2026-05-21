---
name: maplefight-bgm
description: >
  MapleFight 遊戲專屬 BGM 設計顧問。當使用者詢問任何與 MapleFight 遊戲音樂相關的問題時，
  必須立即使用此技能——包括但不限於：BGM 設計、音樂風格、SoundManager 實作、
  各地圖配樂、新地圖 BGM 創作、MIDI 程式生成、Suno/Udio AI 生成提示詞、
  以及任何含有「音樂」「BGM」「背景音樂」「音效」「楓之谷風」「SoundManager」等關鍵字的問題。
  就算使用者只是說「這首音樂要改」「幫我設計新地圖的BGM」，也必須觸發此技能。
---

# MapleFight BGM 設計技能

## 第一步：讀取規格書（必做）

**在回答任何音樂相關問題之前，必須先讀取：**

```
H:\MapleGame\BGM_SPECS.md
```

這份文件包含：
- 核心設計哲學（楓之谷美學標準）
- 全部 8 張地圖的完整 BGM 規格（調性、BPM、配器、AI Prompt）
- 新地圖 BGM 的標準輸出格式

## 第二步：依任務類型處理

### 情況 A：查詢現有地圖的 BGM 設定
直接引用 BGM_SPECS.md 中對應地圖的規格回答，包括調性、BPM、配器清單、AI Prompt。

### 情況 B：為新地圖設計 BGM
嚴格依照規格書中的「新地圖 BGM 設計標準格式」輸出，必須包含全部 5 個欄位：
1. 基礎樂理設定（調性、BPM、拍號）
2. 地貌風格專屬配器（流行骨架、主奏管弦、地貌樂器、楓風裝飾）
3. 旋律與樂曲結構（Melody Motif + A/B/C 三段式）
4. 音樂情緒與意境
5. AI 生成提示詞（Style / Instrumentation / Vibe）

設計時參考規格書中同類地圖的範例（例如：戶外戰鬥地圖參考 battle、城鎮地圖參考 village 或 frontier）。

### 情況 C：修改現有地圖的 BGM 設定
先引用原始設定，說明修改點，並確保修改後仍符合楓之谷美學（管弦樂+流行骨架+魔幻裝飾）。
修改後更新 BGM_SPECS.md 中對應地圖的欄位。

### 情況 D：SoundManager.java 實作
若使用者詢問如何用 Java 程式生成音樂，根據 BGM_SPECS.md 中各地圖的 BPM 與調性，
使用 `javax.sound.midi` 提供 MIDI 實作建議，確保程式生成的旋律符合規格書的設計意圖。

## 核心設計原則（從規格書提煉）

- **流行骨架優先**：每首 BGM 都要有讓人想跟著打拍子的 Pop Groove
- **管弦樂主旋律**：主旋律由真實管弦樂器演奏，不用合成器替代
- **楓風裝飾必須出現**：鋼片琴、風鈴、豎琴至少選其一
- **旋律必須 Singable**：掛網掛一下午也不會膩的洗腦旋律
- **地圖情緒梯度**：village(輕鬆) → novice1-3(漸緊張) → frontier(進行曲) → battle(高強度) → icepost(神秘溫暖) → arctic(史詩孤寂)
