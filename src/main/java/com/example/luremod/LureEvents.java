package com.example.luremod;

import com.example.luremod.ai.LureGoal;
import com.example.luremod.manager.LureGroup;
import com.example.luremod.manager.LureGroupManager;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = "luremod")
public class LureEvents {
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide()) {
            Optional<LureGroup> groupOpt = LureGroupManager.getGroupForMob(mob);
            groupOpt.ifPresent(group -> 
                mob.goalSelector.addGoal(3, new LureGoal(mob, group))
            );
        }
    }
}