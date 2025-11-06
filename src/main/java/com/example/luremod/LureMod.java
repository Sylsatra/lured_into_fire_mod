package com.example.luremod;

import com.example.luremod.compat.ExtraGoreCompat;
import com.example.luremod.config.LureConfigHolder;
import com.example.luremod.manager.LureGroupManager;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("luremod")
public class LureMod {
    public LureMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LureConfigHolder.SPEC, "luremod-common.toml");
    }
    

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModList.get().isLoaded("extragore")) {
                ExtraGoreCompat.initialize();
            }
        });
    }

    public void onConfigLoad(final ModConfigEvent event) {

        if (event.getConfig().getSpec() == LureConfigHolder.SPEC) {
            LureConfigHolder.setDefaults();
        }
        LureGroupManager.reload();
    }
}