package github.pitbox46.fluidhoppercart.mixins;

import com.lothrazar.cyclic.block.TileBlockEntityCyclic;
import com.lothrazar.cyclic.block.hopperfluid.TileFluidHopper;
import com.lothrazar.cyclic.capabilities.block.FluidTankBase;
import github.pitbox46.fluidhoppercart.FluidHopperMinecart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

@Mixin(value = TileFluidHopper.class, remap = false)
public abstract class TileFluidHopperMixin extends TileBlockEntityCyclic {
    @Shadow public FluidTankBase tank;

    public TileFluidHopperMixin(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @Inject(at = @At(value = "INVOKE", target = "com/lothrazar/cyclic/util/FluidHelpers.getTank (Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/fluids/capability/IFluidHandler;"), method = "tryExtract", cancellable = true)
    private void checkEntities(CallbackInfo ci) {
        if(FluidHopperMinecart.tryFillFromTankEntities(Stream.of(new AABB(getBlockPos().above())), getLevel(), null, this.tank)) {
            ci.cancel();
        }
    }
}
