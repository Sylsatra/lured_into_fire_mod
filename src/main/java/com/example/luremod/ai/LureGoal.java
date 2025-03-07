package com.example.luremod.ai;

import com.example.luremod.config.LureConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;


public class LureGoal extends Goal {
    private final Mob mob;
    private final double speedMod;

    private double stopDistanceSq = LureConfig.STOP_DISTANCE * LureConfig.STOP_DISTANCE;

    private Vec3 lurePos;
    private boolean lureIsPlayer;

    public LureGoal(Mob mob, double speedMod) {
        this.mob = mob;
        this.speedMod = speedMod;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        lurePos = findNearestLure();
        return lurePos != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (lurePos == null) return false;
        double distSqr = mob.blockPosition().distToCenterSqr(lurePos);
        if (distSqr <= stopDistanceSq) {
            return false;
        }
        return checkLureStillValid();
    }

    @Override
    public void stop() {
        lurePos = null;
        lureIsPlayer = false;
    }

    @Override
    public void tick() {
        if (lurePos != null) {
            mob.getNavigation().moveTo(lurePos.x, lurePos.y, lurePos.z, speedMod);
        }
    }

    private Vec3 findNearestLure() {
        Level level = mob.level;
        BlockPos mobPos = mob.blockPosition();
        double closestDistSq = Double.MAX_VALUE;
        Vec3 closestVec = null;
        boolean foundPlayer = false;

        for (int x = -LureConfig.SEARCH_RADIUS; x <= LureConfig.SEARCH_RADIUS; x++) {
            for (int y = -LureConfig.VERTICAL_RANGE; y <= LureConfig.VERTICAL_RANGE; y++) {
                for (int z = -LureConfig.SEARCH_RADIUS; z <= LureConfig.SEARCH_RADIUS; z++) {
                    BlockPos checkPos = mobPos.offset(x, y, z);
                    if (isLureBlock(level, checkPos)) {
                        double dist = mobPos.distSqr(checkPos);
                        if (dist < closestDistSq) {
                            closestDistSq = dist;
                            closestVec = Vec3.atCenterOf(checkPos);
                            foundPlayer = false;
                        }
                    }
                }
            }
        }

        Vec3 mobCenter = Vec3.atCenterOf(mobPos);
        AABB box = new AABB(
            mobCenter.x - LureConfig.SEARCH_RADIUS, mobCenter.y - LureConfig.VERTICAL_RANGE, mobCenter.z - LureConfig.SEARCH_RADIUS,
            mobCenter.x + LureConfig.SEARCH_RADIUS, mobCenter.y + LureConfig.VERTICAL_RANGE, mobCenter.z + LureConfig.SEARCH_RADIUS
        );
        List<Player> players = level.getEntitiesOfClass(Player.class, box);
        for (Player player : players) {
            if (isHoldingLureItem(player)) {
                double dist = mob.distanceToSqr(player);
                if (dist < closestDistSq) {
                    closestDistSq = dist;
                    closestVec = player.position();
                    foundPlayer = true;
                }
            }
        }

        lureIsPlayer = foundPlayer;
        return closestVec;
    }

    private boolean checkLureStillValid() {
        if (lurePos == null) return false;

        if (lureIsPlayer) {

            double range = 2.0; 
            AABB box = new AABB(
                lurePos.x - range, lurePos.y - 2, lurePos.z - range,
                lurePos.x + range, lurePos.y + 2, lurePos.z + range
            );
            List<Player> nearPlayers = mob.level.getEntitiesOfClass(Player.class, box);
            if (nearPlayers.isEmpty()) return false;
            return isHoldingLureItem(nearPlayers.get(0));
        }
        else {
            BlockPos blockPos = new BlockPos(lurePos.x, lurePos.y, lurePos.z);
            return isLureBlock(mob.level, blockPos);
        }
    }

    private boolean isLureBlock(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        return LureConfig.BLOCKS_TO_LURE.contains(block);
    }

    private boolean isHoldingLureItem(Player player) {
        var main = player.getMainHandItem().getItem();
        if (LureConfig.ITEMS_TO_LURE.contains(main)) return true;
        var off = player.getOffhandItem().getItem();
        return LureConfig.ITEMS_TO_LURE.contains(off);
    }
}
