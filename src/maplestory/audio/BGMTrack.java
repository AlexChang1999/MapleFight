package maplestory.audio;

import javax.sound.midi.*;

/**
 * 每張地圖的 BGM 曲目。
 * 使用純 Java MIDI（不需外部音檔），程式化生成旋律。
 */
public enum BGMTrack {

    VILLAGE,   // 新手村：C major，溫暖歡快
    NOVICE,    // 新手森林：G major，冒險輕快
    BATTLE,    // 冒險平原：E minor，緊張激烈
    ARCTIC;    // 極地冰原：D minor，寒冷神祕

    // ── MIDI 時間常數 ────────────────────────────────────────
    private static final int PPQ = 480;  // ticks per quarter note
    private static final int Q   = 480;  // quarter note
    private static final int H   = 960;  // half note
    private static final int W   = 1920; // whole note
    private static final int E   = 240;  // eighth note
    private static final int DH  = 1440; // dotted half

    // ── GM 樂器編號 ──────────────────────────────────────────
    private static final int FLUTE         = 73;
    private static final int PIANO         = 0;
    private static final int VIBRAPHONE    = 11;
    private static final int MUSIC_BOX     = 10;
    private static final int STRINGS       = 48;
    private static final int LEAD_SQUARE   = 80;
    private static final int BASS_FINGER   = 33;
    private static final int BASS_ACOUSTIC = 32;

    // ── 地圖 ID 對應 ─────────────────────────────────────────
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
    // 新手村 — C major，BPM 125，溫暖歡快，8 小節循環
    // 旋律：長笛 | 和弦：鋼琴 | 貝斯：Acoustic Bass
    // ═══════════════════════════════════════════════════════════
    private static Sequence buildVillage() throws InvalidMidiDataException {
        Sequence seq = new Sequence(Sequence.PPQ, PPQ);
        addTempo(seq.createTrack(), 125);

        // ── 旋律（長笛，ch0）────────────────────────────────
        Track mel = seq.createTrack();
        setProgram(mel, 0, FLUTE);
        setVolume(mel, 0, 100);
        int t = 0;
        // 小節 1: C5 E5 G5 E5
        t = notes(mel, 0, t, 88,  72,Q, 76,Q, 79,Q, 76,Q);
        // 小節 2: F5 A5 G5(H)
        t = notes(mel, 0, t, 88,  77,Q, 81,Q, 79,H);
        // 小節 3: E5 G5 A5 G5
        t = notes(mel, 0, t, 88,  76,Q, 79,Q, 81,Q, 79,Q);
        // 小節 4: C6 B5 A5(H)
        t = notes(mel, 0, t, 88,  84,Q, 83,Q, 81,H);
        // 小節 5: G5 A5 G5 E5
        t = notes(mel, 0, t, 88,  79,Q, 81,Q, 79,Q, 76,Q);
        // 小節 6: F5 E5 D5 C5
        t = notes(mel, 0, t, 88,  77,Q, 76,Q, 74,Q, 72,Q);
        // 小節 7: E5 G5 A5 G5
        t = notes(mel, 0, t, 88,  76,Q, 79,Q, 81,Q, 79,Q);
        // 小節 8: C5(H) G4 C5（回到開頭）
        notes(mel, 0, t, 88,  72,H, 67,Q, 72,Q);

        // ── 和弦（鋼琴，ch1）────────────────────────────────
        // C大調和弦進行：C - G - F - G，每小節兩拍一換
        Track chd = seq.createTrack();
        setProgram(chd, 1, PIANO);
        setVolume(chd, 1, 48);
        int[][] bars = {
            {60,64,67}, {60,64,67},  // C major × 2 bar
            {55,59,62}, {55,59,62},  // G major × 2 bar
            {53,57,60}, {53,57,60},  // F major × 2 bar
            {55,59,62}, {60,64,67}   // G → C
        };
        for (int b = 0; b < 8; b++) {
            for (int n : bars[b]) addNote(chd, 1, n, b * W, H - 20, 45);
        }

        // ── 貝斯（ch2）──────────────────────────────────────
        Track bas = seq.createTrack();
        setProgram(bas, 2, BASS_ACOUSTIC);
        setVolume(bas, 2, 80);
        int[] roots = {48, 43, 41, 43, 41, 43, 48, 43}; // C3 G2 F2 G2 F2 G2 C3 G2
        for (int b = 0; b < 8; b++) {
            addNote(bas, 2, roots[b], b * W,      H - 20, 70);
            addNote(bas, 2, roots[b] + 7, b * W + H, H - 20, 60);
        }

        return seq;
    }

