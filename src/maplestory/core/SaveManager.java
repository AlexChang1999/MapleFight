package maplestory.core;

import maplestory.entity.Player;
import maplestory.item.Equipment;
import maplestory.item.EquipSlot;
import maplestory.job.Warrior;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON 存讀檔管理器（純手寫 JSON，不依賴外部 jar）。
 *
 * 存檔位置：save/slot1.json, save/slot2.json, save/slot3.json
 * 支援：角色名稱、等級、EXP、屬性、職業、金幣、HP/MP。
 *
 * JSON 格式（人工可讀）：
 * {
 *   "name": "楓楓",
 *   "level": 5,
 *   "exp": 120,
 *   "expToNext": 500,
 *   "str": 12, "dex": 4, "intel": 4, "luk": 4,
 *   "hp": 200, "maxHp": 200, "mp": 50, "maxMp": 50,
 *   "jobId": "warrior",
 *   "gold": 300,
 *   "mapId": "village"
 * }
 */
public class SaveManager {

    private static final String SAVE_DIR = "save";

    /** 取得存檔路徑（slot 1~3） */
    public static Path slotPath(int slot) {
        return Paths.get(SAVE_DIR, "slot" + slot + ".json");
    }

    /** 存檔是否存在 */
    public static boolean exists(int slot) {
        return Files.exists(slotPath(slot));
    }

    // ── 存檔 ─────────────────────────────────────────────────

    /**
     * 將玩家資料儲存至指定槽位。
     * @param slot       1~3
     * @param player     玩家物件
     * @param currentMapId  目前所在地圖 ID
     */
    public static void save(int slot, Player player, String currentMapId) {
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
            String json = buildJson(player, currentMapId);
            Files.writeString(slotPath(slot), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[SaveManager] 存檔失敗：" + e.getMessage());
        }
    }

    /** 建構 JSON 字串 */
    private static String buildJson(Player p, String mapId) {
        String jobId = (p.getJob() == null) ? "none" : p.getJob().getJobId();
        return "{\n"
             + "  \"name\": "      + jsonStr(p.getName())  + ",\n"
             + "  \"level\": "     + p.getLevel()          + ",\n"
             + "  \"exp\": "       + p.getExp()             + ",\n"
             + "  \"expToNext\": " + p.getExpToNextLevel()  + ",\n"
             + "  \"str\": "       + p.getStr()             + ",\n"
             + "  \"dex\": "       + p.getDex()             + ",\n"
             + "  \"intel\": "     + p.getIntel()           + ",\n"
             + "  \"luk\": "       + p.getLuk()             + ",\n"
             + "  \"hp\": "        + p.getHp()              + ",\n"
             + "  \"maxHp\": "     + p.getMaxHp()           + ",\n"
             + "  \"mp\": "        + p.getMp()              + ",\n"
             + "  \"maxMp\": "     + p.getMaxMp()           + ",\n"
             + "  \"jobId\": "     + jsonStr(jobId)         + ",\n"
             + "  \"gold\": "      + p.getGold()            + ",\n"
             + "  \"mapId\": "     + jsonStr(mapId)         + "\n"
             + "}";
    }

    private static String jsonStr(String s) {
        // 簡單轉義（雙引號和反斜線）
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    // ── 讀取摘要（給標題畫面顯示用） ─────────────────────────

    /**
     * 讀取存檔摘要，回傳 {name, level, mapId}，
     * 若不存在則回傳 null。
     */
    public static SaveSummary readSummary(int slot) {
        if (!exists(slot)) return null;
        try {
            String json = Files.readString(slotPath(slot), StandardCharsets.UTF_8);
            Map<String, String> data = parseJson(json);
            return new SaveSummary(
                data.getOrDefault("name",  "???"),
                intOf(data, "level", 1),
                data.getOrDefault("mapId", "village")
            );
        } catch (Exception e) {
            return null;
        }
    }

    // ── 讀取並套用至 Player ───────────────────────────────────

    /**
     * 讀取存檔並還原玩家狀態。
     * @return 存檔中記錄的地圖 ID（讓 MapManager 切換地圖用）
     */
    public static String load(int slot, Player player) {
        if (!exists(slot)) return "village";
        try {
            String json = Files.readString(slotPath(slot), StandardCharsets.UTF_8);
            Map<String, String> data = parseJson(json);

            player.setName(data.getOrDefault("name", "新手"));
            player.setLevel(intOf(data, "level", 1));
            player.setExp(intOf(data, "exp", 0));
            player.setExpToNextLevel(intOf(data, "expToNext", 100));
            player.setStr(intOf(data, "str", 10));
            player.setDex(intOf(data, "dex", 4));
            player.setIntel(intOf(data, "intel", 4));
            player.setLuk(intOf(data, "luk", 4));
            player.setMaxHp(intOf(data, "maxHp", 200));
            player.setMaxMp(intOf(data, "maxMp", 50));
            player.setHp(intOf(data, "hp", player.getMaxHp()));
            player.setMp(intOf(data, "mp", player.getMaxMp()));
            player.setGold(intOf(data, "gold", 0));

            // 職業還原
            String jobId = data.getOrDefault("jobId", "none");
            switch (jobId) {
                case "warrior" -> player.setJob(new Warrior());
                // 未來新增職業在此補充 case
                default        -> player.setJob(null);
            }

            return data.getOrDefault("mapId", "village");

        } catch (Exception e) {
            System.err.println("[SaveManager] 讀取失敗：" + e.getMessage());
            return "village";
        }
    }

    /** 刪除存檔 */
    public static void delete(int slot) {
        try { Files.deleteIfExists(slotPath(slot)); }
        catch (IOException e) { System.err.println("[SaveManager] 刪除失敗：" + e.getMessage()); }
    }

    // ── 超輕量 JSON 解析（Key-Value 文字解析） ────────────────

    /**
     * 極簡 JSON 解析：只支援本系統產生的單層 flat JSON，
     * 解析結果為 Map<String, String>（所有值都先轉字串）。
     */
    private static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        // 去掉頭尾大括號、按逗號+換行分割
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}"))   json = json.substring(0, json.length() - 1);

        String[] lines = json.split(",\\s*\n|,\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            int colon = line.indexOf(':');
            if (colon < 0) continue;
            String key = line.substring(0, colon).trim().replace("\"", "");
            String val = line.substring(colon + 1).trim().replace("\"", "");
            map.put(key, val);
        }
        return map;
    }

    private static int intOf(Map<String, String> m, String key, int def) {
        try { return Integer.parseInt(m.getOrDefault(key, String.valueOf(def)).trim()); }
        catch (NumberFormatException e) { return def; }
    }

    // ── 存檔摘要 DTO ─────────────────────────────────────────

    public static class SaveSummary {
        public final String name;
        public final int    level;
        public final String mapId;
        public SaveSummary(String name, int level, String mapId) {
            this.name  = name;
            this.level = level;
            this.mapId = mapId;
        }
    }
}
