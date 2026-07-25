# World Preset Manager

Setzt Standard-World-Presets, Datapacks, Game-Rules und Gamemode für neue Welten per Ingame-Kommando. Nur Client-seitig (Fabric).

## Installation

1. [Fabric Loader](https://fabricmc.net/use/) für Minecraft **1.22** installieren
2. [Fabric API](https://modrinth.com/mod/fabric-api) in den `mods`-Ordner legen
3. `world-preset-1.0.0+26.2.jar` in den `mods`-Ordner legen

## Commands

Alle Commands starten mit `/worldpreset`. Der Server muss nicht laufen — alles funktioniert lokal.

### World Preset

Standard-Preset für den "Create World"-Screen festlegen.

| Command | Beschreibung |
|---|---|
| `/worldpreset preset set <preset>` | Preset als Standard setzen (z.B. `minecraft:amplified`) |
| `/worldpreset preset list` | Alle verfügbaren Presets anzeigen |
| `/worldpreset preset clear` | Standard-Preset entfernen |

### Datapacks

Datapacks aus dem `config/worldpreset/datapacks/`-Ordner werden automatisch in neue Welten kopiert.

| Command | Beschreibung |
|---|---|
| `/worldpreset datapack add <name>` | Datapack-Ordner zur Liste hinzufügen |
| `/worldpreset datapack remove <name>` | Datapack-Ordner aus Liste entfernen |
| `/worldpreset datapack list` | Alle registrierten Datapacks anzeigen |
| `/worldpreset datapack clear` | Alle Datapacks aus Liste entfernen |

Datapack-Ordner in `config/worldpreset/datapacks/` ablegen (z.B. `config/worldpreset/datapacks/mein-pack/`), dann mit `/worldpreset datapack add mein-pack` registrieren.

### Game Rules

| Command | Beschreibung |
|---|---|
| `/worldpreset gamerule <rule> <value>` | Game-Rule setzen (z.B. `keepInventory true`) |
| `/worldpreset gamerule list` | Alle gesetzten Game-Rules anzeigen |

### Gamemode

| Command | Beschreibung |
|---|---|
| `/worldpreset gamemode <mode>` | Standard-Gamemode setzen (`survival`, `creative`, `adventure`, `spectator`) |
| `/worldpreset gamemode clear` | Standard-Gamemode entfernen |

### Konfiguration anzeigen

| Command | Beschreibung |
|---|---|
| `/worldpreset config` | Gesamte aktuelle Konfiguration anzeigen |

## Konfiguration

Einstellungen werden in `config/worldpreset/config.json` gespeichert:

```json
{
  "worldPreset": "minecraft:amplified",
  "gamemode": "survival",
  "datapacks": ["mein-pack"],
  "gameRules": {
    "keepInventory": "true",
    "doFireTick": "false"
  }
}
```

## Beispiel-Workflow

```
/worldpreset preset set minecraft:large_biomes
/worldpreset gamemode survival
/worldpreset gamerule keepInventory true
/worldpreset gamerule doFireTick false
/worldpreset datapack add mein-pack
/worldpreset config
```

Jede neue Welt startet dann mit Large Biomes, Survival, Keep-Inventory und ohne Fire-Tick — plus deinem Datapack.