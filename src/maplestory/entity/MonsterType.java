package maplestory.entity;

/**
 * 怪物種類列舉。
 * 每種類型帶有獨立的碰撞箱尺寸、基礎數值與行動速度。
 */
public enum MonsterType {

    //        名稱      寬   高   基礎HP  攻擊力  偵測距離  移動速度
    SLIME ("史萊姆", 36, 28,  50,  8,  240,  55),
    BOAR  ("野豬",   46, 34, 100, 15,  280,  90),
    BAT   ("蝙蝠",   38, 26,  60, 10,  280,  75);

    public final String displayName;
    public final int width;
    public final int height;
    public final int maxHp;
    public final int atk;
    public final int detectRange;
    public final int moveSpeed;

    MonsterType(String name, int w, int h, int hp, int atk, int detect, int speed) {
        this.displayName  = name;
        this.width        = w;
        this.height       = h;
        this.maxHp        = hp;
        this.atk          = atk;
        this.detectRange  = detect;
        this.moveSpeed    = speed;
    }
}
