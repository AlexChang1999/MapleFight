package maplestory.audio;

import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.util.EnumMap;
import java.util.Map;

/**
 * 音效系統單例管理器。
 *
 * BGM：使用 javax.sound.midi，程式化生成各地圖主題音樂並無縫循環。
 * SFX：使用 javax.sound.sampled，程式化生成 PCM 波形，非同步播放不阻塞主迴圈。
 *
 * 使用方式：
 *   SoundManager.get().playBGM("village");
 *   SoundManager.get().playSFX(SFX.JUMP);
 */
public class SoundManager {

    private static SoundManager instance;

    // ── BGM ──────────────────────────────────────────────────
    private Sequencer sequencer;
    private String    currentMapId = "";

    // ── SFX ──────────────────────────────────────────────────
    private static final int         SAMPLE_RATE = 44100;
    private static final AudioFormat PCM_FORMAT  =
        new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

    private final Map<SFX, byte[]> sfxCache = new EnumMap<>(SFX.class);

    // ── 設定 ─────────────────────────────────────────────────
    private boolean muted = false;

    // ─────────────────────────────────────────────────────────
    private SoundManager() {
        initMidi();
        buildSFX();
    }

    public static SoundManager get() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    // ═════════════════════════════════════════════════════════
    // BGM
    // ═════════════════════════════════════════════════════════

    /** 播放指定地圖的 BGM（相同地圖不重新播放） */
    public void playBGM(String mapId) {
        if (mapId.equals(currentMapId)) return;
        currentMapId = mapId;
        if (muted || sequencer == null) return;
        try {
            BGMTrack track = BGMTrack.forMap(mapId);
            Sequence seq   = track.buildSequence();
            sequencer.stop();
            sequencer.setSequence(seq);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.setLoopStartPoint(0);
            sequencer.setLoopEndPoint(seq.getTickLength());
            sequencer.start();
        } catch (Exception e) {
            System.err.println("[Audio] BGM 播放失敗（" + mapId + "）: " + e.getMessage());
        }
    }

    public void stopBGM() {
        currentMapId = "";
        if (sequencer != null && sequencer.isRunning()) sequencer.stop();
    }

    // ═════════════════════════════════════════════════════════
    // SFX
    // ═════════════════════════════════════════════════════════

    /** 非同步播放音效，不阻塞主迴圈 */
    public void playSFX(SFX sfx) {
        if (muted) return;
        byte[] data = sfxCache.get(sfx);
        if (data == null) return;
        Thread t = new Thread(() -> playPCM(data), "sfx-" + sfx.name());
        t.setDaemon(true);
        t.start();
    }

