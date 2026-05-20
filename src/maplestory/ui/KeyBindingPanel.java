package maplestory.ui;

import maplestory.keybind.ActionType;
import maplestory.keybind.KeyBindingManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * 按鍵配置面板（B 鍵開啟）。
 *
 * 佈局：
 *   左側  — 動作面板（可拖曳的動作 Chip）
 *   右側  — 視覺化鍵盤（顯示目前綁定）
 *
 * 互動方式：
 *   拖曳 Chip → 放到按鍵上     → 綁定
 *   拖曳 Chip → 放到另一個鍵   → 移動綁定
 *   拖曳已綁鍵 → 放到其他鍵    → 換位
 *   拖曳已綁鍵 → 放到面板區域  → 解除綁定
 *   點擊 [重置] 按鈕           → 還原預設
 */
public class KeyBindingPanel {

    // ── 面板邊界（螢幕絕對座標）───────────────────────────────
    private static final int PX = 12, PY = 10;
    private static final int PW = 776, PH = 548;

    // ── 動作面板（左欄）─────────────────────────────────────
    private static final int PAL_X    = PX + 10;
    private static final int PAL_Y    = PY + 48;
    private static final int PAL_W    = 178;
    private static final int CHIP_H   = 30;
    private static final int CHIP_GAP = 6;

    // ── 鍵盤起點 ─────────────────────────────────────────────
    /** 鍵盤繪製原點（絕對座標） */
    private static final int KB_X = PAL_X + PAL_W + 14;   // ≈ 214
    private static final int KB_Y = PAL_Y + 30;           // ≈ 88

    // ── 按鍵格子尺寸 ─────────────────────────────────────────
    private static final int KS = 36;        // key size（正方形邊長）
    private static final int KG = 4;         // key gap
    private static final int KP = KS + KG;  // key pitch = 40

    // ── Reset 按鈕 ───────────────────────────────────────────
    private final Rectangle resetBtn = new Rectangle(PX + PW - 100, PY + 10, 84, 24);

    // ── 資料 ─────────────────────────────────────────────────
    private final KeyBindingManager manager;

    /** 鍵盤上每一個按鍵的描述物件 */
    private final List<KeyCell> keyCells = new ArrayList<>();

    /** 動作面板每個 Chip 的描述物件 */
    private final List<ActionChip> actionChips = new ArrayList<>();

    // ── 拖曳狀態 ─────────────────────────────────────────────
    private ActionType draggedAction = null;   // 正在拖曳的動作（null = 沒在拖）
    private int        dragX, dragY;            // 滑鼠目前位置
    private Integer    dragSourceKeyCode = null; // 若從按鍵開始拖，記錄來源 keyCode

    // ─────────────────────────────────────────────────────────
    // 內部資料類別
    // ─────────────────────────────────────────────────────────

    /** 鍵盤上一個按鍵的幾何 + 標籤資料 */
    private static class KeyCell {
        final String    label;    // 顯示文字（"Q"、"←"、"Space"）
        final int       keyCode;  // Java VK 常數
        final Rectangle rect;     // 螢幕絕對座標

        KeyCell(String label, int keyCode, int x, int y, int w, int h) {
            this.label   = label;
            this.keyCode = keyCode;
            this.rect    = new Rectangle(x, y, w, h);
        }
    }

    /** 動作面板中一個可拖曳的動作格子 */
    private static class ActionChip {
        final ActionType type;
        final Rectangle  rect;
        ActionChip(ActionType t, Rectangle r) { type = t; rect = r; }
    }

    // ─────────────────────────────────────────────────────────
    // 建構子
    // ─────────────────────────────────────────────────────────
    public KeyBindingPanel(KeyBindingManager manager) {
        this.manager = manager;
        buildKeyboard();
        buildActionChips();
    }

    // ─────────────────────────────────────────────────────────
    // 初始化：鍵盤配置
    // ─────────────────────────────────────────────────────────

