package com.example.luremod.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.*;

public class LureConfig {
    private static final String CONFIG_FILE_NAME = "luremod.toml";

    public static List<String> LURE_ENTITIES = new ArrayList<>();

    public static Set<Block> BLOCKS_TO_LURE = new HashSet<>();

    public static Set<Item> ITEMS_TO_LURE = new HashSet<>();

    public static int SEARCH_RADIUS = 8;      
    public static int VERTICAL_RANGE = 2;     
    public static double STOP_DISTANCE = 2.0; 

    public static void loadConfig() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE_NAME);
        CommentedFileConfig data = CommentedFileConfig.builder(configPath)
            .autosave()
            .build();
        data.load();

        if (!data.contains("luremod.entities")) {
            data.set("luremod.entities", Arrays.asList("minecraft:cat", "minecraft:zombie", "minecraft:skeleton", "minecraft:spider", "minecraft:creeper", "minecraft:husk", "minecraft:drowned", "minecraft:villager", "minecraft:vindicator", "minecraft:vex", "minecraft:ravager", "minecraft:evoker", "minecraft:pillager", "minecraft:cod", "minecraft:tropical_fish", "minecraft:salmon", "minecraft:pufferfish", "minecraft:turtle", "minecraft:dolphin", "minecraft:guardian", "minecraft:elder_guardian", "minecraft:iron_golem"));
        }
        data.setComment("luremod.entities",
            "List of entity IDs (in \"modid:entityid\" format) that will be attracted.\n"
          + "Add or remove entries to pick which mobs get this AI.\n"
          + "Examples:\n"
          + "  - \"minecraft:pig\"\n"
          + "  - \"minecraft:cow\"\n"
          + "  - \"someothermod:custommob\"\n"
        );

        if (!data.contains("luremod.blocks")) {
            data.set("luremod.blocks", Arrays.asList("minecraft:campfire", "minecraft:soul_campfire", "minecraft:torch", "minecraft:redstone_torch", "minecraft:soul_torch"));
        }
        data.setComment("luremod.blocks",
            "Block IDs (in \"modid:blockid\" format) that the mob is attracted to.\n"
          + "Any block in this list will attract the entity if within the search radius.\n"
          + "Examples:\n"
          + "  - \"minecraft:hay_block\"\n"
          + "  - \"minecraft:campfire\"\n"
          + "  - \"someothermod:coolblock\"\n"
        );

        if (!data.contains("luremod.items")) {
            data.set("luremod.items", Arrays.asList("minecraft:torch", "minecraft:redstone_torch", "minecraft:soul_torch"));
        }
        data.setComment("luremod.items",
            "Any item (\"modid:itemid\") that attracts the mob if a nearby player holds it.\n"
          + "Examples:\n"
          + "  - \"minecraft:wheat\"\n"
          + "  - \"minecraft:torch\"\n"
          + "  - \"someothermod:specialbait\"\n"
        );

        if (!data.contains("luremod.distance.searchRadius")) {
            data.set("luremod.distance.searchRadius", 8);
        }
        data.setComment("luremod.distance.searchRadius",
            "Horizontal radius (X/Z in blocks) to scan for lure blocks or players.\n"
          + "Increasing this can have a performance impact if many entities are active."
        );

        if (!data.contains("luremod.distance.verticalRange")) {
            data.set("luremod.distance.verticalRange", 2);
        }
        data.setComment("luremod.distance.verticalRange",
            "Vertical range (Y in blocks) above/below the mob to scan for blocks/players.\n"
          + "e.g. \"2\" means 2 blocks up/down from the mob's position."
        );

        if (!data.contains("luremod.distance.stopDistance")) {
            data.set("luremod.distance.stopDistance", 2.0);
        }
        data.setComment("luremod.distance.stopDistance",
            "How close (in blocks) the mob should get before it stops moving toward the lure.\n"
          + "If set to 0.5, for example, the mob will move very close to the lure."
        );

        data.save();

        LURE_ENTITIES = new ArrayList<>(data.get("luremod.entities"));

        List<String> blockStrings = data.get("luremod.blocks");
        Set<Block> tmpBlocks = new HashSet<>();
        for (String idStr : blockStrings) {
            Block b = Registry.BLOCK.getOptional(new ResourceLocation(idStr)).orElse(null);
            if (b != null) tmpBlocks.add(b);
        }
        BLOCKS_TO_LURE = tmpBlocks;

        List<String> itemStrings = data.get("luremod.items");
        Set<Item> tmpItems = new HashSet<>();
        for (String idStr : itemStrings) {
            Item i = Registry.ITEM.getOptional(new ResourceLocation(idStr)).orElse(null);
            if (i != null) tmpItems.add(i);
        }
        ITEMS_TO_LURE = tmpItems;

        SEARCH_RADIUS = data.get("luremod.distance.searchRadius");
        VERTICAL_RANGE = data.get("luremod.distance.verticalRange");
        STOP_DISTANCE = data.get("luremod.distance.stopDistance");

        data.close();

        System.out.println("[LureConfig] Entities: " + LURE_ENTITIES);
        System.out.println("[LureConfig] Blocks:   " + blockStrings);
        System.out.println("[LureConfig] Items:    " + itemStrings);
        System.out.println("[LureConfig] searchRadius: " + SEARCH_RADIUS);
        System.out.println("[LureConfig] verticalRange: " + VERTICAL_RANGE);
        System.out.println("[LureConfig] stopDistance:  " + STOP_DISTANCE);
    }
}
