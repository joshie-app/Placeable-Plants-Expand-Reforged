package it.bisumto.placeable.mixin;

import it.bisumto.placeable.Placeable;
import it.bisumto.placeable.PlaceableConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VegetationBlock.class)
public class PlantBlockMixin {

    // PLACEABLE
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    public void canPlantAnywhere(BlockState state, LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // Keep vanilla placement rules for functional plants by default.
        // In Minecraft 26.1.x, many plants now share VegetationBlock as their common parent.
        Object self = this;
        if (self instanceof CropBlock || self instanceof NetherWartBlock || self instanceof SweetBerryBushBlock || self instanceof SaplingBlock) {
            if (!PlaceableConfig.isAdditionalPlaceablePlant(state)) {
                return;
            }
        }
        if (Placeable.isValidFloor(world, pos)) {
            cir.setReturnValue(true);
        }
    }

}