    private static void playPCM(byte[] data) {
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, PCM_FORMAT);
            if (!AudioSystem.isLineSupported(info)) return;
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(PCM_FORMAT, 4096);
                line.start();
                line.write(data, 0, data.length);
                line.drain();
            }
        } catch (Exception e) {
            System.err.println("[Audio] SFX 播放失敗: " + e.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════
    // SFX 波形生成
    // ═════════════════════════════════════════════════════════

    private void buildSFX() {
        sfxCache.put(SFX.JUMP,             genJump());
        sfxCache.put(SFX.ATTACK,           genAttack());
        sfxCache.put(SFX.SKILL_THRUST,     genSkillThrust());
        sfxCache.put(SFX.SKILL_SHOCKWAVE,  genSkillShockwave());
        sfxCache.put(SFX.LEVEL_UP,         genLevelUp());
        sfxCache.put(SFX.PORTAL,           genPortal());
        sfxCache.put(SFX.MONSTER_DEATH,    genMonsterDeath());
        sfxCache.put(SFX.ITEM_PICKUP,      genItemPickup());
        sfxCache.put(SFX.HURT,             genHurt());
        sfxCache.put(SFX.EQUIP,            genEquip());
        sfxCache.put(SFX.SHOP_BUY,         genShopBuy());
        sfxCache.put(SFX.UI_CLICK,         genUIClick());
    }

    // ── 跳躍：正弦上滑音 280→560Hz ───────────────────────────
    private static byte[] genJump() {
        return glide(280, 560, 0.12, 0.55);
    }

    // ── 攻擊：金屬撞擊 + 短暫雜訊 ────────────────────────────
    private static byte[] genAttack() {
        return concat(metal(300, 0.05, 0.72), noise(0.045, 0.28));
    }

    // ── 突刺技能：銳利金屬刺出 ────────────────────────────────
    private static byte[] genSkillThrust() {
        return concat(metal(450, 0.06, 0.80), glide(600, 180, 0.10, 0.38));
    }

    // ── 衝擊波技能：低頻震動 + 雜訊爆炸 ──────────────────────
    private static byte[] genSkillShockwave() {
        return concat(noise(0.055, 0.55), tone(85, 0.28, 0.68));
    }

    // ── 升等：上行分解和弦 C-E-G-C ──────────────────────────
    private static byte[] genLevelUp() {
        return concat(
            tone(262, 0.10, 0.70),  // C4
            tone(330, 0.10, 0.70),  // E4
            tone(392, 0.10, 0.70),  // G4
            tone(523, 0.32, 0.80)   // C5（較長）
        );
    }

    // ── 傳送門：掃頻下降 1100→350Hz ──────────────────────────
    private static byte[] genPortal() {
        return glide(1100, 350, 0.38, 0.58);
    }

    // ── 怪物死亡：雜訊 + 下降音 ──────────────────────────────
    private static byte[] genMonsterDeath() {
        return concat(noise(0.06, 0.40), glide(360, 75, 0.15, 0.45));
    }

    // ── 撿取道具：雙音符輕快上升 ──────────────────────────────
    private static byte[] genItemPickup() {
        return concat(tone(500, 0.055, 0.48), tone(750, 0.08, 0.58));
    }

    // ── 受傷：雜訊 + 下降掃頻 ────────────────────────────────
    private static byte[] genHurt() {
        return concat(noise(0.04, 0.60), glide(400, 180, 0.10, 0.45));
    }

    // ── 裝備穿戴：金屬質感輕響 ────────────────────────────────
    private static byte[] genEquip() {
        return concat(metal(200, 0.05, 0.50), tone(380, 0.09, 0.42));
    }

    // ── 商店購買：三音符上升確認 ──────────────────────────────
    private static byte[] genShopBuy() {
        return concat(
            tone(400, 0.06, 0.50),
            tone(600, 0.06, 0.52),
            tone(800, 0.10, 0.62)
        );
    }

    // ── UI 點擊：短促高頻 ────────────────────────────────────
    private static byte[] genUIClick() {
        return tone(620, 0.04, 0.35);
    }

    // ═════════════════════════════════════════════════════════
    // PCM 低階生成工具
    // ═════════════════════════════════════════════════════════

    /** 單一頻率正弦波 */
    private static byte[] tone(double freq, double sec, double vol) {
        return glide(freq, freq, sec, vol);
    }

    /** 正弦掃頻（線性頻率滑動） */
    private static byte[] glide(double f0, double f1, double sec, double vol) {
        int n = (int)(SAMPLE_RATE * sec);
        byte[] buf = new byte[n * 2];
        double phase = 0;
        for (int i = 0; i < n; i++) {
            double t    = (double) i / SAMPLE_RATE;
            double freq = f0 + (f1 - f0) * (t / sec);
            double env  = adsr(t, 0.008, sec * 0.6, sec * 0.35);
            double samp = Math.sin(phase) * env * vol * 32767;
            phase += 2 * Math.PI * freq / SAMPLE_RATE;
            int v = clamp((int) samp);
            buf[i * 2]     = (byte)(v & 0xFF);
            buf[i * 2 + 1] = (byte)((v >> 8) & 0xFF);
        }
        return buf;
    }

    /** 正弦 + 方波混合（金屬感） */
    private static byte[] metal(double freq, double sec, double vol) {
        int n = (int)(SAMPLE_RATE * sec);
        byte[] buf = new byte[n * 2];
        double phase = 0;
        for (int i = 0; i < n; i++) {
            double t    = (double) i / SAMPLE_RATE;
            double env  = adsr(t, 0.003, sec * 0.45, sec * 0.52);
            double sine = Math.sin(phase);
            double sq   = sine >= 0 ? 0.28 : -0.28;
            double samp = (sine * 0.72 + sq) * env * vol * 32767;
            phase += 2 * Math.PI * freq / SAMPLE_RATE;
            int v = clamp((int) samp);
            buf[i * 2]     = (byte)(v & 0xFF);
            buf[i * 2 + 1] = (byte)((v >> 8) & 0xFF);
        }
        return buf;
    }

    /** 高斯白雜訊 */
    private static byte[] noise(double sec, double vol) {
        int n = (int)(SAMPLE_RATE * sec);
        byte[] buf = new byte[n * 2];
        java.util.Random rng = new java.util.Random(42);
        for (int i = 0; i < n; i++) {
            double t    = (double) i / SAMPLE_RATE;
            double env  = adsr(t, 0.003, sec * 0.55, sec * 0.40);
            int v = clamp((int)(rng.nextGaussian() * 0.55 * env * vol * 32767));
            buf[i * 2]     = (byte)(v & 0xFF);
            buf[i * 2 + 1] = (byte)((v >> 8) & 0xFF);
        }
        return buf;
    }

    /** ADSR 包絡（attack/sustain 結束點/release 結束點，皆以秒為單位） */
    private static double adsr(double t, double attack, double sustainEnd, double totalDur) {
        if (t < attack)          return t / attack;
        if (t < sustainEnd)      return 1.0;
        if (t < totalDur)        return 1.0 - (t - sustainEnd) / (totalDur - sustainEnd);
        return 0.0;
    }

    /** 多段 PCM 串接 */
    private static byte[] concat(byte[]... parts) {
        int total = 0;
        for (byte[] p : parts) total += p.length;
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] p : parts) { System.arraycopy(p, 0, out, pos, p.length); pos += p.length; }
        return out;
    }

    private static int clamp(int v) {
        return Math.max(-32767, Math.min(32767, v));
    }

    // ═════════════════════════════════════════════════════════
    // 初始化
    // ═════════════════════════════════════════════════════════

    private void initMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
        } catch (Exception e) {
            System.err.println("[Audio] MIDI 初始化失敗（無聲模式）: " + e.getMessage());
            sequencer = null;
        }
    }

    // ═════════════════════════════════════════════════════════
    // 靜音切換
    // ═════════════════════════════════════════════════════════

    public void toggleMute() {
        muted = !muted;
        if (muted) {
            stopBGM();
        } else {
            String id = currentMapId;
            currentMapId = ""; // 強制重播
            playBGM(id);
        }
    }

    public boolean isMuted() { return muted; }
}
