package maplestory.core;

import maplestory.entity.Monster;
import maplestory.entity.MonsterType;
import maplestory.entity.NPC;
import maplestory.entity.Player;
import maplestory.input.InputHandler;
import maplestory.job.Skill;
import maplestory.keybind.ActionType;
import maplestory.keybind.KeyBindingManager;
import maplestory.map.BaseMap;
import maplestory.ui.EquipPanel;
import maplestory.ui.KeyBindingPanel;
import maplestory.ui.SkillPanel;
import maplestory.ui.StatusPanel;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
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
    private final Player            player;
    private final MapManager        mapManager;
    private final Camera            camera;
    private final InputHandler      inputHandler;
    private final KeyBindingManager keyBindings;   // 按鍵綁定資料層（共用）

    // ── 怪物（各地圖分開管理）───────────────────────────────────
    private final List<Monster> monsters       = new ArrayList<>(); // 冒險平原
    private final List<Monster> arcticMonsters = new ArrayList<>(); // 極地冰原
    private boolean prevAttacking = false;

    // ── UI 面板 ───────────────────────────────────────────────
    private final StatusPanel     statusPanel;
    private final SkillPanel      skillPanel  = new SkillPanel();
    private final EquipPanel      equipPanel  = new EquipPanel();
    private final KeyBindingPanel keybindPanel;

    /**
     * 目前顯示的面板（null = 無）。
     * 可選值："status" | "skill" | "equip" | "keybind"
     */
    private String activePanel = null;

    // ─────────────────────────────────────────────────────────
    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        // ── 初始化按鍵系統 ───────────────────────────────────
        keyBindings  = new KeyBindingManager();

        // ── 初始化核心物件 ───────────────────────────────────
        camera       = new Camera(SCREEN_WIDTH, GAME_HEIGHT);
        mapManager   = new MapManager();
        player       = new Player(200, 350, camera);
        inputHandler = new InputHandler(player, keyBindings);
        addKeyListener(inputHandler);

        // ── 初始化 UI 面板 ───────────────────────────────────
        statusPanel  = new StatusPanel();
        keybindPanel = new KeyBindingPanel(keyBindings);

        // ── 冒險平原怪物（3 史萊姆、2 野豬、2 蝙蝠）────────
        monsters.add(new Monster( 450, 300, MonsterType.SLIME));
        monsters.add(new Monster( 750, 300, MonsterType.SLIME));
        monsters.add(new Monster(1050, 300, MonsterType.SLIME));
        monsters.add(new Monster( 620, 290, MonsterType.BOAR));
        monsters.add(new Monster(1280, 290, MonsterType.BOAR));
        monsters.add(new Monster( 550, 200, MonsterType.BAT));
        monsters.add(new Monster(1100, 200, MonsterType.BAT));

        // ── 極地冰原怪物（3 冰晶史萊姆、2 極地熊、2 冰蝠）──
        arcticMonsters.add(new Monster( 400, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster( 900, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster(1600, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster( 700, 290, MonsterType.POLAR_BEAR));
        arcticMonsters.add(new Monster(1900, 290, MonsterType.POLAR_BEAR));
        arcticMonsters.add(new Monster( 600, 200, MonsterType.ICE_BAT));
        arcticMonsters.add(new Monster(1400, 200, MonsterType.ICE_BAT));

        // ── 鍵盤監聽：UI 介面開關 ────────────────────────────
        // 按下任何鍵時，先查 KeyBindingManager 是否是 UI 動作
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                ActionType action = keyBindings.getAction(e.getKeyCode());
                if (action == null) return;

                // 如果按鍵配置面板開著，任何 UI 動作都關掉它
                if ("keybind".equals(activePanel)) {
                    activePanel = null;
                    return;
                }

                switch (action) {
                    case UI_STATUS  -> togglePanel("status");
                    case UI_SKILL   -> togglePanel("skill");
                    case UI_EQUIP   -> togglePanel("equip");
                    case UI_KEYBIND -> togglePanel("keybind");
                    default         -> {} // GAME 類由 InputHandler 處理
                }
            }
        });

        // ── 滑鼠事件：委派給 KeyBindingPanel ─────────────────
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if ("keybind".equals(activePanel))
                    keybindPanel.mousePressed(e.getX(), e.getY());
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if ("keybind".equals(activePanel))
                    keybindPanel.mouseReleased(e.getX(), e.getY());
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if ("keybind".equals(activePanel))
                    keybindPanel.mouseDragged(e.getX(), e.getY());
            }
        });
    }

    /** 切換面板：再按一次就關掉 */
    private void togglePanel(String name) {
        activePanel = name.equals(activePanel) ? null : name;
    }

    /** 根據目前地圖回傳對應的怪物列表 */
    private List<Monster> currentMonsters() {
        if (mapManager.isOnMap("arctic")) return arcticMonsters;
        if (mapManager.isOnMap("battle")) return monsters;
        return java.util.Collections.emptyList();
    }

    // ── 遊戲迴圈 ─────────────────────────────────────────────
    public void startGameLoop() {
        running    = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (running) {
            long   now = System.nanoTime();
            double dt  = (now - lastTime) / 1_000_000_000.0;
            lastTime   = now;
            if (dt > 0.05) dt = 0.05;

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
        // 按鍵配置面板開著時，凍結遊戲邏輯（不凍結動畫）
        if ("keybind".equals(activePanel)) return;

        BaseMap currentMap = mapManager.getCurrentMap();

        // 玩家（含職業被動、技能冷卻）
        player.update(dt, currentMap);

        // 鏡頭跟隨
        camera.update(player.getX(), player.getY(), currentMap.getMapWidth());

        // 地圖切換（傳送門）
        mapManager.update(dt, player);

        // 技能輸入（Q / W 由 InputHandler 存入 pendingSkill）
        List<Monster> curMonsters = currentMonsters();
        int pendingSkill = inputHandler.pollPendingSkill();
        if (pendingSkill >= 0 && !curMonsters.isEmpty()) {
            player.useSkill(pendingSkill, curMonsters);
        }

        // 怪物更新（冒險平原 or 極地冰原）
        if (!curMonsters.isEmpty()) {
            boolean isAttacking = player.isAttacking();

            if (!isAttacking && prevAttacking) {
                for (Monster m : curMonsters) m.setHitThisAttack(false);
            }
            if (isAttacking) player.checkAttackHits(curMonsters);
            prevAttacking = isAttacking;

            for (Monster m : curMonsters) {
                m.update(dt, currentMap, player);
                // 怪物剛死亡時發放 EXP（one-shot flag）
                if (m.pollJustDied()) {
                    player.gainExp(m.getExpReward());
                }
            }
        } else {
            prevAttacking = false;
        }

        // NPC 更新
        for (NPC npc : currentMap.getNPCs()) npc.update(dt);
    }

    // ── 繪製 ─────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 遊戲畫面（裁剪到 GAME_HEIGHT）
        g2d.setClip(0, 0, SCREEN_WIDTH, GAME_HEIGHT);
        drawGameArea(g2d);

        // HUD（移除裁剪後繪製）
        g2d.setClip(null);
        drawHUD(g2d);

        // UI 面板（最上層，可超過裁剪範圍）
        switch (activePanel == null ? "" : activePanel) {
            case "status"  -> statusPanel.draw(g2d, player);
            case "skill"   -> skillPanel.draw(g2d);
            case "equip"   -> equipPanel.draw(g2d);
            case "keybind" -> keybindPanel.draw(g2d);
        }
    }

    /** 繪製遊戲世界 */
    private void drawGameArea(Graphics2D g) {
        BaseMap currentMap = mapManager.getCurrentMap();

        // 天空漸層（極地地圖自己畫夜空，此處跳過）
        if (!mapManager.isOnMap("arctic")) {
            GradientPaint sky = new GradientPaint(
                0, 0,           new Color(100, 180, 240),
                0, GAME_HEIGHT, new Color(170, 220, 255)
            );
            g.setPaint(sky);
            g.fillRect(0, 0, SCREEN_WIDTH, GAME_HEIGHT);
        }

        // 地圖（背景 + 地形 + NPC + 傳送門）
        currentMap.draw(g, camera);

        // 怪物（冒險平原 or 極地冰原）
        for (Monster m : currentMonsters()) m.draw(g, camera);

        // 技能特效（在怪物和玩家之間的層次，僅有職業時繪製）
        if (player.getJob() != null) {
            player.getJob().drawEffects(g, camera);
        }

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

        // 等級 / 職業
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        g.setColor(Color.YELLOW);
        g.drawString("Lv." + player.getLevel() + "  " + player.getJobName(),
                     260, hudY + 25);

        // 地圖名稱
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        g.setColor(new Color(160, 160, 200));
        String mapLabel;
        if      (mapManager.isOnMap("village")) mapLabel = "新手村";
        else if (mapManager.isOnMap("arctic"))  mapLabel = "極地冰原";
        else                                    mapLabel = "冒險平原";
        g.drawString(mapLabel, 260, hudY + 44);

        // 技能冷卻格（有職業且在戰鬥/冰原地圖才顯示）
        if (player.getJob() != null && !mapManager.isOnMap("village")) {
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

        // 快捷按鈕（右側）—— 從 KeyBindingManager 動態讀取目前綁定
        drawHudButton(g, "技能 [" + getBoundKeyName(ActionType.UI_SKILL)  + "]",
                      SCREEN_WIDTH - 255, hudY + 20);
        drawHudButton(g, "裝備 [" + getBoundKeyName(ActionType.UI_EQUIP)  + "]",
                      SCREEN_WIDTH - 160, hudY + 20);
        drawHudButton(g, "狀態 [" + getBoundKeyName(ActionType.UI_STATUS) + "]",
                      SCREEN_WIDTH -  65, hudY + 20);

        // 操作說明（動態顯示目前按鍵）
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(150, 150, 180));
        g.drawString("移動: " + getBoundKeyName(ActionType.MOVE_LEFT)  + "/"
                              + getBoundKeyName(ActionType.MOVE_RIGHT)
                   + "  跳: " + getBoundKeyName(ActionType.JUMP)
                   + "  攻: " + getBoundKeyName(ActionType.ATTACK)
                   + "  Q技: " + getBoundKeyName(ActionType.SKILL_0)
                   + "  W技: " + getBoundKeyName(ActionType.SKILL_1)
                   + "  按鍵設定[" + getBoundKeyName(ActionType.UI_KEYBIND) + "]",
                   SCREEN_WIDTH - 410, hudY + 62);
    }

    /** 取得某動作目前綁定的鍵名（沒綁回傳 "-"） */
    private String getBoundKeyName(ActionType action) {
        Integer kc = keyBindings.getKeyFor(action);
        return kc != null ? KeyBindingManager.keyName(kc) : "-";
    }

    /** 繪製技能冷卻格（HUD 中段，僅在有職業時呼叫） */
    private void drawSkillSlots(Graphics2D g, int hudY) {
        if (player.getJob() == null) return;
        List<Skill> skills = player.getJob().getSkills();
        int slotSize = 38;
        int startX   = 430;
        String[] actions = {
            getBoundKeyName(ActionType.SKILL_0),
            getBoundKeyName(ActionType.SKILL_1)
        };

        for (int i = 0; i < skills.size(); i++) {
            Skill s  = skills.get(i);
            int   sx = startX + i * (slotSize + 6);
            int   sy = hudY + (HUD_HEIGHT - slotSize) / 2 - 2;

            // 底板
            g.setColor(new Color(30, 30, 60));
            g.fillRoundRect(sx, sy, slotSize, slotSize, 6, 6);

            // 冷卻遮罩
            double cdRatio = s.getCooldownRatio();
            if (cdRatio > 0) {
                g.setColor(new Color(0, 0, 0, 160));
                g.fillRect(sx, sy, slotSize, (int)(slotSize * cdRatio));
            }

            // 技能名稱縮寫
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 10));
            g.setColor(cdRatio > 0 ? Color.GRAY : Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            String label = s.getName();
            g.drawString(label, sx + (slotSize - fm.stringWidth(label)) / 2,
                         sy + slotSize / 2 + 3);

            // 邊框
            g.setStroke(new BasicStroke(1.5f));
            g.setColor(cdRatio > 0 ? new Color(80, 80, 120) : new Color(120, 180, 255));
            g.drawRoundRect(sx, sy, slotSize, slotSize, 6, 6);
            g.setStroke(new BasicStroke(1f));

            // 動態按鍵提示
            g.setFont(new Font("Arial", Font.BOLD, 9));
            g.setColor(new Color(200, 200, 200));
            g.drawString("[" + actions[i] + "]", sx + 2, sy + slotSize - 3);

            // MP 消耗
            g.setFont(new Font("Arial", Font.PLAIN, 9));
            g.setColor(new Color(100, 150, 255));
            g.drawString(s.getMpCost() + "MP", sx + 2, sy + 11);
        }
    }

    // ── HUD 工具方法 ─────────────────────────────────────────

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