    /**
     * 建立所有按鍵格子（QWERTY 佈局 + 方向鍵）。
     * 每列有不同的水平偏移量，模擬真實鍵盤的階梯排列。
     */
    private void buildKeyboard() {
        // ── 數字列（Row 0，無偏移）───────────────────────────
        addRow(new String[]{"1","2","3","4","5","6","7","8","9","0"},
               new int[]{KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4,
                         KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8,
                         KeyEvent.VK_9, KeyEvent.VK_0},
               0, 0);

        // ── QWERTY 列（Row 1，偏移 10px）────────────────────
        addRow(new String[]{"Q","W","E","R","T","Y","U","I","O","P"},
               new int[]{KeyEvent.VK_Q, KeyEvent.VK_W, KeyEvent.VK_E, KeyEvent.VK_R,
                         KeyEvent.VK_T, KeyEvent.VK_Y, KeyEvent.VK_U, KeyEvent.VK_I,
                         KeyEvent.VK_O, KeyEvent.VK_P},
               10, 1);

        // ── Home 列（Row 2，偏移 16px）──────────────────────
        addRow(new String[]{"A","S","D","F","G","H","J","K","L"},
               new int[]{KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_F,
                         KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_J, KeyEvent.VK_K,
                         KeyEvent.VK_L},
               16, 2);

        // ── Bottom 列（Row 3，偏移 28px）────────────────────
        addRow(new String[]{"Z","X","C","V","B","N","M"},
               new int[]{KeyEvent.VK_Z, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_V,
                         KeyEvent.VK_B, KeyEvent.VK_N, KeyEvent.VK_M},
               28, 3);

        // ── Space（寬鍵，偏移 60px，寬 204px）───────────────
        int spaceY = KB_Y + 4 * KP + 6;
        keyCells.add(new KeyCell("Space", KeyEvent.VK_SPACE,
                                 KB_X + 60, spaceY, 204, KS));

        // ── 方向鍵（右側獨立區塊）───────────────────────────
        int arX = KB_X + 10 * KP + 16; // 數字列最右鍵右邊再留 16px
        int arY = KB_Y + 2 * KP;        // 從 Row 2 的 Y 開始
        keyCells.add(new KeyCell("↑", KeyEvent.VK_UP,    arX + KP,       arY,       KS, KS));
        keyCells.add(new KeyCell("←", KeyEvent.VK_LEFT,  arX,             arY + KP,  KS, KS));
        keyCells.add(new KeyCell("↓", KeyEvent.VK_DOWN,  arX + KP,       arY + KP,  KS, KS));
        keyCells.add(new KeyCell("→", KeyEvent.VK_RIGHT, arX + 2 * KP,   arY + KP,  KS, KS));
    }

    /** 工具方法：批次加入一列按鍵 */
    private void addRow(String[] labels, int[] keyCodes, int xOffset, int row) {
        for (int i = 0; i < labels.length; i++) {
            keyCells.add(new KeyCell(
                labels[i], keyCodes[i],
                KB_X + xOffset + i * KP,
                KB_Y + row * KP,
                KS, KS
            ));
        }
    }

    // ─────────────────────────────────────────────────────────
    // 初始化：動作 Chip 列表
    // ─────────────────────────────────────────────────────────

    /**
     * 建立左側動作面板中的所有 Chip 格子。
     * 自動按照 ActionType 的宣告順序排列，
     * 且按 Category 分組顯示。
     */
    private void buildActionChips() {
        actionChips.clear();
        int y = PAL_Y + 28; // 分類標題留 28px

        ActionType.Category lastCat = null;
        for (ActionType a : ActionType.values()) {
            // 遇到新分類時多留一點間距
            if (a.category != lastCat) {
                if (lastCat != null) y += 10; // 分類間隔
                y += 20; // 分類標題高度
                lastCat = a.category;
            }
            actionChips.add(new ActionChip(a,
                new Rectangle(PAL_X, y, PAL_W, CHIP_H)));
            y += CHIP_H + CHIP_GAP;
        }
    }

