<!-- Context: Part of [Equipment System](equipment-system.md) -->
<!-- Domain: #game/items #game/rendering -->

# Equipment Appearance & Greyscale Sprite Asset Standard

## Overview

MapleGame uses a **greyscale template + runtime tinting** pipeline for all character and
equipment sprites. A single greyscale PNG per style supports infinite colour variants with
zero extra art assets, while preserving hand-crafted 3-step shading depth at every tint.

---

## 1. Layer Compositing Order (painters algorithm, back → front)

| Layer | ID  | Content                      | Anchor   | Tinted? |
|-------|-----|------------------------------|----------|---------|
| 0     | L0  | Hair — back strand           | headSy   | ✅ hair  |
| 1     | L1  | Cape                         | bodySy   | ✅ cape  |
| 2     | L2  | Body / Top equipment         | bodySy   | ✅ top   |
| 3     | L3  | Legs + Boots *(Graphics2D)*  | bodySy   | —       |
| 4     | L4  | Head skin + blush            | headSy   | ✅ skin  |
| 5     | L5  | Face expression              | headSy   | ❌ raw   |
| 6     | L6  | Hair — front strand          | headSy   | ✅ hair  |
| 7     | L7  | Helmet                       | headSy   | ✅ helm  |
| 8     | L8  | Earring                      | headSy   | ✅ ear   |

**Arms** are drawn by `Player.drawArms()` *before* `drawLayered()`, so the body (L2)
naturally covers the shoulder joints — exactly replicating the sleeve-over-arm look.

---

## 2. Canvas Specification

| Property          | Value                   |
|-------------------|-------------------------|
| Canvas size       | **64 × 64 px**          |
| Colour mode       | ARGB (32-bit with alpha) |
| Scale             | 1 px = 1 game pixel (no sub-pixel rendering) |
| Interpolation     | `NEAREST_NEIGHBOR` — never bilinear |
| Hair overflow     | 6 px above hitbox top for Player; 12 px for NPC |
| Feet alignment    | Canvas bottom = `hitboxY + hitboxH` |
| Horizontal centre | x = 32 (character spine) |

### Hitbox ↔ Canvas Anchor Math

```
canvasX = hitboxCentreX − 32
canvasY = hitboxY + hitboxH − 64     // Player HEIGHT=58 → canvasY = hitboxY − 6
                                      // NPC    HEIGHT=52 → canvasY = hitboxY − 12
```

Physical hitboxes are **locked** and must never change:

| Entity | WIDTH | HEIGHT |
|--------|-------|--------|
| Player | 24    | 58     |
| NPC    | 24    | 52     |

---

## 3. Greyscale Template Specification

All hair, body, and equipment sprites intended for runtime tinting **must** be authored as
near-white greyscale PNGs. Colour is applied at runtime via `AlphaComposite.SRC_ATOP`.

### Required shading passes (3-step)

| Pass        | Luminance target | Purpose                         |
|-------------|------------------|---------------------------------|
| Highlight   | ~230–245         | top-lit surface, specular ramp  |
| Base        | ~180–200         | mid-tone, the "true colour" zone|
| Shadow      | ~110–140         | underside / fold depth          |

- Background pixels: fully transparent (`alpha = 0`)
- Use 1–2 px feathered edges for anti-aliased outlines (pre-multiplied against black)
- Never use pure white (`#ffffff`) for base; reserve it for specular dots only

### Tinting formula (`CharacterSprite.buildTinted`)

```java
// SRC_ATOP overlays the runtime colour only on existing opaque pixels.
// Strength 0.68f keeps shading depth visible through the colour wash.
tg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.68f));
tg.setColor(tint);
tg.fillRect(0, 0, w, h);
```

Tinted images are cached in `TextureCache` under the key `"<path>#<RRGGBB>"` so the
compositing cost is paid once per unique (sprite, colour) pair.

---

## 4. File Naming Convention (`SpritePathResolver`)

All paths are relative to `assets/textures/character/`.

```
hair/back/<style_slug>.png          e.g. hair/back/fluffy_spike.png
hair/front/<style_slug>.png         e.g. hair/front/mushroom_bowl.png
face/<state>/<eye>_<brow>.png       e.g. face/normal/dewy_bright_hero_thick.png
body/skin/<r>_<g>_<b>.png           e.g. body/skin/255_222_178.png
equipment/helmet/<slug>.png
equipment/top/<slug>.png
equipment/cape/<slug>.png
equipment/earring/<slug>.png
```

**Slug rules** (`SpritePathResolver.toSlug()`):
- ASCII names → lowercased, spaces→`_`, special chars stripped
- Non-ASCII names → `"item_" + (Math.abs(name.hashCode()) % 9999)`

---

## 5. Face Expression States

| `FaceState` | Trigger condition                    | File suffix  |
|-------------|--------------------------------------|--------------|
| `NORMAL`    | idle / walking                       | `face/normal/…` |
| `HURT`      | `hurtTimer > 0`                      | `face/hurt/…`   |
| `ATTACK`    | `attacking == true`                  | `face/attack/…` |

Each face sprite is a **full-colour** (not greyscale) transparent PNG — expressions rely
on specific hue choices (red X-eyes for HURT, narrowed pupils for ATTACK) that must not
be colour-shifted by the tinting pass.

---

## 6. bodySy / headSy Phase Offset (breathing bob)

The idle breathing animation applies a **sin-wave vertical offset** to each layer group:

```java
double bobPhase = idleTimer * 1.6;
bodySy = sy + (int)(Math.sin(bobPhase)        * 2.0);  // body layers
headSy = sy + (int)(Math.sin(bobPhase - 0.30) * 2.0);  // head layers (~3-frame lag)
```

This 0.30 radian (~17 ms at 60 fps) lag gives the chibi head a subtle elastic "follow"
that reads as organic weight without any skeleton or bone system.

---

## 7. Graphics2D Fallback

Every layer in `CharacterSprite.drawLayered()` attempts PNG load first.  
When the PNG is absent (e.g. during development before art is produced), it falls back to
the existing `Graphics2D` primitive methods (`drawHead`, `drawHair`, `drawFace`, etc.).

This means **the game runs correctly with zero art assets** — ship prototype builds
immediately while art production catches up.

```
PNG present?  →  drawTintedSprite() / drawSprite()
PNG absent?   →  CharacterSprite.drawHead/drawHair/drawFace (Graphics2D primitives)
```

---

## 8. Adding New Equipment Appearance

1. **Create greyscale PNG** at the correct path (see §4) — 64×64 px, 3-step shading
2. **Register slug** — if the equipment name is ASCII, `toSlug()` derives it
   automatically. For custom slugs override `SpritePathResolver` switch.
3. **Set `displayColor`** in `Equipment` — this becomes the tint colour at runtime.
   Choose the colour that best represents the item's *mid-tone*; the tinting engine
   applies it at 68% opacity, so the greyscale shading shows through.
4. **Verify fallback** — delete or rename the PNG and confirm the Graphics2D fallback
   renders without error before shipping the asset.

---

## 9. Adding New Hair / Eye Styles

1. Add the new enum value to `Appearance.HairStyle` / `EyeStyle` / `EyebrowStyle`
2. Add a `case` in the matching `CharacterSprite` switch (`drawHair`, `drawNormalEye`,
   `drawEyebrow`) with the Graphics2D fallback implementation
3. Add `SpritePathResolver.hairFront()` / `hairBack()` / `face()` case returning the
   PNG path slug
4. Author the greyscale PNG pair (front + back) following §3
