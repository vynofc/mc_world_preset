# World Preset Manager

Konfiguriert Standard-World-Presets, Datapacks, Game-Rules und den Gamemode für neue Welten über ein normales Mod-Menu-Konfigurationsmenü. Die Mod läuft Client-seitig mit Fabric.

## Installation

1. [Fabric Loader](https://fabricmc.net/use/) für Minecraft **26.2** installieren.
2. [Fabric API](https://modrinth.com/mod/fabric-api) und [Mod Menu](https://modrinth.com/mod/modmenu) in den `mods`-Ordner legen.
3. `world-preset-1.0.0+26.2.jar` in den `mods`-Ordner legen.

## Konfiguration

In Minecraft **Mods → World Preset Manager → Konfigurieren** öffnen. Dort lassen sich folgende Werte setzen:

- World-Preset, zum Beispiel `minecraft:large_biomes`
- Standard-Gamemode
- Datapack-Dateien oder -Ordner, mit Komma getrennt
- Game-Rules im Format `rule=value`, mit Komma getrennt

Datapacks müssen zusätzlich unter `config/worldpreset/datapacks/` liegen. Nur die im Menü eingetragenen Namen werden in eine neue Welt kopiert.

Die Einstellungen werden in `config/worldpreset/config.json` gespeichert:

```json
{
  "worldPreset": "minecraft:large_biomes",
  "gamemode": "survival",
  "datapacks": ["mein-pack"],
  "gameRules": {
    "keepInventory": "true",
    "doFireTick": "false"
  }
}
```
