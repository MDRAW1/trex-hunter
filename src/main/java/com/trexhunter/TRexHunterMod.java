package com.trexhunter;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TRexHunterMod implements ModInitializer {
    public static final String MOD_ID = "trexhunter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        TRexConfig.load();
        ModEntities.register();
        ModItems.register();
        TRexDirector.init();
        LOGGER.info("T-Rex Hunter carregado. Cuidado com a névoa...");
    }
}
