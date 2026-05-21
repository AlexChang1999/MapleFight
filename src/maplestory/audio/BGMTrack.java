package maplestory.audio;

import javax.sound.midi.*;

/**
 * 每張地圖的 BGM 曲目。
 * 情緒設計：
 *   VILLAGE  — 第一次進入遊戲的期待感與家的溫暖（F大調 BPM118）
 *   NOVICE   — 冒險剛開始的純粹興奮、奔跑向前的衝勁（D大調 BPM148）
 *   BATTLE   — 你已足夠強大，真正戰鬥的壓迫感（E小調 BPM165）
 *   ARCTIC   — 踏入未知世界的敬畏、美麗又危險的空靈感（A小調 BPM90）
 */
public enum BGMTrack {

    VILLAGE, NOVICE, BATTLE, ARCTIC;

    // ── MIDI 時值常數（PPQ=480）───────────────────────────────
    private static final int PPQ = 480;
    private static final int Q   = 480;   // 四分音符
    private static final int H   = 960;   // 二分音符
    private static final int W   = 1920;  // 全音符
    private static final int E   = 240;   // 八分音符
    private static final int S   = 120;   // 十六分音符
    private static final int DH  = 1440;  // 附點二分
    private static final int DQ  = 720;   // 附點四分

    // ── GM 樂器 ──────────────────────────────────────────────
    private static final int PIANO          = 0;
    private static final int CELESTA        = 8;   // 鋼片琴（冰晶感）
    private static final int MUSIC_BOX      = 10;  // 音樂盒
    private static final int XYLOPHONE      = 13;  // 木琴（彈跳）
    private static final int HARP           = 46;  // 豎琴
    private static final int STRINGS        = 48;  // 弦樂
    private static final int TREMOLO_STR    = 44;  // 震音弦樂
    private static final int VIOLIN         = 40;  // 小提琴
    private static final int FLUTE          = 73;  // 長笛
    private static final int LEAD_SAW       = 81;  // 鋸齒波合成器
    private static final int BASS_ACOUSTIC  = 32;  // 原聲貝斯
    private static final int BASS_ELECTRIC  = 33;  // 電貝斯
    private static final int BASS_OVERDRIVE = 29;  // 破音吉他（和弦墊）

    // ── 打擊樂器（ch9）───────────────────────────────────────
    private static final int KICK  = 36;
    private static final int SNARE = 38;
    private static final int HH_C  = 42;  // 踩鑔閉合
    private static final int HH_O  = 46;  // 踩鑔開放
    private static final int RIDE  = 51;  // 叮鑔

    // ─────────────────────────────────────────────────────────
    public static BGMTrack forMap(String mapId) {
        return switch (mapId) {
            case "village"                       -> VILLAGE;
            case "novice1", "novice2", "novice3" -> NOVICE;
            case "battle"                        -> BATTLE;
            case "arctic"                        -> ARCTIC;
            default                              -> VILLAGE;
        };
    }

    public Sequence buildSequence() throws InvalidMidiDataException {
        return switch (this) {
            case VILLAGE -> buildVillage();
            case NOVICE  -> buildNovice();
            case BATTLE  -> buildBattle();
            case ARCTIC  -> buildArctic();
        };
    }

