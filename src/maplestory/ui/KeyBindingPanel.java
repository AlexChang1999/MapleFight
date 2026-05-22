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
    private static final int PX = 12, PY = 8;
    private static final int PW = 776, PH = 562;

    // ── 動作面板（左欄）─────────────────────────────────────
    private static final int PAL_X    = PX + 10;
    private static final int PAL_Y    = PY + 50;
    private static final int PAL_W    = 178;
    private static final int CHIP_H   = 18;
    private static final int CHIP_GAP = 2;

    // ── 鍵盤起點 ─────────────────────────────────────────────
    private static final int KB_X = PAL_X + PAL_W + 14;   // ≈ 214
    private static final int KB_Y = PAL_Y + 30;           // ≈ 88

    // ── 按鍵格子尺寸 ─────────────────────────────────────────
    private static final int KS = 36;        // key size（正方形邊長）
    private static final int KG = 4;         // key gap
    private static final int KP = KS + KG;  // key pitch = 40

    // ── Reset 按鈕 ───────────────────────────────────────────
    private final Rectangle resetBtn = new Rectangle(PX + PW - 100, PY + 12, 84, 24);

    // ── 資料 ─────────────────────────────────────────────────
    private final KeyBindingManager manager;

    private final List<KeyCell>    keyCells    = new ArrayList<>();
    private final List<ActionChip> actionChips = new ArrayList<>();

    // ── 拖曳狀態 ─────────────────────────────────────────────
    private ActionType draggedAction     = null;
    private int        dragX, dragY;
    private Integer    dragSourceKeyCode = null;

    // ─────────────────────────────────────────────────────────
    // 內部資料類別
    // ─────────────────────────────────────────────────────────

    private static class KeyCell {
        final String    label;
        final int       keyCode;
        final Rectangle rect;
        KeyCell(String label, int keyCode, int x, int y, int w, int h) {
            this.label   = label;
            this.keyCode = keyCode;
            this.rect    = new Rectangle(x, y, w, h);
        }
    }

    private static class ActionChip {
        final ActionType type;
        final Rectangle  rect;
        ActionChip(ActionType t, Rectangle r) { type = t; rect = r; }
    }

    // ─────────────────────────────────────────────────────────
    public KeyBindingPanel(KeyBindingManager manager) {
        this.manager = manager;
        buildKeyboard();
        buildActionChips();
    }

    // ─────────────────────────────────────────────────────────
    // 鍵盤配置
    // ─────────────────────────────────────────────────────────
    private void buildKeyboard() {
        addRow(new String[]{"1","2","3","4","5","6","7","8","9","0"},
               new int[]{KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4,
                         KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8,
                         KeyEvent.VK_9, KeyEvent.VK_0},
               0, 0);

        addRow(new String[]{"Q","W","E","R","T","Y","U","I","O","P"},
               new int[]{KeyEvent.VK_Q, KeyEvent.VK_W, KeyEvent.VK_E, KeyEvent.VK_R,
                         KeyEvent.VK_T, KeyEvent.VK_Y, KeyEvent.VK_U, KeyEvent.VK_I,
                         KeyEvent.VK_O, KeyEvent.VK_P},
               10, 1);

        addRow(new String[]{"A","S","D","F","G","H","J","K","L"},
               new int[]{KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_F,
                         KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_J, KeyEvent.VK_K,
                         KeyEvent.VK_L},
               16, 2);

        addRow(new String[]{"Z","X","C","V","B","N","M"},
               new int[]{KeyEvent.VK_Z, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_V,
                         KeyEvent.VK_B, KeyEvent.VK_N, KeyEvent.VK_M},
               28, 3);

        int spaceY = KB_Y + 4 * KP + 6;
        keyCells.add(new KeyCell("Space", KeyEvent.VK_SPACE,
                                 KB_X + 60, spaceY, 204, KS));

        int arX = KB_X + 10 * KP + 16;
        int arY = KB_Y + 2 * KP;
        keyCells.add(new KeyCell("↑", KeyEvent.VK_UP,    arX + KP,     arY,      KS, KS));
        keyCells.add(new KeyCell("←", KeyEvent.VK_LEFT,  arX,          arY + KP, KS, KS));
        keyCells.add(new KeyCell("↓", KeyEvent.VK_DOWN,  arX + KP,     arY + KP, KS, KS));
        keyCells.add(new KeyCell("→", KeyEvent.VK_RIGHT, arX + 2 * KP, arY + KP, KS, KS));
    }

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
    // 動作 Chip 列表
    // ─────────────────────────────────────────────────────────
    private void buildActionChips() {
        actionChips.clear();
        int y = PAL_Y + 28;
        ActionType.Category lastCat = null;
        for (ActionType a : ActionType.values()) {
            if (a.category != lastCat) {
                if (lastCat != null) y += 6;
                y += 18;
                lastCat = a.category;
            }
            actionChips.add(new ActionChip(a, new Rectangle(PAL_X, y, PAL_W, CHIP_H)));
            y += CHIP_H + CHIP_GAP;
        }
    }

    // ─────────────────────────────────────────────────────────
    // 繪製主體
    // ─────────────────────────────────────────────────────────
    public void draw(Graphics2D g) {
        // 半透明遮罩
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRect(0, 0, 800, 580);

        // 面板外框（深藍漸層）
        GradientPaint panelGrad = new GradientPaint(
            PX, PY,        new Color(22, 24, 52),
            PX, PY + PH,   new Color(14, 16, 36)
        );
        g.setPaint(panelGrad);
        g.fillRoundRect(PX, PY, PW, PH, 14, 14);

        // 頂部彩色標題帶
        GradientPaint headerGrad = new GradientPaint(
            PX, PY,       new Color(40, 46, 110),
            PX, PY + 46,  new Color(22, 24, 52)
        );
        g.setPaint(headerGrad);
        g.fillRoundRect(PX, PY, PW, 46, 14, 14);
        g.fillRect(PX, PY + 30, PW, 16); // 方角填滿下半

        // 外框
        g.setColor(new Color(70, 80, 160));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(PX, PY, PW, PH, 14, 14);
        g.setStroke(new BasicStroke(1f));

        // 標題文字
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        g.setColor(new Color(210, 220, 255));
        g.drawString("按鍵配置", PX + 14, PY + 24);

        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        g.setColor(new Color(120, 125, 170));
        g.drawString("拖曳動作 → 按鍵 綁定  |  拖離鍵盤 解除  |  B / ESC 關閉", PX + 14, PY + 40);

        // Reset 按鈕
        drawResetButton(g);

        // 分隔線
        g.setColor(new Color(50, 58, 115));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(PX + PAL_W + 20, PY + 50, PX + PAL_W + 20, PY + PH - 10);
        g.setStroke(new BasicStroke(1f));

        // 左側動作面板
        drawPalette(g);

        // 右側鍵盤
        drawKeyboard(g);

        // 拖曳 Ghost
        if (draggedAction != null) drawDragGhost(g);
    }

    // ── 動作面板（左側）─────────────────────────────────────
    private void drawPalette(Graphics2D g) {
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        g.setColor(new Color(170, 180, 220));
        g.drawString("可用動作", PAL_X, PAL_Y + 14);

        ActionType.Category lastCat = null;
        int chipIdx = 0;
        int y = PAL_Y + 28;

        for (ActionType a : ActionType.values()) {
            if (a.category != lastCat) {
                if (lastCat != null) y += 6;
                // 分類標題帶
                Color catCol = catColor(a.category);
                g.setColor(new Color(catCol.getRed(), catCol.getGreen(), catCol.getBlue(), 38));
                g.fillRoundRect(PAL_X, y, PAL_W, 16, 4, 4);
                g.setColor(new Color(catCol.getRed(), catCol.getGreen(), catCol.getBlue(), 130));
                g.setStroke(new BasicStroke(0.8f));
                g.drawRoundRect(PAL_X, y, PAL_W, 16, 4, 4);
                g.setStroke(new BasicStroke(1f));
                g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 9));
                g.setColor(catCol.brighter());
                g.drawString(a.category.label, PAL_X + 6, y + 12);
                y += 18;
                lastCat = a.category;
            }

            ActionChip chip = actionChips.get(chipIdx++);
            boolean isDragging = (draggedAction == a);

            // Chip 背景（含左側色條）
            Color ac = a.color;
            g.setColor(isDragging
                ? new Color(ac.getRed()/5, ac.getGreen()/5, ac.getBlue()/5, 100)
                : new Color(ac.getRed()/4, ac.getGreen()/4, ac.getBlue()/4));
            g.fillRoundRect(chip.rect.x, chip.rect.y, chip.rect.width, chip.rect.height, 4, 4);

            // 左側色條
            if (!isDragging) {
                g.setColor(new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), 180));
                g.fillRoundRect(chip.rect.x, chip.rect.y, 3, chip.rect.height, 2, 2);
            }

            // 邊框
            g.setColor(isDragging ? ac.darker().darker() : new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), 160));
            g.setStroke(new BasicStroke(0.8f));
            g.drawRoundRect(chip.rect.x, chip.rect.y, chip.rect.width, chip.rect.height, 4, 4);
            g.setStroke(new BasicStroke(1f));

            // 動作名稱
            g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
            g.setColor(isDragging ? new Color(120, 120, 120) : new Color(220, 225, 255));
            g.drawString(a.displayName, chip.rect.x + 7, chip.rect.y + CHIP_H - 4);

            // 已綁定鍵標示（右側）
            Integer boundKey = manager.getKeyFor(a);
            if (boundKey != null) {
                String kl = KeyBindingManager.keyName(boundKey);
                g.setFont(new Font("Arial", Font.BOLD, 9));
                g.setColor(new Color(220, 195, 80));
                FontMetrics fm = g.getFontMetrics();
                int klX = chip.rect.x + chip.rect.width - fm.stringWidth(kl) - 6;
                // 小底框
                g.setColor(new Color(80, 70, 20, 140));
                g.fillRoundRect(klX - 2, chip.rect.y + 2, fm.stringWidth(kl) + 4, CHIP_H - 4, 3, 3);
                g.setFont(new Font("Arial", Font.BOLD, 9));
                g.setColor(new Color(255, 220, 80));
                g.drawString(kl, klX, chip.rect.y + CHIP_H - 4);
            }

            y += CHIP_H + CHIP_GAP;
        }
    }

    // ── 鍵盤（右側）─────────────────────────────────────────
    private void drawKeyboard(Graphics2D g) {
        // 鍵盤托盤（淡色背景）
        int kbRight = KB_X + 10 * KP + 4 * KP + 20; // rough right edge
        int kbBottom = KB_Y + 5 * KP + KS + 8;
        g.setColor(new Color(20, 22, 48));
        g.fillRoundRect(KB_X - 10, KB_Y - 10, kbRight - KB_X + 20, kbBottom - KB_Y + 14, 10, 10);
        g.setColor(new Color(38, 44, 88));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(KB_X - 10, KB_Y - 10, kbRight - KB_X + 20, kbBottom - KB_Y + 14, 10, 10);

        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 10));
        g.setColor(new Color(150, 160, 210));
        g.drawString("鍵盤配置（拖曳動作到按鍵進行綁定）", KB_X - 6, PAL_Y + 14);

        KeyCell hoveredCell = draggedAction != null ? findKeyAt(dragX, dragY) : null;
        for (KeyCell kc : keyCells) {
            ActionType boundAction = manager.getAction(kc.keyCode);
            boolean    isHovered   = (kc == hoveredCell);
            drawKeyCell(g, kc, boundAction, isHovered);
        }
    }

    private void drawKeyCell(Graphics2D g, KeyCell kc, ActionType bound, boolean hovered) {
        Rectangle r = kc.rect;

        // 是否為快捷欄按鍵（數字1-5）
        boolean isHotbarKey = (kc.keyCode >= KeyEvent.VK_1 && kc.keyCode <= KeyEvent.VK_5);

        // ── 按鍵底色（3D 底層陰影）────────────────────────────
        g.setColor(new Color(8, 10, 22));
        g.fillRoundRect(r.x + 2, r.y + 3, r.width, r.height, 5, 5);

        // ── 按鍵主體底色 ─────────────────────────────────────
        Color bg;
        if (hovered && draggedAction != null) {
            bg = new Color(40, 75, 35);
        } else if (bound != null) {
            Color c = bound.color;
            bg = new Color(c.getRed()/6, c.getGreen()/6, c.getBlue()/6 + 12);
        } else if (isHotbarKey) {
            bg = new Color(38, 30, 12);
        } else {
            bg = new Color(32, 34, 62);
        }
        g.setColor(bg);
        g.fillRoundRect(r.x, r.y, r.width, r.height, 5, 5);

        // ── 邊框 ─────────────────────────────────────────────
        Color border;
        if (hovered && draggedAction != null) {
            border = new Color(100, 210, 70);
        } else if (bound != null) {
            border = new Color(bound.color.getRed()/2,
                               bound.color.getGreen()/2,
                               bound.color.getBlue()/2 + 20);
        } else if (isHotbarKey) {
            border = new Color(90, 70, 25);
        } else {
            border = new Color(48, 54, 100);
        }
        g.setColor(border);
        g.setStroke(new BasicStroke(hovered ? 1.8f : 1.0f));
        g.drawRoundRect(r.x, r.y, r.width, r.height, 5, 5);
        g.setStroke(new BasicStroke(1f));

        // ── 3D 頂邊高光 ──────────────────────────────────────
        Color hiCol = hovered
            ? new Color(160, 255, 120, 100)
            : (bound != null
                ? new Color(bound.color.getRed(), bound.color.getGreen(), bound.color.getBlue(), 60)
                : (isHotbarKey ? new Color(180, 140, 40, 70) : new Color(80, 90, 160, 50)));
        g.setColor(hiCol);
        g.drawLine(r.x + 2, r.y + 1, r.x + r.width - 3, r.y + 1);

        // ── 鍵名（左上角小字） ───────────────────────────────
        g.setFont(new Font("Arial", Font.BOLD, 8));
        g.setColor(bound != null
            ? new Color(bound.color.getRed(), bound.color.getGreen(), bound.color.getBlue(), 200)
            : (isHotbarKey ? new Color(180, 140, 50) : new Color(110, 115, 175)));
        g.drawString(kc.label, r.x + 3, r.y + 10);

        // ── 綁定動作名稱（置中）─────────────────────────────
        if (bound != null) {
            Color bc = bound.color;

            // 底部稀有色條
            g.setColor(new Color(bc.getRed(), bc.getGreen(), bc.getBlue(), 130));
            g.fillRoundRect(r.x + 2, r.y + r.height - 5, r.width - 4, 4, 2, 2);

            String shortName = abbreviate(bound.displayName, r.width - 6, g);
            g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 8));
            g.setColor(new Color(220, 225, 255));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(shortName,
                         r.x + (r.width  - fm.stringWidth(shortName)) / 2,
                         r.y + r.height - 8);
        } else if (isHotbarKey) {
            // 空的快捷欄鍵：顯示「快」字
            g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 8));
            g.setColor(new Color(120, 95, 35));
            FontMetrics fm = g.getFontMetrics();
            String tag = "快";
            g.drawString(tag, r.x + (r.width - fm.stringWidth(tag)) / 2, r.y + r.height - 8);
        }

        // ── 懸停特效 ─────────────────────────────────────────
        if (hovered && draggedAction != null) {
            g.setColor(new Color(100, 255, 60, 45));
            g.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 4, 4);
        }
    }

    // ── Ghost（拖曳中）───────────────────────────────────────
    private void drawDragGhost(Graphics2D g) {
        int gw = 140, gh = 26;
        int gx = dragX - gw / 2;
        int gy = dragY - gh / 2;

        Color c = draggedAction.color;
        g.setColor(new Color(c.getRed()/3, c.getGreen()/3, c.getBlue()/3, 210));
        g.fillRoundRect(gx, gy, gw, gh, 8, 8);

        g.setColor(c);
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(gx, gy, gw, gh, 8, 8);
        g.setStroke(new BasicStroke(1f));

        // 頂邊高光
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 80));
        g.drawLine(gx + 3, gy + 1, gx + gw - 4, gy + 1);

        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(draggedAction.displayName,
                     gx + (gw - fm.stringWidth(draggedAction.displayName)) / 2,
                     gy + gh - 7);
    }

    // ── Reset 按鈕 ────────────────────────────────────────────
    private void drawResetButton(Graphics2D g) {
        // 陰影
        g.setColor(new Color(0, 0, 0, 80));
        g.fillRoundRect(resetBtn.x + 2, resetBtn.y + 2, resetBtn.width, resetBtn.height, 6, 6);

        GradientPaint btnGrad = new GradientPaint(
            resetBtn.x, resetBtn.y,              new Color(85, 60, 20),
            resetBtn.x, resetBtn.y + resetBtn.height, new Color(55, 38, 12)
        );
        g.setPaint(btnGrad);
        g.fillRoundRect(resetBtn.x, resetBtn.y, resetBtn.width, resetBtn.height, 6, 6);

        g.setColor(new Color(190, 140, 55));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(resetBtn.x, resetBtn.y, resetBtn.width, resetBtn.height, 6, 6);
        g.setStroke(new BasicStroke(1f));

        // 頂邊高光
        g.setColor(new Color(255, 200, 80, 60));
        g.drawLine(resetBtn.x + 3, resetBtn.y + 1, resetBtn.x + resetBtn.width - 4, resetBtn.y + 1);

        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        g.setColor(new Color(240, 200, 100));
        FontMetrics fm = g.getFontMetrics();
        String label = "還原預設";
        g.drawString(label,
                     resetBtn.x + (resetBtn.width  - fm.stringWidth(label)) / 2,
                     resetBtn.y + 16);
    }

    // ─────────────────────────────────────────────────────────
    // 滑鼠事件（由 GamePanel 呼叫）
    // ─────────────────────────────────────────────────────────

    public void mousePressed(int x, int y) {
        if (resetBtn.contains(x, y)) {
            manager.resetToDefault();
            return;
        }
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
        ActionChip chip = findChipAt(x, y);
        if (chip != null) {
            draggedAction     = chip.type;
            dragSourceKeyCode = null;
            dragX = x; dragY = y;
        }
    }

    public void mouseDragged(int x, int y) {
        if (draggedAction == null) return;
        dragX = x; dragY = y;
    }

    public void mouseReleased(int x, int y) {
        if (draggedAction == null) return;
        KeyCell target = findKeyAt(x, y);
        if (target != null) {
            manager.bind(target.keyCode, draggedAction);
        } else {
            if (dragSourceKeyCode != null &&
                    manager.getAction(dragSourceKeyCode) == draggedAction) {
                manager.unbind(dragSourceKeyCode);
            }
        }
        draggedAction     = null;
        dragSourceKeyCode = null;
    }

    // ─────────────────────────────────────────────────────────
    // 工具方法
    // ─────────────────────────────────────────────────────────

    private KeyCell findKeyAt(int x, int y) {
        for (KeyCell kc : keyCells) {
            if (kc.rect.contains(x, y)) return kc;
        }
        return null;
    }

    private ActionChip findChipAt(int x, int y) {
        for (ActionChip chip : actionChips) {
            if (chip.rect.contains(x, y)) return chip;
        }
        return null;
    }

    /** 依可用寬度截斷字串，超出加省略號（使用 FontMetrics 精確計算）*/
    private String abbreviate(String text, int availableWidth, Graphics2D g) {
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(text) <= availableWidth) return text;
        String ellipsis = "..";
        for (int i = text.length() - 1; i > 0; i--) {
            String candidate = text.substring(0, i) + ellipsis;
            if (fm.stringWidth(candidate) <= availableWidth) return candidate;
        }
        return ellipsis;
    }

    /** 各分類的代表色 */
    private static Color catColor(ActionType.Category cat) {
        return switch (cat) {
            case GAME   -> new Color(100, 160, 255);
            case UI     -> new Color(80,  200, 120);
            case HOTBAR -> new Color(220, 160, 40);
        };
    }
}
