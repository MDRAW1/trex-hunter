package com.trexhunter;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class ModItems {
    public static Item TREX_SPAWN_EGG;

    private ModItems() {}

    public static void register() {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, TRexHunterMod.id("trex_spawn_egg"));
        TREX_SPAWN_EGG = Registry.register(
                BuiltInRegistries.ITEM,
                key,
                new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(ModEntities.TREX)));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS)
                .register(entries -> entries.accept(TREX_SPAWN_EGG));
    }
}