    // ═══════════════════════════════════════════════════════════
    // VILLAGE — "歡迎來到這個世界"
    // F大調 · 118 BPM · 8小節循環
    // 鋼琴主旋律帶著上行樂句，傳遞「一切都要從這裡開始」的期待感。
    // 弦樂填墊讓音色溫暖如家，讓玩家覺得這裡是安全的出發點。
    // ═══════════════════════════════════════════════════════════
    private static Sequence buildVillage() throws InvalidMidiDataException {
        Sequence seq = new Sequence(Sequence.PPQ, PPQ);
        addTempo(seq.createTrack(), 118);

        // ── ch0 鋼琴主旋律 ────────────────────────────────────
        // 旋律設計：第1-4小節先上行再穩落（期待→安心），
        //           第5-8小節再次推高再回家（興奮→溫暖）。
        Track mel = seq.createTrack();
        setProgram(mel, 0, PIANO);
        setVolume(mel, 0, 102);
        int t = 0;
        // 小節 1: C5↗E5↗F5↗G5  — 連續上行，開場就充滿期待
        t = notes(mel, 0, t, 90,  72,Q, 76,Q, 77,Q, 79,Q);
        // 小節 2: A5(H) F5(H)   — 到達高點後穩穩落回
        t = notes(mel, 0, t, 90,  81,H, 77,H);
        // 小節 3: G5 F5 E5 D5   — 溫柔下行，像在環顧這個世界
        t = notes(mel, 0, t, 90,  79,Q, 77,Q, 76,Q, 74,Q);
        // 小節 4: C5(DH) Bb4(Q) — 輕輕落地，附點製造搖擺感
        t = notes(mel, 0, t, 90,  72,DH, 70,Q);
        // 小節 5: F5 G5 A5 C6   — 再次出發，這次更高
        t = notes(mel, 0, t, 90,  77,Q, 79,Q, 81,Q, 84,Q);
        // 小節 6: Bb5(Q) A5(Q) G5(H) — 高點的欣喜，然後緩和
        t = notes(mel, 0, t, 92,  82,Q, 81,Q, 79,H);
        // 小節 7: A5 F5 E5 D5   — 帶著一絲留戀緩緩下行
        t = notes(mel, 0, t, 88,  81,Q, 77,Q, 76,Q, 74,Q);
        // 小節 8: F5(H) C5(H)   — 回家，等待下次循環的出發
        notes(mel, 0, t, 90,  77,H, 72,H);

        // ── ch1 長笛副旋律（在鋼琴之間呼吸，輕盈飄逸）────────
        Track fl = seq.createTrack();
        setProgram(fl, 1, FLUTE);
        setVolume(fl, 1, 62);
        int tf = 0;
        // 副旋律：在主旋律間隙填入，稍偏八分音符律動
        tf = notes(fl, 1, tf, 68,  69,E, 72,E, 74,E, 72,E, 77,Q, 79,Q);
        tf = notes(fl, 1, tf, 68,  81,Q, 79,Q, 77,H);
        tf = notes(fl, 1, tf, 68,  76,Q, 74,Q, 72,Q, 74,Q);
        tf = notes(fl, 1, tf, 68,  69,H, 65,H);
        tf = notes(fl, 1, tf, 68,  72,E, 74,E, 77,E, 79,E, 81,Q, 84,Q);
        tf = notes(fl, 1, tf, 72,  82,Q, 81,Q, 79,H);
        tf = notes(fl, 1, tf, 68,  77,Q, 76,Q, 74,Q, 72,Q);
        notes(fl, 1, tf, 68,  77,H, 69,H);

        // ── ch2 弦樂和弦墊（溫暖背景，讓空間感更豐富）──────────
        Track chd = seq.createTrack();
        setProgram(chd, 2, STRINGS);
        setVolume(chd, 2, 44);
        // F大調和弦進行：Fmaj - C/E - Dm - C - Bb - Gm - C - F
        int[][][] progression = {
            {{65,69,72}}, // Fmaj  bar1
            {{64,67,72}}, // C/E   bar2
            {{62,65,69}}, // Dm    bar3
            {{60,64,67}}, // C     bar4
            {{58,62,65}}, // Bb    bar5
            {{55,58,62}}, // Gm    bar6
            {{60,64,67}}, // C     bar7
            {{65,69,72}}  // F     bar8
        };
        for (int b = 0; b < 8; b++) {
            for (int n : progression[b][0]) addNote(chd, 2, n, b * W, W - 30, 38);
        }

        // ── ch3 原聲貝斯（根音走動，穩定又活潑）────────────────
        Track bas = seq.createTrack();
        setProgram(bas, 3, BASS_ACOUSTIC);
        setVolume(bas, 3, 78);
        // F - C - D - C - Bb - G - C - F（低八度）
        int[] roots = {41, 48, 50, 48, 46, 43, 48, 41}; // F2 C3 D3 C3 Bb2 G2 C3 F2
        for (int b = 0; b < 8; b++) {
            int bt = b * W;
            addNote(bas, 3, roots[b],      bt,          H - 20, 72);
            addNote(bas, 3, roots[b] + 7,  bt + H,      Q - 15, 60);
            addNote(bas, 3, roots[b],      bt + H + Q,  Q - 15, 58);
        }

        return seq;
    }

