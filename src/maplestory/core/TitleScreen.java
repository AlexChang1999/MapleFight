package maplestory.core;

import maplestory.core.SaveManager.SaveSummary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 遊戲標題畫面。
 * 顯示 3 個存檔槽，玩家可以：
 *   - 新遊戲（輸入角色名稱）
 *   - 繼續遊戲（讀取現有存檔）
 *   - 刪除存檔
 *
 * 選擇後呼叫 callback 進入 GamePanel。
 */
public class TitleScreen extends JPanel {

    // ── 尺寸（和 GamePanel 相同） ─────────────────────────────
    private static final int W = 800;
    private static final int H = 580;

    // ── 動畫 ─────────────────────────────────────────────────
    private double animTimer = 0;

    // ── 存檔槽資訊（每次顯示前刷新） ─────────────────────────
    private final SaveSummary[] summaries = new SaveSummary[3];

    // ── 滑鼠懸停的槽位（-1 = 無） ────────────────────────────
    private int hoveredSlot  = -1;
    private int hoveredDel   = -1; // 懸停在刪除按鈕的槽

    // ── 按鍵剛剛按下的回饋動畫 ───────────────────────────────
    private int  pressedSlot = -1;
    private long pressedTime = 0;

    // ── 回呼介面 ─────────────────────────────────────────────
    @FunctionalInterface
    public interface StartCallback {
        /**
         * @param slot       存檔槽（1~3）
         * @param playerName 角色名（新遊戲輸入，繼續遊戲為 null 代表從存檔讀取）
         */
        void onStart(int slot, String playerName);
    }

    private final StartCallback callback;

    // ─────────────────────────────────────────────────────────
    public TitleScreen(StartCallback callback) {
        this.callback = callback;
        setPreferredSize(new Dimension(W, H));
        setBackground(Color.BLACK);
        setFocusable(true);
        refreshSummaries();
        setupMouse();
        startAnimLoop();
    }

    private void refreshSummaries() {
        for (int i = 0; i < 3; i++) {
            summaries[i] = SaveManager.readSummary(i + 1);
        }
    }

    // ── 動畫迴圈（每 16ms ≈ 60 FPS） ────────────────────────
    private void startAnimLoop() {
        Timer t = new Timer(16, e -> {
            animTimer += 0.016;
            repaint();
        });
        t.start();
    }

    // ── 縮放輔助 ─────────────────────────────────────────────

    /** 目前縮放比例（等比，維持 800x580 邏輯解析度） */
    private double getScale() {
        return Math.min((double) getWidth() / W, (double) getHeight() / H);
    }
    private int getTransX() { return (getWidth()  - (int)(W * getScale())) / 2; }
    private int getTransY() { return (getHeight() - (int)(H * getScale())) / 2; }

    /** 將畫面（螢幕）座標轉換為邏輯座標 */
    private Point toLogical(int sx, int sy) {
        double s = getScale();
        return new Point((int)((sx - getTransX()) / s), (int)((sy - getTransY()) / s));
    }

    // ── 繪製 ─────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 黑色 letterbox 背景
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // 套用等比縮放 + 置中
        double scale = getScale();
        g.translate(getTransX(), getTransY());
        g.scale(scale, scale);

