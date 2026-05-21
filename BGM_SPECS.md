# MapleFight — BGM 設計規格書
> 凡涉及本遊戲音樂設定，必須先完整閱讀本文件再作答。

---

## 核心設計哲學
本遊戲的 BGM 風格嚴格遵循「楓之谷」美學：
- **流行音樂的律動（Pop Groove）為骨架**，疊加**真實管弦樂（Orchestral）**作為主旋律
- 旋律線必須具備極強的「歌唱性（Singable）」與「記憶點（Catchy）」
- 背景穿插亮晶晶的魔幻裝飾樂器（風鈴、鋼片琴、豎琴）
- 每張地圖 BGM 需呈現該地圖的地貌特色與情緒張力

---

## 快速參照表

| 地圖 ID | 地圖名稱 | 調性 | BPM | 拍號 | 核心樂器 |
|---------|---------|------|-----|------|---------|
| village | 新手村 | C Major | 112 | 4/4 | 長笛、木吉他、手風琴、鋼片琴 |
| novice1 | 新手林 1 | G Major | 126 | 4/4 | 馬林巴、長笛、鳥鳴 |
| novice2 | 新手林 2 | A Minor | 135 | 4/4 | 雙簧管、小提琴撥奏、鋼片琴 |
| novice3 | 新手林 3 | D Minor | 145 | 4/4 | 法國號、鼓組、電吉他 |
| frontier | 前線前哨站 | G Minor | 120 | 4/4 | 小號進行曲、長號、弦樂、鋼片琴 |
| battle | 冒險平原 | D Minor | 150 | 4/4 | 雙簧管、電吉他、定音鼓 |
| icepost | 冰原驛站 | B Minor | 96 | 6/8 | 長笛、鐵琴、北歐笛、豎琴 |
| arctic | 極地冰原 | E Minor | 104 | 4/4 | 大提琴Solo、女聲哼唱、定音鼓 |

---

## 各地圖完整規格

### 🏘️ 新手村 (village) — 溫暖懷抱的冒險起點

- **調性:** C Major（純淨、明亮、最親切的回家感）
- **速度:** 112 BPM　**拍號:** 4/4
- **流行骨架:** 輕撥木吉他（Fingerpicking）、刷邊爵士鼓、溫潤電貝斯
- **主奏管弦:** 長笛主旋律、雙簧管問答副旋律、小提琴群溫暖和聲墊
- **地貌樂器:** 口琴（Harmonica）點綴鄉村感、手風琴（Accordion）市集氣息
- **楓風裝飾:** 鋼片琴在旋律尾句撒星點、豎琴上行刮奏作段落銜接
- **旋律特色:** 長笛吹大調五聲音階，節奏帶跳舞般輕盈切分，副歌時加八度齊奏
- **三段結構:**
  - A段：木吉他獨奏+口琴，描繪清晨炊煙
  - B段：鼓組+貝斯切入，弦樂群鋪底，長笛奏主旋律
  - C段：手風琴+長笛交織，弦樂大齊奏，鋼片琴閃爍，雙簧管帶笑意尾奏
- **AI Prompt:**
  - Style: MapleStory style, Cheerful Orchestral Pop, J-RPG starter town theme, C Major, 112 BPM, warm and nostalgic
  - Instrumentation: Fingerpicking acoustic guitar, playful flute lead, harmonica, light brush drums, warm string section, sparkling celesta, harp glissando, accordion accents
  - Vibe: Heartwarming village morning, safe haven, cheerful townsfolk, catchy singable melody, cozy adventure beginning

---

### 🌿 新手林 1 (novice1) — 第一步踏進的翠綠回憶

