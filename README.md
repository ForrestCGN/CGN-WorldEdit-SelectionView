# CGN SelectionView for WorldEdit

Server-side WorldEdit selection visualization for NeoForge using vanilla particles.

Serverseitige Anzeige von WorldEdit-Auswahlen für NeoForge mit Vanilla-Partikeln.

## Status

Version `0.2.5` displays complete WorldEdit cuboid selections with four selectable particle styles for the outer edges and helper grid. Personal commands are available to normal players by default, while NeoForge permission nodes allow permission managers such as LuckPerms to control access. No client mod is required.

Version `0.2.5` zeigt vollständige WorldEdit-Quaderauswahlen mit vier auswählbaren Partikelstilen für Außenkanten und Hilfsraster. Persönliche Befehle stehen normalen Spielern standardmäßig zur Verfügung, während NeoForge-Permission-Nodes die Steuerung über Rechteverwaltungen wie LuckPerms ermöglichen. Eine Client-Mod ist nicht erforderlich.

## Target / Ziel

- Minecraft 26.1.2
- NeoForge 26.1.2.76
- Java 25
- WorldEdit 7.4.3
- Dedicated server
- No client mod required / Keine Client-Mod erforderlich

## Installation

1. Install WorldEdit on the NeoForge server.
2. Put the CGN SelectionView JAR into the server's `mods` directory.
3. Restart the server.
4. Edit `config/cgn-selection-view-common.toml` when needed.

1. WorldEdit auf dem NeoForge-Server installieren.
2. Die CGN-SelectionView-JAR in den `mods`-Ordner des Servers legen.
3. Den Server neu starten.
4. Bei Bedarf `config/cgn-selection-view-common.toml` bearbeiten.

## Commands / Befehle

Normal players can use the personal commands by default. `/cgnsv reload` still requires operator level 2 or the dedicated reload permission.

Normale Spieler können die persönlichen Befehle standardmäßig verwenden. `/cgnsv reload` benötigt weiterhin Operator-Level 2 oder die eigene Reload-Berechtigung.

- `/cgnsv` – show status and command overview / Status und Befehlsübersicht anzeigen
- `/cgnsv on` – enable visualization for the current session / Anzeige für die aktuelle Sitzung aktivieren
- `/cgnsv off` – disable visualization for the current session / Anzeige für die aktuelle Sitzung deaktivieren
- `/cgnsv toggle` – toggle visualization for the current session / Anzeige für die aktuelle Sitzung umschalten
- `/cgnsv info` – show version and active core settings / Version und aktive Kerneinstellungen anzeigen
- `/cgnsv reload` – reload the server config / Server-Config neu laden

## Permissions / Berechtigungen

The mod registers native NeoForge permission nodes. A separate permission manager is optional. Without one, the documented default values are used.

Die Mod registriert native NeoForge-Permission-Nodes. Eine zusätzliche Rechteverwaltung ist optional. Ohne diese gelten die angegebenen Standardwerte.

- `cgnselectionview.use` – master access to personal commands / Hauptzugriff auf persönliche Befehle – default / Standard: `true`
- `cgnselectionview.toggle` – access to `on`, `off` and `toggle` / Zugriff auf `on`, `off` und `toggle` – default / Standard: `true`
- `cgnselectionview.info` – access to `/cgnsv` and `info` / Zugriff auf `/cgnsv` und `info` – default / Standard: `true`
- `cgnselectionview.reload` – access to `reload` without operator level 2 / Zugriff auf `reload` ohne Operator-Level 2 – default / Standard: `false`

Operator level 2 can always use `/cgnsv reload`.

Operator-Level 2 kann `/cgnsv reload` immer verwenden.

Example for LuckPerms / Beispiel für LuckPerms:

```text
/lp group default permission set cgnselectionview.use true
/lp group default permission set cgnselectionview.toggle true
/lp group default permission set cgnselectionview.info true
/lp group admin permission set cgnselectionview.reload true
```

To disable personal commands for a group, set `cgnselectionview.use` to `false` for that group.

Um die persönlichen Befehle für eine Gruppe zu deaktivieren, wird `cgnselectionview.use` für diese Gruppe auf `false` gesetzt.

## Configuration / Konfiguration

The file is generated automatically on the first server start:

Die Datei wird beim ersten Serverstart automatisch erzeugt:

```text
config/cgn-selection-view-common.toml
```

