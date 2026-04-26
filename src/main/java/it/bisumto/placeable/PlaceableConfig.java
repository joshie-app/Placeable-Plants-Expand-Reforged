package it.bisumto.placeable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class PlaceableConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> FLOOR_BLACKLIST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("placement");

        FLOOR_BLACKLIST = builder
                .comment(
                        "Blocks or block tags that plants are NOT allowed to be placed/generated on, even if they would otherwise be valid floors.",
                        "Use block ids like 'minecraft:stone' or tag ids prefixed with '#', like '#minecraft:leaves'.",
                        "Default blocks leaves, which also prevents most plants from generating on top of leaf blocks during world generation."
                )
                .defineListAllowEmpty(
                        List.of("floor_blacklist"),
                        () -> List.of("#minecraft:leaves"),
                        PlaceableConfig::isValidBlacklistEntry
                );

        builder.pop();

        SPEC = builder.build();
    }

    private static boolean isValidBlacklistEntry(Object value) {
        if (!(value instanceof String entry)) {
            return false;
        }

        entry = entry.trim();
        if (entry.isEmpty()) {
            return false;
        }

        if (entry.startsWith("#")) {
            return ResourceLocation.tryParse(entry.substring(1)) != null;
        }

        return ResourceLocation.tryParse(entry) != null;
    }

    public static boolean isBlacklistedFloor(BlockState state) {
        for (String rawEntry : FLOOR_BLACKLIST.get()) {
            String entry = rawEntry.trim();

            if (entry.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(entry.substring(1));
                if (tagId != null) {
                    TagKey<Block> tag = TagKey.create(Registries.BLOCK, tagId);
                    if (state.is(tag)) {
                        return true;
                    }
                }
            } else {
                ResourceLocation blockId = ResourceLocation.tryParse(entry);
                if (blockId != null && BuiltInRegistries.BLOCK.getOptional(blockId).filter(state::is).isPresent()) {
                    return true;
                }
            }
        }

        return false;
    }
}