- **調性:** G Major（清新、向上、充滿朝氣）
- **速度:** 126 BPM　**拍號:** 4/4，帶輕跳感切分
- **流行骨架:** 爵士鼓全組（Hi-Hat開合律動強）、撥片木吉他節奏刷弦、Slap電貝斯
- **主奏管弦:** 長笛主旋律、中提琴副旋律做層次
- **地貌樂器:** 木魚（Wood Block）、鳥鳴音效（Bird Chirp SFX）、牧笛（Pan Flute）短句
- **楓風裝飾:** 馬林巴木琴（Marimba）歡快裝飾旋律線、風鈴微微搖曳
- **旋律特色:** 長笛以附點節奏吹跳跳糖般主題，馬林巴接答，Call & Response 洗腦句型
- **三段結構:**
  - A段：馬林巴引路+鳥鳴點綴，清新森林空氣感
  - B段：鼓組+貝斯跳入，長笛旋律加速，追著史萊姆衝刺
  - C段：弦樂+木管雙重奏副歌，馬林巴歡快連音Solo，長笛輕吹尾段
- **AI Prompt:**
  - Style: MapleStory style, Bright Woodland Orchestral Pop, J-RPG beginner forest theme, G Major, 126 BPM, energetic and cheerful
  - Instrumentation: Marimba running melody, playful flute lead, slap bass, full pop drum kit, bird chirp SFX, viola harmonies, wind chimes
  - Vibe: First adventure, lush green forest, bouncy and catchy, cheerful slime hunting, light-hearted excitement

---

### 🦇 新手林 2 (novice2) — 深入草叢，節奏加快

- **調性:** A Minor（略帶緊張的小調，仍保有活力）
- **速度:** 135 BPM　**拍號:** 4/4，Offbeat 切分更明顯
- **流行骨架:** 鼓組節奏加重（Kick+Snare對拍更強）、失真電吉他短促刷弦節奏
- **主奏管弦:** 雙簧管主旋律、小提琴快速撥弦（Pizzicato）副旋律
- **地貌樂器:** 蟲鳴音效（Cricket SFX）夜晚感、低音管（Bassoon）烘托陰影
- **楓風裝飾:** 鋼片琴以三連音跑出魔幻感裝飾旋律
- **旋律特色:** 雙簧管在小調音階上吹帶懸疑感卻 Catchy 的旋律，半音階下行暗示「再深入就有寶藏」
- **三段結構:**
  - A段：鋼片琴三連音引路，低音管悄悄進場，氣氛從光亮轉幽暗
  - B段：電吉他切分節奏跳入，雙簧管旋律加速追趕
  - C段：小提琴撥弦+雙簧管齊奏副歌，鋼片琴最後一小節灑閃光
- **AI Prompt:**
  - Style: MapleStory style, Dark Forest Orchestral Pop, tension-building J-RPG theme, A Minor, 135 BPM
  - Instrumentation: Oboe melodic lead, pizzicato violins, crunchy rhythm guitar, aggressive pop drums, bassoon, sparkling celesta triplets, cricket ambience
  - Vibe: Deeper forest danger, mysterious but still energetic, catchy suspense, bat shadows, racing heartbeat

---

### 🐗 新手林 3 (novice3) — 野豬奔馳的險境邊緣

- **調性:** D Minor（厚重、充滿鬥志）
- **速度:** 145 BPM　**拍號:** 4/4，重拍踩實
- **流行骨架:** 重節奏鼓組（Driving Rock Drums）、帶Fuzz電吉他強力刷弦、搶拍電貝斯Riff
- **主奏管弦:** 法國號（French Horn）主旋律、大提琴（Cello）撥弦低音補強
- **地貌樂器:** 牛鈴（Cowbell）加強節奏、森林戰鼓（Taiko Tom）衝擊感打擊
- **楓風裝飾:** 豎琴間奏刮出一道光、三角鐵在弱拍點綴
- **旋律特色:** 法國號以英雄感跳進音程（Perfect 4th & 5th）吹主題，「短短長」（♪♪♩）節奏型反覆
- **三段結構:**
  - A段：低音鼓+大提琴Riff，野豬震地聲從遠方傳來
  - B段：電吉他+全鼓組爆發，法國號奏主旋律，腎上腺素全開
  - C段：大提琴+法國號雙主奏齊鳴，弦樂tremolo推頂點