    // ═══════════════════════════════════════════════════════════
    // NOVICE — "前方就是冒險！"
    // D大調 · 148 BPM · 8小節循環
    // 木琴彈跳旋律帶著強烈的推進感，每個音符都像在催促你向前跑。
    // 豎琴填充分解和弦，讓整體充滿靈動與生命力。
    // ═══════════════════════════════════════════════════════════
    private static Sequence buildNovice() throws InvalidMidiDataException {
        Sequence seq = new Sequence(Sequence.PPQ, PPQ);
        addTempo(seq.createTrack(), 148);

        // ── ch0 木琴主旋律（彈跳、推進、充滿能量）───────────────
        Track mel = seq.createTrack();
        setProgram(mel, 0, XYLOPHONE);
        setVolume(mel, 0, 108);
        int t = 0;
        // 小節 1: 用八分音符分解D大調和弦，充滿彈跳
        t = notes(mel, 0, t, 95,  74,E, 78,E, 81,Q, 78,Q, 74,Q);
        // 小節 2: 爬高到B5，大跳製造興奮感
        t = notes(mel, 0, t, 95,  76,Q, 79,Q, 83,H);
        // 小節 3: A5一路上到C#6，衝刺感
        t = notes(mel, 0, t, 95,  81,E, 83,E, 85,Q, 83,Q, 81,Q);
        // 小節 4: 跳回到根音G5，像落地後再跳起
        t = notes(mel, 0, t, 95,  79,H, 74,H);
        // 小節 5: 上行大跳 D5→F#5→A5→D6
        t = notes(mel, 0, t, 98,  74,Q, 78,Q, 81,Q, 86,Q);
        // 小節 6: 高位旋律徘徊，享受高點
        t = notes(mel, 0, t, 95,  85,Q, 83,Q, 81,H);
        // 小節 7: 八分音符快速下行，像在奔跑
        t = notes(mel, 0, t, 95,  78,E, 81,E, 79,Q, 78,Q, 76,Q);
        // 小節 8: D5(H)＋A4 收尾，自然銜接循環
        notes(mel, 0, t, 92,  74,H, 69,Q, 74,Q);

        // ── ch1 豎琴分解和弦（輕盈靈動，像在森林中飛舞）──────────
        Track hrp = seq.createTrack();
        setProgram(hrp, 1, HARP);
        setVolume(hrp, 1, 55);
        // D大調和弦進行：D - A - Bm - G
        // 每拍一個音（分解和弦律動）
        int[][] harpPat = {
            {62, 66, 69, 74},  // D major arpeggio
            {69, 73, 76, 81},  // A major
            {71, 74, 78, 83},  // Bm
            {67, 71, 74, 79},  // G major
            {62, 66, 69, 74},  // D
            {69, 73, 76, 81},  // A
            {57, 61, 64, 69},  // Em
            {62, 66, 69, 74}   // D
        };
        for (int b = 0; b < 8; b++) {
            for (int i = 0; i < 4; i++) {
                addNote(hrp, 1, harpPat[b][i], b * W + i * Q, Q - 15, 48);
            }
        }

        // ── ch2 原聲貝斯（活潑跳動，帶動律動）──────────────────
        Track bas = seq.createTrack();
        setProgram(bas, 2, BASS_ACOUSTIC);
        setVolume(bas, 2, 82);
        int[] bassRoots = {38, 45, 47, 43, 38, 45, 40, 38}; // D2 A2 B2 G2 D2 A2 E2 D2
        for (int b = 0; b < 8; b++) {
            int bt   = b * W;
            int root = bassRoots[b];
            // 走動式貝斯：根→五度→八度→五度
            addNote(bas, 2, root,      bt,          E - 10, 80);
            addNote(bas, 2, root + 7,  bt + E,      E - 10, 68);
            addNote(bas, 2, root + 12, bt + H,      E - 10, 75);
            addNote(bas, 2, root + 7,  bt + H + E,  E - 10, 62);
            addNote(bas, 2, root,      bt + H * 2,  E - 10, 72);
            addNote(bas, 2, root + 5,  bt + H * 2 + E, E - 10, 58);
            addNote(bas, 2, root + 7,  bt + H * 3,  E - 10, 70);
            addNote(bas, 2, root + 4,  bt + H * 3 + E, E - 10, 55);
        }

        return seq;
    }

