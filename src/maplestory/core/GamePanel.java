package maplestory.core;

import maplestory.entity.Monster;
import maplestory.entity.MonsterType;
import maplestory.entity.NPC;
import maplestory.entity.Player;
import maplestory.input.InputHandler;
import maplestory.job.Skill;
import maplestory.map.BaseMap;
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
    public static final int HUD_HEIGHT    = 80;
    public static final int GAME_HEIGHT   = 500;
    public static final int SCREEN_HEIGHT = GAME_HEIGHT + HUD_HEIGHT;

    // ── 遊戲迴圈 ─────────────────────────────────────────────
    private static final int  FPS       = 60;
    private static final long TARGET_NS = 1_000_000_000L / FPS;
    private Thread  gameThread;
    private boolean running = false;

    // ── 核心系統 ─────────────────────────────────────────────
    private Player       player;
    private MapManager   mapManager;
    private Camera       camera;
    private InputHandler inputHandler;

    // ── 怪物（只在戰鬥地圖出現）────────────────────────────────
    private final List<Monster> monsters = new ArrayList<>();
    private boolean prevAttacking = false;

    // ── UI 面板 ───────────────────────────────────────────────
    private final StatusPanel statusPanel = new StatusPanel();
    private final SkillPanel  skillPanel  = new SkillPanel();
    private final EquipPanel  equipPanel  = new EquipPanel();
    private String activePanel = null;

    // ─────────────────────────────────────────────────────────
    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        // 初始化核心物件
        camera     = new Camera(SCREEN_WIDTH, GAME_HEIGHT);
        mapManager = new MapManager();
        player     = new Player(200, 350, camera);
        inputHandler = new InputHandler(player);
        addKeyListener(inputHandler);

        // ── 戰鬥地圖怪物（3 史萊姆、2 野豬、2 蝙蝠）────────
        monsters.add(new Monster( 450, 300, MonsterType.SLIME));
        monsters.add(new Monster( 750, 300, MonsterType.SLIME));
        monsters.add(new Monster(1050, 300, MonsterType.SLIME));
        monsters.add(new Monster( 620, 290, MonsterType.BOAR));
        monsters.add(new Monster(1280, 290, MonsterType.BOAR));
        monsters.add(new Monster( 550, 200, MonsterType.BAT));   // 蝙蝠出生高度較高
        monsters.add(new Monster(1100, 200, MonsterType.BAT));

        // UI 面板切換（S / K / E）
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String target = switch (e.getKeyCode()) {
                    case KeyEvent.VK_S -> "status";
                    case KeyEvent.VK_K -> "skill";
                    case KeyEvent.VK_E -> "equip";
                    default            -> null;
                };
                if (target != null) {
                    activePanel = target.equals(activePanel) ? null : target;
                }
            }
        });
    }

    public void startGameLoop() {
        running    = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // ── 遊戲迴圈 ─────────────────────────────────────────────
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (running) {
            long   now = System.nanoTime();
            double dt  = (now - lastTime) / 1_000_000_000.0;
            lastTime   = now;
            if (dt > 0.05) dt = 0.05; // 防止幀間隔過大

            update(dt);
            repaint();

            long sleepMs = (TARGET_NS - (System.nanoTime() - now)) / 1_000_000;
            if (sleepMs > 0) {
                try { Thread.sleep(sleepMs); }
                catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            }
        }
    }

    // ── 每幀更新 ─────────────────────────────────────────────
    private void update(double dt) {
        BaseMap currentMap = mapManager.getCurrentMap();

        // 玩家更新（含職業被動、技能冷卻）
        player.update(dt, currentMap);

        // 鏡頭跟隨
        camera.update(player.getX(), player.getY(), currentMap.getMapWidth());

        // 地圖切換偵測（傳送門）
        mapManager.update(dt, player);

        // ── 技能輸入：Q / W ──────────────────────────────────
        int pendingSkill = inputHandler.pollPendingSkill();
        if (pendingSkill >= 0 && mapManager.isOnMap("battle")) {
            player.useSkill(pendingSkill, monsters);
        }

        // ── 怪物更新（只在戰鬥地圖）──────────────────────────
        if (mapManager.isOnMap("battle")) {
            boolean isAttacking = player.isAttacking();

            // 攻擊結束 → 重置命中旗標
            if (!isAttacking && prevAttacking) {
                for (Monster m : monsters) m.setHitThisAttack(false);
            }
            // 揮擊階段才做傷害計算
            if (isAttacking) player.checkAttackHits(monsters);
            prevAttacking = isAttacking;

            for (Monster m : monsters) m.update(dt, currentMap, player);
        }

        // NPC 更新（村莊地圖）
        for (NPC npc : currentMap.getNPCs()) npc.update(dt);
    }

    // ── 繪製 ─────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        // 遊戲畫面（裁剪至 GAME_HEIGHT）
        g2d.setClip(0, 0, SCREEN_WIDTH, GAME_HEIGHT);
        drawGameArea(g2d);

        // HUD（解除裁剪後繪製）
        g2d.setClip(null);
        drawHUD(g2d);

        // UI 面板疊在最上層
        switch (activePanel == null ? "" : activePanel) {
            case "status" -> statusPanel.draw(g2d, player);
            case "skill"  -> skillPanel.draw(g2d);
            case "equip"  -> equipPanel.draw(g2d);
        }
    }

    /** 繪製遊戲世界 */
    private void drawGameArea(Graphics2D g) {
        BaseMap currentMap = mapManager.getCurrentMap();

        // 天空漸層
        GradientPaint sky = new GradientPaint(
            0, 0,           new Color(100, 180, 240),
            0, GAME_HEIGHT, new Color(170, 220, 255)
        );
        g.setPaint(sky);
        g.fillRect(0, 0, SCREEN_WIDTH, GAME_HEIGHT);

        // 地圖（背景 + 地形 + NPC + 傳送門）
        currentMap.draw(g, camera);

        // 怪物（只在戰鬥地圖顯示）
        if (mapManager.isOnMap("battle")) {
            for (Monster m : monsters) m.draw(g, camera);
        }

        // 技能特效（在玩家前面渲染）
        player.getJob().drawEffects(g, camera);

        // 玩家（最上層）
        player.draw(g, camera);
    }

    /** 繪製底部 HUD */
    private void drawHUD(Graphics2D g) {
        int hudY = GAME_HEIGHT;

        // HUD 底板
        g.setColor(new Color(15, 15, 35));
        g.fillRect(0, hudY, SCREEN_WIDTH, HUD_HEIGHT);
        g.setColor(new Color(60, 60, 120));
        g.drawLine(0, hudY, SCREEN_WIDTH, hudY);

        // HP 條
        drawBar(g, 10, hudY + 10, "HP",
                player.getHp(), player.getMaxHp(),
                new Color(180, 30, 30), new Color(220, 60, 60));

        // MP 條
        drawBar(g, 10, hudY + 36, "MP",
                player.getMp(), player.getMaxMp(),
                new Color(20, 40, 140), new Color(60, 110, 220));

        // 等級 / 職業 / 地圖名稱
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        g.setColor(Color.YELLOW);
        g.drawString("Lv." + player.getLevel() + "  " + player.getJobName(),
                     260, hudY + 25);

        // 目前地圖名稱
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        g.setColor(new Color(160, 160, 200));
        String mapLabel = mapManager.isOnMap("village") ? "🏡 新手村" : "⚔ 冒險平原";
        g.drawString(mapLabel, 260, hudY + 44);

        // 技能冷卻圖示（只在戰鬥地圖顯示）
        if (mapManager.isOnMap("battle")) {
            drawSkillSlots(g, hudY);
        }

        // EXP 條
        int expY = SCREEN_HEIGHT - 12;
        g.setColor(new Color(0, 50, 0));
        g.fillRect(0, expY, SCREEN_WIDTH, 12);
        g.setColor(new Color(70, 200, 70));
        g.fillRect(0, expY, (int)(SCREEN_WIDTH * player.getExpRatio()), 12);
        g.setColor(new Color(100, 255, 100, 180));
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.drawString("EXP", 4, expY + 10);

        // 快捷按鈕（右側）
        drawHudButton(g, "技能 [K]", SCREEN_WIDTH - 255, hudY + 20);
        drawHudButton(g, "裝備 [E]", SCREEN_WIDTH - 160, hudY + 20);
        drawHudButton(g, "狀態 [S]", SCREEN_WIDTH -  65, hudY + 20);

        // 操作說明
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(150, 150, 180));
        g.drawString("← → 移動  Space 跳躍  Z 攻擊  Q 突刺  W 衝擊波",
                     SCREEN_WIDTH - 265, hudY + 62);
    }

    /**
     * 繪製技能冷卻格（Q / W）
     * 在 HP/MP 條右側，EXP 條左方。
     */
    private void drawSkillSlots(Graphics2D g, int hudY) {
        List<Skill> skills = player.getJob().getSkills();
        int slotSize = 38;
        int startX   = 430; // HUD 中段，位於等級文字與快捷按鈕之間
        String[] keys = {"Q", "W"};

        for (int i = 0; i < skills.size(); i++) {
            Skill  s  = skills.get(i);
            int    sx = startX + i * (slotSize + 6);
            int    sy = hudY + (HUD_HEIGHT - slotSize) / 2 - 2;

            // 底板
            g.setColor(new Color(30, 30, 60));
            g.fillRoundRect(sx, sy, slotSize, slotSize, 6, 6);

            // 冷卻遮罩（由上往下填色表示剩餘冷卻）
            double cdRatio = s.getCooldownRatio(); // 1.0=冷卻中, 0.0=好了
            if (cdRatio > 0) {
                g.setColor(new Color(0, 0, 0, 160));
                int maskH = (int)(slotSize * cdRatio);
                g.fillRect(sx, sy, slotSize, maskH);
            }

            // 技能名稱縮寫
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 10));
            g.setColor(cdRatio > 0 ? Color.GRAY : Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            String label = s.getName();
            g.drawString(label, sx + (slotSize - fm.stringWidth(label)) / 2, sy + slotSize / 2 + 3);

            // 邊框（可用時發光）
            g.setStroke(new BasicStroke(1.5f));
            g.setColor(cdRatio > 0 ? new Color(80, 80, 120) : new Color(120, 180, 255));
            g.drawRoundRect(sx, sy, slotSize, slotSize, 6, 6);
            g.setStroke(new BasicStroke(1f));

            // 按鍵提示
            g.setFont(new Font("Arial", Font.BOLD, 9));
            g.setColor(new Color(200, 200, 200));
            g.drawString("[" + keys[i] + "]", sx + 2, sy + slotSize - 3);

            // MP 消耗
            g.setFont(new Font("Arial", Font.PLAIN, 9));
            g.setColor(new Color(100, 150, 255));
            g.drawString(s.getMpCost() + "MP", sx + 2, sy + 11);
        }
    }

    private void drawBar(Graphics2D g, int x, int y,
                         String label, int cur, int max,
                         Color bg, Color fg) {
        int barW = 200, barH = 18;
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g.setColor(Color.WHITE);
        g.drawString(label, x, y + 14);
        g.setColor(bg);
        g.fillRect(x + 28, y, barW, barH);
        double ratio = max > 0 ? (double) cur / max : 0;
        g.setColor(fg);
        g.fillRect(x + 28, y, (int)(barW * ratio), barH);
        g.setColor(Color.WHITE);
        g.drawRect(x + 28, y, barW, barH);
        g.setFont(new Font("Arial", Font.BOLD, 11));
        g.drawString(cur + " / " + max, x + 33, y + 13);
    }

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
