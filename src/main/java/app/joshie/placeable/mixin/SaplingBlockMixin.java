package app.joshie.placeable.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SaplingBlock.class)
public class SaplingBlockMixin {

    /**
     * Allow saplings to be placed on any solid block (handled elsewhere via canSurvive mixin),
     * but prevent them from actually growing unless planted on vanilla-appropriate soil.
     *
     * In 1.20.x, SaplingBlock no longer reliably has a concrete randomTick method to inject into,
     * so we intercept advanceTree instead (called by both random tick growth and bonemeal).
     */
    @Inject(method = "advanceTree", at = @At("HEAD"), cancellable = true)
    private void placeable$advanceTreeMixin(ServerLevel world, BlockPos pos, BlockState state, RandomSource random, CallbackInfo ci) {
        BlockState floor = world.getBlockState(pos.below());
        if (!floor.is(BlockTags.DIRT) && !floor.is(Blocks.FARMLAND)) {
            ci.cancel();
        }
    }
}
