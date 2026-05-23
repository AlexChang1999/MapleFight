<!-- Context: Part of [Game Specifications](../dev/game-specifications.md) -->
<!-- Domain: #game/audio -->

| **Feat-1** | BGM & SFX 音效系統補全（frontier/icepost BGM + 全 SFX + M鍵靜音） | audio/BGMTrack, GamePanel |

- BGM MIDI 音質依賴作業系統 SoundFont，Windows 11 預設效果較佳

```
├── audio/
│   ├── BGMTrack.java      <- 6 張地圖 MIDI BGM
│   ├── SFX.java           <- 12 種 SFX 枚舉
│   └── SoundManager.java  <- 音效單例（M鍵靜音）
```

| 音效 | BGM 6 張地圖、SFX 12 種；M 鍵切換靜音 |