    // ═══════════════════════════════════════════════════════════
    // 新手森林 — G major，BPM 138，冒險輕快，8 小節循環
    // 旋律：鐵琴 | 貝斯：Acoustic Bass
    // ═══════════════════════════════════════════════════════════
    private static Sequence buildNovice() throws InvalidMidiDataException {
        Sequence seq = new Sequence(Sequence.PPQ, PPQ);
        addTempo(seq.createTrack(), 138);

        // ── 旋律（鐵琴，ch0）────────────────────────────────
        Track mel = seq.createTrack();
        setProgram(mel, 0, VIBRAPHONE);
        setVolume(mel, 0, 105);
        int t = 0;
        // 小節 1: G5 B5 D6 B5
        t = notes(mel, 0, t, 92,  79,Q, 83,Q, 86,Q, 83,Q);
        // 小節 2: A5 G5 F#5(H)  [F#5=78]
        t = notes(mel, 0, t, 92,  81,Q, 79,Q, 78,H);
        // 小節 3: E5 G5 A5 B5
        t = notes(mel, 0, t, 92,  76,Q, 79,Q, 81,Q, 83,Q);
        // 小節 4: D6(H) G5(H)
        t = notes(mel, 0, t, 92,  86,H, 79,H);
        // 小節 5: B5 A5 G5 F#5
        t = notes(mel, 0, t, 92,  83,Q, 81,Q, 79,Q, 78,Q);
        // 小節 6: E5 D5 E5 G5
        t = notes(mel, 0, t, 92,  76,Q, 74,Q, 76,Q, 79,Q);
        // 小節 7: A5 B5 A5 G5
        t = notes(mel, 0, t, 92,  81,Q, 83,Q, 81,Q, 79,Q);
        // 小節 8: D5(H) G4 D5
        notes(mel, 0, t, 92,  74,H, 67,Q, 74,Q);

        // ── 和弦（鋼琴，ch1）────────────────────────────────
        // G - Em - Am - D，各兩小節
        Track chd = seq.createTrack();
        setProgram(chd, 1, PIANO);
        setVolume(chd, 1, 42);
        int[][] bars = {
            {55,59,62}, {55,59,62},  // G major
            {52,55,59}, {52,55,59},  // Em
            {57,60,64}, {57,60,64},  // Am
            {50,54,57}, {55,59,62}   // D → G
        };
        for (int b = 0; b < 8; b++) {
            for (int n : bars[b]) addNote(chd, 1, n, b * W, H - 20, 42);
        }

        // ── 貝斯（ch2）──────────────────────────────────────
        Track bas = seq.createTrack();
        setProgram(bas, 2, BASS_ACOUSTIC);
        setVolume(bas, 2, 82);
        int[] roots = {43, 43, 40, 40, 45, 45, 50, 43};  // G2 G2 E2 E2 A2 A2 D3 G2
        for (int b = 0; b < 8; b++) {
            addNote(bas, 2, roots[b], b * W,      H - 20, 72);
            addNote(bas, 2, roots[b] + 7, b * W + H, H - 20, 60);
        }

        return seq;
    }

