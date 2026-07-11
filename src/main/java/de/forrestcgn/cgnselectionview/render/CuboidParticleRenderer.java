package de.forrestcgn.cgnselectionview.render;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import de.forrestcgn.cgnselectionview.config.SelectionViewConfig;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class CuboidParticleRenderer {
    private static final int MAX_GRID_POSITIONS_PER_AXIS = 64;
    private static final int ORANGE_DUST_COLOR = 0xFF6600;
    private static final int RED_DUST_COLOR = 0xFF0000;

    private static final int[][] EDGES = {
            {0, 1}, {0, 2}, {0, 4},
            {1, 3}, {1, 5},
            {2, 3}, {2, 6},
            {3, 7},
            {4, 5}, {4, 6},
            {5, 7},
            {6, 7}
    };

    private CuboidParticleRenderer() {
    }

    public static void render(
            ServerPlayer player,
            CuboidRegion region,
            SelectionViewConfig.Values config
    ) {
        BlockVector3 minimum = region.getMinimumPoint();
        BlockVector3 maximum = region.getMaximumPoint();

        double minX = minimum.x();
        double minY = minimum.y();
        double minZ = minimum.z();
        double maxX = maximum.x() + 1.0D;
        double maxY = maximum.y() + 1.0D;
        double maxZ = maximum.z() + 1.0D;

        Vec3[] corners = {
                new Vec3(minX, minY, minZ),
                new Vec3(maxX, minY, minZ),
                new Vec3(minX, maxY, minZ),
                new Vec3(maxX, maxY, minZ),
                new Vec3(minX, minY, maxZ),
                new Vec3(maxX, minY, maxZ),
                new Vec3(minX, maxY, maxZ),
                new Vec3(maxX, maxY, maxZ)
        };

        double width = maxX - minX;
        double height = maxY - minY;
        double depth = maxZ - minZ;
        double selectionSize = Math.max(width, Math.max(height, depth));
        double gridSpacing = config.gridSpacingFor(selectionSize);
        double renderDistanceSquared = config.renderDistance() * config.renderDistance();

        List<LineSegment> edgeLines = createEdgeLines(corners);
        List<LineSegment> gridLines = createGridLines(
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ,
                gridSpacing,
                config.gridTop(),
                config.gridBottom(),
                config.gridSides()
        );

        ParticleOptions edgeParticle = createParticle(
                config.edgeParticleStyle(),
                config.edgeDustScale()
        );
        ParticleOptions gridParticle = createParticle(
                config.gridParticleStyle(),
                config.gridDustScale()
        );

        renderLines(
                player,
                edgeLines,
                edgeParticle,
                config.maxEdgeParticles(),
                config.minimumEdgeSpacing(),
                renderDistanceSquared
        );
        renderLines(
                player,
                gridLines,
                gridParticle,
                config.maxGridParticles(),
                gridSpacing,
                renderDistanceSquared
        );
    }

    private static ParticleOptions createParticle(
            SelectionViewConfig.ParticleStyle style,
            float dustScale
    ) {
        return switch (style) {
            case FLAME -> ParticleTypes.FLAME;
            case ORANGE_DUST -> new DustParticleOptions(ORANGE_DUST_COLOR, dustScale);
            case RED_DUST -> new DustParticleOptions(RED_DUST_COLOR, dustScale);
            case END_ROD -> ParticleTypes.END_ROD;
        };
    }

    private static List<LineSegment> createEdgeLines(Vec3[] corners) {
        List<LineSegment> lines = new ArrayList<>(EDGES.length);
        for (int[] edge : EDGES) {
            lines.add(new LineSegment(corners[edge[0]], corners[edge[1]]));
        }
        return lines;
    }

    private static List<LineSegment> createGridLines(
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ,
            double requestedSpacing,
            boolean gridTop,
            boolean gridBottom,
            boolean gridSides
    ) {
        List<LineSegment> lines = new ArrayList<>();
        List<Double> xPositions = createInteriorPositions(minX, maxX, requestedSpacing);
        List<Double> yPositions = createInteriorPositions(minY, maxY, requestedSpacing);
        List<Double> zPositions = createInteriorPositions(minZ, maxZ, requestedSpacing);

        if (gridBottom) {
            addHorizontalGrid(lines, minY, minX, minZ, maxX, maxZ, xPositions, zPositions);
        }
        if (gridTop) {
            addHorizontalGrid(lines, maxY, minX, minZ, maxX, maxZ, xPositions, zPositions);
        }
        if (gridSides) {
            addSideGrids(
                    lines,
                    minX,
                    minY,
                    minZ,
                    maxX,
                    maxY,
                    maxZ,
                    xPositions,
                    yPositions,
                    zPositions
            );
        }

        return lines;
    }

    private static void addHorizontalGrid(
            List<LineSegment> lines,
            double y,
            double minX,
            double minZ,
            double maxX,
            double maxZ,
            List<Double> xPositions,
            List<Double> zPositions
    ) {
        for (double z : zPositions) {
            lines.add(new LineSegment(new Vec3(minX, y, z), new Vec3(maxX, y, z)));
        }
        for (double x : xPositions) {
            lines.add(new LineSegment(new Vec3(x, y, minZ), new Vec3(x, y, maxZ)));
        }
    }

    private static void addSideGrids(
            List<LineSegment> lines,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ,
            List<Double> xPositions,
            List<Double> yPositions,
            List<Double> zPositions
    ) {
        for (double x : xPositions) {
            lines.add(new LineSegment(new Vec3(x, minY, minZ), new Vec3(x, maxY, minZ)));
            lines.add(new LineSegment(new Vec3(x, minY, maxZ), new Vec3(x, maxY, maxZ)));
        }
        for (double z : zPositions) {
            lines.add(new LineSegment(new Vec3(minX, minY, z), new Vec3(minX, maxY, z)));
            lines.add(new LineSegment(new Vec3(maxX, minY, z), new Vec3(maxX, maxY, z)));
        }
        for (double y : yPositions) {
            lines.add(new LineSegment(new Vec3(minX, y, minZ), new Vec3(maxX, y, minZ)));
            lines.add(new LineSegment(new Vec3(minX, y, maxZ), new Vec3(maxX, y, maxZ)));
            lines.add(new LineSegment(new Vec3(minX, y, minZ), new Vec3(minX, y, maxZ)));
            lines.add(new LineSegment(new Vec3(maxX, y, minZ), new Vec3(maxX, y, maxZ)));
        }
    }

    private static List<Double> createInteriorPositions(double minimum, double maximum, double requestedSpacing) {
        double length = maximum - minimum;
        if (length <= requestedSpacing) {
            return List.of();
        }

        double safeSpacing = Math.max(
                requestedSpacing,
                length / (MAX_GRID_POSITIONS_PER_AXIS + 1.0D)
        );
        List<Double> positions = new ArrayList<>();

        for (double position = minimum + safeSpacing;
             position < maximum - 0.0001D && positions.size() < MAX_GRID_POSITIONS_PER_AXIS;
             position += safeSpacing) {
            positions.add(position);
        }

        return positions;
    }

    private static void renderLines(
            ServerPlayer player,
            List<LineSegment> lines,
            ParticleOptions particle,
            int particleBudget,
            double minimumSpacing,
            double renderDistanceSquared
    ) {
        if (particleBudget <= 0 || lines.isEmpty()) {
            return;
        }

        double totalLength = 0.0D;
        for (LineSegment line : lines) {
            totalLength += line.length();
        }

        double spacing = Math.max(minimumSpacing, totalLength / particleBudget);
        Vec3 playerPosition = player.position();
        int sentParticles = 0;

        for (LineSegment line : lines) {
            double length = line.length();
            int steps = Math.max(1, (int) Math.ceil(length / spacing));

            for (int step = 0; step <= steps; step++) {
                if (sentParticles >= particleBudget) {
                    return;
                }

                double progress = step / (double) steps;
                Vec3 point = line.start().lerp(line.end(), progress);
                if (point.distanceToSqr(playerPosition) > renderDistanceSquared) {
                    continue;
                }

                player.level().sendParticles(
                        player,
                        particle,
                        false,
                        false,
                        point.x,
                        point.y,
                        point.z,
                        1,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D
                );
                sentParticles++;
            }
        }
    }

    private record LineSegment(Vec3 start, Vec3 end) {
        private double length() {
            return start.distanceTo(end);
        }
    }
}