- **AI Prompt:**
  - Style: MapleStory style, Intense Forest Battle Orchestral Rock, hero J-RPG action theme, D Minor, 145 BPM
  - Instrumentation: French horn heroic melody, driving rock drums, fuzz electric guitar riff, cello pizzicato bass, taiko tom percussion, harp flash glissando
  - Vibe: Wild boar charge, adrenaline rush, dangerous forest edge, powerful and catchy, level-up battle spirit

---

### 🏯 前線前哨站 (frontier) — 暮光下的鋼鐵意志

- **調性:** G Minor（嚴肅卻不失溫暖，軍事感）
- **速度:** 120 BPM　**拍號:** 4/4，帶進行曲（March）律動
- **流行骨架:** 軍鼓（Snare Drum）進行曲節奏、大鼓踩點、電貝斯粗獷Walking Bass
- **主奏管弦:** 小號（Trumpet）英雄主題、長號（Trombone）厚實和聲、弦樂壓軸推副歌
- **地貌樂器:** 軍鈸（Military Cymbal）、短笛（Piccolo）高音閃過、煙火爆裂遠景音效
- **楓風裝飾:** 鋼片琴在間奏偷偷穿插——剛強前哨站中提醒玩家這依然是冒險童話
- **旋律特色:** 小號附點節奏進行曲旋律，每隔兩小節弦樂合唱式和聲回應，「長官點兵」感
- **三段結構:**
  - A段：軍鼓Roll帶入，小號單聲部前奏，士兵列隊號角
  - B段：全體管樂+弦樂加入，節奏推進
  - C段：長號+小號交替Solo，短笛快速音群飛越頂端，全員齊奏最強副歌
- **AI Prompt:**
  - Style: MapleStory style, Military March Orchestral Pop, frontier outpost J-RPG town theme, G Minor, 120 BPM, heroic and warm
  - Instrumentation: Trumpet heroic lead, trombone harmony, snare march beat, bass drum, piccolo high flash, lush strings, celesta sparkle, military cymbal
  - Vibe: Twilight outpost, steel-willed soldiers, warm campfire camaraderie, catchy march melody, proud milestone achievement

---

### ⚔️ 冒險平原 (battle) — 大地怒吼的戰場交響

- **調性:** D Minor（最經典的緊張戰鬥小調，楓之谷戰鬥地圖標配）
- **速度:** 150 BPM　**拍號:** 4/4，強調Backbeat（2、4拍）
- **流行骨架:** Rock Drum Kit（加Double Kick踩鼓）、Distortion電吉他強力和弦、重型電貝斯
- **主奏管弦:** 小提琴快速tremolo（顫弓）鋪天蓋地緊張感、雙簧管衝出超記憶點主旋律
- **地貌樂器:** 定音鼓（Timpani）轟鳴、鑼（Gong）在關鍵拍點震撼出場
- **楓風裝飾:** 電子合成器（Synth Arp）以16分音符跑出閃光般高速背景線
- **旋律特色:** 雙簧管「♪♪♩ ♪♪♩」節奏型反覆，讓人掛網幾小時也停不下來
- **三段結構:**
  - A段：電吉他Riff+定音鼓滾奏鋪場，小提琴tremolo升起緊張
  - B段：Double Kick全爆發，雙簧管主旋律衝出
  - C段：弦樂+管樂全員大副歌，電吉他火爆Solo，定音鼓+鑼收尾
- **AI Prompt:**
  - Style: MapleStory style, Epic Battle Orchestral Rock, high-intensity J-RPG field theme, D Minor, 150 BPM, non-stop action
  - Instrumentation: Distortion electric guitar power chords, double kick rock drums, oboe catchy lead melody, tremolo violins, heavy bass, timpani rumble, synth arp 16th notes
  - Vibe: Open battlefield, monster rush, pure adrenaline, unstoppable hero, catchy epic loop, MapleStory Henesys Field energy

---

### ❄️ 冰原驛站 (icepost) — 極光下的溫暖驛火

