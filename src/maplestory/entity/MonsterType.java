package maplestory.entity;

/**
 * 怪物種類列舉。
 * 每種類型帶有獨立的碰撞箱尺寸、基礎數值與行動速度。
 */
public enum MonsterType {

    //           名稱        寬   高   HP   ATK  偵測  速度  EXP  冰屬性  重生秒（已減半）
    SLIME     ("史萊姆",    36, 28,  50,  8,  240,  55,   30, false,  4),
    BOAR      ("野豬",      46, 34, 100, 15,  280,  90,   80, false,  6),
    BAT       ("蝙蝠",      38, 26,  60, 10,  280,  75,   50, false,  5),
    ICE_SLIME ("冰晶史萊姆",38, 30,  80, 10,  240,  45,   55, true,   5),
    POLAR_BEAR("極地熊",    58, 44, 180, 22,  260,  65,  160, true,   8),
    ICE_BAT   ("冰蝠",      38, 26,  90, 12,  300,  95,   80, true,   6);

    public final String  displayName;
    public final int     width;
    public final int     height;
    public final int     maxHp;
    public final int     atk;
    public final int     detectRange;
    public final int     moveSpeed;
    public final int     expReward;   // 擊殺 EXP 獎勵
    public final boolean iceType;     // 冰屬性：攻擊時使玩家緩速
    public final int     respawnTime; // 重生等待秒數

    MonsterType(String name, int w, int h, int hp, int atk,
                int detect, int speed, int exp, boolean ice, int respawn) {
        this.displayName = name;
        this.width       = w;
        this.height      = h;
        this.maxHp       = hp;
        this.atk         = atk;
        this.detectRange = detect;
        this.moveSpeed   = speed;
        this.expReward   = exp;
        this.iceType     = ice;
        this.respawnTime = respawn;
    }
}
