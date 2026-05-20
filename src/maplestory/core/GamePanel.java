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
import maplestory.ui.PauseMenu;
import maplestory.ui.SkillPanel;
import maplestory.ui.StatusPanel;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements Runnable {

    // ── 邏輯解析度（固定，不隨視窗縮放改變） ─────────────────
    public static final int SCREEN_WIDTH  = 800;
    public static final int HUD_HEIGHT    = 80;
    public static final int GAME_HEIGHT   = 500;
    public static final int SCREEN_HEIGHT = GAME_HEIGHT + HUD_HEIGHT;

    // ── 存檔資訊 ─────────────────────────────────────────────
    private final int      saveSlot;
    private final Runnable returnToTitleCallback; // ESC 選單「回主畫面」用

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
    private final KeyBindingManager keyBindings;

    // ── 怪物（各地圖獨立）───────────────────────────────────
    private final List<Monster> novice1Monsters = new ArrayList<>();
    private final List<Monster> novice2Monsters = new ArrayList<>();
    private final List<Monster> novice3Monsters = new ArrayList<>();
    private final List<Monster> battleMonsters  = new ArrayList<>();
    private final List<Monster> arcticMonsters  = new ArrayList<>();
    private boolean prevAttacking = false;

    // ── 暫停選單 ─────────────────────────────────────────────
    private boolean paused = false;
    private final PauseMenu pauseMenu = new PauseMenu();

    // ── UI 面板 ───────────────────────────────────────────────
    private final StatusPanel     statusPanel;
    private final SkillPanel      skillPanel  = new SkillPanel();
    private final EquipPanel      equipPanel  = new EquipPanel();
    private final KeyBindingPanel keybindPanel;
    /** 目前顯示的面板（null = 無）。可選值："status"|"skill"|"equip"|"keybind" */
    private String activePanel = null;

    // ─────────────────────────────────────────────────────────
    /**
     * @param slot             存檔槽（1~3）
     * @param playerName       新遊戲角色名（null = 從存檔讀取）
     * @param returnToTitle    ESC 選單回主畫面的回呼
     */
    public GamePanel(int slot, String playerName, Runnable returnToTitle) {
        this.saveSlot              = slot;
        this.returnToTitleCallback = returnToTitle;

        setBackground(Color.BLACK);
        setFocusable(true);

        // ── 按鍵系統（先讀取存檔配置）────────────────────────
        keyBindings = new KeyBindingManager();
        keyBindings.loadFromFile(); // 讀取上次的按鍵設定

        // ── 核心物件 ─────────────────────────────────────────
        camera     = new Camera(SCREEN_WIDTH, GAME_HEIGHT);
        mapManager = new MapManager();
        player     = new Player(200, 350, camera);

        inputHandler = new InputHandler(player, keyBindings);
        addKeyListener(inputHandler);

        // ── 存讀檔 ───────────────────────────────────────────
        if (playerName != null) {
            player.setName(playerName);
        } else {
            String mapId = SaveManager.load(slot, player);
            mapManager.switchMap(mapId, 200, 350, player);
            camera.snapTo(player.getX(), player.getY(), mapManager.getCurrentMap().getMapWidth());
        }

        // ── UI 面板 ──────────────────────────────────────────
        statusPanel  = new StatusPanel();
        keybindPanel = new KeyBindingPanel(keyBindings);

        // ── 怪物初始化：新手森林一區（史萊姆 x3）────────────
        novice1Monsters.add(new Monster( 420, 300, MonsterType.SLIME));
        novice1Monsters.add(new Monster( 780, 300, MonsterType.SLIME));
        novice1Monsters.add(new Monster(1200, 300, MonsterType.SLIME));

        // ── 新手森林二區（史萊姆 x2 + 蝙蝠 x2）────────────
        novice2Monsters.add(new Monster( 400, 300, MonsterType.SLIME));
        novice2Monsters.add(new Monster( 980, 300, MonsterType.SLIME));
        novice2Monsters.add(new Monster( 560, 200, MonsterType.BAT));
        novice2Monsters.add(new Monster(1350, 200, MonsterType.BAT));

        // ── 新手森林三區（野豬 x2 + 蝙蝠 x2）────────────────
        novice3Monsters.add(new Monster( 520, 290, MonsterType.BOAR));
        novice3Monsters.add(new Monster(1200, 290, MonsterType.BOAR));
        novice3Monsters.add(new Monster( 680, 200, MonsterType.BAT));
        novice3Monsters.add(new Monster(1550, 200, MonsterType.BAT));

        // ── 冒險平原（史萊姆 x3 + 野豬 x2 + 蝙蝠 x2）───────
        battleMonsters.add(new Monster( 450, 300, MonsterType.SLIME));
        battleMonsters.add(new Monster( 750, 300, MonsterType.SLIME));
        battleMonsters.add(new Monster(1050, 300, MonsterType.SLIME));
        battleMonsters.add(new Monster( 620, 290, MonsterType.BOAR));
        battleMonsters.add(new Monster(1280, 290, MonsterType.BOAR));
        battleMonsters.add(new Monster( 550, 200, MonsterType.BAT));
        battleMonsters.add(new Monster(1100, 200, MonsterType.BAT));

        // ── 極地冰原（冰晶史萊姆 x3 + 極地熊 x2 + 冰蝠 x2）
        arcticMonsters.add(new Monster( 400, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster( 900, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster(1600, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster( 700, 290, MonsterType.POLAR_BEAR));
        arcticMonsters.add(new Monster(1900, 290, MonsterType.POLAR_BEAR));
        arcticMonsters.add(new Monster( 600, 200, MonsterType.ICE_BAT));
        arcticMonsters.add(new Monster(1400, 200, MonsterType.ICE_BAT));

        // ── 鍵盤監聽：UI 面板 + ESC + F5 ────────────────────
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int kc = e.getKeyCode();

                // ESC：特殊處理（優先於其他面板）
                if (kc == KeyEvent.VK_ESCAPE) {
                    handleEsc();
                    return;
                }
                // 暫停中不處理其他 UI 按鍵
                if (paused) return;

                ActionType action = keyBindings.getAction(kc);
                if (action == null) return;

                // 按鍵配置面板開著時，任何 UI 動作都關閉它
                if ("keybind".equals(activePanel)) {
                    activePanel = null;
                    keyBindings.saveToFile(); // 關閉鍵盤配置面板時自動存檔
                    return;
                }

                switch (action) {
                    case UI_STATUS  -> togglePanel("status");
                    case UI_SKILL   -> togglePanel("skill");
                    case UI_EQUIP   -> togglePanel("equip");
                    case UI_KEYBIND -> togglePanel("keybind");
                    default         -> {}
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F5 && !paused) {
                    saveGame();
                }
            }
        });

        // ── 滑鼠事件（KeyBindingPanel 拖曳 + 暫停選單點擊）──
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                Point lp = toLogical(e.getX(), e.getY());
                if (paused) {
                    handlePauseMenuClick(lp.x, lp.y);
                } else if ("keybind".equals(activePanel)) {
                    keybindPanel.mousePressed(lp.x, lp.y);
                }
            }
            @Override public void mouseReleased(MouseEvent e) {
                Point lp = toLogical(e.getX(), e.getY());
                if (!paused && "keybind".equals(activePanel)) {
                    keybindPanel.mouseReleased(lp.x, lp.y);
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                Point lp = toLogical(e.getX(), e.getY());
                if (!paused && "keybind".equals(activePanel)) {
                    keybindPanel.mouseDragged(lp.x, lp.y);
                }
            }
            @Override public void mouseMoved(MouseEvent e) {
                Point lp = toLogical(e.getX(), e.getY());
                if (paused) pauseMenu.updateHover(lp.x, lp.y);
            }
        });
    }

    // ── 縮放輔助 ─────────────────────────────────────────────

    /** 目前視窗等比縮放比例 */
    private double getScale() {
        int pw = getWidth(),  ph = getHeight();
        if (pw <= 0 || ph <= 0) return 1.0;
        return Math.min((double) pw / SCREEN_WIDTH, (double) ph / SCREEN_HEIGHT);
    }
    private int getTransX() { return (getWidth()  - (int)(SCREEN_WIDTH  * getScale())) / 2; }
    private int getTransY() { return (getHeight() - (int)(SCREEN_HEIGHT * getScale())) / 2; }

    /** 螢幕座標 → 邏輯座標（800x580 空間） */
    private Point toLogical(int sx, int sy) {
        double s = getScale();
        return new Point((int)((sx - getTransX()) / s), (int)((sy - getTransY()) / s));
    }

    // ── ESC 邏輯 ─────────────────────────────────────────────

    private void handleEsc() {
        if (activePanel != null) {
            // 先關閉目前面板
            if ("keybind".equals(activePanel)) keyBindings.saveToFile();
            activePanel = null;
        } else {
            paused = !paused;
        }
    }

    /** 暫停選單點擊處理 */
    private void handlePauseMenuClick(int lx, int ly) {
        PauseMenu.Action action = pauseMenu.hit(lx, ly);
        switch (action) {
            case RESUME       -> paused = false;
            case SAVE         -> { saveGame(); paused = false; }
            case DELETE_SAVE  -> handleDeleteSave();
            case RETURN_TITLE -> {
                paused = false;
                returnToTitleCallback.run();
            }
            case EXIT         -> System.exit(0);
            default           -> {}
        }
    }

    /** 刪除當前存檔並返回主畫面 */
    private void handleDeleteSave() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "確定要刪除「" + player.getName() + "」的存檔嗎？\n此操作無法復原。",
            "刪除存檔確認",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            SaveManager.delete(saveSlot);
            paused = false;
            returnToTitleCallback.run();
        }
    }

    /** 切換面板：再按一次關閉 */
    private void togglePanel(String name) {
        activePanel = name.equals(activePanel) ? null : name;
    }

    /** 根據目前地圖回傳對應的怪物列表 */
    private List<Monster> currentMonsters() {
        return switch (mapManager.getCurrentMap().getMapId()) {
            case "novice1" -> novice1Monsters;
            case "novice2" -> novice2Monsters;
            case "novice3" -> novice3Monsters;
            case "battle"  -> battleMonsters;
            case "arctic"  -> arcticMonsters;
            default        -> java.util.Collections.emptyList();
        };
    }

    // ── 遊戲迴圈 ─────────────────────────────────────────────
    public void startGameLoop() {
        running    = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stopGameLoop() { running = false; }

    private void saveGame() {
        SaveManager.save(saveSlot, player, mapManager.getCurrentMap().getMapId());
        keyBindings.saveToFile(); // 同時存按鍵設定
        showSaveNotice();
    }

    private double saveNoticeTimer = 0;
    private void showSaveNotice() { saveNoticeTimer = 2.0; }

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
        // 暫停選單動畫仍更新（呼吸效果），遊戲邏輯凍結
        if (paused) {
            pauseMenu.update(dt);
            return;
        }
        // 按鍵配置面板開著時，凍結遊戲邏輯
        if ("keybind".equals(activePanel)) return;

        if (saveNoticeTimer > 0) saveNoticeTimer -= dt;

        BaseMap currentMap = mapManager.getCurrentMap();
        player.update(dt, currentMap);
        camera.update(player.getX(), player.getY(), currentMap.getMapWidth());

        // 地圖切換（傳送門）
        mapManager.update(dt, player);
        if (mapManager.justSwitched()) {
            // 瞬間跳鏡頭，不 lerp
            camera.snapTo(player.getX(), player.getY(),
                          mapManager.getCurrentMap().getMapWidth());
        }

        // 技能輸入
        List<Monster> curMonsters = currentMonsters();
        int pendingSkill = inputHandler.pollPendingSkill();
        if (pendingSkill >= 0 && !curMonsters.isEmpty()) {
            player.useSkill(pendingSkill, curMonsters);
        }

        // 怪物更新
        if (!curMonsters.isEmpty()) {
            boolean isAttacking = player.isAttacking();
            if (!isAttacking && prevAttacking) {
                for (Monster m : curMonsters) m.setHitThisAttack(false);
            }
            if (isAttacking) player.checkAttackHits(curMonsters);
            prevAttacking = isAttacking;

            for (Monster m : curMonsters) {
                m.update(dt, currentMap, player);
                if (m.pollJustDied()) player.gainExp(m.getExpReward());
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

        int pw = getWidth(), ph = getHeight();
        double scale = getScale();
        int tx = getTransX(), ty = getTransY();

        // 黑色 letterbox
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, pw, ph);

        // 套用縮放 transform（邏輯座標 800x580）
        g2d.translate(tx, ty);
        g2d.scale(scale, scale);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 遊戲畫面（裁剪到 GAME_HEIGHT）
        g2d.setClip(0, 0, SCREEN_WIDTH, GAME_HEIGHT);
        drawGameArea(g2d);

        // HUD（解除裁剪後繪製）
        g2d.setClip(null);
        drawHUD(g2d);

        // UI 面板（最上層）
        switch (activePanel == null ? "" : activePanel) {
            case "status"  -> statusPanel.draw(g2d, player);
            case "skill"   -> skillPanel.draw(g2d);
            case "equip"   -> equipPanel.draw(g2d);
            case "keybind" -> keybindPanel.draw(g2d);
        }

        // 暫停選單（最頂層）
        if (paused) pauseMenu.draw(g2d);
    }

    /** 繪製遊戲世界 */
    private void drawGameArea(Graphics2D g) {
        BaseMap currentMap = mapManager.getCurrentMap();

        // 天空漸層（極地地圖自己畫夜空，novice3 畫傍晚，各 novice 地圖也自繪天空）
        String mapId = currentMap.getMapId();
        if (!mapId.equals("arctic") && !mapId.startsWith("novice")) {
            GradientPaint sky = new GradientPaint(
                0, 0,           new Color(100, 180, 240),
                0, GAME_HEIGHT, new Color(170, 220, 255)
            );
            g.setPaint(sky);
            g.fillRect(0, 0, SCREEN_WIDTH, GAME_HEIGHT);
        }

        currentMap.draw(g, camera);

        for (Monster m : currentMonsters()) m.draw(g, camera);

        if (player.getJob() != null) {
            player.getJob().drawEffects(g, camera);
        }

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

        drawBar(g, 10, hudY + 10, "HP",
                player.getHp(), player.getMaxHp(),
                new Color(180, 30, 30), new Color(220, 60, 60));
        drawBar(g, 10, hudY + 36, "MP",
                player.getMp(), player.getMaxMp(),
                new Color(20, 40, 140), new Color(60, 110, 220));

        // 等級 / 職業
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        g.setColor(Color.YELLOW);
        g.drawString("Lv." + player.getLevel() + "  " + player.getJobName(),
                     260, hudY + 25);

        // 金幣
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        g.setColor(new Color(255, 215, 0));
        g.drawString("G " + player.getGold(), 260, hudY + 56);

        // 存檔提示
        if (saveNoticeTimer > 0) {
            float alpha = (float) Math.min(1.0, saveNoticeTimer);
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
            g.setColor(new Color(100, 255, 120, (int)(alpha * 220)));
            g.drawString("存檔成功 (Slot " + saveSlot + ")",
                         SCREEN_WIDTH / 2 - 68, hudY + 30);
        }

        // 地圖名稱（從 mapId 動態解析）
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        g.setColor(new Color(160, 160, 200));
        String mapLabel = mapName(mapManager.getCurrentMap().getMapId());
        g.drawString(mapLabel + "  [F5 存檔]  [ESC 選單]", 260, hudY + 44);

        // 技能冷卻格
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

        // 快捷按鈕（右側）
        drawHudButton(g, "技能 [" + getBoundKeyName(ActionType.UI_SKILL)  + "]",
                      SCREEN_WIDTH - 255, hudY + 20);
        drawHudButton(g, "裝備 [" + getBoundKeyName(ActionType.UI_EQUIP)  + "]",
                      SCREEN_WIDTH - 160, hudY + 20);
        drawHudButton(g, "狀態 [" + getBoundKeyName(ActionType.UI_STATUS) + "]",
                      SCREEN_WIDTH -  65, hudY + 20);

        // 操作說明
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(150, 150, 180));
        g.drawString("移:" + getBoundKeyName(ActionType.MOVE_LEFT)
                         + "/" + getBoundKeyName(ActionType.MOVE_RIGHT)
                   + " 跳:" + getBoundKeyName(ActionType.JUMP)
                   + " 攻:" + getBoundKeyName(ActionType.ATTACK)
                   + " Q技:" + getBoundKeyName(ActionType.SKILL_0)
                   + " W技:" + getBoundKeyName(ActionType.SKILL_1)
                   + " 按鍵[" + getBoundKeyName(ActionType.UI_KEYBIND) + "]",
                   SCREEN_WIDTH - 410, hudY + 62);
    }

    private String mapName(String mapId) {
        return switch (mapId) {
            case "village" -> "新手村";
            case "novice1" -> "新手森林一區";
            case "novice2" -> "新手森林二區";
            case "novice3" -> "新手森林三區";
            case "battle"  -> "冒險平原";
            case "arctic"  -> "極地冰原";
            default        -> mapId;
        };
    }

    private String getBoundKeyName(ActionType action) {
        Integer kc = keyBindings.getKeyFor(action);
        return kc != null ? KeyBindingManager.keyName(kc) : "-";
    }

    private void drawSkillSlots(Graphics2D g, int hudY) {
        if (player.getJob() == null) return;
        List<Skill> skills = player.getJob().getSkills();
        int slotSize = 38, startX = 430;
        String[] actions = {
            getBoundKeyName(ActionType.SKILL_0),
            getBoundKeyName(ActionType.SKILL_1)
        };

        for (int i = 0; i < skills.size(); i++) {
            Skill s  = skills.get(i);
            int   sx = startX + i * (slotSize + 6);
            int   sy = hudY + (HUD_HEIGHT - slotSize) / 2 - 2;

            g.setColor(new Color(30, 30, 60));
            g.fillRoundRect(sx, sy, slotSize, slotSize, 6, 6);

            double cdRatio = s.getCooldownRatio();
            if (cdRatio > 0) {
                g.setColor(new Color(0, 0, 0, 160));
                g.fillRect(sx, sy, slotSize, (int)(slotSize * cdRatio));
            }

            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 10));
            g.setColor(cdRatio > 0 ? Color.GRAY : Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            String label = s.getName();
            g.drawString(label, sx + (slotSize - fm.stringWidth(label)) / 2,
                         sy + slotSize / 2 + 3);

            g.setStroke(new BasicStroke(1.5f));
            g.setColor(cdRatio > 0 ? new Color(80, 80, 120) : new Color(120, 180, 255));
            g.drawRoundRect(sx, sy, slotSize, slotSize, 6, 6);
            g.setStroke(new BasicStroke(1f));

            g.setFont(new Font("Arial", Font.BOLD, 9));
            g.setColor(new Color(200, 200, 200));
            g.drawString("[" + actions[i] + "]", sx + 2, sy + slotSize - 3);

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
