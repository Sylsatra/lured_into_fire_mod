package com.example.luremod;

import com.example.luremod.config.LureConfig;
import net.minecraftforge.fml.common.Mod;

@Mod("luremod")
public class LureMod {
    public LureMod() {
        LureConfig.loadConfig();
    }
}
