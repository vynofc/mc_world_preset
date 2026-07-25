# PLAN: Config-GUI mit ModMenu-Integration

## Ziel

Die Mod erhält ein normales Konfigurations-GUI (Minecraft-Screen), das über den
ModMenu-ModKnopf in den Mods-Einstellungen erreichbar ist.

## Übersicht der Änderungen

```
Neue Dateien:
  src/main/java/com/example/worldpreset/config/gui/
    WorldPresetConfigScreen.java    ← Der Config-Screen (GUI)
  src/main/java/com/example/worldpreset/config/gui/
    ModMenuIntegration.java         ← ModMenu-Entrypoint (brücke zum Screen)

Geänderte Dateien:
  build.gradle                      ← ModMenu als compileOnly-Dependency
  src/main/resources/fabric.mod.json ← modmenu-Entrypoint eintragen
```

---

## Schritt 1: build.gradle — ModMenu-Dependency

ModMenu wird als `modCompileOnly` eingebunden (optional, läuft auch ohne).
Die ModMenu-API wird nur zur Compile-Zeit benötigt.

```groovy
repositories {
    maven { url 'https://maven.terraformersmc.com/releases/' }
}

dependencies {
    // … bestehende …
    modCompileOnly "com.terraformersmc:modmenu:${modmenu_version}"
}
```

In `gradle.properties`:
```properties
modmenu_version=14.0.0
```

> **Hinweis**: Die exakte ModMenu-Version für MC 1.22 muss geprüft werden.
> Falls `14.0.0` nicht existiert, die aktuellste nehmen.

---

## Schritt 2: fabric.mod.json — ModMenu-Entrypoint

```json
"entrypoints": {
  "client": [
    "com.example.worldpreset.WorldPresetMod"
  ],
  "modmenu": [
    "com.example.worldpreset.config.gui.ModMenuIntegration"
  ]
},
"suggests": {
  "modmenu": "*"
}
```

Falls `modmenu` nicht installiert ist, wird der Entrypoint ignoriert —
kein Crash.

---

## Schritt 3: ModMenuIntegration.java

Implementiert `com.terraformersmc.modmenu.api.ModMenuApi` und gibt eine
Factory zurück, die den Config-Screen öffnet.

```java
package com.example.worldpreset.config.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return WorldPresetConfigScreen::new;
    }
}
```

---

## Schritt 4: WorldPresetConfigScreen.java — Das GUI

Ein `Screen`-Subtyp mit folgenden Bedienelementen:

### Layout (von oben nach unten)

| Element | Widget-Typ | Beschreibung |
|---|---|---|
| Titel | `Text` | "World Preset Manager" |
| **World Preset** | `CycleButton` / `DropdownMenu` | Liste aller registrierten Presets (inkl. `minecraft:…`) |
| **Gamemode** | `CycleButton` | survival → creative → adventure → spectator |
| **Datapacks** | `EditBox` + `Button` + Liste | Name eingeben, "Add" klicken; "Remove"-Buttons pro Eintrag |
| **Game Rules** | `EditBox` + `EditBox` + `Button` + Liste | Rule + Value eingeben, "Add" klicken; "Remove" pro Eintrag |
| **Done** | `Button` | Schließt den Screen, speichert automatisch |

### Widget-Details

- **World Preset**: `Button` der den aktuellen Wert zeigt. Beim Klick öffnet
  sich eine Auswahlliste (einfach: Screen wechselt in Liste-Modus; oder
  `CycleButton` wenn die Presets bekannt sind). Am einfachsten: Ein
  `AbstractSelectionList`-ähnlicher Widget-Bereich oder ein `Button`, der
  einen Callback aufruft um das nächste Preset zu wählen.
  
  *Pragmatisch*: `Button` + separates `ObjectSelectionList`-Overlay.
  ODER: `DropdownMenu` (MC 1.22 hat `DropdownMenu`).

- **Gamemode**: `CycleButton.Builder` mit den 4 Modi.

- **Datapacks**: `EditBox` für den Namen + `Button("Add")`. Darunter
  eine scrollbare Liste (`net.minecraft.client.gui.components.ObjectSelectionList`)
  mit je einem `Button("✕")` zum Entfernen.

- **Game Rules**: Zwei `EditBox`-Felder (rule + value) + `Button("Add")`.
  Darunter Liste mit `Button("✕")` pro Eintrag.

### Speichern

- Jede Änderung ruft sofort `ModConfig.save()` auf (wie bei den Commands).
- Der Done-Button ruft `onClose()` → `this.minecraft.setScreen(parent)`.

### Konstruktor

```java
public class WorldPresetConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    public WorldPresetConfigScreen(Screen parent) {
        super(Component.literal("World Preset Manager"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
    }
}
```

---

## Schritt 5: Testen & Bauen

```bash
./gradlew build
```

- Ohne ModMenu installiert: kein GUI, Commands funktionieren weiter.
- Mit ModMenu installiert: Knopf erscheint, Screen öffnet sich.

---

## Risiken / Offene Punkte

1. **ModMenu-Version**: Für MC 1.22 muss die passende ModMenu-Version ermittelt
   werden (aktuell wahrscheinlich `14.x` oder `15.x`). Falls es noch keine
   gibt, `compileOnly` mit `modCompileOnly` + `@Optional`-Annotationen
   verwenden oder ganz auf ModMenu-Dependency verzichten und die Klasse
   mit `@SuppressWarnings` und Reflection laden.

2. **World-Preset-Registry**: Die Preset-Liste wird aus der Registry gelesen.
   Das funktioniert nur, wenn der Client bereits eine Welt geladen hat oder
   die Registry anders verfügbar ist. Notfalls: `Minecraft.getInstance().level`
   prüfen und sonst eine leere Liste zeigen.

3. **Datapack-Liste**: Derzeit werden nur Namen gespeichert, keine Pfade.
   Das ist okay — die Ordner liegen in `config/worldpreset/datapacks/`.

4. **Game-Rules**: Nur Key-Value-Paare als Strings. Keine Validierung gegen
   die echte GameRule-Registry (wäre nice-to-have, aber optional).