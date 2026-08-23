# Cursor Crosshair

Client-side Fabric mod for Minecraft 26.2.

Hold the configured key while playing to unlock the crosshair without showing the operating-system cursor. The camera stays still and Minecraft's own crosshair follows the mouse. Normal attack/use/pick actions target the block or entity under that crosshair. Left-clicking a hotbar slot selects it.

The default key is Left Alt. It can be changed under Options > Controls > Key Binds > Cursor Crosshair.

## Build

Install JDK 25, then run:

```powershell
.\gradlew.bat build
```

The mod jar is written to `build/libs` and does not require Fabric API.
