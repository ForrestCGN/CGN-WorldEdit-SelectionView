package de.forrestcgn.cgnselectionview.config;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SelectionViewConfig {
    public static final String FILE_NAME = "cgn-selection-view-common.toml";

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String PARTICLE_STYLE_CONTENT = """
            # Particle styles for outer edges and helper grid.
            # Partikelstile für Außenkanten und Hilfsraster.
            # Supported / Unterstützt: orange_dust, red_dust, custom_dust, flame, end_rod
            edgeParticleStyle = "orange_dust"
            gridParticleStyle = "red_dust"

            # Custom dust colors in #RRGGBB format. Used only with custom_dust.
            # Eigene Dust-Farben im Format #RRGGBB. Nur bei custom_dust verwendet.
            edgeDustColor = "#FF6600"
            gridDustColor = "#FF0000"

            # Dust particle size. Used by all dust styles.
            # Größe der Dust-Partikel. Wird von allen Dust-Stilen verwendet.
            edgeDustScale = 1.25
            gridDustScale = 0.8
            """;

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

            # Minimum spacing between particles on the outer edges.
            # Mindestabstand der Partikel auf den Außenkanten.
            minimumEdgeSpacing = 0.65

            """ + PARTICLE_STYLE_CONTENT + """

            # Adaptive grid spacing. Selection size is the longest cuboid edge.
            # Adaptiver Rasterabstand. Als Auswahlgröße gilt die längste Quaderkante.
            smallSelectionThreshold = 20.0
            smallGridSpacing = 4.0

            mediumSelectionThreshold = 80.0
            mediumGridSpacing = 6.0

            largeGridSpacing = 10.0

            # Select which surfaces receive the helper grid.
            # Legt fest, welche Flächen das Hilfsraster erhalten.
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

            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            if (appendMissingParticleSettings(lines)) {
                lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            }

            Values loadedValues = parse(lines);
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

    private boolean appendMissingParticleSettings(List<String> lines) throws IOException {
        StringBuilder appendix = new StringBuilder();
        appendMissingSetting(lines, appendix, "edgeParticleStyle", "edgeParticleStyle = \"orange_dust\"");
        appendMissingSetting(lines, appendix, "gridParticleStyle", "gridParticleStyle = \"red_dust\"");
        appendMissingSetting(lines, appendix, "edgeDustColor", "edgeDustColor = \"#FF6600\"");
        appendMissingSetting(lines, appendix, "gridDustColor", "gridDustColor = \"#FF0000\"");
        appendMissingSetting(lines, appendix, "edgeDustScale", "edgeDustScale = 1.25");
        appendMissingSetting(lines, appendix, "gridDustScale", "gridDustScale = 0.8");

        if (appendix.isEmpty()) {
            return false;
        }

        String header = """

                # Particle styles added by CGN SelectionView 0.2.3.
                # Partikelstile, ergänzt durch CGN SelectionView 0.2.3.
                # Supported / Unterstützt: orange_dust, red_dust, custom_dust, flame, end_rod
                # Dust colors use #RRGGBB and apply only to custom_dust.
                # Dust-Farben verwenden #RRGGBB und gelten nur für custom_dust.
                """;
        Files.writeString(
                path,
                header + appendix,
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND
        );
        LOGGER.info("Added missing particle style settings to {}", path);
        return true;
    }

    private static void appendMissingSetting(
            List<String> lines,
            StringBuilder appendix,
            String key,
            String defaultLine
    ) {
        if (!containsSetting(lines, key)) {
            appendix.append(defaultLine).append('\n');
        }
    }

    private static boolean containsSetting(List<String> lines, String key) {
        for (String rawLine : lines) {
            String line = stripComment(rawLine).trim();
            int separator = line.indexOf('=');
            if (separator > 0 && line.substring(0, separator).trim().equals(key)) {
                return true;
            }
        }
        return false;
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

        ParticleStyle edgeParticleStyle = readParticleStyle(entries, "edgeParticleStyle", defaults.edgeParticleStyle());
        ParticleStyle gridParticleStyle = readParticleStyle(entries, "gridParticleStyle", defaults.gridParticleStyle());
        int edgeDustColor = readColor(entries, "edgeDustColor", defaults.edgeDustColor());
        int gridDustColor = readColor(entries, "gridDustColor", defaults.gridDustColor());
        float edgeDustScale = (float) readDouble(entries, "edgeDustScale", defaults.edgeDustScale(), 0.1D, 4.0D);
        float gridDustScale = (float) readDouble(entries, "gridDustScale", defaults.gridDustScale(), 0.1D, 4.0D);

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
                edgeParticleStyle,
                gridParticleStyle,
                edgeDustColor,
                gridDustColor,
                edgeDustScale,
                gridDustScale,
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

    private static String readString(Map<String, String> entries, String key, String defaultValue) {
        String raw = entries.get(key);
        if (raw == null) {
            return defaultValue;
        }

        String value = raw.trim();
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static ParticleStyle readParticleStyle(
            Map<String, String> entries,
            String key,
            ParticleStyle defaultValue
    ) {
        String value = readString(entries, key, defaultValue.configName());
        return ParticleStyle.parse(value, key);
    }

    private static int readColor(Map<String, String> entries, String key, int defaultValue) {
        String fallback = String.format(Locale.ROOT, "#%06X", defaultValue & 0xFFFFFF);
        String value = readString(entries, key, fallback).trim();

        if (value.startsWith("#")) {
            value = value.substring(1);
        } else if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
        }

        if (!value.matches("[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException(key + " muss #RRGGBB sein / must use #RRGGBB");
        }

        return Integer.parseInt(value, 16);
    }

    public enum ParticleStyle {
        ORANGE_DUST("orange_dust"),
        RED_DUST("red_dust"),
        CUSTOM_DUST("custom_dust"),
        FLAME("flame"),
        END_ROD("end_rod");

        private final String configName;

        ParticleStyle(String configName) {
            this.configName = configName;
        }

        public String configName() {
            return configName;
        }

        public static ParticleStyle parse(String value, String key) {
            String normalized = value.trim().toLowerCase(Locale.ROOT).replace('-', '_');
            if (normalized.equals("dust")) {
                return CUSTOM_DUST;
            }

            for (ParticleStyle style : values()) {
                if (style.configName.equals(normalized)) {
                    return style;
                }
            }

            throw new IllegalArgumentException(
                    key + " ungültig / invalid. Erlaubt / allowed: orange_dust, red_dust, custom_dust, flame, end_rod"
            );
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
            ParticleStyle edgeParticleStyle,
            ParticleStyle gridParticleStyle,
            int edgeDustColor,
            int gridDustColor,
            float edgeDustScale,
            float gridDustScale,
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
                    ParticleStyle.ORANGE_DUST,
                    ParticleStyle.RED_DUST,
                    0xFF6600,
                    0xFF0000,
                    1.25F,
                    0.8F,
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