    // ═══════════════════════════════════════════════════════════
    // BATTLE — "這才是真正的戰場"
    // E小調 · 165 BPM · 8小節循環
    // 鋸齒波合成器帶來壓迫感，鼓組給予強烈推進。
    // 旋律在高速鼓點下仍保有記憶點，讓玩家感受自己的強大。
    // ═══════════════════════════════════════════════════════════
    private static Sequence buildBattle() throws InvalidMidiDataException {
        Sequence seq = new Sequence(Sequence.PPQ, PPQ);
        addTempo(seq.createTrack(), 165);

        // ── ch0 鋸齒波主旋律（攻擊性強，充滿張力）───────────────
        Track mel = seq.createTrack();
        setProgram(mel, 0, LEAD_SAW);
        setVolume(mel, 0, 112);
        int t = 0;
        // 小節 1-2: 密集八分音符攻擊主題
        t = notes(mel, 0, t, 100, 76,E, 79,E, 81,E, 83,E, 79,E, 81,E, 83,E, 81,E);
        t = notes(mel, 0, t, 100, 79,E, 78,E, 76,E, 74,E, 76,E, 79,E, 81,E, 83,E);
        // 小節 3: 爬升到C6（最高點），緊繃感
        t = notes(mel, 0, t, 100, 81,E, 79,E, 78,E, 79,E, 81,E, 83,E, 84,E, 83,E);
        // 小節 4: 四分音符喘息，讓節奏透氣
        t = notes(mel, 0, t, 95,  79,Q, 76,Q, 74,Q, 76,Q);
        // 小節 5: 再次衝鋒
        t = notes(mel, 0, t, 100, 83,E, 81,E, 79,E, 78,E, 76,E, 79,E, 81,E, 79,E);
        // 小節 6: 充滿張力的音型
        t = notes(mel, 0, t, 98,  78,E, 76,E, 74,E, 76,E, 78,E, 79,E, 81,E, 78,E);
        // 小節 7: 八分音符快速下行到最低，再彈回
        t = notes(mel, 0, t, 100, 76,E, 79,E, 83,E, 81,E, 79,E, 78,E, 76,E, 74,E);
        // 小節 8: E5(H)+B4(H) 收尾，製造緊張感銜接循環
        notes(mel, 0, t, 95,  76,H, 71,H);

        // ── ch1 破音吉他（Power chord 墊，給低頻厚度）──────────
        Track gtr = seq.createTrack();
        setProgram(gtr, 1, BASS_OVERDRIVE);
        setVolume(gtr, 1, 72);
        // Em - Am - D - Em 進行，每兩拍切換
        int[][] powerChords = {
            {40, 47}, // Em: E2+B2
            {40, 47},
            {45, 52}, // Am: A2+E3
            {45, 52},
            {38, 45}, // D: D2+A2
            {38, 45},
            {40, 47}, // Em
            {40, 47}
        };
        for (int b = 0; b < 8; b++) {
            int bt = b * W;
            for (int n : powerChords[b]) {
                addNote(gtr, 1, n, bt,      H - 20, 65);
                addNote(gtr, 1, n, bt + H,  H - 20, 60);
            }
        }

        // ── ch2 電貝斯（強力推進，切分律動）─────────────────────
        Track bas = seq.createTrack();
        setProgram(bas, 2, BASS_ELECTRIC);
        setVolume(bas, 2, 98);
        int[] bRoots = {40, 40, 45, 45, 38, 38, 40, 40}; // E2 E2 A2 A2 D2 D2 E2 E2
        for (int b = 0; b < 8; b++) {
            int bt   = b * W;
            int root = bRoots[b];
            // 切分節奏貝斯：讓律動更有力量
            addNote(bas, 2, root,      bt,              E - 8,  95);
            addNote(bas, 2, root,      bt + E,          E - 8,  78);
            addNote(bas, 2, root + 7,  bt + Q,          S - 5,  88);
            addNote(bas, 2, root + 7,  bt + Q + S,      E - 8,  72);
            addNote(bas, 2, root,      bt + H,          E - 8,  92);
            addNote(bas, 2, root + 5,  bt + H + Q,      E - 8,  82);
            addNote(bas, 2, root + 7,  bt + H + Q + E,  E - 8,  75);
            addNote(bas, 2, root,      bt + W - E,      E - 8,  70);
        }

        // ── ch9 鼓組（強勁有力，搭配切分感）─────────────────────
        Track drm = seq.createTrack();
        setVolume(drm, 9, 90);
        for (int b = 0; b < 8; b++) {
            int bt = b * W;
            // 大鼓：1拍、2拍半、3拍（切分）
            addNote(drm, 9, KICK,  bt,              S - 5, 105);
            addNote(drm, 9, KICK,  bt + Q + E,      S - 5, 88);
            addNote(drm, 9, KICK,  bt + H,          S - 5, 100);
            addNote(drm, 9, KICK,  bt + H + Q + E,  S - 5, 82);
            // 小鼓：2拍、4拍（帶力量）
            addNote(drm, 9, SNARE, bt + Q,          S - 5, 95);
            addNote(drm, 9, SNARE, bt + H + Q,      S - 5, 92);
            // 踩鑔閉合：每個八分音符，奇偶交替音量
            for (int i = 0; i < 8; i++) {
                int hhVel = (i % 2 == 0) ? 65 : 48;
                addNote(drm, 9, HH_C,  bt + i * E,  S - 5, hhVel);
            }
            // 踩鑔開放：每小節最後一個八分音符（增加衝擊感）
            addNote(drm, 9, HH_O,  bt + W - E,  S - 5, 72);
        }

        return seq;
    }

