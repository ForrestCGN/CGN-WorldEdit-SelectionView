package de.forrestcgn.cgnselectionview.render;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class CuboidParticleRenderer {
    private static final int MAX_PARTICLES_PER_RENDER = 240;
    private static final double MIN_SPACING = 0.75D;
    private static final double MAX_RENDER_DISTANCE = 128.0D;
    private static final double MAX_RENDER_DISTANCE_SQUARED = MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE;

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

        double width = maxX - minX;
        double height = maxY - minY;
        double depth = maxZ - minZ;
        double totalEdgeLength = 4.0D * (width + height + depth);
        double spacing = Math.max(MIN_SPACING, totalEdgeLength / MAX_PARTICLES_PER_RENDER);

        Vec3 playerPosition = player.position();
        int sentParticles = 0;

        for (int[] edge : EDGES) {
            Vec3 start = corners[edge[0]];
            Vec3 end = corners[edge[1]];
            double length = start.distanceTo(end);
            int steps = Math.max(1, (int) Math.ceil(length / spacing));

            for (int step = 0; step <= steps; step++) {
                if (sentParticles >= MAX_PARTICLES_PER_RENDER) {
                    return;
                }

                double progress = step / (double) steps;
                Vec3 point = start.lerp(end, progress);
                if (point.distanceToSqr(playerPosition) > MAX_RENDER_DISTANCE_SQUARED) {
                    continue;
                }

                player.serverLevel().sendParticles(
                        player,
                        ParticleTypes.END_ROD,
                        true,
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
}