        drawBackground(g);
        drawTitle(g);
        drawSlots(g);
        drawFooter(g);
    }

    /** 星空漸層背景 */
    private void drawBackground(Graphics2D g) {
        GradientPaint sky = new GradientPaint(0, 0, new Color(5, 8, 25),
                                               0, H, new Color(15, 30, 60));
        g.setPaint(sky);
        g.fillRect(0, 0, W, H);

        // 星星
        long[] seeds = {1231, 5678, 2341, 9012, 3456, 7890, 4562, 1239, 8765, 3210,
                        6543, 9871, 2468, 1357, 8024, 5309, 7654, 4321, 6789, 1111};
        g.setColor(new Color(255, 255, 255, 160));
        for (int i = 0; i < seeds.length; i++) {
            int sx = (int)(seeds[i] % W);
            int sy = (int)((seeds[i] * 3 + i * 53) % (H / 2));
            float twinkle = (float)(0.6 + 0.4 * Math.sin(animTimer * 2.5 + i));
            g.setColor(new Color(1f, 1f, 1f, twinkle * 0.7f));
            g.fillOval(sx, sy, 2, 2);
        }

        // 裝飾性霓虹光暈（橘/青）
        drawGlow(g, W / 2, 160, 200, new Color(255, 165, 0, 18));
        drawGlow(g, 150,   350, 120, new Color(0, 200, 255, 12));
        drawGlow(g, 650,   380, 140, new Color(180, 0, 255, 12));
    }

    private void drawGlow(Graphics2D g, int cx, int cy, int r, Color c) {
        for (int i = r; i > 0; i -= 10) {
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(),
                                 (int)(c.getAlpha() * (1.0 - (double)i / r))));
            g.fillOval(cx - i, cy - i, i * 2, i * 2);
        }
    }

    /** 遊戲標題 */
    private void drawTitle(Graphics2D g) {
        // 光暈底色
        drawGlow(g, W / 2, 95, 140, new Color(255, 200, 50, 20));

        // 主標題
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 46));
        String title = "楓之谷 冒險傳說";
        FontMetrics fm = g.getFontMetrics();
        int tx = (W - fm.stringWidth(title)) / 2;
        // 陰影
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(title, tx + 3, 98);
        // 漸層金色
        GradientPaint gold = new GradientPaint(tx, 60, new Color(255, 220, 80),
                                               tx, 100, new Color(200, 130, 20));
        g.setPaint(gold);
        g.drawString(title, tx, 95);

        // 副標題（閃爍）
        float blink = (float)(0.5 + 0.5 * Math.sin(animTimer * 2.5));
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        g.setColor(new Color(180, 220, 255, (int)(blink * 200 + 55)));
        String sub = "─── 請選擇存檔槽開始冒險 ───";
        g.drawString(sub, (W - g.getFontMetrics().stringWidth(sub)) / 2, 125);
    }

    /** 三個存檔槽 */
    private void drawSlots(Graphics2D g) {
        int slotW = 210, slotH = 220;
        int totalW = slotW * 3 + 40 * 2;
        int startX = (W - totalW) / 2;
        int startY = 155;

        for (int i = 0; i < 3; i++) {
            int sx = startX + i * (slotW + 40);
            drawSlot(g, i, sx, startY, slotW, slotH);
        }
    }

    private void drawSlot(Graphics2D g, int idx, int sx, int sy, int sw, int sh) {
        boolean hovered = (hoveredSlot == idx);
        boolean pressed = (pressedSlot == idx &&
                           System.currentTimeMillis() - pressedTime < 150);
        SaveSummary save = summaries[idx];

        // ── 外框（懸停時亮起） ────────────────────────────────
        Color borderColor = hovered
            ? (save != null ? new Color(100, 220, 120) : new Color(100, 180, 255))
            : new Color(60, 80, 120);
        int lift = pressed ? 2 : (hovered ? -4 : 0);

        // 背景
        Color bg = hovered ? new Color(25, 45, 80) : new Color(15, 25, 50);
        g.setColor(bg);
        g.fillRoundRect(sx, sy + lift, sw, sh, 16, 16);

        // 光邊
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(hovered ? 2f : 1.5f));
        g.drawRoundRect(sx, sy + lift, sw, sh, 16, 16);
        g.setStroke(new BasicStroke(1f));

        // ── 槽位標題 ─────────────────────────────────────────
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        String slotLabel = "存檔 " + (idx + 1);
        g.setColor(new Color(140, 160, 220));
        g.drawString(slotLabel, sx + 12, sy + lift + 22);

        if (save == null) {
            // ── 空白存檔 ─────────────────────────────────────
            g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
            g.setColor(new Color(80, 100, 140));
            String empty = "[ 空白 ]";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(empty, sx + (sw - fm.stringWidth(empty)) / 2, sy + lift + sh / 2 - 10);

            // 新遊戲按鈕
            drawActionBtn(g, sx + 20, sy + lift + sh - 48, sw - 40, 32,
                          "✦ 開始新遊戲", new Color(60, 160, 80), hovered);
        } else {
            // ── 有存檔 ───────────────────────────────────────
            // 火柴人小圖示
            drawMiniStickman(g, sx + sw / 2, sy + lift + 70);

            // 角色名稱
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            String nameStr = save.name;
            g.drawString(nameStr, sx + (sw - fm.stringWidth(nameStr)) / 2, sy + lift + 105);

            // 等級
            g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
            g.setColor(new Color(255, 220, 80));
            String lvStr = "Lv." + save.level;
            g.drawString(lvStr, sx + (sw - g.getFontMetrics().stringWidth(lvStr)) / 2, sy + lift + 125);

            // 地圖位置
            g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
            g.setColor(new Color(140, 180, 220));
            String loc = mapName(save.mapId);
            g.drawString(loc, sx + (sw - g.getFontMetrics().stringWidth(loc)) / 2, sy + lift + 143);

            // 繼續按鈕
            drawActionBtn(g, sx + 20, sy + lift + sh - 52, sw - 40, 32,
                          "▶ 繼續冒險", new Color(50, 130, 200), hovered);

            // 刪除按鈕（小紅色，右上角）
            boolean delHover = (hoveredDel == idx);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.setColor(delHover ? new Color(255, 80, 80) : new Color(160, 60, 60));
            g.drawString("✕", sx + sw - 22, sy + lift + 20);
        }
    }

    /** 小按鈕 */
    private void drawActionBtn(Graphics2D g, int x, int y, int w, int h,
                                String label, Color col, boolean bright) {
        Color bg = bright ? col.brighter() : col;
        g.setColor(bg);
        g.fillRoundRect(x, y, w, h, 8, 8);
        g.setColor(bg.darker());
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, w, h, 8, 8);
        g.setStroke(new BasicStroke(1f));

        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + h / 2 + 5);
    }

    /** 小火柴人裝飾 */
    private void drawMiniStickman(Graphics2D g, int cx, int cy) {
        g.setColor(new Color(180, 220, 255));
        g.setStroke(new BasicStroke(2f));
        g.drawOval(cx - 8, cy - 28, 16, 16);              // 頭
        g.drawLine(cx, cy - 12, cx, cy + 5);              // 身體
        g.drawLine(cx, cy - 5, cx - 10, cy + 3);          // 左手
        g.drawLine(cx, cy - 5, cx + 10, cy + 3);          // 右手
        g.drawLine(cx, cy + 5, cx - 8,  cy + 18);         // 左腳
        g.drawLine(cx, cy + 5, cx + 8,  cy + 18);         // 右腳
        g.setStroke(new BasicStroke(1f));
    }

    /** 頁尾操作提示 */
    private void drawFooter(Graphics2D g) {
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        g.setColor(new Color(100, 120, 160));
        String hint = "點擊存檔槽開始遊戲  ·  點擊右上角 ✕ 刪除存檔";
        g.drawString(hint, (W - g.getFontMetrics().stringWidth(hint)) / 2, H - 20);
    }

    private String mapName(String mapId) {
        return switch (mapId) {
            case "village" -> "📍 新手村";
            case "battle"  -> "📍 冒險平原";
            case "arctic"  -> "📍 極地冰原";
            default        -> "📍 " + mapId;
        };
    }

    // ── 滑鼠互動 ─────────────────────────────────────────────

    private void setupMouse() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point lp = toLogical(e.getX(), e.getY());
                int slot = slotAt(lp.x, lp.y);
                int del  = delBtnAt(lp.x, lp.y);

                if (del >= 0) {
                    // 確認刪除
                    int confirm = JOptionPane.showConfirmDialog(
                        TitleScreen.this,
                        "確定要刪除「" + (summaries[del] != null ? summaries[del].name : "")
                            + "」的存檔嗎？",
                        "刪除確認",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        SaveManager.delete(del + 1);
                        refreshSummaries();
                        repaint();
                    }
                    return;
                }

                if (slot < 0) return;
                pressedSlot = slot;
                pressedTime = System.currentTimeMillis();

                if (summaries[slot] == null) {
                    // 新遊戲 → 取名
                    String name = promptName();
                    if (name != null) {
                        callback.onStart(slot + 1, name);
                    }
                } else {
                    // 繼續遊戲
                    callback.onStart(slot + 1, null);
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point lp = toLogical(e.getX(), e.getY());
                hoveredSlot = slotAt(lp.x, lp.y);
                hoveredDel  = delBtnAt(lp.x, lp.y);
                setCursor(hoveredSlot >= 0 || hoveredDel >= 0
                          ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                          : Cursor.getDefaultCursor());
            }
        });
    }

    /** 取得 (x,y) 對應的存檔槽索引（0~2），找不到回傳 -1 */
    private int slotAt(int mx, int my) {
        int slotW = 210, slotH = 220;
        int totalW = slotW * 3 + 40 * 2;
        int startX = (W - totalW) / 2;
        int startY = 155;
        for (int i = 0; i < 3; i++) {
            int sx = startX + i * (slotW + 40);
            if (mx >= sx && mx <= sx + slotW && my >= startY && my <= startY + slotH)
                return i;
        }
        return -1;
    }

    /** 取得 (x,y) 對應的刪除按鈕槽索引（只有非空存檔才有），找不到回傳 -1 */
    private int delBtnAt(int mx, int my) {
        int slotW = 210;
        int totalW = slotW * 3 + 40 * 2;
        int startX = (W - totalW) / 2;
        int startY = 155;
        for (int i = 0; i < 3; i++) {
            if (summaries[i] == null) continue;
            int sx = startX + i * (slotW + 40);
            // 刪除按鈕：右上角 (sx+sw-28, sy+8) ~ (sx+sw, sy+26)
            if (mx >= sx + slotW - 28 && mx <= sx + slotW &&
                my >= startY + 8 && my <= startY + 26) return i;
        }
        return -1;
    }

    /** 彈出取名對話框，回傳名稱（取消則回傳 null） */
    private String promptName() {
        JTextField field = new JTextField(10);
        field.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        field.setHorizontalAlignment(JTextField.CENTER);

        Object[] msg = {
            new JLabel("<html><b>請為你的角色取名</b>（最多 8 個字）</html>"),
            field
        };

        int result = JOptionPane.showConfirmDialog(
            this, msg, "新角色", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return null;
        String name = field.getText().trim();
        if (name.isEmpty()) name = "楓之冒險家";
        if (name.length() > 8) name = name.substring(0, 8);
        return name;
    }
}
