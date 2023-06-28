package github.pitbox46.fluidhoppercart;

import com.lothrazar.cyclic.ModCyclic;
import com.lothrazar.cyclic.block.BlockCyclic;
import com.lothrazar.cyclic.config.ClientConfigCyclic;
import com.lothrazar.cyclic.registry.BlockRegistry;
import com.lothrazar.cyclic.util.FluidHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FluidHopperMinecart extends AbstractMinecart {
    protected static final EntityDataAccessor<CompoundTag> FLUID = SynchedEntityData.defineId(FluidHopperMinecart.class, EntityDataSerializers.COMPOUND_TAG);

    private static final int FLOW = 1000;
    public static final int CAPACITY = 1000;
    public FluidTank tank = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            IFluidHandler handler = FluidHopperMinecart.this.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElse(null);
            if (handler != null && handler.getFluidInTank(0) != null) {
                FluidStack f = handler.getFluidInTank(0);
                if (!FluidHopperMinecart.this.getLevel().isClientSide) {
                    FluidHopperMinecart.this.setFluid(f);
                }

            }
        }
    };
    LazyOptional<FluidTank> fluidCap = LazyOptional.of(() -> this.tank);

    protected boolean enabled = true;

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_20059_) {
        super.onSyncedDataUpdated(p_20059_);
        if(FLUID.equals(p_20059_)) {
            tank.setFluid(FluidStack.loadFluidStackFromNBT((CompoundTag) this.entityData.get(p_20059_)));
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(FLUID, FluidStack.EMPTY.writeToNBT(new CompoundTag()));
    }

    public void setFluid(FluidStack stack) {
        tank.setFluid(stack);
        this.entityData.set(FLUID, stack.writeToNBT(new CompoundTag()));
    }

    public FluidStack getFluid() {
        return tank.getFluid();
    }

    @Override
    public void invalidateCaps() {
        this.fluidCap.invalidate();
        super.invalidateCaps();
    }

    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == ForgeCapabilities.FLUID_HANDLER ? this.fluidCap.cast() : super.getCapability(cap, side);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_38137_) {
        super.readAdditionalSaveData(p_38137_);
        this.tank.readFromNBT(p_38137_.getCompound("fluid"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_38151_) {
        super.addAdditionalSaveData(p_38151_);
        CompoundTag fluid = new CompoundTag();
        this.tank.writeToNBT(fluid);
        p_38151_.put("fluid", fluid);
    }

    protected FluidHopperMinecart(EntityType<?> p_38087_, Level p_38088_) {
        super(p_38087_, p_38088_);
    }

    protected FluidHopperMinecart(Level p_38091_, double p_38092_, double p_38093_, double p_38094_) {
        super(Mod.FLUID_HOPPER_MINECART.get(), p_38091_, p_38092_, p_38093_, p_38094_);
    }

    @Override
    protected Item getDropItem() {
        return Mod.FLUID_HOPPER_MINECART_ITEM.get();
    }

    @Override
    public Type getMinecartType() {
        return Type.HOPPER;
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return BlockRegistry.FLUIDHOPPER.get().defaultBlockState();
    }

    @Override
    public int getDefaultDisplayOffset() {
        return 1;
    }

    public void activateMinecart(int p_38596_, int p_38597_, int p_38598_, boolean p_38599_) {
        boolean flag = !p_38599_;
        if (flag != this.isEnabled()) {
            this.setEnabled(flag);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean flag) {
        enabled = flag;
    }

    @Override
    public InteractionResult interact(Player p_19978_, InteractionHand p_19979_) {
        InteractionResult ret = super.interact(p_19978_, p_19979_);
        if (ret.consumesAction())
            return ret;
        if (p_19978_.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        else if (!this.level.isClientSide) {
            if (ClientConfigCyclic.FLUID_BLOCK_STATUS.get()) {
                p_19978_.displayClientMessage(Component.translatable(BlockCyclic.getFluidRatioName(this.tank)), true);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        else {
            return InteractionResult.SUCCESS;
        }
    }

    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            if (this.isEnabled()) {
                this.tryExtract();
            }
        }
    }

    protected void tryExtract() {
        if (this.tank != null) {
            BlockPos target = this.blockPosition().relative(Direction.UP);
            List<AABB> aabbs = Hopper.SUCK.toAabbs();
            aabbs.add(this.getBoundingBox().move(position().scale(-1)).move(0.5, 0, 0.5).inflate(0.25, 0, 0.25));
            if(tryFillFromTankEntities(aabbs.stream().map(aabb -> aabb.move(getX() - 0.5, getY(), getZ() - 0.5)), getLevel(), this, this.tank))
                return;
            if(tryFillFromTankBlocks(aabbs.stream().map(aabb -> aabb.move(getX() - 0.5, getY(), getZ() - 0.5)), getLevel(), this.tank))
                return;
            if (this.tank.getSpace() >= CAPACITY) {
                FluidHelpers.extractSourceWaterloggedCauldron(this.level, target, this.tank);
            }
        }
    }

    public static boolean tryFillFromTankEntities(Stream<AABB> aabbs, Level level, @Nullable Entity entity, IFluidHandler hopper) {
        return aabbs.flatMap(aabb -> level.getEntities(entity,
                        aabb,
                        e -> e.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).isPresent())
                .stream())
                .map(e -> e.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).orElseThrow(NoSuchElementException::new))
                .anyMatch(handler -> tryFillPositionFromTank(hopper, handler, FLOW));
    }

    public static boolean tryFillFromTankBlocks(Stream<AABB> aabbs, Level level, IFluidHandler hopper) {
        return aabbs.flatMap(BlockPos::betweenClosedStream)
                .filter(pos -> {
                    BlockEntity e;
                    return (e = level.getBlockEntity(pos)) != null && e.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).isPresent();
                })
                .map(pos -> level.getBlockEntity(pos).getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).orElseThrow(NoSuchElementException::new))
                .anyMatch(handler -> tryFillPositionFromTank(hopper, handler, 1000));
    }

    public static boolean tryFillPositionFromTank(IFluidHandler fluidTo, IFluidHandler tankFrom, int amount) {
        if (tankFrom == null || fluidTo == null) {
            return false;
        } else {
            try {
                FluidStack wasDrained = tankFrom.drain(amount, IFluidHandler.FluidAction.SIMULATE);
                if (wasDrained == null) {
                    return false;
                }

                int filled = fluidTo.fill(wasDrained, IFluidHandler.FluidAction.SIMULATE);
                if (wasDrained != null && wasDrained.getAmount() > 0 && filled > 0) {
                    int realAmt = Math.min(filled, wasDrained.getAmount());
                    wasDrained = tankFrom.drain(realAmt, IFluidHandler.FluidAction.EXECUTE);
                    if (wasDrained == null) {
                        return false;
                    }

                    int actuallyFilled = fluidTo.fill(wasDrained, IFluidHandler.FluidAction.EXECUTE);
                    return actuallyFilled > 0;
                }

                return false;
            } catch (Exception var10) {
                ModCyclic.LOGGER.error("A fluid tank had an issue when we tried to fill", var10);
                return false;
            }
        }
    }

    public static class MinecartItem extends AbstractMinecartItem {
        public MinecartItem(Properties properties) {
            super(properties);
        }

        @Override
        void createMinecart(ItemStack stack, Level world, double x, double y, double z) {
            FluidHopperMinecart cart = new FluidHopperMinecart(world, x, y, z);
            if (stack.hasCustomHoverName()) {
                cart.setCustomName(stack.getDisplayName());
            }
            world.addFreshEntity(cart);
        }
    }
}
