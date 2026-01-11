package com.viaversion.addons.legacyforge;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.*;

public class ForgeRegistryData extends ForgePayload {

    private ForgeModList.ForgeVersion version;

    public boolean hasMore;

    public RegistryTypes registryType;

    public int idCount, removeCount, substitutionCount;

    public Integer dummyCount;

    public Map<String, Integer> idMap = new HashMap<>();

    public List<String> substitutionList = new ArrayList<>();

    public List<String> dummyList = new ArrayList<>();

    public ForgeRegistryData() {}

    @Override
    public void read(ForgePacketHandler handler, ByteBuf buffer, Direction direction, byte packetID) {
        super.read(handler, buffer, direction, packetID);
        version = this.handler.connection.get(ForgeModList.ForgeVersion.class);
        hasMore = buffer.readBoolean();
        registryType = RegistryTypes.from(readString(buffer));
        idCount = readVarInt(buffer);
        for (int i = 0; i < idCount; i++) {
            String name = readString(buffer);
            int id = readVarInt(buffer);
            if (shouldRemove(registryType, name, id)) {
                removeCount++;
            } else {
                idMap.put(name, id);
            }
        }
        substitutionCount = readVarInt(buffer);
        for (int i = 0; i < substitutionCount; i++) {
            String substitution = readString(buffer);
            substitutionList.add(substitution);
        }
        if (buffer.isReadable()) {
            dummyCount = readVarInt(buffer);
            for (int i = 0; i < dummyCount; i++) {
                String dummy = readString(buffer);
                dummyList.add(dummy);
            }
        }
    }

    @Override
    public ByteBuf write() {
        if (version == ForgeModList.ForgeVersion.V0809) {
            LOGGER.info("Didn't rewrite for actually 1.8 client");
            return super.write();
        }
        if (this.registryType == RegistryTypes.ITEMS) {
            if (version == ForgeModList.ForgeVersion.V1202) {
                ForgeRegistryData.RegistryDatas.addRecipes();
                Map<String, Integer> recipes = ForgeRegistryData.RegistryDatas.recipes;
                return writeData(false, RegistryTypes.RECIPES, recipes.size(), recipes);
            } // Trick 1.12.2 Forge Instead
            if (handler.flag == 0) {
                return writeData(false, RegistryTypes.ITEMS, idCount - removeCount, idMap);
            }
            return writeData(hasMore, RegistryTypes.ITEMS, idCount - removeCount, idMap);
        }
        return super.write();
    }

    public ByteBuf writeData(boolean more, RegistryTypes types, int counts, Map<String, Integer> registry) {
        ByteBuf rewrite = Unpooled.buffer();
        rewrite.writeByte(3);
        rewrite.writeBoolean(more);
        writeString(rewrite, types.type);
        writeVarInt(rewrite, counts);
        for (Map.Entry<String, Integer> entry : registry.entrySet()) {
            writeString(rewrite, entry.getKey());
            writeVarInt(rewrite, entry.getValue());
        }
        writeVarInt(rewrite, 0);
        return rewrite;
    }

    @Override
    public String toString() {
        return "RegistryData name=" + registryType.type + " side=" + direction + " hasMore=" + hasMore
                + " ids=" + idCount + " substitutions=" + substitutionCount + " dummy=" + dummyCount;
    }

    public boolean shouldRemove(RegistryTypes type, String entry, int id) {
        ForgeModList.ForgeVersion version = handler.connection.get(ForgeModList.ForgeVersion.class);
        if (version != null && version.ordinal() >= ForgeModList.ForgeVersion.V0900.ordinal()) {
            if (type == RegistryTypes.ITEMS && (RegistryDatas.removeList.contains(entry) || RegistryDatas.removeListID.contains(id))) {
                LOGGER.info("Removing registry {} entry: {}", registryType, entry);
                return true;
            }
        }
        return false;
    }

    public static class RegistryTypes {
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

