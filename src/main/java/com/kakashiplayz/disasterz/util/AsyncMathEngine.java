package com.kakashiplayz.disasterz.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * High-performance Asynchronous Math Engine.
 * Offloads heavy mathematical matrices, parametric double-helix calculations,
 * and projectile trajectories off the main Minecraft server tick thread.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public final class AsyncMathEngine {

    private AsyncMathEngine() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Represents a 3D point offset with optional particle color/density information.
     */
    public record VectorPoint(double x, double y, double z) {
        public Location toLocation(@NotNull Location center) {
            return center.clone().add(x, y, z);
        }

        public Vector toVector() {
            return new Vector(x, y, z);
        }
    }

    /**
     * Asynchronously calculates parametric double-helix spiral coordinates for a Tornado funnel.
     *
     * @param height         Total height of the tornado in blocks.
     * @param baseRadius     Base bottom radius.
     * @param topRadius      Top expanding radius.
     * @param angleOffset    Rotational angle phase (radians).
     * @param pointsPerLayer Number of points calculated per vertical slice.
     * @return CompletableFuture containing list of relative VectorPoints.
     */
    @NotNull
    public static CompletableFuture<List<VectorPoint>> calculateTornadoFunnelAsync(
            double height, double baseRadius, double topRadius, double angleOffset, int pointsPerLayer) {
        return CompletableFuture.supplyAsync(() -> {
            List<VectorPoint> points = new ArrayList<>();
            double heightStep = 0.8;

            for (double y = 0; y < height; y += heightStep) {
                double progress = y / height;
                // Conical expansion: radius expands from baseRadius to topRadius
                double radius = baseRadius + (topRadius - baseRadius) * Math.pow(progress, 1.2);

                // Double helix ribbons (primary and secondary arm)
                double theta1 = angleOffset + (y * 0.6);
                double theta2 = theta1 + Math.PI;

                double x1 = radius * Math.cos(theta1);
                double z1 = radius * Math.sin(theta1);
                points.add(new VectorPoint(x1, y, z1));

                double x2 = radius * Math.cos(theta2);
                double z2 = radius * Math.sin(theta2);
                points.add(new VectorPoint(x2, y, z2));

                // Add swirling internal vortex debris points
                for (int p = 0; p < pointsPerLayer; p++) {
                    double randomAngle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
                    double internalRadius = ThreadLocalRandom.current().nextDouble(0.2, radius);
                    double randY = y + ThreadLocalRandom.current().nextDouble(-0.3, 0.3);
                    points.add(new VectorPoint(
                            internalRadius * Math.cos(randomAngle),
                            randY,
                            internalRadius * Math.sin(randomAngle)
                    ));
                }
            }
            return points;
        });
    }

    /**
     * Calculates suction impulse vector pulling an entity towards the tornado eye and upward.
     *
     * @param entityLoc    Current entity location.
     * @param tornadoLoc   Current center of the tornado base.
     * @param vortexRadius Pull threshold radius.
     * @return 3D Velocity impulse vector.
     */
    @NotNull
    public static Vector calculateVortexSuction(@NotNull Location entityLoc, @NotNull Location tornadoLoc, double vortexRadius) {
        Vector toCenter = tornadoLoc.toVector().subtract(entityLoc.toVector());
        double horizontalDist = Math.sqrt(toCenter.getX() * toCenter.getX() + toCenter.getZ() * toCenter.getZ());

        if (horizontalDist > vortexRadius || horizontalDist < 0.1) {
            return new Vector(0, 0, 0);
        }

        // Suction strength increases closer to the eye
        double pullStrength = 1.0 - (horizontalDist / vortexRadius);
        toCenter.normalize().multiply(pullStrength * 0.65);

        // Tangential velocity creates rotational spin around the eye
        Vector tangential = new Vector(-toCenter.getZ(), 0, toCenter.getX()).normalize().multiply(0.45);

        // Vertical updraft lift
        double upwardLift = Math.min(0.65, 0.15 + (pullStrength * 0.4));

        return toCenter.add(tangential).setY(upwardLift);
    }

    /**
     * Asynchronously calculates a tsunami wave front particle wall.
     *
     * @param waveWidth  Width of the wave in blocks.
     * @param waveHeight Crest height of the wave in blocks.
     * @param direction  Normalized forward movement vector.
     * @param waveOffset Current wave advancement distance.
     * @return CompletableFuture containing relative points.
     */
    @NotNull
    public static CompletableFuture<List<VectorPoint>> calculateTsunamiWaveWallAsync(
            double waveWidth, double waveHeight, @NotNull Vector direction, double waveOffset) {
        return CompletableFuture.supplyAsync(() -> {
            List<VectorPoint> points = new ArrayList<>();
            Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

            double halfWidth = waveWidth / 2.0;
            double step = 1.0;

            for (double w = -halfWidth; w <= halfWidth; w += step) {
                // Sinusoidal crest variation across wave span
                double crestVariation = Math.sin(w * 0.15 + waveOffset * 0.5) * 1.2;
                double effectiveHeight = Math.max(2.0, waveHeight + crestVariation);

                for (double y = 0; y <= effectiveHeight; y += 0.8) {
                    double curlForward = (y / effectiveHeight) * 2.0; // Wave curls forward near crest
                    Vector pointVec = direction.clone().multiply(curlForward)
                            .add(perpendicular.clone().multiply(w))
                            .setY(y);

                    points.add(new VectorPoint(pointVec.getX(), pointVec.getY(), pointVec.getZ()));
                }
            }
            return points;
        });
    }

    /**
     * Asynchronously generates jagged earthquake fissure branch offsets.
     */
    @NotNull
    public static CompletableFuture<List<VectorPoint>> calculateFissurePathAsync(int length, double angle) {
        return CompletableFuture.supplyAsync(() -> {
            List<VectorPoint> fissurePoints = new ArrayList<>();
            double currentX = 0;
            double currentZ = 0;
            double baseCos = Math.cos(angle);
            double baseSin = Math.sin(angle);

            for (int i = 0; i < length; i++) {
                // Jitter angle to create jagged fracture lines
                double jitter = ThreadLocalRandom.current().nextDouble(-0.35, 0.35);
                double stepX = baseCos + jitter;
                double stepZ = baseSin + jitter;

                currentX += stepX;
                currentZ += stepZ;
                fissurePoints.add(new VectorPoint(currentX, 0, currentZ));

                // Occasional secondary branch
                if (ThreadLocalRandom.current().nextDouble() < 0.25) {
                    double branchX = currentX + ThreadLocalRandom.current().nextDouble(-2, 2);
                    double branchZ = currentZ + ThreadLocalRandom.current().nextDouble(-2, 2);
                    fissurePoints.add(new VectorPoint(branchX, 0, branchZ));
                }
            }
            return fissurePoints;
        });
    }
}

