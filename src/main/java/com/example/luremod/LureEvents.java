package com.example.luremod;

import com.example.luremod.ai.LureGoal;
import com.example.luremod.config.LureConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "luremod")
public class LureEvents {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            ResourceLocation id = mob.getType().getRegistryName();
            if (id == null) return;

            String entityIdStr = id.toString();
            if (!LureConfig.LURE_ENTITIES.contains(entityIdStr)) {
                return;
            }

            mob.goalSelector.addGoal(2, new LureGoal(mob, 1.0));
        }
    }
}
