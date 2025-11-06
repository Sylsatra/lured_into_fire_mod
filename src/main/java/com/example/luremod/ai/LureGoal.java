package com.example.luremod.ai;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.example.luremod.config.LureConfigHolder;
import com.example.luremod.integration.SoundAttractIntegration;
import com.example.luremod.manager.BloodLureManager;
import com.example.luremod.manager.LureGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

public class LureGoal extends Goal {

    private final Mob mob;
    private final LureGroup lureGroup;
    private Vec3 targetPosition;
    private int scanCooldown;
    private int repathCooldown;

    private static final int REPTH_COOLDOWN_TICKS = 10;

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
        if (this.targetPosition == null) {
            return false;
        }
        return this.mob.position().distanceToSqr(this.targetPosition) >= 4.0;
    }


    @Override
    public void tick() {
        if (this.targetPosition == null) {
            return;
        }

        if (this.repathCooldown > 0) {
            --this.repathCooldown;
        }

        if (this.repathCooldown <= 0) {
            this.repathCooldown = REPTH_COOLDOWN_TICKS;
            PathNavigation navigation = this.mob.getNavigation();
            if (navigation.isStuck() || navigation.isDone() || this.mob.position().distanceToSqr(this.targetPosition) > 4.0) {
                navigation.moveTo(this.targetPosition.x, this.targetPosition.y, this.targetPosition.z, this.lureGroup.lureSpeed());
            }
        }
    }

    @Override
    public void start() {
        this.repathCooldown = 0;
        this.mob.getNavigation().moveTo(this.targetPosition.x, this.targetPosition.y, this.targetPosition.z, this.lureGroup.lureSpeed());
    }

    @Override
    public void stop() {
        this.targetPosition = null;
        this.mob.getNavigation().stop();
        this.repathCooldown = 0;
    }

    private Vec3 findNearestLure() {
        double closestDistSq = Double.MAX_VALUE;
        Vec3 bestTarget = null;
        Level level = this.mob.level();
        int radius = this.lureGroup.searchRadius();
        int vertical = radius / 2;
        BlockPos mobPos = this.mob.blockPosition();


        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -vertical; y <= vertical; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    checkPos.set(mobPos.getX() + x, mobPos.getY() + y, mobPos.getZ() + z);
                    BlockState blockState = level.getBlockState(checkPos);
                    if (!blockState.isAir()) {
                        BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(checkPos) : null;
                        if (this.lureGroup.isLuredBlock(blockState, blockEntity)) {
                            Vec3 blockCenter = Vec3.atCenterOf(checkPos);
                            if (!isTargetVisible(blockCenter, null)) {
                                continue;
                            }
                            Vec3 reachablePos = findReachablePositionNear(checkPos.immutable());
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
            }
        }


        double maxItemFrameRadius = radius * 1.2;
        AABB searchBox = this.mob.getBoundingBox().inflate(maxItemFrameRadius, vertical * 1.2, maxItemFrameRadius);
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, searchBox);

        for (Entity entity : entities) {
            double specificRadiusSq = 0;
            ItemFrame itemFrame = null;

            if (entity instanceof GlowItemFrame) {
                specificRadiusSq = Math.pow(radius * 1.2, 2);
                itemFrame = (GlowItemFrame) entity;
            } else if (entity instanceof ItemFrame) {
                specificRadiusSq = Math.pow(radius * 0.8, 2);
                itemFrame = (ItemFrame) entity;
            }

            if (itemFrame != null) {
                if (this.lureGroup.isLuredItem(itemFrame.getItem())) {
                    if (this.mob.position().distanceToSqr(itemFrame.position()) <= specificRadiusSq) {
                        if (isTargetVisible(itemFrame.position(), itemFrame)) {
                            Vec3 reachablePos = findReachablePositionNear(itemFrame.blockPosition());
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
            }
        }


        AABB playerSearchBox = this.mob.getBoundingBox().inflate(radius, vertical, radius);
        List<Player> players = level.getEntitiesOfClass(Player.class, playerSearchBox);
        for (Player player : players) {
            if (this.lureGroup.isLuredItem(player.getMainHandItem()) || this.lureGroup.isLuredItem(player.getOffhandItem())) {
                if (isTargetVisible(player.getEyePosition(), player)) {
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
        

        if (this.lureGroup.attractedToBlood() && ModList.get().isLoaded("extragore") && LureConfigHolder.ENABLE_EXTRA_GORE_INTEGRATION.get()) {

            double bloodSearchRadius = radius * LureConfigHolder.BLOOD_SEARCH_RADIUS_MULTIPLIER.get();
            Optional<BlockPos> bloodPosOpt = BloodLureManager.findNearestLure(mobPos, bloodSearchRadius);

            if (bloodPosOpt.isPresent()) {
                BlockPos bloodPos = bloodPosOpt.get();
                Vec3 bloodCenter = Vec3.atCenterOf(bloodPos);
                if (this.lureGroup.bloodBypassesLos() || isTargetVisible(bloodCenter, null)) {
                    Vec3 reachablePos = findReachablePositionNear(bloodPos);
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
        Level level = this.mob.level();

        for (int i = 0; i < 10; ++i) {
            BlockPos randomPos = target.offset(
                    this.mob.getRandom().nextInt(5) - 2, 
                    this.mob.getRandom().nextInt(3) - 1, 
                    this.mob.getRandom().nextInt(5) - 2  
            );

            if (level.getBlockState(randomPos).isPathfindable(level, randomPos, PathComputationType.LAND)) {
                Path path = navigation.createPath(randomPos, 0);
                if (path != null && path.canReach()) {
                    return Vec3.atBottomCenterOf(randomPos);
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

    private boolean isTargetVisible(Vec3 target, @Nullable Entity targetEntity) {
        Vec3 fovTarget = target;

        if (targetEntity instanceof LivingEntity livingEntity) {
            if (!SoundAttractIntegration.isEntityInFov(this.mob, livingEntity)) {
                return false;
            }
            fovTarget = livingEntity.getEyePosition();
        } else if (targetEntity != null) {
            fovTarget = targetEntity.position();
        }

        if (!SoundAttractIntegration.isPositionInFov(this.mob, fovTarget)) {
            return false;
        }

        Vec3 eyePos = this.mob.getEyePosition();
        Vec3 losTarget = fovTarget;
        ClipContext context = new ClipContext(eyePos, losTarget, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.mob);
        BlockHitResult hit = this.mob.level().clip(context);

        if (hit.getType() == HitResult.Type.MISS) {
            return true;
        }

        if (targetEntity == null) {
            BlockPos targetBlock = BlockPos.containing(target);
            if (hit.getBlockPos().equals(targetBlock)) {
                return true;
            }
        }

        return false;
    }
}