package maplestory.core;

import maplestory.entity.Monster;
import maplestory.entity.Player;
import maplestory.input.InputHandler;
import maplestory.map.GameMap;
import maplestory.ui.EquipPanel;
import maplestory.ui.SkillPanel;
import maplestory.ui.StatusPanel;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements Runnable {

    // ── 視窗尺寸常數 ──────────────────────────────────────────
    public static final int SCREEN_WIDTH  = 800;
    public static final int HUD_HEIGHT    = 80;   // 底部 UI 高度
    public static final int GAME_HEIGHT   = 500;  // 遊戲畫面高度
    public static final int SCREEN_HEIGHT = GAME_HEIGHT + HUD_HEIGHT;

    // ── 遊戲迴圈 ─────────────────────────────────────────────
    private static final int    FPS         = 60;
    private static final long   TARGET_NS   = 1_000_000_000L / FPS; // 每幀奈秒數
    private Thread  gameThread;
    private boolean running = false;

    // ── 遊戲物件 ─────────────────────────────────────────────
    private Player       player;
    private GameMap      gameMap;
    private Camera       camera;
    private InputHandler inputHandler;
    private List<Monster> monsters;

    // 追蹤攻擊狀態（用於重置怪物的被打旗標）
    private boolean prevAttacking = false;

    // ── UI 面板 ───────────────────────────────────────────────
    private final StatusPanel statusPanel = new StatusPanel();
    private final SkillPanel  skillPanel  = new SkillPanel();
    private final EquipPanel  equipPanel  = new EquipPanel();
    // 目前開啟的面板：null = 全關, "status" / "skill" / "equip"
    private String activePanel = null;

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true); // 讓面板能接收鍵盤輸入

        // 初始化遊戲物件
        gameMap  = new GameMap();
        camera   = new Camera(SCREEN_WIDTH, GAME_HEIGHT);
        player   = new Player(100, 300, camera);
        inputHandler = new InputHandler(player);
        addKeyListener(inputHandler);

        // UI 面板切換（S / K / E）—— 與戰鬥按鍵分開管理
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String target = switch (e.getKeyCode()) {
                    case KeyEvent.VK_S -> "status";
                    case KeyEvent.VK_K -> "skill";
                    case KeyEvent.VK_E -> "equip";
                    default -> null;
                };
                if (target != null) {
                    // 同一個面板再按一次 → 關閉；按不同面板 → 切換
                    activePanel = target.equals(activePanel) ? null : target;
                }
            }
        });

        // 初始化怪物（三隻，放在不同位置）
        monsters = new ArrayList<>();
        monsters.add(new Monster(450,  300));
        monsters.add(new Monster(850,  300));
        monsters.add(new Monster(1200, 300));
    }

    /** 啟動遊戲迴圈執行緒 */
    public void startGameLoop() {
        running    = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // ── 遊戲迴圈（固定 60 FPS）────────────────────────────────
    @Override
    public void run() {
        long lastTime = System.nanoTime();

        while (running) {
            long now   = System.nanoTime();
            double dt  = (now - lastTime) / 1_000_000_000.0; // 轉換成秒
            lastTime   = now;

            // 限制最大 dt，避免視窗拖動時跳幀
            if (dt > 0.05) dt = 0.05;

            update(dt);
            repaint();

            // 控制 FPS：睡到這一幀結束
            long elapsed   = System.nanoTime() - now;
            long sleepMs   = (TARGET_NS - elapsed) / 1_000_000;
            if (sleepMs > 0) {
                try { Thread.sleep(sleepMs); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }

    // ── 每幀更新邏輯 ──────────────────────────────────────────
    private void update(double dt) {
        player.update(dt, gameMap);
        camera.update(player.getX(), player.getY(), gameMap.getMapWidth());

        // 攻擊命中判斷（每幀偵測，hitThisAttack 防止一次攻擊打中多次）
        boolean isAttacking = player.isAttacking();
        if (!isAttacking && prevAttacking) {
            // 攻擊剛結束：重置所有怪物的被打旗標
            for (Monster m : monsters) m.setHitThisAttack(false);
        }
        if (isAttacking) {
            player.checkAttackHits(monsters);
        }
        prevAttacking = isAttacking;

        // 更新怪物
        for (Monster m : monsters) {
            m.update(dt, gameMap, player);
        }
    }

    // ── 繪製 ─────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 開啟反鋸齒，讓火柴人線條更平滑
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        // 畫遊戲區（上方），裁剪不讓內容蓋到 HUD
        g2d.setClip(0, 0, SCREEN_WIDTH, GAME_HEIGHT);
        drawGameArea(g2d);

        // 解除裁剪後畫 HUD
        g2d.setClip(null);
        drawHUD(g2d);

        // UI 面板（疊在最上層，不受鏡頭影響）
        switch (activePanel == null ? "" : activePanel) {
            case "status" -> statusPanel.draw(g2d, player);
            case "skill"  -> skillPanel.draw(g2d);
            case "equip"  -> equipPanel.draw(g2d);
        }
    }

    /** 繪製遊戲畫面（天空、地圖、怪物、玩家） */
    private void drawGameArea(Graphics2D g) {
        // 天空漸層
        GradientPaint sky = new GradientPaint(
            0, 0,           new Color(100, 180, 240),
            0, GAME_HEIGHT, new Color(170, 220, 255)
        );
        g.setPaint(sky);
        g.fillRect(0, 0, SCREEN_WIDTH, GAME_HEIGHT);

        // 地圖（包含背景山丘 + 平台）
        gameMap.draw(g, camera);

        // 怪物
        for (Monster m : monsters) m.draw(g, camera);

        // 玩家（最後畫，顯示在最上層）
        player.draw(g, camera);
    }

    /** 繪製底部 HUD（HP/MP 條、EXP 條、快捷按鈕） */
    private void drawHUD(Graphics2D g) {
        int hudY = GAME_HEIGHT;

        // HUD 底板
        g.setColor(new Color(15, 15, 35));
        g.fillRect(0, hudY, SCREEN_WIDTH, HUD_HEIGHT);
        g.setColor(new Color(60, 60, 120));
        g.drawLine(0, hudY, SCREEN_WIDTH, hudY); // 分隔線

        // ── HP 條 ────────────────────────────────────────────
        drawBar(g,
            10, hudY + 10,           // 位置
            "HP",
            player.getHp(), player.getMaxHp(),
            new Color(180, 30, 30),  // 血條顏色（深紅）
            new Color(220, 60, 60)   // 血條顏色（亮紅）
        );

        // ── MP 條 ────────────────────────────────────────────
        drawBar(g,
            10, hudY + 36,
            "MP",
            player.getMp(), player.getMaxMp(),
            new Color(20, 40, 140),
            new Color(60, 110, 220)
        );

        // ── 等級與職業 ───────────────────────────────────────
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        g.setColor(Color.YELLOW);
        g.drawString("Lv." + player.getLevel() + "  " + player.getJobName(),
                     260, hudY + 30);

        // ── EXP 條（最底部整排） ─────────────────────────────
        int expY = SCREEN_HEIGHT - 12;
        g.setColor(new Color(0, 50, 0));
        g.fillRect(0, expY, SCREEN_WIDTH, 12);
        double expRatio = player.getExpRatio();
        g.setColor(new Color(70, 200, 70));
        g.fillRect(0, expY, (int)(SCREEN_WIDTH * expRatio), 12);
        g.setColor(new Color(100, 255, 100, 180));
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.drawString("EXP", 4, expY + 10);

        // ── 快捷按鈕 ─────────────────────────────────────────
        drawHudButton(g, "技能 [K]", SCREEN_WIDTH - 255, hudY + 20);
        drawHudButton(g, "裝備 [E]", SCREEN_WIDTH - 160, hudY + 20);
        drawHudButton(g, "狀態 [S]", SCREEN_WIDTH - 65,  hudY + 20);

        // ── 操作說明（右下角小字）────────────────────────────
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(150, 150, 180));
        g.drawString("← → 移動  Space 跳躍  Z 攻擊", SCREEN_WIDTH - 255, hudY + 62);
    }

    /** 畫一條帶標籤和數值的狀態條（HP / MP 共用） */
    private void drawBar(Graphics2D g, int x, int y,
                         String label, int cur, int max,
                         Color bgColor, Color fgColor) {
        int barW = 200, barH = 18;

        // 標籤
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g.setColor(Color.WHITE);
        g.drawString(label, x, y + 14);

        // 底色
        g.setColor(bgColor);
        g.fillRect(x + 28, y, barW, barH);

        // 前景（依比例）
        double ratio = max > 0 ? (double) cur / max : 0;
        g.setColor(fgColor);
        g.fillRect(x + 28, y, (int)(barW * ratio), barH);

        // 邊框
        g.setColor(Color.WHITE);
        g.drawRect(x + 28, y, barW, barH);

        // 數值文字
        g.setFont(new Font("Arial", Font.BOLD, 11));
        g.drawString(cur + " / " + max, x + 33, y + 13);
    }

    /** 畫 HUD 快捷按鈕 */
    private void drawHudButton(Graphics2D g, String label, int x, int y) {
        g.setColor(new Color(40, 40, 75));
        g.fillRoundRect(x, y, 85, 28, 8, 8);
        g.setColor(new Color(100, 100, 180));
        g.drawRoundRect(x, y, 85, 28, 8, 8);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, x + (85 - fm.stringWidth(label)) / 2, y + 18);
    }
}
