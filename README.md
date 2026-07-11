# CGN SelectionView for WorldEdit

Server-side WorldEdit selection visualization for NeoForge using vanilla particles.

Serverseitige Anzeige von WorldEdit-Auswahlen für NeoForge mit Vanilla-Partikeln.

## Status

Version `0.2.1` displays complete WorldEdit cuboid selections with orange flame edges and a red helper grid on the selection surfaces. The visualization is calculated on the server and sent only to the player who enabled it. No client mod is required.

Version `0.2.1` zeigt vollständige WorldEdit-Quaderauswahlen mit orangefarbenen Flammenkanten und einem roten Hilfsraster auf den Auswahlflächen an. Die Darstellung wird auf dem Server berechnet und nur an den Spieler gesendet, der sie aktiviert hat. Eine Client-Mod ist nicht erforderlich.

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

1. WorldEdit auf dem NeoForge-Server installieren.
2. Die CGN-SelectionView-JAR in den `mods`-Ordner des Servers legen.
3. Den Server neu starten.

## Commands / Befehle

The commands currently require permission level 2.

Die Befehle benötigen aktuell Berechtigungsstufe 2.

- `/cgnsv` – show status and command overview / Status und Befehlsübersicht anzeigen
- `/cgnsv on` – enable visualization / Anzeige aktivieren
- `/cgnsv off` – disable visualization / Anzeige deaktivieren
- `/cgnsv toggle` – toggle visualization / Anzeige umschalten
- `/cgnsv info` – show version and supported selection type / Version und unterstützten Auswahltyp anzeigen

## Current limits / Aktuelle Grenzen

- Cuboid selections only / Nur Quaderauswahlen
- Up to 280 flame particles and 300 red helper particles per render pass / Bis zu 280 Flammenpartikel und 300 rote Hilfspartikel pro Renderdurchlauf
- Particles are only sent within 256 blocks of the player / Partikel werden nur im Umkreis von 256 Blöcken gesendet
- Large selections are automatically rendered with wider spacing / Große Auswahlen werden automatisch mit größeren Abständen dargestellt
- Enabled state is kept until the server restarts / Der Aktivierungsstatus bleibt bis zum Serverneustart erhalten

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
