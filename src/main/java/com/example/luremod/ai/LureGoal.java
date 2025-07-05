package com.example.luremod.ai;

import com.example.luremod.config.LureConfigHolder; 
import com.example.luremod.manager.LureGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class LureGoal extends Goal {
    private final Mob mob;
    private final LureGroup lureGroup;
    private Vec3 targetPosition;
    private int scanCooldown;

    public LureGoal(Mob mob, LureGroup lureGroup) {
        this.mob = mob;
        this.lureGroup = lureGroup;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.scanCooldown > 0) {
            --this.scanCooldown;
            return false;
        }
        this.scanCooldown = LureConfigHolder.SCAN_COOLDOWN_TICKS.get();

        if (!isPlayerNearbyForActivation()) {
            return false;
        }

        this.targetPosition = this.findNearestLure();
        return this.targetPosition != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.getNavigation().isDone()) {
            return false;
        }
        if (this.targetPosition != null && this.mob.position().distanceToSqr(this.targetPosition) < 4.0) { // 2-block radius
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.targetPosition.x, this.targetPosition.y, this.targetPosition.z, this.lureGroup.lureSpeed());
    }

    @Override
    public void stop() {
        this.targetPosition = null;
        this.mob.getNavigation().stop();
    }

    private Vec3 findNearestLure() {
        double closestDistSq = Double.MAX_VALUE;
        Vec3 bestTarget = null;
        Level level = this.mob.level();
        
        int radius = this.lureGroup.searchRadius();
        int vertical = radius / 2;

        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        BlockPos mobPos = this.mob.blockPosition();
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -vertical; y <= vertical; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    checkPos.set(mobPos.getX() + x, mobPos.getY() + y, mobPos.getZ() + z);
                    BlockState blockState = level.getBlockState(checkPos);
                    if (!blockState.isAir()) {
                        BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(checkPos) : null;
                        if (this.lureGroup.isLuredBlock(blockState, blockEntity)) {
                            Vec3 reachablePos = findReachablePositionNear(checkPos);
                            if (reachablePos != null && hasLineOfSight(reachablePos)) {
                                double distSq = this.mob.position().distanceToSqr(reachablePos);
                                if (distSq < closestDistSq) {
                                    closestDistSq = distSq;
                                    bestTarget = reachablePos;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        AABB playerSearchBox = this.mob.getBoundingBox().inflate(radius, vertical, radius);
        List<Player> players = level.getEntitiesOfClass(Player.class, playerSearchBox);
        for (Player player : players) {
            if (this.lureGroup.isLuredItem(player.getMainHandItem()) || this.lureGroup.isLuredItem(player.getOffhandItem())) {
                if (hasLineOfSight(player.position())) {
                    Vec3 reachablePos = findReachablePositionNear(player.blockPosition());
                    if (reachablePos != null) {
                        double distSq = this.mob.position().distanceToSqr(reachablePos);
                        if (distSq < closestDistSq) {
                            closestDistSq = distSq;
                            bestTarget = reachablePos;
                        }
                    }
                }
            }
        }
        
        return bestTarget;
    }

    private Vec3 findReachablePositionNear(BlockPos target) {
        PathNavigation navigation = this.mob.getNavigation();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = target.relative(direction);
            Level level = this.mob.level();
            
            if (level.getBlockState(adjacentPos).isPathfindable(level, adjacentPos, PathComputationType.LAND) &&
                level.getBlockState(adjacentPos.above()).isPathfindable(level, adjacentPos.above(), PathComputationType.LAND)) {
                
                Path path = navigation.createPath(adjacentPos, 0);
                if (path != null && path.canReach()) {
                    return Vec3.atBottomCenterOf(adjacentPos);
                }
            }
        }
        return null;
    }
    
    private boolean isPlayerNearbyForActivation() {
        double activationRadius = LureConfigHolder.BLOCK_CHECK_PLAYER_RADIUS.get();
        AABB checkArea = this.mob.getBoundingBox().inflate(activationRadius);
        return !this.mob.level().getEntitiesOfClass(Player.class, checkArea).isEmpty();
    }

    private boolean hasLineOfSight(Vec3 target) {
        Vec3 eyePos = this.mob.getEyePosition();
        return this.mob.level().clip(new ClipContext(eyePos, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.mob)).getType() == BlockHitResult.Type.MISS;
    }
}