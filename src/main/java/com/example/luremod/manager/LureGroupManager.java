package com.example.luremod.manager;

import com.electronwill.nightconfig.core.Config;
import com.example.luremod.config.LureConfigHolder;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = "luremod")
public class LureGroupManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<LureGroup> LURE_GROUPS = new ArrayList<>();

    public static void reload() {
        LURE_GROUPS.clear();
        LureConfigHolder.SPEC.isLoaded(); 
        LOGGER.info("Loading Lure Groups from config...");
        List<? extends Config> groupConfigs = LureConfigHolder.LURE_GROUPS.get();
        for (Config groupConfig : groupConfigs) {
            try {
                LURE_GROUPS.add(LureGroup.fromConfig(groupConfig));
            } catch (Exception e) {
                String id = groupConfig.getOptional("group_id").map(String::valueOf).orElse("UNKNOWN");
                LOGGER.error("Failed to parse fear group '{}'. Reason: {}", id, e.getMessage());
            }
        }
        LOGGER.info("Loaded {} Lure Groups.", LURE_GROUPS.size());
    }

    public static Optional<LureGroup> getGroupForMob(Mob mob) {
        LureGroup bestMatch = null;
        int bestScore = -1;
        for (LureGroup group : LURE_GROUPS) {
            int currentScore = group.getMatchScore(mob);
            if (currentScore > bestScore) {
                bestScore = currentScore;
                bestMatch = group;
            }
        }
        return Optional.ofNullable(bestMatch);
    }
}