    public static class RegistryDatas {

        public static boolean init = false;

        public static Map<String, Integer> recipes = new HashMap<>();

        public static List<String> removeList = new ArrayList<>();

        public static List<Integer> removeListID = new ArrayList<>();

        static {
            removeList.add("minecraft:lit_furnace");
        }

        static void addRecipes() {
            if (init) {
                return;
            }
            init = true;
            recipes.put("minecraft:armordye", 0);
            recipes.put("minecraft:bookcloning", 1);
            recipes.put("minecraft:mapcloning", 2);
            recipes.put("minecraft:mapextending", 3);
            recipes.put("minecraft:fireworks", 4);
            recipes.put("minecraft:repairitem", 5);
            recipes.put("minecraft:tippedarrow", 6);
            recipes.put("minecraft:bannerduplicate", 7);
            recipes.put("minecraft:banneraddpattern", 8);
            recipes.put("minecraft:shielddecoration", 9);
            recipes.put("minecraft:shulkerboxcoloring", 10);
            recipes.put("minecraft:yellow_wool", 11);
            recipes.put("minecraft:yellow_stained_hardened_clay", 12);
            recipes.put("minecraft:yellow_stained_glass_pane", 13);
            recipes.put("minecraft:yellow_stained_glass", 14);
            recipes.put("minecraft:yellow_dye_from_sunflower", 15);
            recipes.put("minecraft:yellow_dye_from_dandelion", 16);
            recipes.put("minecraft:yellow_concrete_powder", 17);
            recipes.put("minecraft:yellow_carpet", 18);
            recipes.put("minecraft:yellow_bed_from_white_bed", 19);
            recipes.put("minecraft:yellow_bed", 20);
            recipes.put("minecraft:yellow_banner", 21);
            recipes.put("minecraft:writable_book", 22);
            recipes.put("minecraft:wooden_sword", 23);
            recipes.put("minecraft:wooden_shovel", 24);
            recipes.put("minecraft:wooden_pressure_plate", 25);
            recipes.put("minecraft:wooden_pickaxe", 26);
            recipes.put("minecraft:wooden_hoe", 27);
            recipes.put("minecraft:wooden_door", 28);
            recipes.put("minecraft:wooden_button", 29);
            recipes.put("minecraft:wooden_axe", 30);
            recipes.put("minecraft:white_stained_hardened_clay", 31);
            recipes.put("minecraft:white_stained_glass_pane", 32);
            recipes.put("minecraft:white_stained_glass", 33);
            recipes.put("minecraft:white_concrete_powder", 34);
            recipes.put("minecraft:white_carpet", 35);
            recipes.put("minecraft:white_bed", 36);
            recipes.put("minecraft:white_banner", 37);
            recipes.put("minecraft:wheat", 38);
            recipes.put("minecraft:tripwire_hook", 39);
            recipes.put("minecraft:trapped_chest", 40);
            recipes.put("minecraft:trapdoor", 41);
            recipes.put("minecraft:torch", 42);
            recipes.put("minecraft:tnt_minecart", 43);
            recipes.put("minecraft:tnt", 44);
            recipes.put("minecraft:sugar", 45);
            recipes.put("minecraft:string_to_wool", 46);
            recipes.put("minecraft:stonebrick", 47);
            recipes.put("minecraft:stone_sword", 48);
            recipes.put("minecraft:stone_stairs", 49);
            recipes.put("minecraft:stone_slab", 50);
            recipes.put("minecraft:stone_shovel", 51);
            recipes.put("minecraft:stone_pressure_plate", 52);
            recipes.put("minecraft:stone_pickaxe", 53);
            recipes.put("minecraft:stone_hoe", 54);
            recipes.put("minecraft:stone_button", 55);
            recipes.put("minecraft:stone_brick_stairs", 56);
            recipes.put("minecraft:stone_brick_slab", 57);
            recipes.put("minecraft:stone_axe", 58);
            recipes.put("minecraft:sticky_piston", 59);
            recipes.put("minecraft:stick", 60);
            recipes.put("minecraft:spruce_wooden_slab", 61);
            recipes.put("minecraft:spruce_stairs", 62);
            recipes.put("minecraft:spruce_planks", 63);
            recipes.put("minecraft:spruce_fence_gate", 64);
            recipes.put("minecraft:spruce_fence", 65);
            recipes.put("minecraft:spruce_door", 66);
            recipes.put("minecraft:spruce_boat", 67);
            recipes.put("minecraft:spectral_arrow", 68);
            recipes.put("minecraft:speckled_melon", 69);
            recipes.put("minecraft:snow_layer", 70);
            recipes.put("minecraft:snow", 71);
            recipes.put("minecraft:smooth_sandstone", 72);
            recipes.put("minecraft:smooth_red_sandstone", 73);
            recipes.put("minecraft:slime_ball", 74);
            recipes.put("minecraft:slime", 75);
            recipes.put("minecraft:sign", 76);
            recipes.put("minecraft:shield", 77);
            recipes.put("minecraft:shears", 78);
            recipes.put("minecraft:sea_lantern", 79);
            recipes.put("minecraft:sandstone_stairs", 80);
            recipes.put("minecraft:sandstone_slab", 81);
            recipes.put("minecraft:sandstone", 82);
            recipes.put("minecraft:repeater", 83);
            recipes.put("minecraft:redstone_torch", 84);
            recipes.put("minecraft:redstone_lamp", 85);
            recipes.put("minecraft:redstone_block", 86);
            recipes.put("minecraft:redstone", 87);
            recipes.put("minecraft:red_wool", 88);
            recipes.put("minecraft:red_stained_hardened_clay", 89);
            recipes.put("minecraft:red_stained_glass_pane", 90);
            recipes.put("minecraft:red_stained_glass", 91);
            recipes.put("minecraft:red_sandstone_stairs", 92);
            recipes.put("minecraft:red_sandstone_slab", 93);
            recipes.put("minecraft:red_sandstone", 94);
            recipes.put("minecraft:red_nether_brick", 95);
            recipes.put("minecraft:red_dye_from_tulip", 96);
            recipes.put("minecraft:red_dye_from_rose_bush", 97);
            recipes.put("minecraft:red_dye_from_poppy", 98);
            recipes.put("minecraft:red_dye_from_beetroot", 99);
            recipes.put("minecraft:red_concrete_powder", 100);
            recipes.put("minecraft:red_carpet", 101);
            recipes.put("minecraft:red_bed_from_white_bed", 102);
            recipes.put("minecraft:red_bed", 103);
            recipes.put("minecraft:red_banner", 104);
            recipes.put("minecraft:rail", 105);
            recipes.put("minecraft:rabbit_stew_from_red_mushroom", 106);
            recipes.put("minecraft:rabbit_stew_from_brown_mushroom", 107);
            recipes.put("minecraft:quartz_stairs", 108);
            recipes.put("minecraft:quartz_slab", 109);
            recipes.put("minecraft:quartz_block", 110);
            recipes.put("minecraft:purpur_stairs", 111);
            recipes.put("minecraft:purpur_slab", 112);
            recipes.put("minecraft:purpur_pillar", 113);
            recipes.put("minecraft:purpur_block", 114);
            recipes.put("minecraft:purple_wool", 115);
            recipes.put("minecraft:purple_stained_hardened_clay", 116);
            recipes.put("minecraft:purple_stained_glass_pane", 117);
            recipes.put("minecraft:purple_stained_glass", 118);
            recipes.put("minecraft:purple_shulker_box", 119);
            recipes.put("minecraft:purple_dye", 120);
            recipes.put("minecraft:purple_concrete_powder", 121);
            recipes.put("minecraft:purple_carpet", 122);
            recipes.put("minecraft:purple_bed_from_white_bed", 123);
            recipes.put("minecraft:purple_bed", 124);
            recipes.put("minecraft:purple_banner", 125);
            recipes.put("minecraft:pumpkin_seeds", 126);
            recipes.put("minecraft:pumpkin_pie", 127);
            recipes.put("minecraft:prismarine_bricks", 128);
            recipes.put("minecraft:prismarine", 129);
            recipes.put("minecraft:polished_granite", 130);
            recipes.put("minecraft:polished_diorite", 131);
            recipes.put("minecraft:polished_andesite", 132);
            recipes.put("minecraft:piston", 133);
            recipes.put("minecraft:pink_wool", 134);
            recipes.put("minecraft:pink_stained_hardened_clay", 135);
            recipes.put("minecraft:pink_stained_glass_pane", 136);
            recipes.put("minecraft:pink_stained_glass", 137);
            recipes.put("minecraft:pink_dye_from_red_bonemeal", 138);
            recipes.put("minecraft:pink_dye_from_pink_tulip", 139);
            recipes.put("minecraft:pink_dye_from_peony", 140);
            recipes.put("minecraft:pink_concrete_powder", 141);
            recipes.put("minecraft:pink_carpet", 142);
            recipes.put("minecraft:pink_bed_from_white_bed", 143);
            recipes.put("minecraft:pink_bed", 144);
            recipes.put("minecraft:pink_banner", 145);
            recipes.put("minecraft:pillar_quartz_block", 146);
            recipes.put("minecraft:paper", 147);
            recipes.put("minecraft:painting", 148);
            recipes.put("minecraft:orange_wool", 149);
            recipes.put("minecraft:orange_stained_hardened_clay", 150);
            recipes.put("minecraft:orange_stained_glass_pane", 151);
            recipes.put("minecraft:orange_stained_glass", 152);
            recipes.put("minecraft:orange_dye_from_red_yellow", 153);
            recipes.put("minecraft:orange_dye_from_orange_tulip", 154);
            recipes.put("minecraft:orange_concrete_powder", 155);
            recipes.put("minecraft:orange_carpet", 156);
            recipes.put("minecraft:orange_bed_from_white_bed", 157);
            recipes.put("minecraft:orange_bed", 158);
            recipes.put("minecraft:orange_banner", 159);
            recipes.put("minecraft:observer", 160);
            recipes.put("minecraft:oak_wooden_slab", 161);
            recipes.put("minecraft:oak_stairs", 162);
            recipes.put("minecraft:oak_planks", 163);
            recipes.put("minecraft:noteblock", 164);
            recipes.put("minecraft:nether_wart_block", 165);
            recipes.put("minecraft:nether_brick_stairs", 166);
            recipes.put("minecraft:nether_brick_slab", 167);
            recipes.put("minecraft:nether_brick_fence", 168);
            recipes.put("minecraft:nether_brick", 169);
            recipes.put("minecraft:mushroom_stew", 170);
            recipes.put("minecraft:mossy_stonebrick", 171);
            recipes.put("minecraft:mossy_cobblestone_wall", 172);
            recipes.put("minecraft:mossy_cobblestone", 173);
            recipes.put("minecraft:minecart", 174);
            recipes.put("minecraft:melon_seeds", 175);
            recipes.put("minecraft:melon_block", 176);
            recipes.put("minecraft:map", 177);
            recipes.put("minecraft:magma_cream", 178);
            recipes.put("minecraft:magma", 179);
            recipes.put("minecraft:magenta_wool", 180);
            recipes.put("minecraft:magenta_stained_hardened_clay", 181);
            recipes.put("minecraft:magenta_stained_glass_pane", 182);
            recipes.put("minecraft:magenta_stained_glass", 183);
            recipes.put("minecraft:magenta_dye_from_purple_and_pink", 184);
            recipes.put("minecraft:magenta_dye_from_lilac", 185);
            recipes.put("minecraft:magenta_dye_from_lapis_red_pink", 186);
            recipes.put("minecraft:magenta_dye_from_lapis_ink_bonemeal", 187);
            recipes.put("minecraft:magenta_dye_from_allium", 188);
            recipes.put("minecraft:magenta_concrete_powder", 189);
            recipes.put("minecraft:magenta_carpet", 190);
            recipes.put("minecraft:magenta_bed_from_white_bed", 191);
            recipes.put("minecraft:magenta_bed", 192);
            recipes.put("minecraft:magenta_banner", 193);
            recipes.put("minecraft:lit_pumpkin", 194);
            recipes.put("minecraft:lime_wool", 195);
            recipes.put("minecraft:lime_stained_hardened_clay", 196);
            recipes.put("minecraft:lime_stained_glass_pane", 197);
            recipes.put("minecraft:lime_stained_glass", 198);
            recipes.put("minecraft:lime_dye", 199);
            recipes.put("minecraft:lime_concrete_powder", 200);
            recipes.put("minecraft:lime_carpet", 201);
            recipes.put("minecraft:lime_bed_from_white_bed", 202);
            recipes.put("minecraft:lime_bed", 203);
            recipes.put("minecraft:lime_banner", 204);
            recipes.put("minecraft:light_weighted_pressure_plate", 205);
            recipes.put("minecraft:light_gray_wool", 206);
            recipes.put("minecraft:light_gray_stained_hardened_clay", 207);
            recipes.put("minecraft:light_gray_stained_glass_pane", 208);
            recipes.put("minecraft:light_gray_stained_glass", 209);
            recipes.put("minecraft:light_gray_dye_from_white_tulip", 210);
            recipes.put("minecraft:light_gray_dye_from_oxeye_daisy", 211);
            recipes.put("minecraft:light_gray_dye_from_ink_bonemeal", 212);
            recipes.put("minecraft:light_gray_dye_from_gray_bonemeal", 213);
            recipes.put("minecraft:light_gray_dye_from_azure_bluet", 214);
            recipes.put("minecraft:light_gray_concrete_powder", 215);
            recipes.put("minecraft:light_gray_carpet", 216);
            recipes.put("minecraft:light_gray_bed_from_white_bed", 217);
            recipes.put("minecraft:light_gray_bed", 218);
            recipes.put("minecraft:light_gray_banner", 219);
            recipes.put("minecraft:light_blue_wool", 220);
            recipes.put("minecraft:light_blue_stained_hardened_clay", 221);
            recipes.put("minecraft:light_blue_stained_glass_pane", 222);
            recipes.put("minecraft:light_blue_stained_glass", 223);
            recipes.put("minecraft:light_blue_dye_from_lapis_bonemeal", 224);
            recipes.put("minecraft:light_blue_dye_from_blue_orchid", 225);
            recipes.put("minecraft:light_blue_concrete_powder", 226);
            recipes.put("minecraft:light_blue_carpet", 227);
            recipes.put("minecraft:light_blue_bed_from_white_bed", 228);
            recipes.put("minecraft:light_blue_bed", 229);
            recipes.put("minecraft:light_blue_banner", 230);
            recipes.put("minecraft:lever", 231);
            recipes.put("minecraft:leather_leggings", 232);
            recipes.put("minecraft:leather_helmet", 233);
            recipes.put("minecraft:leather_chestplate", 234);
            recipes.put("minecraft:leather_boots", 235);
            recipes.put("minecraft:leather", 236);
            recipes.put("minecraft:lead", 237);
            recipes.put("minecraft:lapis_lazuli", 238);
            recipes.put("minecraft:lapis_block", 239);
            recipes.put("minecraft:ladder", 240);
            recipes.put("minecraft:jungle_wooden_slab", 241);
            recipes.put("minecraft:jungle_stairs", 242);
            recipes.put("minecraft:jungle_planks", 243);
            recipes.put("minecraft:jungle_fence_gate", 244);
            recipes.put("minecraft:jungle_fence", 245);
            recipes.put("minecraft:jungle_door", 246);
            recipes.put("minecraft:jungle_boat", 247);
            recipes.put("minecraft:jukebox", 248);
            recipes.put("minecraft:item_frame", 249);
            recipes.put("minecraft:iron_trapdoor", 250);
            recipes.put("minecraft:iron_sword", 251);
            recipes.put("minecraft:iron_shovel", 252);
            recipes.put("minecraft:iron_pickaxe", 253);
            recipes.put("minecraft:iron_nugget", 254);
            recipes.put("minecraft:iron_leggings", 255);
            recipes.put("minecraft:iron_ingot_from_nuggets", 256);
            recipes.put("minecraft:iron_ingot_from_block", 257);
            recipes.put("minecraft:iron_hoe", 258);
            recipes.put("minecraft:iron_helmet", 259);
            recipes.put("minecraft:iron_door", 260);
            recipes.put("minecraft:iron_chestplate", 261);
            recipes.put("minecraft:iron_boots", 262);
            recipes.put("minecraft:iron_block", 263);
            recipes.put("minecraft:iron_bars", 264);
            recipes.put("minecraft:iron_axe", 265);
            recipes.put("minecraft:hopper_minecart", 266);
            recipes.put("minecraft:hopper", 267);
            recipes.put("minecraft:heavy_weighted_pressure_plate", 268);
            recipes.put("minecraft:hay_block", 269);
            recipes.put("minecraft:green_wool", 270);
            recipes.put("minecraft:green_stained_hardened_clay", 271);
            recipes.put("minecraft:green_stained_glass_pane", 272);
            recipes.put("minecraft:green_stained_glass", 273);
            recipes.put("minecraft:green_concrete_powder", 274);
            recipes.put("minecraft:green_carpet", 275);
            recipes.put("minecraft:green_bed_from_white_bed", 276);
            recipes.put("minecraft:green_bed", 277);
            recipes.put("minecraft:green_banner", 278);
            recipes.put("minecraft:gray_wool", 279);
            recipes.put("minecraft:gray_stained_hardened_clay", 280);
            recipes.put("minecraft:gray_stained_glass_pane", 281);
            recipes.put("minecraft:gray_stained_glass", 282);
            recipes.put("minecraft:gray_dye", 283);
            recipes.put("minecraft:gray_concrete_powder", 284);
            recipes.put("minecraft:gray_carpet", 285);
            recipes.put("minecraft:gray_bed_from_white_bed", 286);
            recipes.put("minecraft:gray_bed", 287);
            recipes.put("minecraft:gray_banner", 288);
            recipes.put("minecraft:granite", 289);
            recipes.put("minecraft:golden_sword", 290);
            recipes.put("minecraft:golden_shovel", 291);
            recipes.put("minecraft:golden_rail", 292);
            recipes.put("minecraft:golden_pickaxe", 293);
            recipes.put("minecraft:golden_leggings", 294);
            recipes.put("minecraft:golden_hoe", 295);
            recipes.put("minecraft:golden_helmet", 296);
            recipes.put("minecraft:golden_chestplate", 297);
            recipes.put("minecraft:golden_carrot", 298);
            recipes.put("minecraft:golden_boots", 299);
            recipes.put("minecraft:golden_axe", 300);
            recipes.put("minecraft:golden_apple", 301);
            recipes.put("minecraft:gold_nugget", 302);
            recipes.put("minecraft:gold_ingot_from_nuggets", 303);
            recipes.put("minecraft:gold_ingot_from_block", 304);
            recipes.put("minecraft:gold_block", 305);
            recipes.put("minecraft:glowstone", 306);
            recipes.put("minecraft:glass_pane", 307);
            recipes.put("minecraft:glass_bottle", 308);
            recipes.put("minecraft:furnace_minecart", 309);
            recipes.put("minecraft:furnace", 310);
            recipes.put("minecraft:flower_pot", 311);
            recipes.put("minecraft:flint_and_steel", 312);
            recipes.put("minecraft:fishing_rod", 313);
            recipes.put("minecraft:fire_charge", 314);
            recipes.put("minecraft:fermented_spider_eye", 315);
            recipes.put("minecraft:fence_gate", 316);
            recipes.put("minecraft:fence", 317);
            recipes.put("minecraft:ender_eye", 318);
            recipes.put("minecraft:ender_chest", 319);
            recipes.put("minecraft:end_rod", 320);
            recipes.put("minecraft:end_crystal", 321);
            recipes.put("minecraft:end_bricks", 322);
            recipes.put("minecraft:enchanting_table", 323);
            recipes.put("minecraft:emerald_block", 324);
            recipes.put("minecraft:emerald", 325);
            recipes.put("minecraft:dropper", 326);
            recipes.put("minecraft:dispenser", 327);
            recipes.put("minecraft:diorite", 328);
            recipes.put("minecraft:diamond_sword", 329);
            recipes.put("minecraft:diamond_shovel", 330);
            recipes.put("minecraft:diamond_pickaxe", 331);
            recipes.put("minecraft:diamond_leggings", 332);
            recipes.put("minecraft:diamond_hoe", 333);
            recipes.put("minecraft:diamond_helmet", 334);
            recipes.put("minecraft:diamond_chestplate", 335);
            recipes.put("minecraft:diamond_boots", 336);
            recipes.put("minecraft:diamond_block", 337);
            recipes.put("minecraft:diamond_axe", 338);
            recipes.put("minecraft:diamond", 339);
            recipes.put("minecraft:detector_rail", 340);
            recipes.put("minecraft:daylight_detector", 341);
            recipes.put("minecraft:dark_prismarine", 342);
            recipes.put("minecraft:dark_oak_wooden_slab", 343);
            recipes.put("minecraft:dark_oak_stairs", 344);
            recipes.put("minecraft:dark_oak_planks", 345);
            recipes.put("minecraft:dark_oak_fence_gate", 346);
            recipes.put("minecraft:dark_oak_fence", 347);
            recipes.put("minecraft:dark_oak_door", 348);
            recipes.put("minecraft:dark_oak_boat", 349);
            recipes.put("minecraft:cyan_wool", 350);
            recipes.put("minecraft:cyan_stained_hardened_clay", 351);
            recipes.put("minecraft:cyan_stained_glass_pane", 352);
            recipes.put("minecraft:cyan_stained_glass", 353);
            recipes.put("minecraft:cyan_dye", 354);
            recipes.put("minecraft:cyan_concrete_powder", 355);
            recipes.put("minecraft:cyan_carpet", 356);
            recipes.put("minecraft:cyan_bed_from_white_bed", 357);
            recipes.put("minecraft:cyan_bed", 358);
            recipes.put("minecraft:cyan_banner", 359);
            recipes.put("minecraft:crafting_table", 360);
            recipes.put("minecraft:cookie", 361);
            recipes.put("minecraft:compass", 362);
            recipes.put("minecraft:comparator", 363);
            recipes.put("minecraft:cobblestone_wall", 364);
            recipes.put("minecraft:cobblestone_slab", 365);
            recipes.put("minecraft:coarse_dirt", 366);
            recipes.put("minecraft:coal_block", 367);
            recipes.put("minecraft:coal", 368);
            recipes.put("minecraft:clock", 369);
            recipes.put("minecraft:clay", 370);
            recipes.put("minecraft:chiseled_stonebrick", 371);
            recipes.put("minecraft:chiseled_sandstone", 372);
            recipes.put("minecraft:chiseled_red_sandstone", 373);
            recipes.put("minecraft:chiseled_quartz_block", 374);
            recipes.put("minecraft:chest_minecart", 375);
            recipes.put("minecraft:chest", 376);
            recipes.put("minecraft:cauldron", 377);
            recipes.put("minecraft:carrot_on_a_stick", 378);
            recipes.put("minecraft:cake", 379);
            recipes.put("minecraft:bucket", 380);
            recipes.put("minecraft:brown_wool", 381);
            recipes.put("minecraft:brown_stained_hardened_clay", 382);
            recipes.put("minecraft:brown_stained_glass_pane", 383);
            recipes.put("minecraft:brown_stained_glass", 384);
            recipes.put("minecraft:brown_concrete_powder", 385);
            recipes.put("minecraft:brown_carpet", 386);
            recipes.put("minecraft:brown_bed_from_white_bed", 387);
            recipes.put("minecraft:brown_bed", 388);
            recipes.put("minecraft:brown_banner", 389);
            recipes.put("minecraft:brick_stairs", 390);
            recipes.put("minecraft:brick_slab", 391);
            recipes.put("minecraft:brick_block", 392);
            recipes.put("minecraft:brewing_stand", 393);
            recipes.put("minecraft:bread", 394);
            recipes.put("minecraft:bowl", 395);
            recipes.put("minecraft:bow", 396);
            recipes.put("minecraft:bookshelf", 397);
            recipes.put("minecraft:book", 398);
            recipes.put("minecraft:bone_meal_from_bone", 399);
            recipes.put("minecraft:bone_meal_from_block", 400);
            recipes.put("minecraft:bone_block", 401);
            recipes.put("minecraft:boat", 402);
            recipes.put("minecraft:blue_wool", 403);
            recipes.put("minecraft:blue_stained_hardened_clay", 404);
            recipes.put("minecraft:blue_stained_glass_pane", 405);
            recipes.put("minecraft:blue_stained_glass", 406);
            recipes.put("minecraft:blue_concrete_powder", 407);
            recipes.put("minecraft:blue_carpet", 408);
            recipes.put("minecraft:blue_bed_from_white_bed", 409);
            recipes.put("minecraft:blue_bed", 410);
            recipes.put("minecraft:blue_banner", 411);
            recipes.put("minecraft:blaze_powder", 412);
            recipes.put("minecraft:black_wool", 413);
            recipes.put("minecraft:black_stained_hardened_clay", 414);
            recipes.put("minecraft:black_stained_glass_pane", 415);
            recipes.put("minecraft:black_stained_glass", 416);
            recipes.put("minecraft:black_concrete_powder", 417);
            recipes.put("minecraft:black_carpet", 418);
            recipes.put("minecraft:black_bed_from_white_bed", 419);
            recipes.put("minecraft:black_bed", 420);
            recipes.put("minecraft:black_banner", 421);
            recipes.put("minecraft:birch_wooden_slab", 422);
            recipes.put("minecraft:birch_stairs", 423);
            recipes.put("minecraft:birch_planks", 424);
            recipes.put("minecraft:birch_fence_gate", 425);
            recipes.put("minecraft:birch_fence", 426);
            recipes.put("minecraft:birch_door", 427);
            recipes.put("minecraft:birch_boat", 428);
            recipes.put("minecraft:beetroot_soup", 429);
            recipes.put("minecraft:beacon", 430);
            recipes.put("minecraft:arrow", 431);
            recipes.put("minecraft:armor_stand", 432);
            recipes.put("minecraft:anvil", 433);
            recipes.put("minecraft:andesite", 434);
            recipes.put("minecraft:activator_rail", 435);
            recipes.put("minecraft:acacia_wooden_slab", 436);
            recipes.put("minecraft:acacia_stairs", 437);
            recipes.put("minecraft:acacia_planks", 438);
            recipes.put("minecraft:acacia_fence_gate", 439);
            recipes.put("minecraft:acacia_fence", 440);
            recipes.put("minecraft:acacia_door", 441);
            recipes.put("minecraft:acacia_boat", 442);
        }
    }
}
