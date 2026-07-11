package de.forrestcgn.cgnselectionview.render;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class CuboidParticleRenderer {
    private static final int MAX_EDGE_PARTICLES_PER_RENDER = 280;
    private static final int MAX_HELPER_PARTICLES_PER_RENDER = 300;
    private static final double MIN_EDGE_SPACING = 0.65D;
    private static final double MIN_HELPER_SPACING = 1.0D;
    private static final double MAX_RENDER_DISTANCE = 256.0D;
    private static final double MAX_RENDER_DISTANCE_SQUARED = MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE;
    private static final double[] HELPER_RATIOS = {0.25D, 0.5D, 0.75D};

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

    public static void render(ServerPlayer player, CuboidRegion region) {
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

        List<LineSegment> edgeLines = createEdgeLines(corners);
        List<LineSegment> helperLines = createHelperLines(minX, minY, minZ, maxX, maxY, maxZ);

        renderLines(
                player,
                edgeLines,
                ParticleTypes.FLAME,
                MAX_EDGE_PARTICLES_PER_RENDER,
                MIN_EDGE_SPACING
        );
        renderLines(
                player,
                helperLines,
                DustParticleOptions.REDSTONE,
                MAX_HELPER_PARTICLES_PER_RENDER,
                MIN_HELPER_SPACING
        );
    }

    private static List<LineSegment> createEdgeLines(Vec3[] corners) {
        List<LineSegment> lines = new ArrayList<>(EDGES.length);
        for (int[] edge : EDGES) {
            lines.add(new LineSegment(corners[edge[0]], corners[edge[1]]));
        }
        return lines;
    }

    private static List<LineSegment> createHelperLines(
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ
    ) {
        List<LineSegment> lines = new ArrayList<>();

        for (double ratio : HELPER_RATIOS) {
            double x = lerp(minX, maxX, ratio);
            double z = lerp(minZ, maxZ, ratio);

            lines.add(new LineSegment(new Vec3(minX, minY, z), new Vec3(maxX, minY, z)));
            lines.add(new LineSegment(new Vec3(minX, maxY, z), new Vec3(maxX, maxY, z)));
            lines.add(new LineSegment(new Vec3(x, minY, minZ), new Vec3(x, minY, maxZ)));
            lines.add(new LineSegment(new Vec3(x, maxY, minZ), new Vec3(x, maxY, maxZ)));

            lines.add(new LineSegment(new Vec3(x, minY, minZ), new Vec3(x, maxY, minZ)));
            lines.add(new LineSegment(new Vec3(x, minY, maxZ), new Vec3(x, maxY, maxZ)));
            lines.add(new LineSegment(new Vec3(minX, minY, z), new Vec3(minX, maxY, z)));
            lines.add(new LineSegment(new Vec3(maxX, minY, z), new Vec3(maxX, maxY, z)));
        }

        double middleY = lerp(minY, maxY, 0.5D);
        lines.add(new LineSegment(new Vec3(minX, middleY, minZ), new Vec3(maxX, middleY, minZ)));
        lines.add(new LineSegment(new Vec3(minX, middleY, maxZ), new Vec3(maxX, middleY, maxZ)));
        lines.add(new LineSegment(new Vec3(minX, middleY, minZ), new Vec3(minX, middleY, maxZ)));
        lines.add(new LineSegment(new Vec3(maxX, middleY, minZ), new Vec3(maxX, middleY, maxZ)));

        return lines;
    }

    private static <T extends ParticleOptions> void renderLines(
            ServerPlayer player,
            List<LineSegment> lines,
            T particle,
            int particleBudget,
            double minimumSpacing
    ) {
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
                if (point.distanceToSqr(playerPosition) > MAX_RENDER_DISTANCE_SQUARED) {
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

    private static double lerp(double start, double end, double ratio) {
        return start + (end - start) * ratio;
    }

    private record LineSegment(Vec3 start, Vec3 end) {
        private double length() {
            return start.distanceTo(end);
        }
    }
}
