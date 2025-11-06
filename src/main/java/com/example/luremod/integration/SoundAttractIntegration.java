package com.example.luremod.integration;

import java.lang.reflect.Method;
import java.util.Optional;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

public final class SoundAttractIntegration {

    private static final String MOD_ID = "soundattract";
    private static final double DEFAULT_HORIZONTAL_FOV = 120.0;
    private static final double DEFAULT_VERTICAL_FOV = 90.0;

    private static boolean checked;
    private static boolean available;
    private static Method isTargetInFov;

    private SoundAttractIntegration() {
    }

    public static boolean isModPresent() {
        if (!checked) {
            available = ModList.get().isLoaded(MOD_ID);
            checked = true;
        }
        return available;
    }

    private static Optional<Method> resolveIsTargetInFov() {
        if (isTargetInFov != null) {
            return Optional.of(isTargetInFov);
        }
        if (!isModPresent()) {
            return Optional.empty();
        }
        try {
            Class<?> clazz = Class.forName("com.example.soundattract.FovEvents");
            Method method = clazz.getMethod("isTargetInFov", Mob.class, Entity.class, boolean.class);
            method.setAccessible(true);
            isTargetInFov = method;
            return Optional.of(method);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            available = false;
            return Optional.empty();
        }
    }

    public static boolean isEntityInFov(Mob looker, Entity target) {
        Optional<Method> methodOpt = resolveIsTargetInFov();
        if (methodOpt.isPresent()) {
            try {
                return (boolean) methodOpt.get().invoke(null, looker, target, false);
            } catch (ReflectiveOperationException e) {
                available = false;
            }
        }
        return defaultFovCheck(looker, target.position());
    }

    public static boolean isPositionInFov(Mob looker, Vec3 targetPos) {
        Optional<Method> methodOpt = resolveIsTargetInFov();
        if (methodOpt.isPresent()) {
            Level level = looker.level();
            Entity probe = EntityType.ARMOR_STAND.create(level);
            if (probe != null) {
                probe.setPos(targetPos.x, targetPos.y, targetPos.z);
                boolean result = false;
                try {
                    result = (boolean) methodOpt.get().invoke(null, looker, probe, false);
                } catch (ReflectiveOperationException e) {
                    available = false;
                }
                probe.discard();
                if (available) {
                    return result;
                }
            }
        }
        return defaultFovCheck(looker, targetPos);
    }

    private static boolean defaultFovCheck(Mob looker, Vec3 targetPos) {
        Vec3 eyePos = looker.getEyePosition();
        Vec3 toTarget = targetPos.subtract(eyePos);
        double distanceSq = toTarget.lengthSqr();
        if (distanceSq < 1.0e-6) {
            return true;
        }
        Vec3 lookNorm = looker.getLookAngle().normalize();
        Vec3 targetNorm = toTarget.normalize();

        double horizontalAngle = computeHorizontalAngle(lookNorm, targetNorm);
        if (horizontalAngle > DEFAULT_HORIZONTAL_FOV / 2.0) {
            return false;
        }

        double verticalAngle = computeVerticalAngle(lookNorm, targetNorm);
        return verticalAngle <= DEFAULT_VERTICAL_FOV / 2.0;
    }

    private static double computeHorizontalAngle(Vec3 lookNorm, Vec3 targetNorm) {
        Vec3 lookHoriz = new Vec3(lookNorm.x, 0.0, lookNorm.z);
        Vec3 targetHoriz = new Vec3(targetNorm.x, 0.0, targetNorm.z);
        double lookLen = lookHoriz.length();
        double targetLen = targetHoriz.length();
        if (lookLen < 1.0e-6 || targetLen < 1.0e-6) {
            return 0.0;
        }
        double dot = lookHoriz.dot(targetHoriz) / (lookLen * targetLen);
        dot = Mth.clamp(dot, -1.0, 1.0);
        return Math.toDegrees(Math.acos(dot));
    }

    private static double computeVerticalAngle(Vec3 lookNorm, Vec3 targetNorm) {
        double lookPitch = Math.toDegrees(Math.asin(Mth.clamp(lookNorm.y, -1.0, 1.0)));
        double targetPitch = Math.toDegrees(Math.asin(Mth.clamp(targetNorm.y, -1.0, 1.0)));
        return Math.abs(lookPitch - targetPitch);
    }
}
