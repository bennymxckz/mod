package com.craftoria.dashboardmod;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CraftoriaDashboard.MODID)
public class CraftoriaDashboard {
    public static final String MODID = "craftoriadashboard";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CraftoriaDashboard() {
        // Start the API Server when the mod loads
        ApiServer.start();
        LOGGER.info("Craftoria Dashboard Mod Loaded and API Started!");
    }
}