    // ═══════════════════════════════════════════════════════════
    // 冒險平原 — E minor，BPM 155，緊張激烈，8 小節循環
    // 旋律：方波合成器 | 貝斯：Electric Bass | 打擊：Ch9
    // ═══════════════════════════════════════════════════════════
    private static Sequence buildBattle() throws InvalidMidiDataException {
        Sequence seq = new Sequence(Sequence.PPQ, PPQ);
        addTempo(seq.createTrack(), 155);

        // ── 旋律（方波，ch0）────────────────────────────────
        Track mel = seq.createTrack();
        setProgram(mel, 0, LEAD_SQUARE);
        setVolume(mel, 0, 110);
        int t = 0;
        // 小節 1: E5 G5 A5 B5 G5 A5 B5 A5（全八分音符）
        t = notes(mel, 0, t, 95,  76,E, 79,E, 81,E, 83,E, 79,E, 81,E, 83,E, 81,E);
        // 小節 2: G5 F#5 E5 D5 E5 G5 A5 B5
        t = notes(mel, 0, t, 95,  79,E, 78,E, 76,E, 74,E, 76,E, 79,E, 81,E, 83,E);
        // 小節 3: A5 G5 F#5 G5 A5 B5 C6 B5
        t = notes(mel, 0, t, 95,  81,E, 79,E, 78,E, 79,E, 81,E, 83,E, 84,E, 83,E);
        // 小節 4: G5 E5 D5 E5（四分音符）
        t = notes(mel, 0, t, 95,  79,Q, 76,Q, 74,Q, 76,Q);
        // 小節 5: B5 A5 G5 F#5 E5 G5 A5 G5
        t = notes(mel, 0, t, 95,  83,E, 81,E, 79,E, 78,E, 76,E, 79,E, 81,E, 79,E);
        // 小節 6: F#5 E5 D5 E5 F#5 G5 A5 F#5
        t = notes(mel, 0, t, 95,  78,E, 76,E, 74,E, 76,E, 78,E, 79,E, 81,E, 78,E);
        // 小節 7: E5 G5 B5 A5 G5 F#5 E5 D5
        t = notes(mel, 0, t, 95,  76,E, 79,E, 83,E, 81,E, 79,E, 78,E, 76,E, 74,E);
        // 小節 8: E5(H) B4(H)
        notes(mel, 0, t, 95,  76,H, 71,H);

        // ── 貝斯（電貝斯，ch1）───────────────────────────────
        Track bas = seq.createTrack();
        setProgram(bas, 1, BASS_FINGER);
        setVolume(bas, 1, 95);
        // E minor 和弦進行：Em - Am - D - Em
        int[] bassRoots = {40, 40, 45, 45, 38, 38, 40, 40};  // E2 E2 A2 A2 D2 D2 E2 E2
        for (int b = 0; b < 8; b++) {
            int root = bassRoots[b];
            addNote(bas, 1, root,      b * W,          E - 5, 90);
            addNote(bas, 1, root,      b * W + E,      E - 5, 75);
            addNote(bas, 1, root + 7,  b * W + H,      E - 5, 80);
            addNote(bas, 1, root + 7,  b * W + H + E,  E - 5, 68);
            addNote(bas, 1, root,      b * W + H * 2,  E - 5, 85);
            addNote(bas, 1, root + 5,  b * W + H * 2 + E, E - 5, 70);
            addNote(bas, 1, root + 7,  b * W + H * 3,  E - 5, 80);
            addNote(bas, 1, root,      b * W + H * 3 + E, E - 5, 65);
        }

        // ── 打擊樂（ch9，固定 GM）────────────────────────────
        Track drm = seq.createTrack();
        setVolume(drm, 9, 88);
        int BD = 35; // Bass Drum
        int SD = 38; // Snare
        int HH = 42; // Closed Hi-Hat
        for (int b = 0; b < 8; b++) {
            int bt = b * W;
            // 大鼓：拍 1 和拍 3
            addNote(drm, 9, BD, bt,           E - 5, 100);
            addNote(drm, 9, BD, bt + H,       E - 5, 95);
            // 小鼓：拍 2 和拍 4
            addNote(drm, 9, SD, bt + Q,       E - 5, 88);
            addNote(drm, 9, SD, bt + H + Q,   E - 5, 85);
            // 踩鑔：每個八分音符
            for (int i = 0; i < 8; i++) {
                addNote(drm, 9, HH, bt + i * E, E - 10, 60);
            }
        }

        return seq;
    }

