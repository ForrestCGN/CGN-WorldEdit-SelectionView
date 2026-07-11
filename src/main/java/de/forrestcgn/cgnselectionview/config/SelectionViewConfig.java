package de.forrestcgn.cgnselectionview.config;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SelectionViewConfig {
    public static final String FILE_NAME = "cgn-selection-view-common.toml";

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String DEFAULT_CONTENT = """
            # CGN SelectionView for WorldEdit
            # Server configuration / Server-Konfiguration

            # Enable the visualization by default for every player session.
            # Aktiviert die Anzeige standardmäßig für jede Spielersitzung.
            defaultEnabled = true

            # Allow the existing on/off/toggle commands.
            # Erlaubt die vorhandenen Befehle on/off/toggle.
            allowPlayerToggle = true

            # Render interval in server ticks. 20 ticks = 1 second.
            # Aktualisierungsintervall in Server-Ticks. 20 Ticks = 1 Sekunde.
            renderIntervalTicks = 10

            # Maximum distance between the player and a rendered particle.
            # Maximale Entfernung zwischen Spieler und angezeigtem Partikel.
            renderDistance = 256.0

            # Particle limits per render pass.
            # Partikellimits pro Renderdurchlauf.
            maxEdgeParticles = 280
            maxGridParticles = 300

            # Minimum spacing between flame particles on the outer edges.
            # Mindestabstand der Flammenpartikel auf den Außenkanten.
            minimumEdgeSpacing = 0.65

            # Adaptive grid spacing. Selection size is the longest cuboid edge.
            # Adaptiver Rasterabstand. Als Auswahlgröße gilt die längste Quaderkante.
            smallSelectionThreshold = 20.0
            smallGridSpacing = 4.0

            mediumSelectionThreshold = 80.0
            mediumGridSpacing = 6.0

            largeGridSpacing = 10.0

            # Select which surfaces receive the red helper grid.
            # Legt fest, welche Flächen das rote Hilfsraster erhalten.
            gridTop = true
            gridBottom = true
            gridSides = true
            """;

    private final Path path;
    private volatile Values values = Values.defaults();

    public SelectionViewConfig() {
        this.path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        ReloadResult result = reload();
        if (!result.success()) {
            LOGGER.error("Failed to load {}: {}", path, result.message());
        }
    }

    public Values values() {
        return values;
    }

    public Path path() {
        return path;
    }

    public synchronized ReloadResult reload() {
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) {
                Files.writeString(path, DEFAULT_CONTENT, StandardCharsets.UTF_8);
            }

            Values loadedValues = parse(Files.readAllLines(path, StandardCharsets.UTF_8));
            values = loadedValues;
            LOGGER.info("Loaded CGN SelectionView config from {}", path);
            return new ReloadResult(true, "Config neu geladen. / Config reloaded.");
        } catch (Exception exception) {
            LOGGER.error("Could not reload CGN SelectionView config from {}", path, exception);
            return new ReloadResult(false, exception.getMessage() == null
                    ? exception.getClass().getSimpleName()
                    : exception.getMessage());
        }
    }

    private static Values parse(List<String> lines) {
        Map<String, String> entries = new HashMap<>();

        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = stripComment(lines.get(lineNumber)).trim();
            if (line.isEmpty()) {
                continue;
            }

            int separator = line.indexOf('=');
            if (separator <= 0 || separator == line.length() - 1) {
                throw new IllegalArgumentException("Ungültige Config-Zeile / Invalid config line " + (lineNumber + 1));
            }

            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            entries.put(key, value);
        }

        Values defaults = Values.defaults();

        boolean defaultEnabled = readBoolean(entries, "defaultEnabled", defaults.defaultEnabled());
        boolean allowPlayerToggle = readBoolean(entries, "allowPlayerToggle", defaults.allowPlayerToggle());
        int renderIntervalTicks = readInt(entries, "renderIntervalTicks", defaults.renderIntervalTicks(), 1, 200);
        double renderDistance = readDouble(entries, "renderDistance", defaults.renderDistance(), 16.0D, 2048.0D);
        int maxEdgeParticles = readInt(entries, "maxEdgeParticles", defaults.maxEdgeParticles(), 0, 10000);
        int maxGridParticles = readInt(entries, "maxGridParticles", defaults.maxGridParticles(), 0, 20000);
        double minimumEdgeSpacing = readDouble(entries, "minimumEdgeSpacing", defaults.minimumEdgeSpacing(), 0.1D, 128.0D);
        double smallSelectionThreshold = readDouble(entries, "smallSelectionThreshold", defaults.smallSelectionThreshold(), 1.0D, 1_000_000.0D);
        double smallGridSpacing = readDouble(entries, "smallGridSpacing", defaults.smallGridSpacing(), 0.25D, 1024.0D);
        double mediumSelectionThreshold = readDouble(entries, "mediumSelectionThreshold", defaults.mediumSelectionThreshold(), 1.0D, 1_000_000.0D);
        double mediumGridSpacing = readDouble(entries, "mediumGridSpacing", defaults.mediumGridSpacing(), 0.25D, 1024.0D);
        double largeGridSpacing = readDouble(entries, "largeGridSpacing", defaults.largeGridSpacing(), 0.25D, 1024.0D);
        boolean gridTop = readBoolean(entries, "gridTop", defaults.gridTop());
        boolean gridBottom = readBoolean(entries, "gridBottom", defaults.gridBottom());
        boolean gridSides = readBoolean(entries, "gridSides", defaults.gridSides());

        if (smallSelectionThreshold >= mediumSelectionThreshold) {
            throw new IllegalArgumentException(
                    "smallSelectionThreshold muss kleiner als mediumSelectionThreshold sein / must be smaller"
            );
        }

        return new Values(
                defaultEnabled,
                allowPlayerToggle,
                renderIntervalTicks,
                renderDistance,
                maxEdgeParticles,
                maxGridParticles,
                minimumEdgeSpacing,
                smallSelectionThreshold,
                smallGridSpacing,
                mediumSelectionThreshold,
                mediumGridSpacing,
                largeGridSpacing,
                gridTop,
                gridBottom,
                gridSides
        );
    }

    private static String stripComment(String line) {
        boolean inQuotes = false;
        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (current == '"') {
                inQuotes = !inQuotes;
            } else if (current == '#' && !inQuotes) {
                return line.substring(0, index);
            }
        }
        return line;
    }

    private static boolean readBoolean(Map<String, String> entries, String key, boolean defaultValue) {
        String raw = entries.get(key);
        if (raw == null) {
            return defaultValue;
        }

        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException(key + " muss true oder false sein / must be true or false");
        };
    }

    private static int readInt(Map<String, String> entries, String key, int defaultValue, int minimum, int maximum) {
        String raw = entries.get(key);
        if (raw == null) {
            return defaultValue;
        }

        try {
            int value = Integer.parseInt(raw);
            if (value < minimum || value > maximum) {
                throw new IllegalArgumentException(key + " außerhalb des Bereichs / outside range " + minimum + "-" + maximum);
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + " ist keine ganze Zahl / is not an integer", exception);
        }
    }

    private static double readDouble(
            Map<String, String> entries,
            String key,
            double defaultValue,
            double minimum,
            double maximum
    ) {
        String raw = entries.get(key);
        if (raw == null) {
            return defaultValue;
        }

        try {
            double value = Double.parseDouble(raw);
            if (!Double.isFinite(value) || value < minimum || value > maximum) {
                throw new IllegalArgumentException(key + " außerhalb des Bereichs / outside range " + minimum + "-" + maximum);
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + " ist keine Zahl / is not a number", exception);
        }
    }

    public record ReloadResult(boolean success, String message) {
    }

    public record Values(
            boolean defaultEnabled,
            boolean allowPlayerToggle,
            int renderIntervalTicks,
            double renderDistance,
            int maxEdgeParticles,
            int maxGridParticles,
            double minimumEdgeSpacing,
            double smallSelectionThreshold,
            double smallGridSpacing,
            double mediumSelectionThreshold,
            double mediumGridSpacing,
            double largeGridSpacing,
            boolean gridTop,
            boolean gridBottom,
            boolean gridSides
    ) {
        public static Values defaults() {
            return new Values(
                    true,
                    true,
                    10,
                    256.0D,
                    280,
                    300,
                    0.65D,
                    20.0D,
                    4.0D,
                    80.0D,
                    6.0D,
                    10.0D,
                    true,
                    true,
                    true
            );
        }

        public double gridSpacingFor(double selectionSize) {
            if (selectionSize <= smallSelectionThreshold) {
                return smallGridSpacing;
            }
            if (selectionSize <= mediumSelectionThreshold) {
                return mediumGridSpacing;
            }
            return largeGridSpacing;
        }
    }
}
