# CGN SelectionView for WorldEdit

Server-side selection visualization for WorldEdit on NeoForge.

Serverseitige Visualisierung von WorldEdit-Auswahlen für NeoForge.

## Status

The project is in early development. The first milestone provides the NeoForge project structure and the `/wesv` command. Selection rendering will follow in the next step.

Das Projekt befindet sich in einer frühen Entwicklungsphase. Der erste Meilenstein stellt die NeoForge-Projektstruktur und den Befehl `/wesv` bereit. Die eigentliche Auswahl-Anzeige folgt im nächsten Schritt.

## Target / Ziel

- Minecraft 26.1.2
- NeoForge 26.1.2.76
- Java 25
- WorldEdit 7.4.3
- Dedicated-server compatible / Für Dedicated Server geeignet
- No client mod required / Keine Client-Mod erforderlich

## Commands / Befehle

- `/wesv`
- `/wesv info`

## Planned / Geplant

- Cuboid selection visualization using vanilla particles
- Quader-Auswahl per Vanilla-Partikel anzeigen
- Per-player enable and disable state
- Anzeige pro Spieler ein- und ausschalten
- Particle and size limits
- Begrenzung von Partikelmenge und Auswahlgröße

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