- **調性:** B Minor（深邃神祕，冰冷中透出溫暖）
- **速度:** 96 BPM　**拍號:** 6/8（搖晃感，像雪花飄落）
- **流行骨架:** 指彈六絃琴帶6/8搖籃律動、輕柔貝斯長音（Sustained Bass）
- **主奏管弦:** 長笛空靈高音主旋律、中提琴（Viola）溫暖厚實中間層
- **地貌樂器:** 鐵琴（Glockenspiel）冰晶音質、北歐笛（Nordic Flute）穿插短句
- **楓風裝飾:** 豎琴下行琶音像雪花緩緩落下、鋼片琴描繪極光閃光
- **旋律特色:** 長笛在B小調五聲音階吹清泉流過的曲折旋律，每句以豎琴琶音收尾
- **三段結構:**
  - A段：鐵琴冰晶音粒落下，北歐笛孤寂引子，驛站燈光在夜空點亮
  - B段：吉他撥弦帶入6/8律動，中提琴加溫，長笛奏主旋律
  - C段：弦樂漸強達溫暖頂點，鋼片琴+長笛交織副歌，豎琴大刮奏畫滿極光
- **AI Prompt:**
  - Style: MapleStory style, Arctic Town Orchestral Folk, mystical J-RPG waystation theme, B Minor, 96 BPM, gentle 6/8 waltz
  - Instrumentation: Fingerpicking guitar, airy flute lead, glockenspiel ice tones, Nordic flute phrases, warm viola middle harmony, harp descending arpeggio, celesta aurora sparkle
  - Vibe: Cozy inn in blizzard, aurora borealis sky, warm campfire against cold, lonely yet beautiful, emotional safe haven melody

---

### 🐻 極地冰原 (arctic) — 凜冬的冰魂史詩

- **調性:** E Minor（莊嚴壯闊，史詩感最強烈）
- **速度:** 104 BPM　**拍號:** 4/4，帶Half-Time Feel（每兩拍一大步，雪中沉重前進感）
- **流行骨架:** Half-Time Drum（Kick只踩1、3拍）、長音電貝斯低鳴、Synth Pad鋪底
- **主奏管弦:** 大提琴（Cello）Solo主旋律（楓之谷冰原定番音色）、定音鼓補強史詩感
- **地貌樂器:** 女聲哼唱（Vocalise，無歌詞北歐史詩人聲）、冰裂音效（Ice Crack SFX）
- **楓風裝飾:** 鋼片琴以緩慢的五度音程進行，像極光在黑夜孤獨漂移
- **旋律特色:** 大提琴在低音區奏寬廣「呼吸暫停感」旋律，每句之間有沉默才讓下一句更揪心
- **三段結構:**
  - A段：Synth Pad低沉鋪底，女聲哼唱從遠方飄來，冰裂聲點綴，極地無邊孤寂
  - B段：大提琴主旋律進入，Timpani脈動，鋼片琴畫極光線條
  - C段：全弦樂爆發，女聲升八度衝頂，大提琴Solo，漸弱回歸孤寂
- **AI Prompt:**
  - Style: MapleStory style, Epic Arctic Orchestral, Nordic cinematic J-RPG field theme, E Minor, 104 BPM, half-time epic
  - Instrumentation: Cello solo lead, female vocalise choir, massive timpani, half-time slow drums, sustained synth pad, slow celesta aurora motif, ice cracking ambience, lush string ensemble
  - Vibe: Frozen wasteland, lonely hero against blizzard, majestic polar bears, breathtaking aurora, haunting and beautiful, emotionally overwhelming

---

## 新地圖 BGM 設計標準格式

新增地圖時，BGM 設計必須輸出以下 5 欄位：

1. **基礎樂理設定** — 調性、BPM、拍號
2. **地貌風格專屬配器** — 流行骨架、主奏管弦、地貌樂器、楓風裝飾
3. **旋律與樂曲結構** — Melody Motif + A/B/C 三段式
4. **音樂情緒與意境** — 具體畫面感描述
5. **AI 生成提示詞** — Style / Instrumentation / Vibe 三欄英文 prompt
