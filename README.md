# CGN SelectionView for WorldEdit

Server-side WorldEdit selection visualization for NeoForge using vanilla particles.

Serverseitige Anzeige von WorldEdit-Auswahlen für NeoForge mit Vanilla-Partikeln.

## Status

Version `0.2.2` displays complete WorldEdit cuboid selections with orange flame edges and an adaptive red helper grid. A reloadable server config controls the default state, render interval, distance, particle limits, grid spacing and visible grid surfaces. No client mod is required.

Version `0.2.2` zeigt vollständige WorldEdit-Quaderauswahlen mit orangefarbenen Flammenkanten und einem adaptiven roten Hilfsraster an. Eine neu ladbare Server-Config steuert Standardstatus, Aktualisierungsintervall, Distanz, Partikellimits, Rasterabstände und sichtbare Rasterflächen. Eine Client-Mod ist nicht erforderlich.

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

The commands currently require permission level 2.

Die Befehle benötigen aktuell Berechtigungsstufe 2.

- `/cgnsv` – show status and command overview / Status und Befehlsübersicht anzeigen
- `/cgnsv on` – enable visualization for the current session / Anzeige für die aktuelle Sitzung aktivieren
- `/cgnsv off` – disable visualization for the current session / Anzeige für die aktuelle Sitzung deaktivieren
- `/cgnsv toggle` – toggle visualization for the current session / Anzeige für die aktuelle Sitzung umschalten
- `/cgnsv info` – show version and active core settings / Version und aktive Kerneinstellungen anzeigen
- `/cgnsv reload` – reload the server config / Server-Config neu laden

## Configuration / Konfiguration

The file is generated automatically on the first server start:

Die Datei wird beim ersten Serverstart automatisch erzeugt:

```text
config/cgn-selection-view-common.toml
```

Available settings / Verfügbare Einstellungen:

- `defaultEnabled` – default state for every new player session / Standardstatus für jede neue Spielersitzung
- `allowPlayerToggle` – allows `on`, `off` and `toggle` / erlaubt `on`, `off` und `toggle`
- `renderIntervalTicks` – update interval / Aktualisierungsintervall
- `renderDistance` – maximum particle distance / maximale Partikelentfernung
- `maxEdgeParticles` – flame particle budget / Budget für Flammenpartikel
- `maxGridParticles` – red grid particle budget / Budget für rote Rasterpartikel
- `minimumEdgeSpacing` – minimum flame spacing / minimaler Flammenabstand
- `smallSelectionThreshold`, `smallGridSpacing` – small selection grid / Raster für kleine Auswahlen
- `mediumSelectionThreshold`, `mediumGridSpacing` – medium selection grid / Raster für mittlere Auswahlen
- `largeGridSpacing` – large selection grid / Raster für große Auswahlen
- `gridTop`, `gridBottom`, `gridSides` – visible grid surfaces / sichtbare Rasterflächen

The default is `defaultEnabled = true`. Player changes are intentionally not stored permanently. After logout or a server restart, the configured default applies again.

Standardmäßig gilt `defaultEnabled = true`. Spieleränderungen werden bewusst nicht dauerhaft gespeichert. Nach Logout oder Serverneustart gilt wieder der konfigurierte Standard.

## Current limits / Aktuelle Grenzen

- Cuboid selections only / Nur Quaderauswahlen
- Default budgets: 280 flame particles and 300 red grid particles per render pass / Standardbudgets: 280 Flammenpartikel und 300 rote Rasterpartikel pro Renderdurchlauf
- Default render distance: 256 blocks / Standard-Sichtweite: 256 Blöcke
- Large grids are internally limited to prevent excessive line generation / Große Raster werden intern begrenzt, um übermäßige Linienmengen zu verhindern

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
