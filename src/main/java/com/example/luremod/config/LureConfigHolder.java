package com.example.luremod.config;

import com.electronwill.nightconfig.core.Config;
import net.minecraftforge.common.ForgeConfigSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LureConfigHolder {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<List<? extends Config>> LURE_GROUPS; 
    public static final ForgeConfigSpec.IntValue SCAN_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.IntValue PLAYER_CHECK_RADIUS;
    public static final ForgeConfigSpec.IntValue PLAYER_CHECK_VERTICAL;
    public static final ForgeConfigSpec.IntValue BLOCK_CHECK_PLAYER_RADIUS;
    static {
        BUILDER.push("Attraction Groups");
        LURE_GROUPS = BUILDER
                .comment("""
                    A list of attraction groups. Each group defines mobs and what they are lured by.
                          Each group has:
                         - group_id (string, required): A unique name for the group.
                         - lure_speed (double, optional, default: 1.2): How fast mobs in this group go to the location.
                         - search_radius (int, optional, default: 8): How far mobs in this group search for temptation :).
                         - mobs (list, required): A list of mobs in this group. Can specify 'id' and 'nbt'.
                         - lured_blocks (list, required): Blocks this group are attracted to. Can specify 'id', 'states', and 'nbt' (for block entities).
                         - lured_items (list, required): Items this group are attracted. Can specify 'id' and 'nbt'.
                            
                            """)
                .defineList("lure_groups", new ArrayList<>(), obj -> obj instanceof Config);
        BUILDER.pop();
        BUILDER.push("Optimizations");
        SCAN_COOLDOWN_TICKS = BUILDER.defineInRange("scanCooldownTicks", 15, 1, 200);
        PLAYER_CHECK_RADIUS = BUILDER.defineInRange("playerCheckRadius", 8, 1, 64);
        PLAYER_CHECK_VERTICAL = BUILDER.defineInRange("playerCheckVertical", 4, 1, 64);
        BLOCK_CHECK_PLAYER_RADIUS = BUILDER.defineInRange("blockCheckPlayerRadius", 32, 1, 64);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void setDefaults() {
        if (LURE_GROUPS.get().isEmpty()) {
            List<Config> defaultGroups = new ArrayList<>();
            defaultGroups.add(createDefaultUndeadGroup());
            defaultGroups.add(createDefaultArthropodGroup());
            defaultGroups.add(createDefaultTeachingGroup());
            defaultGroups.add(createDefaultSporeGroup());
            LURE_GROUPS.set(defaultGroups);
            LURE_GROUPS.save();
        }
    }

    private static Config createDefaultUndeadGroup() {
        Config group = Config.inMemory();
        group.set("group_id", "hostile_lure_artificial");
        group.set("lure_speed", 1.3);
        group.set("search_radius", 10);
        group.set("mobs", List.of(
            createMobDef("minecraft:zombie"),
            createMobDef("minecraft:skeleton"),
            createMobDef("minecraft:husk"),
            createMobDef("minecraft:stray"),
            createMobDef("minecraft:zombie_villager"),
            createMobDef("minecraft:phantom"),
            createMobDef("minecraft:drowned"),
            createMobDef("minecraft:piglin"),
            createMobDef("minecraft:piglin_brute"),
            createMobDef("minecraft:vindicator"),
            createMobDef("minecraft:evoker"),
            createMobDef("minecraft:illusioner"),
            createMobDef("minecraft:ravager"),
            createMobDef("minecraft:pillager"),
            createMobDef("minecraft:witch"),
            createMobDef("minecraft:vex"),
            createMobDef("minecraft:warden"),
            createMobDef("minecraft:wither_skeleton"),
            createMobDef("minecraft:slime"),
            createMobDef("minecraft:creeper")
        ));
        group.set("lured_blocks", List.of(
            createLureSourceDef("minecraft:torch"),
            createLureSourceDef("minecraft:redstone_torch"),
            createLureSourceDef("minecraft:soul_torch"),
            createLureSourceDef("minecraft:armor_stand"),
            createLureSourceDef("minecraft:note_block"),
            createLureSourceDef("minecraft:endchanting_table"),
            createLureSourceDef("minecraft:jukebox"),
            createLureSourceDef("minecraft:brewing_stand"),
            createLureSourceDef("minecraft:beacon"),
            createLureSourceDef("minecraft:lantern"),
            createLureSourceDef("minecraft:bell"),
            createLureSourceDef("minecraft:glowstone"),
            createLureSourceDef("minecraft:sea_lantern"),
            createLureSourceDef("minecraft:end_rod"),
            createLureSourceDef("minecraft:shroomlight"),
            createLureSourceDef("minecraft:bed"),
            createLureSourceDefWithStates("minecraft:furnace", Map.of("lit", "true")),
            createLureSourceDefWithStates("minecraft:blast_furnace", Map.of("lit", "true")),
            createLureSourceDefWithStates("minecraft:smoker", Map.of("lit", "true")),
            createLureSourceDefWithStates("minecraft:respawn_anchor", Map.of("charges", "1")),
            createLureSourceDefWithStates("minecraft:respawn_anchor", Map.of("charges", "2")),
            createLureSourceDefWithStates("minecraft:respawn_anchor", Map.of("charges", "3")),
            createLureSourceDefWithStates("minecraft:respawn_anchor", Map.of("charges", "4")),
            createLureSourceDefWithStates("minecraft:campfire", Map.of("lit", "true")),
            createLureSourceDefWithStates("minecraft:soul_campfire", Map.of("lit", "true"))
        ));
        group.set("lured_items", List.of(
            createLureSourceDef("minecraft:torch"),
            createLureSourceDef("minecraft:redstone_torch"),
            createLureSourceDef("minecraft:soul_torch"),
            createLureSourceDef("minecraft:lantern"),
            createLureSourceDef("minecraft:glowstone"),
            createLureSourceDef("minecraft:sea_lantern"),
            createLureSourceDef("minecraft:end_rod"),
            createLureSourceDef("minecraft:shroomlight"),
            createLureSourceDef("minecraft:golden_apple"),
            createLureSourceDef("minecraft:enchanted_golden_apple"),
            createLureSourceDef("minecraft:raw_fish"),
            createLureSourceDef("minecraft:cooked_fish"),
            createLureSourceDef("minecraft:fish"),
            createLureSourceDef("minecraft:porkchop"),
            createLureSourceDef("minecraft:cooked_porkchop"),
            createLureSourceDef("minecraft:beef"),
            createLureSourceDef("minecraft:cooked_beef"),
            createLureSourceDef("minecraft:chicken"),
            createLureSourceDef("minecraft:cooked_chicken"),
            createLureSourceDef("minecraft:mutton"),
            createLureSourceDef("minecraft:cooked_mutton"),
            createLureSourceDef("minecraft:rabbit"),
            createLureSourceDef("minecraft:cooked_rabbit")
        ));
        return group;
    }

    private static Config createDefaultArthropodGroup() {
        Config group = Config.inMemory();
        group.set("group_id", "arthropod_custom_lure");
        group.set("lure_speed", 1.5);
        group.set("search_radius", 8);
        group.set("mobs", List.of(
            createMobDef("minecraft:spider"),
            createMobDef("minecraft:cave_spider"),
            createMobDef("minecraft:silverfish"),
            createMobDefWithNbt("minecraft:endermite", "{PlayerSpawned:1b}")
        ));
        group.set("lured_blocks", List.of(
            createLureSourceDef("minecraft:torch"),
            createLureSourceDef("minecraft:redstone_torch"),
            createLureSourceDef("minecraft:soul_torch"),
            createLureSourceDef("minecraft:armor_stand"),
            createLureSourceDef("minecraft:note_block"),
            createLureSourceDef("minecraft:endchanting_table"),
            createLureSourceDef("minecraft:jukebox"),
            createLureSourceDef("minecraft:brewing_stand"),
            createLureSourceDef("minecraft:beacon"),
            createLureSourceDef("minecraft:lantern"),
            createLureSourceDef("minecraft:bell"),
            createLureSourceDef("minecraft:glowstone"),
            createLureSourceDef("minecraft:sea_lantern"),
            createLureSourceDef("minecraft:end_rod"),
            createLureSourceDef("minecraft:shroomlight"),
            createLureSourceDef("minecraft:bed"),
            createLureSourceDefWithStates("minecraft:furnace", Map.of("lit", "true")),
            createLureSourceDefWithStates("minecraft:blast_furnace", Map.of("lit", "true")),
            createLureSourceDefWithStates("minecraft:smoker", Map.of("lit", "true")),
            createLureSourceDefWithStates("minecraft:respawn_anchor", Map.of("charges", "1")),
            createLureSourceDefWithStates("minecraft:respawn_anchor", Map.of("charges", "2")),
            createLureSourceDefWithStates("minecraft:respawn_anchor", Map.of("charges", "3")),
            createLureSourceDefWithStates("minecraft:respawn_anchor", Map.of("charges", "4")),
            createLureSourceDefWithStates("minecraft:campfire", Map.of("lit", "true")),
            createLureSourceDefWithStates("minecraft:soul_campfire", Map.of("lit", "true"))
        ));
        group.set("lured_items", List.of(
            createLureSourceDef("minecraft:torch"),
            createLureSourceDef("minecraft:redstone_torch"),
            createLureSourceDef("minecraft:soul_torch"),
            createLureSourceDef("minecraft:lantern"),
            createLureSourceDef("minecraft:glowstone"),
            createLureSourceDef("minecraft:sea_lantern"),
            createLureSourceDef("minecraft:end_rod"),
            createLureSourceDef("minecraft:shroomlight"),
            createLureSourceDef("minecraft:golden_apple"),
            createLureSourceDef("minecraft:enchanted_golden_apple"),
            createLureSourceDef("minecraft:raw_fish"),
            createLureSourceDef("minecraft:cooked_fish"),
            createLureSourceDef("minecraft:fish"),
            createLureSourceDef("minecraft:porkchop"),
            createLureSourceDef("minecraft:cooked_porkchop"),
            createLureSourceDef("minecraft:beef"),
            createLureSourceDef("minecraft:cooked_beef"),
            createLureSourceDef("minecraft:chicken"),
            createLureSourceDef("minecraft:cooked_chicken"),
            createLureSourceDef("minecraft:mutton"),
            createLureSourceDef("minecraft:cooked_mutton"),
            createLureSourceDef("minecraft:rabbit"),
            createLureSourceDef("minecraft:cooked_rabbit")
        ));
        return group;
    }
    private static Config createDefaultTeachingGroup() {
        Config group = Config.inMemory();
        group.set("group_id", "teaching_example_zombies");
        group.set("lure_speed", 2.0);
        group.set("search_radius", 16);

        group.set("mobs", List.of(

            createMobDefWithNbt("minecraft:zombie", "{Silent:1b}"),

            createMobDefWithCustomName("minecraft:drowned", "Patches")
        ));

        group.set("lured_blocks", List.of(
            createLureSourceDef("minecraft:beacon"),

            createLureSourceDefWithStates("minecraft:conduit", Map.of("waterlogged", "false")),

            createLureSourceDefWithNbt("minecraft:spawner", "{SpawnData:{entity:{id:\"minecraft:skeleton\"}}}")
        ));

        group.set("lured_items", List.of(
        createLureSourceDef("minecraft:golden_apple"),

            createLureSourceDefWithNbt("minecraft:netherite_sword", "{Enchantments:[{id:\"minecraft:smite\"}]}"),

            createLureSourceDefWithCustomName("minecraft:paper", "Exorcism Scroll")
        ));

        return group;
    }

        private static Config createDefaultSporeGroup() {
        Config group = Config.inMemory();
        group.set("group_id", "Spore_fire_fear");
        group.set("lure_speed", 1.4);
        group.set("search_radius", 12);
        group.set("mobs", List.of(
            createMobDef("spore:braiomil"),
            createMobDef("spore:braurei"),
            createMobDef("spore:brot"),
            createMobDef("spore:brute"),
            createMobDef("spore:busser"),
            createMobDef("spore:inf_construct"),
            createMobDef("spore:delusioner"),
            createMobDef("spore:gastgaber"),
            createMobDef("spore:gazenbreacher"),
            createMobDef("spore:griefer"),
            createMobDef("spore:hevoker"),
            createMobDef("spore:hidenburg"),
            createMobDef("spore:howitzer"),
            createMobDef("spore:howler"),
            createMobDef("spore:hvindicator"),
            createMobDef("spore:inf_drownded"),
            createMobDef("spore:inf_evoker"),
            createMobDef("spore:inf_hazmat"),
            createMobDef("spore:husk"),
            createMobDef("spore:inf_pillager"),
            createMobDef("spore:inf_player"),
            createMobDef("spore:inf_villager"),
            createMobDef("spore:inf_vindicator"),
            createMobDef("spore:inf_wanderer"),
            createMobDef("spore:inf_witch"),
            createMobDef("spore:inf_human"),
            createMobDef("spore:jagd"),
            createMobDef("spore:knight"),
            createMobDef("spore:lacerator"),
            createMobDef("spore:inquisitor"),
            createMobDef("spore:leaper"),
            createMobDef("spore:mound"),
            createMobDef("spore:nuclea"),
            createMobDef("spore:ogre"),
            createMobDef("spore:plagued"),
            createMobDef("spore:proto"),
            createMobDef("spore:reconstructor"),
            createMobDef("spore:scamper"),
            createMobDef("spore:scavenger"),
            createMobDef("spore:scent"),
            createMobDef("spore:sieger"),
            createMobDef("spore:specter"),
            createMobDef("spore:spitter"),
            createMobDef("spore:stalker"),
            createMobDef("spore:thorn"),
            createMobDef("spore:umarmed"),
            createMobDef("spore:usurper"),
            createMobDef("spore:verva"),
            createMobDef("spore:vigil"),
            createMobDef("spore:volatile"),
            createMobDef("spore:wendigo")
        ));
        group.set("lured_blocks", List.of(
            createLureSourceDef("minecraft:torch"),
            createLureSourceDef("minecraft:redstone_torch"),
            createLureSourceDef("minecraft:soul_torch"),
            createLureSourceDef("minecraft:armor_stand"),
            createLureSourceDef("minecraft:note_block"),
            createLureSourceDef("minecraft:endchanting_table"),
            createLureSourceDef("minecraft:jukebox"),
            createLureSourceDef("minecraft:brewing_stand"),
            createLureSourceDef("minecraft:beacon"),
            createLureSourceDef("minecraft:lantern"),
            createLureSourceDef("minecraft:bell"),
            createLureSourceDef("minecraft:glowstone"),
            createLureSourceDef("minecraft:sea_lantern"),
            createLureSourceDef("minecraft:end_rod"),
            createLureSourceDef("minecraft:shroomlight"),
            createLureSourceDef("minecraft:bed"),
            createLureSourceDefWithStates("minecraft:furnace", Map.of("lit", "true")),
            createLureSourceDefWithStates("minecraft:blast_furnace", Map.of("lit", "true")),
            createLureSourceDefWithStates("minecraft:smoker", Map.of("lit", "true")),
            createLureSourceDefWithStates("minecraft:respawn_anchor", Map.of("charges", "1")),
            createLureSourceDefWithStates("minecraft:respawn_anchor", Map.of("charges", "2")),
            createLureSourceDefWithStates("minecraft:respawn_anchor", Map.of("charges", "3")),
            createLureSourceDefWithStates("minecraft:respawn_anchor", Map.of("charges", "4")),
            createLureSourceDefWithStates("minecraft:campfire", Map.of("lit", "true")),
            createLureSourceDefWithStates("minecraft:soul_campfire", Map.of("lit", "true"))
        ));
        group.set("lured_items", List.of(
            createLureSourceDef("minecraft:torch"),
            createLureSourceDef("minecraft:redstone_torch"),
            createLureSourceDef("minecraft:soul_torch"),
            createLureSourceDef("minecraft:lantern"),
            createLureSourceDef("minecraft:glowstone"),
            createLureSourceDef("minecraft:sea_lantern"),
            createLureSourceDef("minecraft:end_rod"),
            createLureSourceDef("minecraft:shroomlight"),
            createLureSourceDef("minecraft:golden_apple"),
            createLureSourceDef("minecraft:enchanted_golden_apple"),
            createLureSourceDef("minecraft:raw_fish"),
            createLureSourceDef("minecraft:cooked_fish"),
            createLureSourceDef("minecraft:fish"),
            createLureSourceDef("minecraft:porkchop"),
            createLureSourceDef("minecraft:cooked_porkchop"),
            createLureSourceDef("minecraft:beef"),
            createLureSourceDef("minecraft:cooked_beef"),
            createLureSourceDef("minecraft:chicken"),
            createLureSourceDef("minecraft:cooked_chicken"),
            createLureSourceDef("minecraft:mutton"),
            createLureSourceDef("minecraft:cooked_mutton"),
            createLureSourceDef("minecraft:rabbit"),
            createLureSourceDef("minecraft:cooked_rabbit")
        ));
        return group;
    }

    private static Config createMobDef(String id) {
        Config table = Config.inMemory();
        table.set("id", id);
        return table;
    }

    private static Config createMobDefWithNbt(String id, String nbt) {
        Config table = createMobDef(id);
        table.set("nbt", nbt);
        return table;
    }

    private static Config createLureSourceDef(String id) {
        Config table = Config.inMemory();
        table.set("id", id);
        return table;
    }

    private static Config createLureSourceDefWithNbt(String id, String nbt) {
        Config table = createLureSourceDef(id);
        table.set("nbt", nbt);
        return table;
    }

    private static Config createLureSourceDefWithStates(String id, Map<String, String> states) {
        Config table = Config.inMemory();
        table.set("id", id);
        Config statesTable = table.createSubConfig();
        states.forEach(statesTable::set);
        table.set("states", statesTable);
        return table;
    }
    private static Config createMobDefWithCustomName(String id, String name) {
        Config table = createMobDef(id);
        table.set("custom_name", name);
        return table;
    }
    private static Config createLureSourceDefWithCustomName(String id, String name) {
        Config table = createLureSourceDef(id);
        table.set("custom_name", name);
        return table;
    }
}