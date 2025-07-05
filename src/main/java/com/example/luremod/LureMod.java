package com.example.luremod;

import com.example.luremod.config.LureConfigHolder; 
import com.example.luremod.manager.LureGroupManager; 
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("luremod") 
public class LureMod {
    public LureMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoad);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LureConfigHolder.SPEC, "luremod-common.toml"); 
    }
    
    public void onConfigLoad(final ModConfigEvent event) {
        LureConfigHolder.setDefaults();
        
        LureGroupManager.reload(); 
    }
}