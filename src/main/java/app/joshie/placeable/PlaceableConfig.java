package app.joshie.placeable;

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

    public static final ModConfigSpec.EnumValue<FilterMode> FILTER_MODE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PLACEMENT_WHITELIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WORLDGEN_WHITELIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PLACEMENT_BLACKLIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WORLDGEN_BLACKLIST;
    public static final ModConfigSpec.BooleanValue DISABLE_EXPANDED_WORLDGEN_PLACEMENT;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ADDITIONAL_PLACEABLE_PLANTS;

    public enum FilterMode {
        WHITELIST,
        BLACKLIST
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("placement");

        FILTER_MODE = builder
                .comment(
                        "Whether the placement filter lists act as a whitelist or a blacklist.",
                        "WHITELIST: the expanded placement rules only apply on blocks matched by placement_whitelist (and worldgen_whitelist during world generation).",
                        "BLACKLIST: the expanded placement rules apply on any solid block except those matched by placement_blacklist (and worldgen_blacklist during world generation).",
                        "Blocks not covered by the expanded placement rules simply keep their normal vanilla/modded placement behavior."
                )
                .defineEnum("filter_mode", FilterMode.WHITELIST);

        PLACEMENT_WHITELIST = builder
                .comment(
                        "Blocks or block tags that plants ARE allowed to be placed on, including manual player placement and world generation.",
                        "Only used when filter_mode is WHITELIST.",
                        "Whitelisted blocks must still have a solid top surface (or be leaves or a dirt path) to actually hold a plant.",
                        "Use block ids like 'minecraft:stone' or tag ids prefixed with '#', like '#minecraft:leaves'.",
                        "Defaults to leaves and dirt paths, the most common decorative placement targets for this mod."
                )
                .defineListAllowEmpty(
                        List.of("placement_whitelist"),
                        () -> List.of("#minecraft:leaves", "minecraft:dirt_path"),
                        PlaceableConfig::isValidListEntry
                );

        WORLDGEN_WHITELIST = builder
                .comment(
                        "Blocks or block tags that plants are allowed to generate on during world generation.",
                        "During world generation a block must be matched by BOTH placement_whitelist and this list.",
                        "This does not affect manual player placement.",
                        "Only used when filter_mode is WHITELIST.",
                        "Use block ids like 'minecraft:stone' or tag ids prefixed with '#', like '#minecraft:leaves'.",
                        "Empty by default, so plants never naturally generate on the expanded placement blocks."
                )
                .defineListAllowEmpty(
                        List.of("worldgen_whitelist"),
                        () -> List.of(),
                        PlaceableConfig::isValidListEntry
                );

        PLACEMENT_BLACKLIST = builder
                .comment(
                        "Blocks or block tags that plants are NOT allowed to be placed on at all, including manual player placement and world generation.",
                        "Only used when filter_mode is BLACKLIST.",
                        "Use block ids like 'minecraft:stone' or tag ids prefixed with '#', like '#minecraft:leaves'.",
                        "Empty by default, so players can still manually place plants on leaves or other solid blocks."
                )
                .defineListAllowEmpty(
                        List.of("placement_blacklist"),
                        () -> List.of(),
                        PlaceableConfig::isValidListEntry
                );

        WORLDGEN_BLACKLIST = builder
                .comment(
                        "Blocks or block tags that plants are NOT allowed to generate on during world generation.",
                        "This does not affect manual player placement.",
                        "Only used when filter_mode is BLACKLIST.",
                        "Use block ids like 'minecraft:stone' or tag ids prefixed with '#', like '#minecraft:leaves'.",
                        "Default blocks leaves, so plants should not naturally generate on top of leaf blocks."
                )
                .defineListAllowEmpty(
                        List.of("worldgen_blacklist"),
                        () -> List.of("#minecraft:leaves"),
                        PlaceableConfig::isValidListEntry
                );

        DISABLE_EXPANDED_WORLDGEN_PLACEMENT = builder
                .comment(
                        "Globally disables this mod's expanded plant-placement rules during world generation.",
                        "When true, plants generated by worldgen keep their normal vanilla/modded soil checks instead of generating on any solid non-soil block.",
                        "Manual player placement is not affected by this option.",
                        "When this is enabled, worldgen_whitelist and worldgen_blacklist are effectively unnecessary because expanded worldgen placement is skipped entirely."
                )
                .define("disable_expanded_worldgen_placement", false);

        ADDITIONAL_PLACEABLE_PLANTS = builder
                .comment(
                        "Optional plant block ids or block tags that should use the mod's expanded placement rules even if they are normally kept vanilla-only.",
                        "Useful for decorative/building setups where you want to allow specific saplings, crops, berry bushes, nether wart, or similar BushBlock-based plants on non-soil blocks.",
                        "Use plant block ids like 'minecraft:oak_sapling' or tag ids prefixed with '#', like '#minecraft:saplings'.",
                        "Empty by default to preserve vanilla behavior for crops, saplings, and similar functional plants."
                )
                .defineListAllowEmpty(
                        List.of("additional_placeable_plants"),
                        () -> List.of(),
                        PlaceableConfig::isValidListEntry
                );

        builder.pop();

        SPEC = builder.build();
    }

    private static boolean isValidListEntry(Object value) {
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

    public static boolean isDisallowedFloor(BlockState state, boolean worldgen) {
        if (FILTER_MODE.get() == FilterMode.WHITELIST) {
            // Whitelist mode: the floor must be explicitly allowed for placement,
            // and worldgen additionally requires the floor to be on the worldgen whitelist.
            if (!matchesList(state, PLACEMENT_WHITELIST.get())) {
                return true;
            }

            return worldgen && !matchesList(state, WORLDGEN_WHITELIST.get());
        }

        // Blacklist mode: the floor is allowed unless explicitly blocked,
        // and worldgen additionally blocks floors on the worldgen blacklist.
        if (matchesList(state, PLACEMENT_BLACKLIST.get())) {
            return true;
        }

        return worldgen && matchesList(state, WORLDGEN_BLACKLIST.get());
    }

    public static boolean shouldSkipExpandedWorldgenPlacement(boolean worldgen) {
        return worldgen && DISABLE_EXPANDED_WORLDGEN_PLACEMENT.get();
    }

    public static boolean isAdditionalPlaceablePlant(BlockState state) {
        return matchesList(state, ADDITIONAL_PLACEABLE_PLANTS.get());
    }

    private static boolean matchesList(BlockState state, List<? extends String> entries) {
        if (entries.isEmpty()) {
            return false;
        }

        for (String rawEntry : entries) {
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
