package com.example.luremod.compat;

import com.eruannie_9.extragore.ModConfiguration;
import com.eruannie_9.extragore.ParticleConfiguration;
import com.eruannie_9.extragore.events.EntityHurtEvent;
import com.eruannie_9.extragore.packet.BloodDataPacket;
import com.eruannie_9.extragore.packet.PacketHandler;
import com.eruannie_9.extragore.util.EntityConfig;
import com.eruannie_9.extragore.util.EntityUtil;
import com.eruannie_9.extragore.util.ParticleVariant;
import com.example.luremod.config.LureConfigHolder;
import com.example.luremod.manager.BloodLureManager;
import com.example.luremod.manager.BloodPuddle;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ExtraGoreCompat {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int PUDDLE_COOLDOWN_TICKS = 10;
    private static final Map<UUID, Long> entityCooldowns = new ConcurrentHashMap<>();


    public static void initialize() {
        LOGGER.info("Attempting to initialize Extra Gore compatibility...");
        try {
            MinecraftForge.EVENT_BUS.unregister(com.eruannie_9.extragore.events.EntityHurtEvent.class);
            MinecraftForge.EVENT_BUS.register(new ExtraGoreCompat());
            LOGGER.info("Successfully hijacked Extra Gore event handler to add blood lure feature.");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Extra Gore compatibility. Mobs will not be attracted to blood.", e);
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide() || entity.isDeadOrDying() || (entity.isInvisible() && !entityTakesWaterDamage(entity))) {
            return;
        }

        List<? extends String> blacklistedEntities = ModConfiguration.BLACKLISTED_ENTITY_IDS.get();
        if (blacklistedEntities.contains(EntityType.getKey(entity.getType()).toString())) {
            return;
        }


        long currentTime = entity.level().getGameTime();
        Long lastPuddleTime = entityCooldowns.get(entity.getUUID());

        if (lastPuddleTime != null && (currentTime - lastPuddleTime) < PUDDLE_COOLDOWN_TICKS) {
            return;
        }

        entityCooldowns.put(entity.getUUID(), currentTime);
        
        BloodPuddle currentPuddle = new BloodPuddle(LureConfigHolder.BLOOD_LURE_LIFETIME_TICKS.get());
        boolean isLureEnabled = LureConfigHolder.ENABLE_EXTRA_GORE_INTEGRATION.get();

        float damage = event.getAmount();
        ServerLevel serverLevel = (ServerLevel) entity.level();
        Color color = ParticleConfiguration.getColorForEntity(entity);
        EntityUtil.CollisionMode collisionMode = ParticleConfiguration.getControlModeForEntity(entity);
        EntityUtil.WeightMode weightMode = ParticleConfiguration.getWeightModeForEntity(entity);
        EntityUtil.StateMode stateMode = ParticleConfiguration.getStateModeForEntity(entity);
        int particleVariant = "V1".equals(ParticleVariant.getParticleVariant()) ? 1 : 2;
        int rgbColor = color.getRGB();

        EntityUtil.SizeInfo sizeInfo = EntityUtil.getSizeInfo(entity);
        EntityConfig config = EntityHurtEvent.getDynamicEntityConfig(sizeInfo, entity, damage);

        double entityX = entity.getX();
        double entityY = entity.getY();
        double entityZ = entity.getZ();
        double entityWidth = entity.getBbWidth();
        double entityHeight = entity.getBbHeight() * 0.8;

        for (int i = 0; i < config.maxCount; ++i) {
            double speedFactor = 0.1 + Math.random() * 0.12;
            double dx = entity.getRandom().nextGaussian() * config.spread * speedFactor;
            double dy = entity.getRandom().nextGaussian() * config.spread * speedFactor + config.ySpeed;
            double dz = entity.getRandom().nextGaussian() * config.spread * speedFactor;
            EntityUtil.CollisionMode finalCollisionMode = collisionMode;
            if (collisionMode == EntityUtil.CollisionMode.MAGIC && Math.random() < 0.3) {
                finalCollisionMode = EntityUtil.CollisionMode.NONE;
            }

            double offsetX = (Math.random() - 0.5) * entityWidth;
            double offsetY = Math.random() * entityHeight;
            double offsetZ = (Math.random() - 0.5) * entityWidth;
            double particleX = entityX + offsetX;
            double particleY = entityY + offsetY;
            double particleZ = entityZ + offsetZ;

            BlockPos pos = BlockPos.containing(particleX, particleY, particleZ);
            

            if (isLureEnabled) {
                currentPuddle.addParticle(pos);
            }


            float finalScale = finalCollisionMode == EntityUtil.CollisionMode.MAGIC ? config.maxScale * 0.7F : config.maxScale;
            BloodDataPacket packet = new BloodDataPacket(particleX, particleY, particleZ, dx, dy, dz, rgbColor, finalScale, particleVariant, finalCollisionMode, weightMode, stateMode);
            PacketHandler.sendToNear(serverLevel, pos, packet);
        }
        

        if (isLureEnabled) {
            BloodLureManager.addPuddle(currentPuddle);
        }
    }

    private boolean entityTakesWaterDamage(LivingEntity entity) {
        return entity instanceof EnderMan || entity instanceof Blaze;
    }
}