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
    private static final int CONFIG_VERSION = 2;

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
                Files.writeString(path, renderConfig(Values.defaults()), StandardCharsets.UTF_8);
            }

            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            if (migrateConfigIfNeeded(lines)) {
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

    private boolean migrateConfigIfNeeded(List<String> lines) throws IOException {
        Map<String, String> entries = parseEntries(lines);
        int configVersion = readInt(entries, "configVersion", 0, 0, CONFIG_VERSION);
        boolean removedColorSettings = entries.containsKey("edgeDustColor") || entries.containsKey("gridDustColor");
        boolean legacyParticleStyle = isLegacyParticleStyle(entries.get("edgeParticleStyle"))
                || isLegacyParticleStyle(entries.get("gridParticleStyle"));

        if (configVersion >= CONFIG_VERSION && !removedColorSettings && !legacyParticleStyle) {
            return false;
        }

        Values migratedValues = readValues(entries, true);

        // The exact old 0.2.3 defaults are migrated to the new 0.2.4 defaults.
        if (configVersion == 0
                && normalizedStyle(entries.get("edgeParticleStyle")).equals("orange_dust")
                && normalizedStyle(entries.get("gridParticleStyle")).equals("red_dust")) {
            migratedValues = withParticleStyles(
                    migratedValues,
                    ParticleStyle.FLAME,
                    ParticleStyle.ORANGE_DUST
            );
        }

        Files.writeString(path, renderConfig(migratedValues), StandardCharsets.UTF_8);
        LOGGER.info("Migrated CGN SelectionView config to version {} at {}", CONFIG_VERSION, path);
        return true;
    }

    private static Values parse(List<String> lines) {
        return readValues(parseEntries(lines), false);
    }

    private static Map<String, String> parseEntries(List<String> lines) {
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

        return entries;
    }

    private static Values readValues(Map<String, String> entries, boolean allowLegacyStyles) {
        Values defaults = Values.defaults();

        boolean defaultEnabled = readBoolean(entries, "defaultEnabled", defaults.defaultEnabled());
        boolean allowPlayerToggle = readBoolean(entries, "allowPlayerToggle", defaults.allowPlayerToggle());
        int renderIntervalTicks = readInt(entries, "renderIntervalTicks", defaults.renderIntervalTicks(), 1, 200);
        double renderDistance = readDouble(entries, "renderDistance", defaults.renderDistance(), 16.0D, 2048.0D);
        int maxEdgeParticles = readInt(entries, "maxEdgeParticles", defaults.maxEdgeParticles(), 0, 10000);
        int maxGridParticles = readInt(entries, "maxGridParticles", defaults.maxGridParticles(), 0, 20000);
        double minimumEdgeSpacing = readDouble(entries, "minimumEdgeSpacing", defaults.minimumEdgeSpacing(), 0.1D, 128.0D);

        ParticleStyle edgeParticleStyle = readParticleStyle(
                entries,
                "edgeParticleStyle",
                defaults.edgeParticleStyle(),
                allowLegacyStyles
        );
        ParticleStyle gridParticleStyle = readParticleStyle(
                entries,
                "gridParticleStyle",
                defaults.gridParticleStyle(),
                allowLegacyStyles
        );
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

    private static Values withParticleStyles(
            Values source,
            ParticleStyle edgeParticleStyle,
            ParticleStyle gridParticleStyle
    ) {
        return new Values(
                source.defaultEnabled(),
                source.allowPlayerToggle(),
                source.renderIntervalTicks(),
                source.renderDistance(),
                source.maxEdgeParticles(),
                source.maxGridParticles(),
                source.minimumEdgeSpacing(),
                edgeParticleStyle,
                gridParticleStyle,
                source.edgeDustScale(),
                source.gridDustScale(),
                source.smallSelectionThreshold(),
                source.smallGridSpacing(),
                source.mediumSelectionThreshold(),
                source.mediumGridSpacing(),
                source.largeGridSpacing(),
                source.gridTop(),
                source.gridBottom(),
                source.gridSides()
        );
    }

    private static String renderConfig(Values config) {
        return """
                # CGN SelectionView for WorldEdit
                # Server configuration / Server-Konfiguration

                # Internal config format. Do not change manually.
                # Internes Config-Format. Nicht manuell ändern.
                configVersion = %d

                # Enable the visualization by default for every player session.
                # Aktiviert die Anzeige standardmäßig für jede Spielersitzung.
                defaultEnabled = %s

                # Allow the existing on/off/toggle commands.
                # Erlaubt die vorhandenen Befehle on/off/toggle.
                allowPlayerToggle = %s

                # Render interval in server ticks. 20 ticks = 1 second.
                # Aktualisierungsintervall in Server-Ticks. 20 Ticks = 1 Sekunde.
                renderIntervalTicks = %d

                # Maximum distance between the player and a rendered particle.
                # Maximale Entfernung zwischen Spieler und angezeigtem Partikel.
                renderDistance = %s

                # Particle limits per render pass.
                # Partikellimits pro Renderdurchlauf.
                maxEdgeParticles = %d
                maxGridParticles = %d

                # Minimum spacing between particles on the outer edges.
                # Mindestabstand der Partikel auf den Außenkanten.
                minimumEdgeSpacing = %s

                # PARTICLE STYLES / PARTIKELSTILE
                # Exactly these four values are supported for BOTH settings:
                # Für BEIDE Einstellungen werden genau diese vier Werte unterstützt:
                # flame, orange_dust, red_dust, end_rod
                #
                # Default: flame outer edges and orange dust helper grid.
                # Standard: Flammen-Außenkanten und orangefarbenes Dust-Hilfsraster.
                edgeParticleStyle = "%s"
                gridParticleStyle = "%s"

                # Size for orange_dust and red_dust. Ignored by flame and end_rod.
                # Größe für orange_dust und red_dust. Bei flame und end_rod ohne Wirkung.
                edgeDustScale = %s
                gridDustScale = %s

                # Adaptive grid spacing. Selection size is the longest cuboid edge.
                # Adaptiver Rasterabstand. Als Auswahlgröße gilt die längste Quaderkante.
                smallSelectionThreshold = %s
                smallGridSpacing = %s

                mediumSelectionThreshold = %s
                mediumGridSpacing = %s

                largeGridSpacing = %s

                # Select which surfaces receive the helper grid.
                # Legt fest, welche Flächen das Hilfsraster erhalten.
                gridTop = %s
                gridBottom = %s
                gridSides = %s
                """.formatted(
                CONFIG_VERSION,
                config.defaultEnabled(),
                config.allowPlayerToggle(),
                config.renderIntervalTicks(),
                formatDouble(config.renderDistance()),
                config.maxEdgeParticles(),
                config.maxGridParticles(),
                formatDouble(config.minimumEdgeSpacing()),
                config.edgeParticleStyle().configName(),
                config.gridParticleStyle().configName(),
                formatFloat(config.edgeDustScale()),
                formatFloat(config.gridDustScale()),
                formatDouble(config.smallSelectionThreshold()),
                formatDouble(config.smallGridSpacing()),
                formatDouble(config.mediumSelectionThreshold()),
                formatDouble(config.mediumGridSpacing()),
                formatDouble(config.largeGridSpacing()),
                config.gridTop(),
                config.gridBottom(),
                config.gridSides()
        );
    }

    private static String formatDouble(double value) {
        return Double.toString(value);
    }

    private static String formatFloat(float value) {
        return Float.toString(value);
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
            ParticleStyle defaultValue,
            boolean allowLegacyStyles
    ) {
        String value = readString(entries, key, defaultValue.configName());
        return ParticleStyle.parse(value, key, allowLegacyStyles);
    }

    private static boolean isLegacyParticleStyle(String rawValue) {
        String normalized = normalizedStyle(rawValue);
        return normalized.equals("custom_dust") || normalized.equals("dust");
    }

    private static String normalizedStyle(String rawValue) {
        if (rawValue == null) {
            return "";
        }

        String value = rawValue.trim();
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }

        return value.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }

    public enum ParticleStyle {
        FLAME("flame"),
        ORANGE_DUST("orange_dust"),
        RED_DUST("red_dust"),
        END_ROD("end_rod");

        private final String configName;

        ParticleStyle(String configName) {
            this.configName = configName;
        }

        public String configName() {
            return configName;
        }

        public static ParticleStyle parse(String value, String key, boolean allowLegacyStyles) {
            String normalized = value.trim().toLowerCase(Locale.ROOT).replace('-', '_');
            if (allowLegacyStyles && (normalized.equals("custom_dust") || normalized.equals("dust"))) {
                return ORANGE_DUST;
            }

            for (ParticleStyle style : values()) {
                if (style.configName.equals(normalized)) {
                    return style;
                }
            }

            throw new IllegalArgumentException(
                    key + " ungültig / invalid. Erlaubt / allowed: flame, orange_dust, red_dust, end_rod"
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
                    ParticleStyle.FLAME,
                    ParticleStyle.ORANGE_DUST,
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
