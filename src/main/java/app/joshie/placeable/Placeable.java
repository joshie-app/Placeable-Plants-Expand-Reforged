package app.joshie.placeable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod("placeable")
public class Placeable {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger("placeable");

    public Placeable(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, PlaceableConfig.SPEC);
        // Most behavior is implemented via Mixins.
        LOGGER.info("Placeable loaded");
    }

    public static boolean isValidFloor(LevelReader world, BlockPos pos) {
        // Avoid force-loading chunks during survival checks.
        // In 1.21.x, LevelReader does not expose getChunk(BlockPos), so use the lightweight loaded-area checks.
        BlockPos below = pos.below();
        if (!world.hasChunkAt(below)) {
            return false;
        }
        return isValidFloor(world.getBlockState(below), world, below);
    }

    public static boolean isValidFloor(BlockState floor, BlockGetter world, BlockPos pos) {
        boolean worldgen = isWorldgenCheck(world);
        if (PlaceableConfig.shouldSkipExpandedWorldgenPlacement(worldgen)) {
            return false;
        }

        if (PlaceableConfig.isDisallowedFloor(floor, worldgen)) {
            return false;
        }

        return Block.canSupportRigidBlock(world, pos) || floor.is(BlockTags.LEAVES) || floor.is(Blocks.DIRT_PATH);
    }

    public static boolean shouldSkipExpandedPlacement(BlockGetter world) {
        return PlaceableConfig.shouldSkipExpandedWorldgenPlacement(isWorldgenCheck(world));
    }

    private static boolean isWorldgenCheck(BlockGetter world) {
        return world instanceof WorldGenRegion;
    }
}