    // ═══════════════════════════════════════════════════════════
    // ARCTIC — "凍結的彼方"
    // A小調 · 90 BPM · 8小節循環
    // 鋼片琴主旋律猶如冰晶折射的光，稀疏而純淨。
    // 長休止讓空間感放大，讓玩家感受到這片土地的遼闊與危險。
    // 震音弦樂如凜風悄悄滑過。
    // ═══════════════════════════════════════════════════════════
    private static Sequence buildArctic() throws InvalidMidiDataException {
        Sequence seq = new Sequence(Sequence.PPQ, PPQ);
        addTempo(seq.createTrack(), 90);

        // ── ch0 鋼片琴主旋律（冰晶般純淨，充滿空間感）───────────
        Track mel = seq.createTrack();
        setProgram(mel, 0, CELESTA);
        setVolume(mel, 0, 88);
        int t = 0;
        // 小節 1: A5(H) E5(Q) G5(Q)  — 從高點落下，奠定神祕基調
        t = notes(mel, 0, t, 78,  81,H, 76,Q, 79,Q);
        // 小節 2: C6(DH) 休止        — 長音後的靜默增強空靈感
        t = notes(mel, 0, t, 78,  84,DH);
        t += Q; // 明確休止
        // 小節 3: B4 A4 G4 F4        — 下行音階，如步入深雪
        t = notes(mel, 0, t, 75,  71,Q, 69,Q, 67,Q, 65,Q);
        // 小節 4: E5(W)              — 單音長留，寂靜中的存在感
        t = notes(mel, 0, t, 75,  76,W);
        // 小節 5: A5 G5 F5 E5        — 回到高處，環顧四方
        t = notes(mel, 0, t, 78,  81,Q, 79,Q, 77,Q, 76,Q);
        // 小節 6: C5(H) E5(H)        — 深呼吸
        t = notes(mel, 0, t, 75,  72,H, 76,H);
        // 小節 7: A4 C5 E5 A5        — 上行分解和弦，敬畏感
        t = notes(mel, 0, t, 80,  69,Q, 72,Q, 76,Q, 81,Q);
        // 小節 8: E5(H) A4(H)        — 歸回根音，再次出發
        notes(mel, 0, t, 75,  76,H, 69,H);

        // ── ch1 音樂盒副旋律（在鋼片琴靜默時輕輕回響）──────────
        Track mb = seq.createTrack();
        setProgram(mb, 1, MUSIC_BOX);
        setVolume(mb, 1, 52);
        int tm = W; // 從第2小節才加入（讓鋼片琴先說話）
        tm = notes(mb, 1, tm, 55,  72,E, 76,E, 72,E, 69,E, 72,H);   // bar2
        tm = notes(mb, 1, tm, 55,  67,Q, 65,Q, 67,Q, 69,Q);          // bar3
        tm = notes(mb, 1, tm, 55,  71,H, 69,H);                       // bar4
        tm = notes(mb, 1, tm, 55,  76,E, 79,E, 77,Q, 76,Q, 74,Q);    // bar5
        tm = notes(mb, 1, tm, 55,  69,H, 64,H);                       // bar6
        tm = notes(mb, 1, tm, 55,  72,Q, 76,Q, 72,Q, 69,Q);          // bar7
        notes(mb, 1, tm, 55,  71,H, 67,H);                            // bar8

        // ── ch2 震音弦樂（低頻凜風，塑造寬廣空間）──────────────
        Track str = seq.createTrack();
        setProgram(str, 2, TREMOLO_STR);
        setVolume(str, 2, 32);
        // Am - G - F - Em 進行，非常輕柔，每兩小節一組
        int[][] pads = {
            {45, 48, 52},  // Am: A2+C3+E3  bar1-2
            {43, 47, 50},  // G:  G2+B2+D3  bar3-4
            {41, 45, 48},  // F:  F2+A2+C3  bar5-6
            {40, 44, 47},  // Em: E2+G#2+B2 (自然小調用E2+G2+B2)
        };
        int[][] padsFixed = {
            {45, 48, 52},  // Am
            {43, 47, 50},  // G
            {41, 45, 48},  // F
            {40, 43, 47}   // Em: E2+G2+B2
        };
        for (int g = 0; g < 4; g++) {
            int tick = g * 2 * W;
            for (int n : padsFixed[g]) addNote(str, 2, n, tick, 2 * W - 30, 26);
        }

        // ── ch3 原聲貝斯（極其稀疏，每小節只有一兩個音）──────────
        Track bas = seq.createTrack();
        setProgram(bas, 3, BASS_ACOUSTIC);
        setVolume(bas, 3, 55);
        // 只在強拍落根音，大量留白
        int[] bNotes = {45, 48, 43, 52, 45, 48, 43, 40}; // A3 C3 G2 E3 A3 C3 G2 E2
        int[] bDurs  = {H,  H,  W,  W,  H,  H,  W,  W};
        int tb = 0;
        for (int b = 0; b < 8; b++) {
            addNote(bas, 3, bNotes[b], tb, bDurs[b] - 30, 48);
            tb += W;
        }

        return seq;
    }

