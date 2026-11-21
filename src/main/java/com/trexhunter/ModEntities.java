package com.trexhunter;

import com.trexhunter.entity.TRexEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static EntityType<TRexEntity> TREX;

    private ModEntities() {}

    public static void register() {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, TRexHunterMod.id("trex"));
        TREX = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                EntityType.Builder.of(TRexEntity::new, MobCategory.MONSTER)
                        .sized(2.8F, 4.2F)
                        .eyeHeight(3.7F)
                        .clientTrackingRange(12)
                        .build(key));
        FabricDefaultAttributeRegistry.register(TREX, TRexEntity.createAttributes());
    }
}
