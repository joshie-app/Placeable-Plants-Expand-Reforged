package it.bisumto.placeable;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("placeable")
public class Placeable {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger("placeable");

    public Placeable() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PlaceableConfig.SPEC);
        // Most behavior is implemented via Mixins.
        LOGGER.info("Placeable loaded");
    }

    public static boolean isValidFloor(LevelReader world, BlockPos pos) {
        // Avoid force-loading chunks during survival checks.
        // In 1.20.x, LevelReader does not expose getChunk(BlockPos), so use the lightweight loaded-area checks.
        BlockPos below = pos.below();
        if (!world.hasChunkAt(below)) {
            return false;
        }
        return isValidFloor(world.getBlockState(below), world, below);
    }

    public static boolean isValidFloor(BlockState floor, BlockGetter world, BlockPos pos) {
        if (PlaceableConfig.isBlacklistedFloor(floor)) {
            return false;
        }

        return Block.canSupportRigidBlock(world, pos) || floor.is(BlockTags.LEAVES) || floor.is(Blocks.DIRT_PATH);
    }
}
