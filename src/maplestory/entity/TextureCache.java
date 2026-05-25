package maplestory.entity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TextureCache — singleton PNG asset loader and in-memory cache.
 *
 * Asset root: <working directory>/assets/textures/character/
 * Override with JVM property: -Dmaple.asset.root=<path>
 *
 * All paths are relative to that root, e.g.
 *   "hair/front/fluffy_spike.png"
 *   "face/normal/dewy_bright_hero_thick.png"
 *
 * Returns null (never throws) when a file is absent — callers must fall back
 * to Graphics2D rendering in that case.
 */
public final class TextureCache {

    /** All PNGs must be exactly this size. */
    public static final int CANVAS_W = 64;
    public static final int CANVAS_H = 64;

    private static final ConcurrentHashMap<String, BufferedImage> CACHE =
        new ConcurrentHashMap<>();

    /** Sentinel stored in the map when an asset is confirmed missing. */
    private static final BufferedImage MISSING = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private static final String ASSET_ROOT = resolveRoot();

    private TextureCache() {}

    // ── Public API ────────────────────────────────────────────

    /**
     * Returns the cached image for the given relative path,
     * or {@code null} if the file does not exist or failed to load.
     */
    public static BufferedImage get(String relativePath) {
        BufferedImage img = CACHE.computeIfAbsent(relativePath, TextureCache::load);
        return img == MISSING ? null : img;
    }

    /** Returns true iff the path resolves to a loadable image. */
    public static boolean has(String relativePath) {
        return get(relativePath) != null;
    }

    /**
     * Stores an image directly — used by SpriteGenerator to inject
     * programmatically-created placeholder sprites without disk I/O.
     */
    public static void put(String relativePath, BufferedImage img) {
        CACHE.put(relativePath, img != null ? img : MISSING);
    }

    /**
     * Eagerly loads all known sprite paths to prevent first-frame stutter.
     * Call once from GamePanel or Main during startup.
     */
    public static void preloadAll() {
        for (String p : SpritePathResolver.ALL_PATHS) {
            get(p);  // triggers load if not cached
        }
    }

    /** Absolute path to the asset root directory (for tools / debugging). */
    public static String assetRoot() { return ASSET_ROOT; }

    // ── Internal ──────────────────────────────────────────────

    private static BufferedImage load(String relativePath) {
        File f = new File(ASSET_ROOT, relativePath);
        if (!f.exists() || !f.canRead()) return MISSING;
        try {
            BufferedImage raw = ImageIO.read(f);
            if (raw == null) return MISSING;
            // Normalise to ARGB for consistent compositing
            if (raw.getType() == BufferedImage.TYPE_INT_ARGB) return raw;
            BufferedImage argb = new BufferedImage(raw.getWidth(), raw.getHeight(),
                                                   BufferedImage.TYPE_INT_ARGB);
            argb.createGraphics().drawImage(raw, 0, 0, null);
            return argb;
        } catch (IOException e) {
            System.err.println("[TextureCache] Failed to load: " + f.getPath());
            return MISSING;
        }
    }

    private static String resolveRoot() {
        String override = System.getProperty("maple.asset.root");
        if (override != null) return override;
        return System.getProperty("user.dir")
             + File.separator + "assets"
             + File.separator + "textures"
             + File.separator + "character";
    }
}
