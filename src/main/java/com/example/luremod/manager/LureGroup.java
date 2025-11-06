package com.example.luremod.manager;

import com.electronwill.nightconfig.core.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record LureGroup(
    String groupId,
    double lureSpeed,
    int searchRadius,
    boolean attractedToBlood,
    boolean bloodBypassesLos,
    List<MobDefinition> mobs,
    List<LureSourceDefinition> luredBlocks, 
    List<LureSourceDefinition> luredItems   
) {
    public static LureGroup fromConfig(Config config) {
        String groupId = config.get("group_id");
        double lureSpeed = config.getOptional("lure_speed").map(o -> ((Number) o).doubleValue()).orElse(1.2);
        int searchRadius = config.getOptional("search_radius").map(o -> ((Number) o).intValue()).orElse(8);
        boolean attractedToBlood = config.getOptional("attracted_to_blood").map(o -> (Boolean) o).orElse(false);
        boolean bloodBypassesLos = config.getOptional("blood_bypasses_los").map(o -> (Boolean) o).orElse(false);
        List<MobDefinition> mobs = ((List<Config>) config.get("mobs")).stream()
                .map(MobDefinition::fromConfig).collect(Collectors.toList());
        List<LureSourceDefinition> luredBlocks = ((List<Config>) config.get("lured_blocks")).stream()
                .map(LureSourceDefinition::fromConfig).collect(Collectors.toList());
        List<LureSourceDefinition> luredItems = ((List<Config>) config.get("lured_items")).stream()
                .map(LureSourceDefinition::fromConfig).collect(Collectors.toList());
        return new LureGroup(groupId, lureSpeed, searchRadius, attractedToBlood, bloodBypassesLos, mobs, luredBlocks, luredItems);
    }

    public int getMatchScore(Mob mob) {
        ResourceLocation mobId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (mobId == null) return -1;
        int bestScore = -1;
        for (MobDefinition def : mobs) {
            int score = def.getMatchScore(mob, mobId);
            if (score > bestScore) {
                bestScore = score;
            }
        }
        return bestScore;
    }
    
    public boolean isLuredBlock(BlockState blockState, @Nullable BlockEntity blockEntity) {
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(blockState.getBlock());
        if (blockId == null) return false;
        for (LureSourceDefinition def : luredBlocks) {
            if (def.matches(blockId, blockState, blockEntity, null)) { return true; }
        }
        return false;
    }

    public boolean isLuredItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) return false;
        for (LureSourceDefinition def : luredItems) {
            if (def.matches(itemId, null, null, stack)) { return true; }
        }
        return false;
    }

    public record MobDefinition(ResourceLocation id, @Nullable String customName, @Nullable CompoundTag nbt) {
        public static MobDefinition fromConfig(Config config) {
            ResourceLocation id = new ResourceLocation((String)config.get("id"));
            String customName = config.getOptional("custom_name").map(String::valueOf).orElse(null);
            CompoundTag nbt = config.getOptional("nbt").map(nbtStr -> {
                try {
                    return TagParser.parseTag((String) nbtStr);
                } catch (Exception e) { throw new IllegalArgumentException("Invalid NBT: " + e.getMessage()); }
            }).orElse(null);
            return new MobDefinition(id, customName, nbt);
        }

        public int getMatchScore(Mob mob, ResourceLocation mobId) {
            if (!this.id.equals(mobId)) return -1;
            
            boolean nameMatch = (this.customName == null) || (mob.hasCustomName() && mob.getName().getString().equals(this.customName));
            boolean nbtMatch = (this.nbt == null) || (NbtUtils.compareNbt(this.nbt, mob.saveWithoutId(new CompoundTag()), true));

            if (!nameMatch || !nbtMatch) {
                return -1;
            }

            int score = 0;
            if (this.customName != null) score += 2;
            if (this.nbt != null) score += 1;
            return score;
        }
    }

    public record LureSourceDefinition(ResourceLocation id, @Nullable String customName, @Nullable Config states, @Nullable CompoundTag nbt) {
        public static LureSourceDefinition fromConfig(Config config) {
            ResourceLocation id = new ResourceLocation((String)config.get("id"));
            String customName = config.getOptional("custom_name").map(String::valueOf).orElse(null);
            Config states = config.getOptional("states").map(o -> (Config) o).orElse(null);
            CompoundTag nbt = config.getOptional("nbt").map(nbtStr -> {
                try {
                    return TagParser.parseTag((String) nbtStr);
                } catch (Exception e) { throw new IllegalArgumentException("Invalid NBT: " + e.getMessage()); }
            }).orElse(null);
            return new LureSourceDefinition(id, customName, states, nbt);
        }
        
        public boolean matches(ResourceLocation targetId, @Nullable BlockState blockState, @Nullable BlockEntity blockEntity, @Nullable ItemStack itemStack) {
            if (!this.id.equals(targetId)) return false;
            if (blockState != null) {
                if (states != null) {
                    for (Config.Entry entry : states.entrySet()) {
                        String key = entry.getKey();
                        if (key == null) { continue; }
                        Property<?> property = blockState.getBlock().getStateDefinition().getProperty(key);
                        if (property == null) return false;
                        Optional<?> value = property.getValue(entry.getValue().toString());
                        if (value.isEmpty() || !blockState.getValue(property).equals(value.get())) { return false; }
                    }
                }
                if (nbt != null) {
                    if (blockEntity == null) return false;
                    CompoundTag blockNbt = blockEntity.saveWithoutMetadata();
                    if (!NbtUtils.compareNbt(nbt, blockNbt, true)) { return false; }
                }
                return true;
            }
            if (itemStack != null) {
                if (this.customName != null && (!itemStack.hasCustomHoverName() || !itemStack.getHoverName().getString().equals(this.customName))) return false;
                if (this.nbt != null) {
                    CompoundTag itemNbt = itemStack.getTag();
                    if (itemNbt == null || !NbtUtils.compareNbt(this.nbt, itemNbt, true)) return false;
                }
                return true;
            }
            return false;
        }
    }
}