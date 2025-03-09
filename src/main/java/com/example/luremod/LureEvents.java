package com.example.luremod;

import com.example.luremod.ai.LureGoal;
import com.example.luremod.config.LureConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.core.Registry;

@Mod.EventBusSubscriber(modid = "luremod")
public class LureEvents {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            ResourceLocation entityId = Registry.ENTITY_TYPE.getKey(mob.getType());
            if (entityId == null) return;

            String entityIdStr = entityId.toString();
            if (!LureConfig.LURE_ENTITIES.contains(entityIdStr)) {
                return;
            }

            mob.goalSelector.addGoal(2, new LureGoal(mob, 1.0));
        }
    }
}