    // ═══════════════════════════════════════════════════════════
    // MIDI 工具方法
    // ═══════════════════════════════════════════════════════════

    /** 連續加入 (note, dur) 對，回傳結束 tick */
    private static int notes(Track track, int ch, int startTick, int vel, int... nd)
            throws InvalidMidiDataException {
        int t = startTick;
        for (int i = 0; i + 1 < nd.length; i += 2) {
            addNote(track, ch, nd[i], t, nd[i + 1] - 10, vel);
            t += nd[i + 1];
        }
        return t;
    }

    private static void addNote(Track t, int ch, int note, int tick, int dur, int vel)
            throws InvalidMidiDataException {
        t.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON,  ch, note, vel), tick));
        t.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, ch, note, 0),   tick + Math.max(1, dur)));
    }

    private static void setProgram(Track t, int ch, int prog) throws InvalidMidiDataException {
        t.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, ch, prog, 0), 0));
    }

    private static void setVolume(Track t, int ch, int vol) throws InvalidMidiDataException {
        t.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, ch, 7, vol), 0));
    }

    private static void addTempo(Track t, int bpm) throws InvalidMidiDataException {
        int us = 60_000_000 / bpm;
        byte[] d = {(byte)(us >> 16), (byte)(us >> 8), (byte) us};
        t.add(new MidiEvent(new MetaMessage(0x51, d, 3), 0));
    }
}
