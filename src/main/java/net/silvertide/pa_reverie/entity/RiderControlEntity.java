package net.silvertide.pa_reverie.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class RiderControlEntity extends Entity {

    protected boolean finished;
    protected UUID riderUuid;

    protected RiderControlEntity(EntityType<? extends RiderControlEntity> type, Level level) {
        super(type, level);
    }

    protected abstract void controlRiderTick();

    protected void onControlEnded(ServerLevel level) {
    }

    public void handleRiderDisconnect(ServerPlayer rider) {
        finished = true;
        riderUuid = null;
        rider.stopRiding();
        discard();
    }

    public boolean isFinished() {
        return finished;
    }

    public void endControl() {
        if (!finished) {
            finishAndDiscard();
        }
    }

    protected void setRiderUuid(UUID uuid) {
        this.riderUuid = uuid;
    }

    @Nullable
    protected ServerPlayer lookupRider(ServerLevel level) {
        if (riderUuid == null) {
            return null;
        }
        return level.getServer().getPlayerList().getPlayer(riderUuid);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || finished) {
            return;
        }
        if (getPassengers().isEmpty()) {
            finishAndDiscard();
            return;
        }
        controlRiderTick();
    }

    protected void finishAndDiscard() {
        finished = true;
        if (level() instanceof ServerLevel serverLevel) {
            onControlEnded(serverLevel);
        }
        ejectPassengers();
        discard();
    }

    @Override
    public void onRemovedFromWorld() {
        finished = true;
        if (level() instanceof ServerLevel serverLevel) {
            onControlEnded(serverLevel);
        }
        ejectPassengers();
        super.onRemovedFromWorld();
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return null;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.0;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {}
}