    // ─────────────────────────────────────────────────────────
    // 繪製
    // ─────────────────────────────────────────────────────────

    public void draw(Graphics2D g) {
        // ── 遮罩（半透明黑） ──────────────────────────────────
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, 800, 580);

        // ── 面板主體 ──────────────────────────────────────────
        g.setColor(new Color(18, 20, 40));
        g.fillRoundRect(PX, PY, PW, PH, 14, 14);
        g.setColor(new Color(80, 90, 160));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(PX, PY, PW, PH, 14, 14);

        // ── 標題列 ───────────────────────────────────────────
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        g.setColor(new Color(200, 210, 255));
        g.drawString("⌨  按鍵配置", PX + 14, PY + 26);

        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(130, 130, 170));
        g.drawString("B 鍵關閉 | 拖曳動作到按鍵以綁定 | 拖離鍵盤以解除", PX + 14, PY + 42);

        // ── Reset 按鈕 ────────────────────────────────────────
        drawResetButton(g);

        // ── 分隔線 ───────────────────────────────────────────
        g.setColor(new Color(60, 65, 120));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(PX + PAL_W + 20, PY + 48, PX + PAL_W + 20, PY + PH - 10);

        // ── 左側：動作面板 ────────────────────────────────────
        drawPalette(g);

        // ── 右側：鍵盤 ───────────────────────────────────────
        drawKeyboard(g);

        // ── 拖曳中的 Ghost ────────────────────────────────────
        if (draggedAction != null) {
            drawDragGhost(g);
        }
    }

    // ── 動作面板（左側）─────────────────────────────────────

    private void drawPalette(Graphics2D g) {
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        g.setColor(new Color(160, 170, 210));
        g.drawString("可用動作", PAL_X, PAL_Y + 16);

        ActionType.Category lastCat = null;
        int chipIdx = 0;
        int y = PAL_Y + 28;

        for (ActionType a : ActionType.values()) {
            // 分類標題
            if (a.category != lastCat) {
                if (lastCat != null) y += 10;
                g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
                g.setColor(new Color(100, 110, 150));
                g.drawString("── " + a.category.label + " ──", PAL_X, y + 14);
                y += 20;
                lastCat = a.category;
            }

            // Chip 本體
            ActionChip chip = actionChips.get(chipIdx++);
            boolean isDragging = (draggedAction == a);

            // 底色（拖曳中的 chip 變暗）
            Color bgColor = isDragging
                ? new Color(a.color.getRed()/4, a.color.getGreen()/4, a.color.getBlue()/4, 120)
                : new Color(a.color.getRed()/3, a.color.getGreen()/3, a.color.getBlue()/3);
            g.setColor(bgColor);
            g.fillRoundRect(chip.rect.x, chip.rect.y, chip.rect.width, chip.rect.height, 6, 6);

            // 邊框
            g.setColor(isDragging ? a.color.darker() : a.color);
            g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(chip.rect.x, chip.rect.y, chip.rect.width, chip.rect.height, 6, 6);

            // 動作名稱
            g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
            g.setColor(isDragging ? new Color(150, 150, 150) : Color.WHITE);
            g.drawString(a.displayName, chip.rect.x + 8, chip.rect.y + CHIP_H - 9);

            // 目前綁定的按鍵標示（右側小字）
            Integer boundKey = manager.getKeyFor(a);
            if (boundKey != null) {
                String keyLabel = "[" + KeyBindingManager.keyName(boundKey) + "]";
                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.setColor(new Color(200, 200, 100));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(keyLabel,
                             chip.rect.x + chip.rect.width - fm.stringWidth(keyLabel) - 6,
                             chip.rect.y + CHIP_H - 9);
            }

            y += CHIP_H + CHIP_GAP;
        }
    }

    // ── 鍵盤（右側）─────────────────────────────────────────

    private void drawKeyboard(Graphics2D g) {
        // 鍵盤區標題
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        g.setColor(new Color(160, 170, 210));
        g.drawString("鍵盤配置（拖曳動作到按鍵）", KB_X, PAL_Y + 16);

        // 找滑鼠懸停的按鍵（拖曳中才需要）
        KeyCell hoveredCell = draggedAction != null ? findKeyAt(dragX, dragY) : null;

        for (KeyCell kc : keyCells) {
            ActionType boundAction = manager.getAction(kc.keyCode);
            boolean    isHovered   = (kc == hoveredCell);

            drawKeyCell(g, kc, boundAction, isHovered);
        }
    }

    private void drawKeyCell(Graphics2D g, KeyCell kc, ActionType bound, boolean hovered) {
        Rectangle r = kc.rect;

        // ── 按鍵底色 ─────────────────────────────────────────
        Color bg;
        if (hovered && draggedAction != null) {
            // 懸停且正在拖曳 → 亮色提示可放置
            bg = new Color(60, 80, 50);
        } else if (bound != null) {
            // 有綁定 → 用動作的代表色淡化
            bg = new Color(
                bound.color.getRed()  / 5,
                bound.color.getGreen()/ 5,
                bound.color.getBlue() / 5
            );
        } else {
            // 空白鍵
            bg = new Color(30, 32, 58);
        }
        g.setColor(bg);
        g.fillRoundRect(r.x, r.y, r.width, r.height, 5, 5);

        // ── 按鍵邊框 ─────────────────────────────────────────
        Color border;
        if (hovered && draggedAction != null) {
            border = new Color(120, 220, 80);
        } else if (bound != null) {
            border = bound.color.darker();
        } else {
            border = new Color(55, 60, 100);
        }
        g.setColor(border);
        g.setStroke(new BasicStroke(hovered ? 2f : 1.2f));
        g.drawRoundRect(r.x, r.y, r.width, r.height, 5, 5);
        g.setStroke(new BasicStroke(1f));

        // ── 按鍵標籤（鍵名，右上角小字） ─────────────────────
        g.setFont(new Font("Arial", Font.BOLD, 9));
        g.setColor(new Color(140, 140, 180));
        g.drawString(kc.label, r.x + 3, r.y + 11);

        // ── 綁定動作名稱（置中，白字） ───────────────────────
        if (bound != null) {
            // 顏色條（按鍵底部）
            g.setColor(new Color(bound.color.getRed(), bound.color.getGreen(),
                                  bound.color.getBlue(), 140));
            g.fillRoundRect(r.x + 2, r.y + r.height - 6, r.width - 4, 5, 3, 3);

            // 動作縮寫（最多 4 個字，否則截斷）
            String shortName = abbreviate(bound.displayName, r.width - 6);
            g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 9));
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(shortName,
                         r.x + (r.width  - fm.stringWidth(shortName)) / 2,
                         r.y + r.height - 9);
        }

        // ── 懸停特效（綠色光暈提示） ─────────────────────────
        if (hovered && draggedAction != null) {
            g.setColor(new Color(100, 255, 60, 60));
            g.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 4, 4);
        }
    }

    // ── 拖曳中的 Ghost ────────────────────────────────────────

    private void drawDragGhost(Graphics2D g) {
        int gw = 140, gh = 28;
        int gx = dragX - gw / 2;
        int gy = dragY - gh / 2;

        // 半透明背景
        Color c = draggedAction.color;
        g.setColor(new Color(c.getRed() / 3, c.getGreen() / 3, c.getBlue() / 3, 200));
        g.fillRoundRect(gx, gy, gw, gh, 8, 8);

        // 邊框（動作顏色）
        g.setColor(c);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(gx, gy, gw, gh, 8, 8);
        g.setStroke(new BasicStroke(1f));

        // 動作名稱
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(draggedAction.displayName,
                     gx + (gw - fm.stringWidth(draggedAction.displayName)) / 2,
                     gy + gh - 8);
    }

    // ── Reset 按鈕 ────────────────────────────────────────────

    private void drawResetButton(Graphics2D g) {
        g.setColor(new Color(70, 50, 20));
        g.fillRoundRect(resetBtn.x, resetBtn.y, resetBtn.width, resetBtn.height, 6, 6);
        g.setColor(new Color(180, 130, 50));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(resetBtn.x, resetBtn.y, resetBtn.width, resetBtn.height, 6, 6);
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        g.setColor(new Color(230, 190, 100));
        FontMetrics fm = g.getFontMetrics();
        String label = "還原預設";
        g.drawString(label,
                     resetBtn.x + (resetBtn.width - fm.stringWidth(label)) / 2,
                     resetBtn.y + 16);
    }

    // ─────────────────────────────────────────────────────────
    // 滑鼠事件（由 GamePanel 呼叫）
    // ─────────────────────────────────────────────────────────

    /** 滑鼠按下：開始拖曳 */
    public void mousePressed(int x, int y) {
        // 點擊 Reset 按鈕
        if (resetBtn.contains(x, y)) {
            manager.resetToDefault();
            return;
        }

        // 從鍵盤上拖曳已綁定的動作
        KeyCell kc = findKeyAt(x, y);
        if (kc != null) {
            ActionType bound = manager.getAction(kc.keyCode);
            if (bound != null) {
                draggedAction    = bound;
                dragSourceKeyCode = kc.keyCode;
                dragX = x; dragY = y;
                return;
            }
        }

        // 從動作面板拖曳
        ActionChip chip = findChipAt(x, y);
        if (chip != null) {
            draggedAction     = chip.type;
            dragSourceKeyCode = null; // 從 palette 開始
            dragX = x; dragY = y;
        }
    }

    /** 滑鼠拖曳中：更新 Ghost 位置 */
    public void mouseDragged(int x, int y) {
        if (draggedAction == null) return;
        dragX = x; dragY = y;
    }

    /** 滑鼠放開：完成綁定或解除 */
    public void mouseReleased(int x, int y) {
        if (draggedAction == null) return;

        KeyCell target = findKeyAt(x, y);

        if (target != null) {
            // 放到按鍵上 → 綁定
            manager.bind(target.keyCode, draggedAction);
        } else {
            // 放到鍵盤外 → 如果是從按鍵上拖來的，解除原來的綁定
            if (dragSourceKeyCode != null) {
                // 確認動作還在原鍵上（沒被 bind() 移走）
                if (manager.getAction(dragSourceKeyCode) == draggedAction) {
                    manager.unbind(dragSourceKeyCode);
                }
            }
        }

        // 清除拖曳狀態
        draggedAction     = null;
        dragSourceKeyCode = null;
    }

    // ─────────────────────────────────────────────────────────
    // 工具方法
    // ─────────────────────────────────────────────────────────

    /** 找到包含 (x,y) 的按鍵格子（沒找到回傳 null） */
    private KeyCell findKeyAt(int x, int y) {
        for (KeyCell kc : keyCells) {
            if (kc.rect.contains(x, y)) return kc;
        }
        return null;
    }

    /** 找到包含 (x,y) 的動作 Chip（沒找到回傳 null） */
    private ActionChip findChipAt(int x, int y) {
        for (ActionChip chip : actionChips) {
            if (chip.rect.contains(x, y)) return chip;
        }
        return null;
    }

    /**
     * 截斷過長的字串，加上「…」。
     * 根據可用寬度決定最多顯示幾個字。
     */
    private String abbreviate(String text, int availableWidth) {
        // 每個中文字約 9px（9pt 字體），英文字母約 6px
        // 用字元數粗估：顯示不超過 (availableWidth / 8) 個字
        int maxChars = Math.max(2, availableWidth / 8);
        if (text.length() <= maxChars) return text;
        return text.substring(0, maxChars - 1) + "…";
    }
}
