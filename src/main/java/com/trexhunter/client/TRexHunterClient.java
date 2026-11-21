package com.trexhunter.client;

import com.trexhunter.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class TRexHunterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLayerRegistry.registerModelLayer(TRexClientLayers.TREX, TRexModel::createBodyLayer);
        EntityRenderers.register(ModEntities.TREX, TRexRenderer::new);
    }
}
