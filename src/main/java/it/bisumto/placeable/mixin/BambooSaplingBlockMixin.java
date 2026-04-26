package it.bisumto.placeable.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BambooSaplingBlock.class)
public class BambooSaplingBlockMixin {

    // Keep vanilla placement rules for bamboo saplings to avoid odd worldgen,
    // such as bamboo stacking or generating on top of unsuitable blocks.

    // PREVENT GROWING
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    public void randomTickMixin(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        BlockState floor = world.getBlockState(pos.below());
        if (!floor.is(BlockTags.BAMBOO_PLANTABLE_ON))
            ci.cancel();
    }

}
