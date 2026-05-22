package maplestory.core;

import maplestory.entity.Monster;
import maplestory.entity.MonsterType;
import maplestory.entity.NPC;
import maplestory.entity.Player;
import maplestory.input.InputHandler;
import maplestory.item.DropItem;
import maplestory.job.Skill;
import maplestory.keybind.ActionType;
import maplestory.keybind.KeyBindingManager;
import maplestory.audio.SFX;
import maplestory.audio.SoundManager;
import maplestory.map.BaseMap;
import maplestory.quest.QuestManager;
import maplestory.ui.DialoguePanel;
import maplestory.ui.EquipPanel;
import maplestory.ui.Hotbar;
import maplestory.ui.InventoryPanel;
import maplestory.ui.KeyBindingPanel;
import maplestory.ui.PauseMenu;
import maplestory.ui.ShopPanel;
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
    private final SkillPanel      skillPanel      = new SkillPanel();
    private final EquipPanel      equipPanel      = new EquipPanel();
    private final InventoryPanel  inventoryPanel  = new InventoryPanel();
    private final ShopPanel       shopPanel       = new ShopPanel();
    private final KeyBindingPanel keybindPanel;
    /** 目前顯示的面板（null = 無）。可選值："status"|"skill"|"equip"|"inventory"|"shop"|"keybind" */
    private String activePanel = null;

    // ── Hotbar（快捷欄）────────────────────────────────────────
    private final Hotbar hotbar = new Hotbar();

    // ── NPC 互動 ─────────────────────────────────────────────
    /** 目前玩家旁邊可互動的商店 NPC（null = 無） */
    private NPC nearShopNpc     = null;
    /** 目前玩家旁邊可互動的對話 NPC（null = 無） */
    private NPC nearDialogueNpc = null;
    private static final double NPC_INTERACT_RANGE = 80;

    // ── 任務系統 ─────────────────────────────────────────────
    private final QuestManager questManager = new QuestManager();

    // ── 對話面板 ─────────────────────────────────────────────
    private final DialoguePanel dialoguePanel = new DialoguePanel();

    // ── 掉落物 ───────────────────────────────────────────────
    private final List<DropItem> drops = new ArrayList<>();

    // ── 撿取提示文字 ─────────────────────────────────────────
    private String pickupNotice      = "";
    private double pickupNoticeTimer = 0;

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
            String mapId = SaveManager.load(slot, player, questManager);
            mapManager.switchMap(mapId, 200, 350, player);
            camera.snapTo(player.getX(), player.getY(), mapManager.getCurrentMap().getMapWidth());
        }

        // ── UI 面板 ──────────────────────────────────────────
        statusPanel  = new StatusPanel();
        keybindPanel = new KeyBindingPanel(keyBindings);

        // ── 新手森林一區（史萊姆 ×12）────────────────────────
        novice1Monsters.add(new Monster( 350, 300, MonsterType.SLIME));
        novice1Monsters.add(new Monster( 490, 300, MonsterType.SLIME));
        novice1Monsters.add(new Monster( 630, 300, MonsterType.SLIME));
        novice1Monsters.add(new Monster( 770, 300, MonsterType.SLIME));
        novice1Monsters.add(new Monster( 920, 300, MonsterType.SLIME));
        novice1Monsters.add(new Monster(1060, 300, MonsterType.SLIME));
        novice1Monsters.add(new Monster(1200, 300, MonsterType.SLIME));
        novice1Monsters.add(new Monster(1340, 300, MonsterType.SLIME));
        novice1Monsters.add(new Monster(1480, 300, MonsterType.SLIME));
        novice1Monsters.add(new Monster(1620, 300, MonsterType.SLIME));
        novice1Monsters.add(new Monster( 430, 220, MonsterType.SLIME));
        novice1Monsters.add(new Monster( 980, 220, MonsterType.SLIME));

        // ── 新手森林二區（史萊姆 ×8 + 蝙蝠 ×8）────────────
        novice2Monsters.add(new Monster( 300, 300, MonsterType.SLIME));
        novice2Monsters.add(new Monster( 470, 300, MonsterType.SLIME));
        novice2Monsters.add(new Monster( 680, 300, MonsterType.SLIME));
        novice2Monsters.add(new Monster( 900, 300, MonsterType.SLIME));
        novice2Monsters.add(new Monster(1100, 300, MonsterType.SLIME));
        novice2Monsters.add(new Monster(1330, 300, MonsterType.SLIME));
        novice2Monsters.add(new Monster(1560, 300, MonsterType.SLIME));
        novice2Monsters.add(new Monster(1780, 300, MonsterType.SLIME));
        novice2Monsters.add(new Monster( 400, 200, MonsterType.BAT));
        novice2Monsters.add(new Monster( 590, 200, MonsterType.BAT));
        novice2Monsters.add(new Monster( 810, 200, MonsterType.BAT));
        novice2Monsters.add(new Monster(1010, 200, MonsterType.BAT));
        novice2Monsters.add(new Monster(1240, 200, MonsterType.BAT));
        novice2Monsters.add(new Monster(1440, 200, MonsterType.BAT));
        novice2Monsters.add(new Monster(1670, 200, MonsterType.BAT));
        novice2Monsters.add(new Monster(1880, 200, MonsterType.BAT));

        // ── 新手森林三區（野豬 ×8 + 蝙蝠 ×10）──────────────
        novice3Monsters.add(new Monster( 350, 290, MonsterType.BOAR));
        novice3Monsters.add(new Monster( 540, 290, MonsterType.BOAR));
        novice3Monsters.add(new Monster( 780, 290, MonsterType.BOAR));
        novice3Monsters.add(new Monster(1010, 290, MonsterType.BOAR));
        novice3Monsters.add(new Monster(1250, 290, MonsterType.BOAR));
        novice3Monsters.add(new Monster(1490, 290, MonsterType.BOAR));
        novice3Monsters.add(new Monster(1720, 290, MonsterType.BOAR));
        novice3Monsters.add(new Monster(1960, 290, MonsterType.BOAR));
        novice3Monsters.add(new Monster( 280, 200, MonsterType.BAT));
        novice3Monsters.add(new Monster( 470, 200, MonsterType.BAT));
        novice3Monsters.add(new Monster( 660, 200, MonsterType.BAT));
        novice3Monsters.add(new Monster( 870, 200, MonsterType.BAT));
        novice3Monsters.add(new Monster(1100, 200, MonsterType.BAT));
        novice3Monsters.add(new Monster(1330, 200, MonsterType.BAT));
        novice3Monsters.add(new Monster(1560, 200, MonsterType.BAT));
        novice3Monsters.add(new Monster(1790, 200, MonsterType.BAT));
        novice3Monsters.add(new Monster(2020, 200, MonsterType.BAT));
        novice3Monsters.add(new Monster( 180, 200, MonsterType.BAT));

        // ── 冒險平原（史萊姆 ×10 + 野豬 ×8 + 蝙蝠 ×8）──────
        battleMonsters.add(new Monster( 280, 300, MonsterType.SLIME));
        battleMonsters.add(new Monster( 440, 300, MonsterType.SLIME));
        battleMonsters.add(new Monster( 610, 300, MonsterType.SLIME));
        battleMonsters.add(new Monster( 780, 300, MonsterType.SLIME));
        battleMonsters.add(new Monster( 960, 300, MonsterType.SLIME));
        battleMonsters.add(new Monster(1140, 300, MonsterType.SLIME));
        battleMonsters.add(new Monster(1330, 300, MonsterType.SLIME));
        battleMonsters.add(new Monster(1510, 300, MonsterType.SLIME));
        battleMonsters.add(new Monster(1690, 300, MonsterType.SLIME));
        battleMonsters.add(new Monster(1870, 300, MonsterType.SLIME));
        battleMonsters.add(new Monster( 360, 290, MonsterType.BOAR));
        battleMonsters.add(new Monster( 550, 290, MonsterType.BOAR));
        battleMonsters.add(new Monster( 750, 290, MonsterType.BOAR));
        battleMonsters.add(new Monster( 970, 290, MonsterType.BOAR));
        battleMonsters.add(new Monster(1180, 290, MonsterType.BOAR));
        battleMonsters.add(new Monster(1390, 290, MonsterType.BOAR));
        battleMonsters.add(new Monster(1600, 290, MonsterType.BOAR));
        battleMonsters.add(new Monster(1810, 290, MonsterType.BOAR));
        battleMonsters.add(new Monster( 420, 200, MonsterType.BAT));
        battleMonsters.add(new Monster( 650, 200, MonsterType.BAT));
        battleMonsters.add(new Monster( 880, 200, MonsterType.BAT));
        battleMonsters.add(new Monster(1110, 200, MonsterType.BAT));
        battleMonsters.add(new Monster(1340, 200, MonsterType.BAT));
        battleMonsters.add(new Monster(1570, 200, MonsterType.BAT));
        battleMonsters.add(new Monster(1760, 200, MonsterType.BAT));
        battleMonsters.add(new Monster(1930, 200, MonsterType.BAT));

        // ── 極地冰原（冰晶史萊姆 ×10 + 極地熊 ×6 + 冰蝠 ×8）
        arcticMonsters.add(new Monster( 300, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster( 500, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster( 700, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster( 920, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster(1130, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster(1360, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster(1600, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster(1820, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster(2050, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster(2250, 300, MonsterType.ICE_SLIME));
        arcticMonsters.add(new Monster( 420, 290, MonsterType.POLAR_BEAR));
        arcticMonsters.add(new Monster( 780, 290, MonsterType.POLAR_BEAR));
        arcticMonsters.add(new Monster(1180, 290, MonsterType.POLAR_BEAR));
        arcticMonsters.add(new Monster(1560, 290, MonsterType.POLAR_BEAR));
        arcticMonsters.add(new Monster(1940, 290, MonsterType.POLAR_BEAR));
        arcticMonsters.add(new Monster(2200, 290, MonsterType.POLAR_BEAR));
        arcticMonsters.add(new Monster( 360, 200, MonsterType.ICE_BAT));
        arcticMonsters.add(new Monster( 620, 200, MonsterType.ICE_BAT));
        arcticMonsters.add(new Monster( 880, 200, MonsterType.ICE_BAT));
        arcticMonsters.add(new Monster(1120, 200, MonsterType.ICE_BAT));
        arcticMonsters.add(new Monster(1380, 200, MonsterType.ICE_BAT));
        arcticMonsters.add(new Monster(1660, 200, MonsterType.ICE_BAT));
        arcticMonsters.add(new Monster(1910, 200, MonsterType.ICE_BAT));
        arcticMonsters.add(new Monster(2160, 200, MonsterType.ICE_BAT));

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

                // M 鍵靜音（固定鍵，不可改綁，需在 getAction 之前處理）
                if (kc == KeyEvent.VK_M) {
                    SoundManager.get().toggleMute();
                    return;
                }

                ActionType action = keyBindings.getAction(kc);

                // 對話面板開著時：↑↓ 導航、Enter 確認、其餘按鍵忽略
                if ("dialogue".equals(activePanel)) {
                    if (kc == KeyEvent.VK_UP)    { dialoguePanel.navUp();   return; }
                    if (kc == KeyEvent.VK_DOWN)  { dialoguePanel.navDown(); return; }
                    if (kc == KeyEvent.VK_ENTER) {
                        handleDialogueConfirm(dialoguePanel.confirm());
                        return;
                    }
                    if (action == ActionType.UI_INTERACT) { handleInteract(); return; }
                    return; // 其他按鍵對話期間忽略
                }

                // 按鍵配置面板開著時，任何按鍵都關閉它
                if ("keybind".equals(activePanel)) {
                    activePanel = null;
                    keyBindings.saveToFile();
                    return;
                }

                if (action == null) return;

                switch (action) {
                    case UI_STATUS    -> togglePanel("status");
                    case UI_SKILL     -> togglePanel("skill");
                    case UI_EQUIP     -> togglePanel("equip");
                    case UI_INVENTORY -> togglePanel("inventory");
                    case UI_INTERACT  -> handleInteract();
                    case UI_KEYBIND   -> togglePanel("keybind");
                    case HOTBAR_1     -> handleHotbar(0);
                    case HOTBAR_2     -> handleHotbar(1);
                    case HOTBAR_3     -> handleHotbar(2);
                    case HOTBAR_4     -> handleHotbar(3);
                    case HOTBAR_5     -> handleHotbar(4);
                    default           -> {}
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
                } else if ("inventory".equals(activePanel)) {
                    String result = inventoryPanel.mouseClicked(lp.x, lp.y, player);
                    if (result != null) handleConsumableResult(result);
                } else if ("equip".equals(activePanel)) {
                    equipPanel.mouseClicked(lp.x, lp.y, player);
                } else if ("shop".equals(activePanel)) {
                    shopPanel.mouseClicked(lp.x, lp.y, player);
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
                if (paused) {
                    pauseMenu.updateHover(lp.x, lp.y);
                } else if ("inventory".equals(activePanel)) {
                    inventoryPanel.mouseMoved(lp.x, lp.y);
                } else if ("equip".equals(activePanel)) {
                    equipPanel.mouseMoved(lp.x, lp.y);
                } else if ("shop".equals(activePanel)) {
                    shopPanel.mouseMoved(lp.x, lp.y);
                }
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

    /** F 鍵互動：對話 NPC → 開對話；商店 NPC → 開商店；已開 → 關閉 */
    private void handleInteract() {
        if ("dialogue".equals(activePanel)) { activePanel = null; return; }
        if ("shop".equals(activePanel))     { activePanel = null; return; }
        if (nearDialogueNpc != null) {
            openDialogue(nearDialogueNpc);
            return;
        }
        if (nearShopNpc != null) {
            String shopId   = nearShopNpc.getShopId();
            String curMapId = mapManager.getCurrentMap().getMapId();
            if ("item".equals(shopId)) {
                shopPanel.open("道具商人的商店", ShopPanel.itemShopEntries(), "item");
            } else if ("weapon".equals(shopId)) {
                // 前線前哨站或冰原驛站使用進階裝備清單
                boolean advanced = "frontier".equals(curMapId) || "icepost".equals(curMapId);
                java.util.List<maplestory.item.ShopEntry> entries = advanced
                    ? ShopPanel.frontierWeaponEntries()
                    : ShopPanel.weaponShopEntries();
                shopPanel.open("武器鐵匠的商店", entries, "weapon");
            }
            activePanel = "shop";
        }
    }

    /** 開啟 NPC 對話面板 */
    private void openDialogue(NPC npc) {
        String did = npc.getDialogueId();
        if (did == null) return;

        QuestManager.DialogueData dd;
        if ("elder".equals(did) || "frontier_elder".equals(did)) {
            dd = questManager.getElderDialogue();
        } else if (did.startsWith("job_")) {
            dd = questManager.getJobMasterDialogue(did, player);
        } else {
            return;
        }
        dialoguePanel.open(dd.npcName, dd.text, dd.options, dd.actionIds);
        activePanel = "dialogue";
    }

    /** 處理對話選項確認 */
    private void handleDialogueConfirm(String actionId) {
        if (actionId == null || "dismiss".equals(actionId)) {
            activePanel = null;
            return;
        }
        // 任務相關
        if (actionId.startsWith("accept_")) {
            int id = Integer.parseInt(actionId.substring(7));
            questManager.acceptQuest(id);
            if (nearDialogueNpc != null) openDialogue(nearDialogueNpc);
            return;
        }
        if (actionId.startsWith("complete_")) {
            int id = Integer.parseInt(actionId.substring(9));
            questManager.completeQuest(id, player);
            if (nearDialogueNpc != null) openDialogue(nearDialogueNpc);
            return;
        }
        if (actionId.startsWith("abandon_")) {
            int id = Integer.parseInt(actionId.substring(8));
            questManager.abandonQuest(id);
            if (nearDialogueNpc != null) openDialogue(nearDialogueNpc);
            return;
        }
        // 轉職確認
        if (actionId.endsWith("_confirm") && actionId.startsWith("job_")) {
            String jobType = actionId.replace("_confirm", "");
            if (player.canChangeJob() && player.getGold() >= 5000
                    && player.getTotalKills() >= 30) {
                player.spendGold(5000);
                maplestory.job.Job newJob = switch (jobType) {
                    case "job_warrior" -> new maplestory.job.Warrior();
                    case "job_mage"    -> new maplestory.job.Mage();
                    case "job_archer"  -> new maplestory.job.Archer();
                    default            -> null;
                };
                if (newJob != null) {
                    player.changeJob(newJob);
                    maplestory.audio.SoundManager.get()
                        .playSFX(maplestory.audio.SFX.LEVEL_UP);
                }
            }
            activePanel = null;
            return;
        }
        activePanel = null;
    }

    /** 處理撿取掉落物：金幣加入玩家，道具加入背包，並顯示提示文字 */
    private void handlePickup(DropItem drop) {
        switch (drop.getType()) {
            case GOLD -> {
                player.gainGold(drop.getGoldAmount());
                pickupNotice      = "+" + drop.getGoldAmount() + " G";
                pickupNoticeTimer = 1.5;
                SoundManager.get().playSFX(SFX.ITEM_PICKUP);
            }
            case CONSUMABLE -> {
                boolean ok = player.getInventory().addConsumable(drop.getConsumable());
                pickupNotice      = ok ? "撿取：" + drop.getConsumable().getName()
                                       : "消耗品背包已滿！";
                pickupNoticeTimer = 1.5;
                if (ok) SoundManager.get().playSFX(SFX.ITEM_PICKUP);
            }
            case EQUIPMENT -> {
                boolean ok = player.getInventory().addEquipment(drop.getEquipment());
                pickupNotice      = ok ? "撿取：" + drop.getEquipment().getName()
                                       : "裝備背包已滿！";
                pickupNoticeTimer = 1.5;
                if (ok) SoundManager.get().playSFX(SFX.ITEM_PICKUP);
            }
        }
    }

    /**
     * 處理消耗品使用結果字串（藥水 or 傳送卷軸）。
     * InventoryPanel 使用藥水後回傳 result，此方法統一解析。
     */
    private void handleConsumableResult(String result) {
        if (result == null || result.isEmpty()) return;
        if (result.startsWith("TELEPORT:")) {
            String targetMapId = result.substring(9);
            int gY = GamePanel.GAME_HEIGHT - 40;
            double spawnX = switch (targetMapId) {
                case "village"  -> 400;
                case "frontier" -> 400;
                case "battle"   -> 300;
                default         -> 200;
            };
            mapManager.switchMap(targetMapId, spawnX, gY - 80, player);
            camera.snapTo(player.getX(), player.getY(),
                          mapManager.getCurrentMap().getMapWidth());
            pickupNotice      = "傳送至：" + mapManager.getCurrentMap().getMapName();
            pickupNoticeTimer = 2.0;
        } else if (!result.isEmpty()) {
            pickupNotice      = result;
            pickupNoticeTimer = 1.5;
        }
    }

    /** 切換面板：再按一次關閉 */
    private void togglePanel(String name) {
        activePanel = name.equals(activePanel) ? null : name;
        SoundManager.get().playSFX(SFX.UI_CLICK);
    }

    /** 根據目前地圖回傳對應的怪物列表（村莊類地圖回傳空列表） */
    private List<Monster> currentMonsters() {
        return switch (mapManager.getCurrentMap().getMapId()) {
            case "novice1"   -> novice1Monsters;
            case "novice2"   -> novice2Monsters;
            case "novice3"   -> novice3Monsters;
            case "battle"    -> battleMonsters;
            case "arctic"    -> arcticMonsters;
            default          -> java.util.Collections.emptyList(); // village/frontier/icepost
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
        SaveManager.save(saveSlot, player, mapManager.getCurrentMap().getMapId(), questManager);
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
            camera.snapTo(player.getX(), player.getY(),
                          mapManager.getCurrentMap().getMapWidth());
            questManager.onMapEntered(mapManager.getCurrentMap().getMapId());
            // 進入新地圖時，強制重生所有怪物（讓玩家回到同地圖時怪物已復活）
            for (Monster m : currentMonsters()) m.forceRespawn();
            drops.clear(); // 清除上個地圖殘留的掉落物
        }

        // 技能輸入
        List<Monster> curMonsters = currentMonsters();
        int pendingSkill = inputHandler.pollPendingSkill();
        if (pendingSkill >= 0 && !curMonsters.isEmpty()) {
            player.useSkill(pendingSkill, curMonsters);
            SoundManager.get().playSFX(pendingSkill == 0 ? SFX.SKILL_THRUST : SFX.SKILL_SHOCKWAVE);
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
                if (m.pollJustDied()) {
                    player.gainExp(m.getExpReward());
                    player.addKill();
                    questManager.onMonsterKilled(m.getType());
                }
                if (m.pollDropPending()) {
                    drops.addAll(m.rollDrops());
                }
            }
        } else {
            prevAttacking = false;
        }

        // 掉落物更新 + 自動撿取
        if (pickupNoticeTimer > 0) pickupNoticeTimer -= dt;
        drops.removeIf(drop -> {
            drop.update(dt);
            if (drop.tryPickup(player)) {
                handlePickup(drop);
                return true;
            }
            return drop.isExpired();
        });

        // 面板計時更新
        if ("inventory".equals(activePanel)) inventoryPanel.update(dt);
        if ("equip".equals(activePanel))     equipPanel.update(dt);
        if ("shop".equals(activePanel))      shopPanel.update(dt);
        if ("dialogue".equals(activePanel))  dialoguePanel.update(dt);

        // NPC 更新 + 近距離偵測
        nearShopNpc     = null;
        nearDialogueNpc = null;
        for (NPC npc : currentMap.getNPCs()) {
            npc.update(dt);
            boolean near = npc.isNearPlayer(player.getX(), player.getY(), NPC_INTERACT_RANGE);
            npc.setShowHint(near);
            if (near) {
                if (npc.hasShop())     nearShopNpc     = npc;
                if (npc.hasDialogue()) nearDialogueNpc = npc;
            }
        }
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
            case "status"    -> statusPanel.draw(g2d, player);
            case "skill"     -> skillPanel.draw(g2d);
            case "equip"     -> equipPanel.draw(g2d, player);
            case "inventory" -> inventoryPanel.draw(g2d, player);
            case "shop"      -> shopPanel.draw(g2d, player);
            case "keybind"   -> keybindPanel.draw(g2d);
            case "dialogue"  -> dialoguePanel.draw(g2d);
        }

        // 等級不足攔截提示（顯示在遊戲畫面底部）
        String blocked = mapManager.getLevelBlockedNotice();
        if (blocked != null) {
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
            FontMetrics bfm = g2d.getFontMetrics();
            int bw = bfm.stringWidth(blocked);
            int bx = (SCREEN_WIDTH - bw) / 2;
            int by = GAME_HEIGHT - 25;
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRoundRect(bx - 10, by - 18, bw + 20, 24, 8, 8);
            g2d.setColor(new Color(255, 80, 80));
            g2d.drawString(blocked, bx, by);
        }

        // 撿取提示（浮動於 HUD 上方）
        if (pickupNoticeTimer > 0) {
            float alpha = (float) Math.min(1.0, pickupNoticeTimer);
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
            g2d.setColor(new Color(220, 220, 100, (int)(alpha * 220)));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(pickupNotice,
                           (SCREEN_WIDTH - fm.stringWidth(pickupNotice)) / 2,
                           GAME_HEIGHT - 20);
        }

        // 暫停選單（最頂層）
        if (paused) pauseMenu.draw(g2d);
    }

    /** 繪製遊戲世界 */
    private void drawGameArea(Graphics2D g) {
        BaseMap currentMap = mapManager.getCurrentMap();

        // 天空漸層（極地地圖自己畫夜空，novice3 畫傍晚，各 novice 地圖也自繪天空）
        String mapId = currentMap.getMapId();
        if (!mapId.equals("arctic") && !mapId.startsWith("novice") && !mapId.equals("icepost")) {
            GradientPaint sky = new GradientPaint(
                0, 0,           new Color(100, 180, 240),
                0, GAME_HEIGHT, new Color(170, 220, 255)
            );
            g.setPaint(sky);
            g.fillRect(0, 0, SCREEN_WIDTH, GAME_HEIGHT);
        }

        currentMap.draw(g, camera);

        // 掉落物（在怪物下層，玩家下層）
        for (DropItem drop : drops) drop.draw(g, camera);

        for (Monster m : currentMonsters()) m.draw(g, camera);

        if (player.getJob() != null) {
            player.getJob().drawEffects(g, camera);
        }

        player.draw(g, camera);
    }

    /**
     * 繪製底部 HUD（重新設計，三排無重疊）
     *
     * 左區(0-202): HP/MP 條
     * 中左(205-400): Lv/職業 + 地圖 + 金幣
     * 中右(405-510): 技能槽（Q/W）
     * 右區(514-800): 四個快捷按鈕（每個 71px，3px 間距）
     * 底部：EXP 條
     */
    private void drawHUD(Graphics2D g) {
        int hudY = GAME_HEIGHT; // 500

        // ── HUD 底板 ─────────────────────────────────────────
        g.setColor(new Color(15, 15, 35));
        g.fillRect(0, hudY, SCREEN_WIDTH, HUD_HEIGHT);
        g.setColor(new Color(60, 60, 120));
        g.drawLine(0, hudY, SCREEN_WIDTH, hudY);

        // ── 左區：HP / MP 條（y+6 / y+28，高 16）───────────
        drawBar(g, 8, hudY + 6, "HP",
                player.getHp(), player.getMaxHp(),
                new Color(160, 25, 25), new Color(210, 55, 55));
        drawBar(g, 8, hudY + 28, "MP",
                player.getMp(), player.getMaxMp(),
                new Color(15, 35, 130), new Color(50, 100, 210));

        // ── 中左區：Lv/職業、地圖名、金幣、互動提示 ────────
        int cx = 207;
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        g.setColor(Color.YELLOW);
        g.drawString("Lv." + player.getLevel() + " " + player.getJobName(), cx, hudY + 18);

        String mapLabel = mapManager.getCurrentMap().getMapName();
        String interactHint = "";
        if (nearDialogueNpc != null)
            interactHint = " [" + getBoundKeyName(ActionType.UI_INTERACT) + "]對話";
        else if (nearShopNpc != null)
            interactHint = " [" + getBoundKeyName(ActionType.UI_INTERACT) + "]購物";

        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(150, 155, 200));
        g.drawString(mapLabel + interactHint, cx, hudY + 34);

        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        g.setColor(new Color(255, 210, 0));
        g.drawString("G " + player.getGold(), cx, hudY + 52);

        // 存檔提示（疊加在中央）
        if (saveNoticeTimer > 0) {
            float alpha = (float) Math.min(1.0, saveNoticeTimer);
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
            g.setColor(new Color(80, 240, 110, (int)(alpha * 230)));
            g.drawString("✔ 存檔成功 (Slot " + saveSlot + ")", cx, hudY + 67);
        }

        // ── 中右區：技能槽（Q/W）─────────────────────────
        if (player.getJob() != null) drawSkillSlots(g, hudY);

        // ── 快捷欄（HUD 中央，y+48）──────────────────────
        hotbar.draw(g, hudY, getHotbarKeyLabels());

        // ── 右區：四個快捷按鈕（71px×22px，間距 3px）──────
        // 共 4×71 + 3×3 = 284+9 = 293px，起點 x = 800-293 = 507
        int btnX = 507, btnY = hudY + 6, btnW = 71, btnH = 22;
        drawHudButton(g, "技能[" + getBoundKeyName(ActionType.UI_SKILL)     + "]", btnX,          btnY, btnW, btnH);
        drawHudButton(g, "背包[" + getBoundKeyName(ActionType.UI_INVENTORY) + "]", btnX + 74,     btnY, btnW, btnH);
        drawHudButton(g, "裝備[" + getBoundKeyName(ActionType.UI_EQUIP)     + "]", btnX + 74 * 2, btnY, btnW, btnH);
        drawHudButton(g, "狀態[" + getBoundKeyName(ActionType.UI_STATUS)    + "]", btnX + 74 * 3, btnY, btnW, btnH);

        // 操作說明（右下角，9pt 小字）
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 9));
        g.setColor(new Color(110, 110, 150));
        String muteLabel = SoundManager.get().isMuted() ? "[靜音]M" : "[音效]M";
        g.drawString("移:AD 跳:W 攻:Z  按鍵[" + getBoundKeyName(ActionType.UI_KEYBIND) + "] F5存 " + muteLabel, 507, hudY + 58);

        // ── EXP 條（最底部 7px）──────────────────────────
        int expY = SCREEN_HEIGHT - 7;
        g.setColor(new Color(0, 40, 0));
        g.fillRect(0, expY, SCREEN_WIDTH, 7);
        g.setColor(new Color(65, 190, 65));
        g.fillRect(0, expY, (int)(SCREEN_WIDTH * player.getExpRatio()), 7);
        g.setColor(new Color(90, 240, 90, 160));
        g.setFont(new Font("Arial", Font.BOLD, 7));
        g.drawString("EXP", 3, expY + 6);
    }



    private String getBoundKeyName(ActionType action) {
        Integer kc = keyBindings.getKeyFor(action);
        return kc != null ? KeyBindingManager.keyName(kc) : "-";
    }

    private String[] getHotbarKeyLabels() {
        ActionType[] acts = {
            ActionType.HOTBAR_1, ActionType.HOTBAR_2, ActionType.HOTBAR_3,
            ActionType.HOTBAR_4, ActionType.HOTBAR_5
        };
        String[] labels = new String[5];
        for (int i = 0; i < 5; i++) {
            Integer kc = keyBindings.getKeyFor(acts[i]);
            labels[i] = kc != null ? KeyBindingManager.keyName(kc) : String.valueOf(i + 1);
        }
        return labels;
    }

    private void handleHotbar(int slot) {
        if ("inventory".equals(activePanel)) {
            maplestory.item.Consumable hovered =
                inventoryPanel.getHoveredConsumable(player.getInventory());
            if (hovered != null) {
                hotbar.assign(slot, hovered);
                pickupNotice      = "已指派「" + hovered.getName() + "」到快捷欄 " + (slot + 1);
                pickupNoticeTimer = 1.5;
            }
        } else if (activePanel == null) {
            String result = hotbar.use(slot, player);
            handleConsumableResult(result);
        }
    }

    /** 技能槽繪製（中右區，y+8 開始，槽 34×34）*/
    private void drawSkillSlots(Graphics2D g, int hudY) {
        if (player.getJob() == null) return;
        List<Skill> skills = player.getJob().getSkills();
        int slotSize = 34, startX = 408;
        String[] actions = {
            getBoundKeyName(ActionType.SKILL_0),
            getBoundKeyName(ActionType.SKILL_1)
        };

        for (int i = 0; i < Math.min(skills.size(), 2); i++) {
            Skill s  = skills.get(i);
            int   sx = startX + i * (slotSize + 4);
            int   sy = hudY + 8;

            g.setColor(new Color(25, 25, 55));
            g.fillRoundRect(sx, sy, slotSize, slotSize, 6, 6);

            double cdRatio = s.getCooldownRatio();
            if (cdRatio > 0) {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(sx, sy, slotSize, (int)(slotSize * cdRatio));
            }

            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 9));
            g.setColor(cdRatio > 0 ? Color.GRAY : Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            String label = s.getName();
            g.drawString(label, sx + (slotSize - fm.stringWidth(label)) / 2,
                         sy + slotSize / 2 + 3);

            g.setStroke(new BasicStroke(1.5f));
            g.setColor(cdRatio > 0 ? new Color(70, 70, 110) : new Color(110, 170, 250));
            g.drawRoundRect(sx, sy, slotSize, slotSize, 6, 6);
            g.setStroke(new BasicStroke(1f));

            g.setFont(new Font("Arial", Font.BOLD, 8));
            g.setColor(new Color(190, 190, 190));
            g.drawString("[" + actions[i] + "]", sx + 2, sy + slotSize - 2);

            g.setFont(new Font("Arial", Font.PLAIN, 8));
            g.setColor(new Color(90, 140, 250));
            g.drawString(s.getMpCost() + "MP", sx + 2, sy + 10);
        }
    }

    // ── HUD 工具方法 ─────────────────────────────────────────

    private void drawBar(Graphics2D g, int x, int y,
                         String label, int cur, int max,
                         Color bg, Color fg) {
        int barW = 192, barH = 16;
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        g.setColor(Color.WHITE);
        g.drawString(label, x, y + 12);
        g.setColor(bg);
        g.fillRect(x + 26, y, barW, barH);
        double ratio = max > 0 ? (double) cur / max : 0;
        g.setColor(fg);
        g.fillRect(x + 26, y, (int)(barW * ratio), barH);
        g.setColor(Color.WHITE);
        g.drawRect(x + 26, y, barW, barH);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString(cur + "/" + max, x + 30, y + 12);
    }

    private void drawHudButton(Graphics2D g, String label, int x, int y, int w, int h) {
        g.setColor(new Color(38, 38, 70));
        g.fillRoundRect(x, y, w, h, 6, 6);
        g.setColor(new Color(90, 90, 165));
        g.drawRoundRect(x, y, w, h, 6, 6);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + h - 5);
    }
}
