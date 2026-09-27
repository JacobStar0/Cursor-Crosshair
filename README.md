# Cursor Crosshair

Client-side Fabric mod for Minecraft 26.3.

Hold the configured key while playing to unlock the crosshair without showing the operating-system cursor. The camera stays still and Minecraft's own crosshair follows the mouse. Normal attack/use/pick actions target the block or entity under that crosshair. Left-clicking a hotbar slot selects it.

While free-crosshair mode is active, click the middle mouse button to use Minecraft's normal pick-block action. Hold it to look around with normal camera controls. The crosshair stays fixed at its last screen position and continues from that position when the middle button is released.

The mod also renders vanilla Minecraft's crosshair at subpixel coordinates while free-crosshair mode is inactive. Both crosshair modes use the same precise center, so switching modes does not cause a horizontal or vertical jump.

The default key is Left Alt. It can be changed under Options > Controls > Key Binds > Cursor Crosshair.

## Compatibility

- Accurate Block Placement Reborn uses the movable crosshair's current target for repeated placement.
- Axiom's Infinite Reach follows the movable crosshair and preserves Axiom's own reach handling and restrictions.
- Axiom's Bulldozer follows the movable crosshair during continuous instant breaking.
- Axiom's Fast Place follows the movable crosshair during continuous placement.
- Axiom's Angel Placement uses the movable crosshair for placement and its preview.
- Axiom's Replace Mode follows the movable crosshair during continuous replacement.
- Cursor Crosshair does not change either mod's key bindings.

## Build

Install JDK 25, then run:

```powershell
.\gradlew.bat build
```

The mod jar is written to `build/libs` and does not require Fabric API.