Existing config files are migrated automatically to the clean `0.2.4` format. Removed custom-color settings are deleted, legacy `custom_dust` values become `orange_dust`, and all unrelated settings are retained.

Vorhandene Config-Dateien werden automatisch in das aufgeräumte Format von `0.2.4` migriert. Entfernte Einstellungen für eigene Farben werden gelöscht, alte `custom_dust`-Werte werden zu `orange_dust`, und alle übrigen Einstellungen bleiben erhalten.

### Particle styles / Partikelstile

```toml
# Supported / Unterstützt:
# flame, orange_dust, red_dust, end_rod

edgeParticleStyle = "flame"
gridParticleStyle = "orange_dust"

edgeDustScale = 1.25
gridDustScale = 0.8
```

Exactly these four values can be used for both `edgeParticleStyle` and `gridParticleStyle`:

Für `edgeParticleStyle` und `gridParticleStyle` können genau diese vier Werte verwendet werden:

- `flame` – animated orange flame / animierte orangefarbene Flamme
- `orange_dust` – orange dust / orangefarbenes Dust
- `red_dust` – red dust / rotes Dust
- `end_rod` – bright white end-rod particle / heller weißer Endstab-Partikel

The scale values affect only `orange_dust` and `red_dust`. They have no effect on `flame` or `end_rod`.

Die Größenwerte wirken nur bei `orange_dust` und `red_dust`. Bei `flame` und `end_rod` haben sie keine Wirkung.

Apply changes without a server restart:

Änderungen ohne Serverneustart übernehmen:

```text
/cgnsv reload
```

### Other settings / Weitere Einstellungen

- `defaultEnabled` – default state for every new player session / Standardstatus für jede neue Spielersitzung
- `allowPlayerToggle` – allows `on`, `off` and `toggle` / erlaubt `on`, `off` und `toggle`
- `renderIntervalTicks` – update interval / Aktualisierungsintervall
- `renderDistance` – maximum particle distance / maximale Partikelentfernung
- `maxEdgeParticles` – outer edge particle budget / Partikelbudget für Außenkanten
- `maxGridParticles` – helper grid particle budget / Partikelbudget für das Hilfsraster
- `minimumEdgeSpacing` – minimum outer edge spacing / minimaler Partikelabstand der Außenkanten
- `smallSelectionThreshold`, `smallGridSpacing` – small selection grid / Raster für kleine Auswahlen
- `mediumSelectionThreshold`, `mediumGridSpacing` – medium selection grid / Raster für mittlere Auswahlen
- `largeGridSpacing` – large selection grid / Raster für große Auswahlen
- `gridTop`, `gridBottom`, `gridSides` – visible grid surfaces / sichtbare Rasterflächen

The default is `defaultEnabled = true`. Player changes are intentionally not stored permanently. After logout or a server restart, the configured default applies again.

Standardmäßig gilt `defaultEnabled = true`. Spieleränderungen werden bewusst nicht dauerhaft gespeichert. Nach Logout oder Serverneustart gilt wieder der konfigurierte Standard.

## Current limits / Aktuelle Grenzen

- Cuboid selections only / Nur Quaderauswahlen
- Default budgets: 280 edge particles and 300 grid particles per render pass / Standardbudgets: 280 Kantenpartikel und 300 Rasterpartikel pro Renderdurchlauf
- Default render distance: 256 blocks / Standard-Sichtweite: 256 Blöcke
- Large grids are internally limited to prevent excessive line generation / Große Raster werden intern begrenzt, um übermäßige Linienmengen zu verhindern

## Other versions / Weitere Versionen

Ports to other Minecraft and NeoForge versions can be requested. Please include the desired Minecraft, NeoForge and WorldEdit versions.

Portierungen auf weitere Minecraft- und NeoForge-Versionen können angefragt werden. Bitte dabei die gewünschte Minecraft-, NeoForge- und WorldEdit-Version angeben.

## Build

```bash
gradle build
```

The generated JAR is written to `build/libs/`.

Die erzeugte JAR liegt anschließend unter `build/libs/`.

## License / Lizenz

MIT

## Notice / Hinweis

This is an independent community project and is not affiliated with or endorsed by EngineHub or the WorldEdit project.

Dies ist ein unabhängiges Community-Projekt und steht nicht in Verbindung mit EngineHub oder dem WorldEdit-Projekt.
