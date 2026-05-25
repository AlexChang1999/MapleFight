package maplestory.entity;

import maplestory.item.EquipSlot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * SpritePathResolver — pure enum-to-filename mapper.
 *
 * Zero state, zero I/O.  Every method returns a path relative to
 * TextureCache.assetRoot() (i.e. relative to assets/textures/character/).
 *
 * Naming convention summary:
 *
 *   hair/back/<style>.png
 *   hair/front/<style>.png
 *   body/<tone>.png                     (ivory | peach | tan)
 *   face/<state>/<eye>_<brow>.png
 *   equipment/<slot>/<slug>.png
 *
 * Sprite files should be 64×64 px RGBA PNGs.
 * Hair and body sprites use GREY-SCALE silhouettes — the engine applies
 * Appearance.hairColor / skinColor as a runtime tint via AlphaComposite.
 * Equipment sprites are pre-coloured (no runtime tint).
 *
 * ==========================================================================
 * Enum → path examples:
 *
 *   HairStyle.FLUFFY_SPIKE   → "hair/back/fluffy_spike.png"
 *                              "hair/front/fluffy_spike.png"
 *
 *   EyeStyle.DEWY_BRIGHT +
 *   EyebrowStyle.HERO_THICK +
 *   FaceState.NORMAL         → "face/normal/dewy_bright_hero_thick.png"
 *
 *   EquipSlot.HELMET +
 *   name "novice helmet"     → "equipment/helmet/novice_helmet.png"
 * ==========================================================================
 */
public final class SpritePathResolver {

    /** Pre-built flat list of all possible paths — used by TextureCache.preloadAll(). */
    public static final String[] ALL_PATHS = buildAllPaths();

    private SpritePathResolver() {}

    // ── Hair ──────────────────────────────────────────────────

    /** Back hair layer — drawn BEHIND the body. */
    public static String hairBack(Appearance.HairStyle style) {
        return "hair/back/" + enumKey(style) + ".png";
    }

    /** Front hair layer — drawn OVER the face, UNDER the helmet. */
    public static String hairFront(Appearance.HairStyle style) {
        return "hair/front/" + enumKey(style) + ".png";
    }

    // ── Body / Skin ───────────────────────────────────────────

    /**
     * Skin-layer sprite (head + visible arm skin).
     * Three canonical tones; runtime tint applied by engine.
     */
    public static String body(Color skinColor) {
        return "body/" + skinToneKey(skinColor) + ".png";
    }

    // ── Face Expression ───────────────────────────────────────

    /**
     * Face expression sprite — keyed by eye style × eyebrow style × state.
     *
     * Example: face(DEWY_BRIGHT, HERO_THICK, NORMAL)
     *       → "face/normal/dewy_bright_hero_thick.png"
     */
    public static String face(Appearance.EyeStyle eye,
                               Appearance.EyebrowStyle brow,
                               FaceState state) {
        return "face/" + state.dirName + "/"
             + enumKey(eye) + "_" + enumKey(brow) + ".png";
    }

    // ── Equipment ─────────────────────────────────────────────

    /**
     * Equipment sprite path.
     * itemName is the Equipment's display name (any language — slugified).
     *
     * Example: equipment(HELMET, "新手頭盔")  → "equipment/helmet/item_4812.png"
     *          equipment(TOP,    "cotton shirt") → "equipment/top/cotton_shirt.png"
     */
    public static String equipment(EquipSlot slot, String itemName) {
        return "equipment/" + slot.name().toLowerCase() + "/" + toSlug(itemName) + ".png";
    }

    // ── Face State enum ───────────────────────────────────────

    /**
     * The three expression states a face sprite can be in.
     * Maps directly to the face/ subdirectory name.
     */
    public enum FaceState {
        NORMAL ("normal"),
        HURT   ("hurt"),
        ATTACK ("attack");

        /** Subdirectory name within face/. */
        public final String dirName;
        FaceState(String d) { this.dirName = d; }

        /** Derive state from runtime combat flags. */
        public static FaceState from(boolean attacking, double hurtTimer) {
            if (hurtTimer > 0) return HURT;
            if (attacking)     return ATTACK;
            return NORMAL;
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private static String enumKey(Enum<?> e) {
        return e.name().toLowerCase();
    }

    private static String skinToneKey(Color c) {
        int brightness = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
        if (brightness > 228) return "ivory";
        if (brightness > 195) return "peach";
        return "tan";
    }

    /**
     * Converts an arbitrary item name to a lowercase ASCII filesystem slug.
     * ASCII letters/digits map directly; non-ASCII chars hash to "item_NNNN".
     */
    static String toSlug(String name) {
        if (name == null || name.isBlank()) return "unknown";
        StringBuilder sb = new StringBuilder();
        boolean allAscii = true;
        for (char c : name.toCharArray()) {
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') { sb.append(c); }
            else if (c >= 'A' && c <= 'Z')                     { sb.append((char)(c + 32)); }
            else if (c == ' ' || c == '-')                     { sb.append('_'); }
            else { allAscii = false; }
        }
        String ascii = sb.toString().replaceAll("_+", "_").replaceAll("^_|_$", "");
        if (!allAscii || ascii.isEmpty()) {
            return "item_" + (Math.abs(name.hashCode()) % 9999);
        }
        return ascii;
    }

    /** Builds the exhaustive list of all sprite paths for preloading. */
    private static String[] buildAllPaths() {
        List<String> paths = new ArrayList<>();

        // Hair (back + front per style)
        for (Appearance.HairStyle h : Appearance.HairStyle.values()) {
            paths.add(hairBack(h));
            paths.add(hairFront(h));
        }

        // Body skin tones
        for (String tone : new String[]{"ivory", "peach", "tan"}) {
            paths.add("body/" + tone + ".png");
        }

        // Face expressions (eye × brow × state = 3×4×3 = 36 sprites)
        for (Appearance.EyeStyle eye : Appearance.EyeStyle.values()) {
            for (Appearance.EyebrowStyle brow : Appearance.EyebrowStyle.values()) {
                for (FaceState state : FaceState.values()) {
                    paths.add(face(eye, brow, state));
                }
            }
        }

        return paths.toArray(new String[0]);
    }
}