    // ═══════════════════════════════════════════════════════════
    // 極地冰原 — D minor，BPM 88，寒冷神祕稀疏，8 小節循環
    // 旋律：音樂盒 | Pad：弦樂
    // ═══════════════════════════════════════════════════════════
    private static Sequence buildArctic() throws InvalidMidiDataException {
        Sequence seq = new Sequence(Sequence.PPQ, PPQ);
        addTempo(seq.createTrack(), 88);

        // ── 旋律（音樂盒，ch0）───────────────────────────────
        Track mel = seq.createTrack();
        setProgram(mel, 0, MUSIC_BOX);
        setVolume(mel, 0, 90);
        int t = 0;
        // 小節 1: D5(H) F5(Q) A5(Q)
        t = notes(mel, 0, t, 80,  74,H, 77,Q, 81,Q);
        // 小節 2: C5(DH) 休止(Q)
        t = notes(mel, 0, t, 80,  72,DH);
        t += Q; // 休止
        // 小節 3: Bb4 A4 G4 F4  [Bb4=70, G4=67, F4=65]
        t = notes(mel, 0, t, 80,  70,Q, 69,Q, 67,Q, 65,Q);
        // 小節 4: A4(W)
        t = notes(mel, 0, t, 80,  69,W);
        // 小節 5: D5 C5 Bb4 A4
        t = notes(mel, 0, t, 80,  74,Q, 72,Q, 70,Q, 69,Q);
        // 小節 6: F4(H) A4(H)
        t = notes(mel, 0, t, 80,  65,H, 69,H);
        // 小節 7: D5 E5 F5 G5
        t = notes(mel, 0, t, 80,  74,Q, 76,Q, 77,Q, 79,Q);
        // 小節 8: A5(H) D5(H)
        notes(mel, 0, t, 80,  81,H, 74,H);

        // ── Pad（弦樂，ch1，持續柔和和弦）────────────────────
        Track pad = seq.createTrack();
        setProgram(pad, 1, STRINGS);
        setVolume(pad, 1, 38);
        // Dm - C - Bb - Am，各兩小節，非常輕柔
        int[][] padChords = {
            {50,53,57},  // Dm: D3 F3 A3 — bar 1-2
            {48,52,55},  // C: C3 E3 G3 — bar 3-4
            {46,50,53},  // Bb: Bb2 D3 F3 — bar 5-6
            {45,48,52}   // Am: A2 C3 E3 — bar 7-8
        };
        for (int g = 0; g < 4; g++) {
            int tick = g * 2 * W;
            for (int n : padChords[g]) {
                addNote(pad, 1, n, tick, 2 * W - 30, 32);
            }
        }

        return seq;
    }

    // ═══════════════════════════════════════════════════════════
    // MIDI 工具方法
    // ═══════════════════════════════════════════════════════════

    /** 新增一連串 (音高, 時值) 對，回傳結束 tick */
    private static int notes(Track track, int ch, int startTick, int vel, int... noteDurs)
            throws InvalidMidiDataException {
        int t = startTick;
        for (int i = 0; i + 1 < noteDurs.length; i += 2) {
            int note = noteDurs[i];
            int dur  = noteDurs[i + 1];
            addNote(track, ch, note, t, dur - 10, vel);
            t += dur;
        }
        return t;
    }

    private static void addNote(Track track, int ch, int note, int tick, int dur, int vel)
            throws InvalidMidiDataException {
        ShortMessage on  = new ShortMessage(ShortMessage.NOTE_ON,  ch, note, vel);
        ShortMessage off = new ShortMessage(ShortMessage.NOTE_OFF, ch, note, 0);
        track.add(new MidiEvent(on,  tick));
        track.add(new MidiEvent(off, tick + Math.max(1, dur)));
    }

    private static void setProgram(Track track, int ch, int program)
            throws InvalidMidiDataException {
        track.add(new MidiEvent(
            new ShortMessage(ShortMessage.PROGRAM_CHANGE, ch, program, 0), 0));
    }

    private static void setVolume(Track track, int ch, int vol)
            throws InvalidMidiDataException {
        track.add(new MidiEvent(
            new ShortMessage(ShortMessage.CONTROL_CHANGE, ch, 7, vol), 0));
    }

    private static void addTempo(Track track, int bpm) throws InvalidMidiDataException {
        int us = 60_000_000 / bpm;
        byte[] data = {(byte)(us >> 16), (byte)(us >> 8), (byte) us};
        track.add(new MidiEvent(new MetaMessage(0x51, data, 3), 0));
    }
}
