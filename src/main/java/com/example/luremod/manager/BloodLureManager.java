package com.example.luremod.manager;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BloodLureManager {

    private static final List<BloodPuddle> ACTIVE_PUDDLES = new ArrayList<>();

    public static void tick() {
        ACTIVE_PUDDLES.removeIf(BloodPuddle::tick);
    }

    public static void addPuddle(BloodPuddle puddle) {
        ACTIVE_PUDDLES.add(puddle);
    }

    public static Optional<BlockPos> findNearestLure(BlockPos searcherPos, double searchRadius) {
        double searchRadiusSq = searchRadius * searchRadius;


        for (int i = ACTIVE_PUDDLES.size() - 1; i >= 0; i--) {
            BloodPuddle puddle = ACTIVE_PUDDLES.get(i);
            Optional<BlockPos> puddleCenterOpt = puddle.getCenter();

            if (puddleCenterOpt.isPresent()) {
                BlockPos puddleCenter = puddleCenterOpt.get();
                if (puddleCenter.distSqr(searcherPos) <= searchRadiusSq) {

                    return Optional.of(puddleCenter);
                }
            }
        }


        return Optional.empty();
    }
}