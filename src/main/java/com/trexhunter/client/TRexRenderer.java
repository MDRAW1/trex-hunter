package com.trexhunter.client;

import com.trexhunter.TRexHunterMod;
import com.trexhunter.entity.TRexEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class TRexRenderer extends MobRenderer<TRexEntity, TRexRenderState, TRexModel> {
    private static final Identifier TEXTURE = TRexHunterMod.id("textures/entity/trex.png");

    public TRexRenderer(EntityRendererProvider.Context context) {
        super(context, new TRexModel(context.bakeLayer(TRexClientLayers.TREX)), 2.2F);
    }

    @Override
    public TRexRenderState createRenderState() {
        return new TRexRenderState();
    }

    @Override
    public void extractRenderState(TRexEntity entity, TRexRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.roaring = entity.isRoaring();
    }

    @Override
    public Identifier getTextureLocation(TRexRenderState state) {
        return TEXTURE;
    }
}
