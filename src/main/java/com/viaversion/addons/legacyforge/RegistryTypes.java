package com.viaversion.addons.legacyforge;

public class RegistryTypes {
    static final RegistryTypes BLOCKS = new RegistryTypes("minecraft:blocks");
    static final RegistryTypes DATAS = new RegistryTypes("minecraft:dataserializers");
    static final RegistryTypes ENCHANTMENTS = new RegistryTypes("minecraft:enchantments");
    static final RegistryTypes ENTITIES = new RegistryTypes("minecraft:entities");
    static final RegistryTypes ITEMS = new RegistryTypes("minecraft:items");
    static final RegistryTypes POTIONS = new RegistryTypes("minecraft:potions");
    static final RegistryTypes POTIONTYPES = new RegistryTypes("minecraft:potiontypes");
    static final RegistryTypes RECIPES = new RegistryTypes("minecraft:recipes");
    static final RegistryTypes SOUNDS = new RegistryTypes("minecraft:soundevents");
    static final RegistryTypes VILLAGER = new RegistryTypes("minecraft:villagerprofessions");
    final String type;

    RegistryTypes(String type) {
        this.type = type;
    }

    static RegistryTypes from(String type) {
        return switch (type) {
            case "minecraft:blocks" -> BLOCKS;
            case "minecraft:dataserializers" -> DATAS;
            case "minecraft:enchantments" -> ENCHANTMENTS;
            case "minecraft:entities" -> ENTITIES;
            case "minecraft:items" -> ITEMS;
            case "minecraft:potions" -> POTIONS;
            case "minecraft:potiontypes" -> POTIONTYPES;
            case "minecraft:recipes" -> RECIPES;
            case "minecraft:soundevents" -> SOUNDS;
            case "minecraft:villagerprofessions" -> VILLAGER;
            default -> new RegistryTypes(type);
        };
    }

    @Override
    public String toString() {
        return this.type;
    }
}
