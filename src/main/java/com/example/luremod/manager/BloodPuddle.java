package com.example.luremod.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;

public class BloodPuddle {
    private final List<BlockPos> particlePositions = new ArrayList<>();
    private int remainingTicks;
    private BlockPos center;

    public BloodPuddle(int lifetime) {
        this.remainingTicks = lifetime;
    }

    public void addParticle(BlockPos pos) {
        this.particlePositions.add(pos);
        this.center = null;
    }

    public boolean tick() {
        this.remainingTicks--;
        return this.remainingTicks <= 0;
    }


    public Optional<BlockPos> getCenter() {
        if (this.particlePositions.isEmpty()) {
            return Optional.empty();
        }

        if (this.center != null) {
            return Optional.of(this.center);
        }

        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;

        for (BlockPos pos : this.particlePositions) {
            sumX += pos.getX();
            sumY += pos.getY();
            sumZ += pos.getZ();
        }

        int count = this.particlePositions.size();
        this.center = new BlockPos((int)(sumX / count), (int)(sumY / count), (int)(sumZ / count));
        return Optional.of(this.center);
    }


    @Deprecated
    public Optional<BlockPos> getNearestParticle(BlockPos searcherPos, double searchRadiusSq) {
        BlockPos bestTarget = null;
        double closestDistSq = searchRadiusSq;

        for (BlockPos pos : this.particlePositions) {
            double distSq = pos.distSqr(searcherPos);
            if (distSq < closestDistSq) {
                closestDistSq = distSq;
                bestTarget = pos;
            }
        }
        return Optional.ofNullable(bestTarget);
    }
}