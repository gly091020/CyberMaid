package com.gly091020.CyberMaid.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CyberMaid.registry.CyberMaidItems;
import com.gly091020.CyberMaid.util.HandleCyberwareSyncWithoutPlayer;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.WeakHashMap;

public class EmergencyJetEscapeItem extends MaidLegCyberwareItem {
    public static final int JET_ENERGY_COST = 1000;
    public static final int JET_COOLDOWN_TICKS = 160;
    public static final float JET_DAMAGE_THRESHOLD = 0.10F;
    public static final float JET_LOW_HEALTH_THRESHOLD = 0.50F;
    public static final int JET_LOW_HEALTH_COOLDOWN_TICKS = 2400;
    public static final int JET_HORIZONTAL_DELAY_TICKS = 5;
    public static final double JET_HORIZONTAL_FORCE = 1.55D;
    public static final double JET_VERTICAL_FORCE = 1.55D;
    public static final int JET_FALL_PROTECTION_TIMEOUT = 400;

    private static final double MIN_HORIZONTAL_LENGTH_SQUARED = 1.0E-6D;
    private static final Vec3[] LOCAL_SEARCH_DIRECTIONS = {
            new Vec3(1.0D, 0.0D, 0.0D),
            new Vec3(-1.0D, 0.0D, 0.0D),
            new Vec3(0.0D, 0.0D, 1.0D),
            new Vec3(0.0D, 0.0D, -1.0D),
            new Vec3(1.0D, 0.0D, 1.0D).normalize(),
            new Vec3(1.0D, 0.0D, -1.0D).normalize(),
            new Vec3(-1.0D, 0.0D, 1.0D).normalize(),
            new Vec3(-1.0D, 0.0D, -1.0D).normalize()
    };
    private static final Map<EntityMaid, JetState> JET_STATES = new WeakHashMap<>();

    public EmergencyJetEscapeItem() {
        super(legBuilder(8)
                .energy(0, 0, 0, ICyberware.StackingRule.STATIC)
                .eventCost(JET_ENERGY_COST));
    }

    @Override
    public boolean canToggle(ItemStack stack) {
        return true;
    }

    @Override
    public void onMaidTick(LivingEntity wearer, ItemStack stack) {
        if (!(wearer instanceof EntityMaid maid) || maid.level().isClientSide) return;
        tickState(maid);
        if (isBelowLowHealthThreshold(maid) && isLowHealthTriggerReady(maid)
                && tryTrigger(maid, null, true)) return;
        if (maid.isInLava()) tryTrigger(maid, null, false);
    }

    @Override
    public void onRemoved(LivingEntity wearer, ItemStack stack) {
        if (wearer instanceof EntityMaid maid) clearState(maid);
    }

    public static void tryTriggerFromDamage(EntityMaid maid, DamageSource source, float actualDamage) {
        if (actualDamage <= 0.0F) return;
        boolean lavaDanger = maid.isInLava() || source.is(DamageTypes.LAVA);
        boolean severeDamage = actualDamage > maid.getMaxHealth() * JET_DAMAGE_THRESHOLD;
        if (isBelowLowHealthThreshold(maid) && isLowHealthTriggerReady(maid)
                && tryTrigger(maid, source, true)) return;
        if (lavaDanger || severeDamage) tryTrigger(maid, source, false);
    }

    private static boolean tryTrigger(EntityMaid maid, DamageSource source, boolean lowHealthTrigger) {
        if (maid.level().isClientSide || !maid.isAlive() || maid.isRemoved()) return false;

        ItemStack stack = findOperationalStack(maid, CyberMaidItems.EMERGENCY_JET_ESCAPE.get());
        if (stack.isEmpty()) return false;

        CyberwareUserData data = maid.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        long gameTime = maid.level().getGameTime();
        JetState state = JET_STATES.get(maid);
        if (state != null && gameTime < state.cooldownUntil) return false;
        if (data.getEnergyStored() < JET_ENERGY_COST || data.extractEnergy(JET_ENERGY_COST, true) != JET_ENERGY_COST) {
            return false;
        }

        Vec3 direction = getEscapeDirection(maid, source);
        if (!isSafeHorizontalDirection(direction)) return false;
        Vec3 verticalMotion = new Vec3(0.0D, JET_VERTICAL_FORCE, 0.0D);
        if (!isFinite(verticalMotion)) return false;

        if (data.extractEnergy(JET_ENERGY_COST, false) != JET_ENERGY_COST) return false;
        maid.setDeltaMovement(verticalMotion);
        maid.hasImpulse = true;
        maid.fallDistance = 0.0F;

        JetState newState = state == null ? new JetState() : state;
        newState.cooldownUntil = gameTime + JET_COOLDOWN_TICKS;
        newState.fallProtectionUntil = gameTime + JET_FALL_PROTECTION_TIMEOUT;
        newState.fallProtection = true;
        if (lowHealthTrigger) {
            newState.lowHealthCooldownUntil = gameTime + JET_LOW_HEALTH_COOLDOWN_TICKS;
        }
        newState.horizontalImpulseAt = gameTime + JET_HORIZONTAL_DELAY_TICKS;
        newState.horizontalDirection = direction;
        newState.horizontalImpulsePending = true;
        JET_STATES.put(maid, newState);

        playEffects(maid);
        HandleCyberwareSyncWithoutPlayer.syncToTrackingPlayers(maid);
        return true;
    }

    public static boolean consumeFallProtection(EntityMaid maid) {
        if (maid.level().isClientSide) return false;
        JetState state = JET_STATES.get(maid);
        if (state == null || !state.fallProtection) return false;
        if (maid.level().getGameTime() > state.fallProtectionUntil) {
            state.fallProtection = false;
            discardExpiredState(maid, state);
            return false;
        }
        state.fallProtection = false;
        maid.fallDistance = 0.0F;
        discardExpiredState(maid, state);
        return true;
    }

    private static void tickState(EntityMaid maid) {
        if (maid.level().isClientSide) return;
        JetState state = JET_STATES.get(maid);
        if (state == null) return;
        if (!maid.isAlive() || maid.isRemoved()) {
            clearState(maid);
            return;
        }
        long gameTime = maid.level().getGameTime();
        if (state.horizontalImpulsePending && gameTime >= state.horizontalImpulseAt) {
            applyHorizontalImpulse(maid, state);
        }
        if (state.fallProtection && gameTime > state.fallProtectionUntil) {
            state.fallProtection = false;
        }
        discardExpiredState(maid, state);
    }

    public static void clearState(EntityMaid maid) {
        JET_STATES.remove(maid);
    }

    private static void discardExpiredState(EntityMaid maid, JetState state) {
        if (!state.fallProtection && !state.horizontalImpulsePending
                && maid.level().getGameTime() >= state.cooldownUntil
                && maid.level().getGameTime() >= state.lowHealthCooldownUntil) {
            JET_STATES.remove(maid);
        }
    }

    private static boolean isBelowLowHealthThreshold(EntityMaid maid) {
        return maid.getHealth() > 0.0F && maid.getHealth() < maid.getMaxHealth() * JET_LOW_HEALTH_THRESHOLD;
    }

    private static boolean isLowHealthTriggerReady(EntityMaid maid) {
        JetState state = JET_STATES.get(maid);
        return state == null || maid.level().getGameTime() >= state.lowHealthCooldownUntil;
    }

    private static void applyHorizontalImpulse(EntityMaid maid, JetState state) {
        state.horizontalImpulsePending = false;
        Vec3 direction = state.horizontalDirection;
        state.horizontalDirection = null;
        if (!isSafeHorizontalDirection(direction)) return;

        Vec3 currentMotion = maid.getDeltaMovement();
        Vec3 separatedMotion = new Vec3(direction.x * JET_HORIZONTAL_FORCE, currentMotion.y,
                direction.z * JET_HORIZONTAL_FORCE);
        if (!isFinite(separatedMotion)) return;
        maid.setDeltaMovement(separatedMotion);
        maid.hasImpulse = true;
    }

    private static Vec3 getEscapeDirection(EntityMaid maid, DamageSource source) {
        Vec3 dangerPosition = getDangerPosition(maid, source);
        if (dangerPosition != null) {
            Vec3 away = new Vec3(maid.getX() - dangerPosition.x, 0.0D, maid.getZ() - dangerPosition.z);
            if (isSafeHorizontalDirection(away)) return normalizeHorizontal(away);
        }

        Vec3 localSafeDirection = findLocalSafeDirection(maid);
        if (localSafeDirection != null) return localSafeDirection;

        Vec3 movement = maid.getDeltaMovement();
        Vec3 reverseMovement = new Vec3(-movement.x, 0.0D, -movement.z);
        if (isSafeHorizontalDirection(reverseMovement)) return normalizeHorizontal(reverseMovement);

        Vec3 look = maid.getLookAngle();
        Vec3 reverseLook = new Vec3(-look.x, 0.0D, -look.z);
        if (isSafeHorizontalDirection(reverseLook)) return normalizeHorizontal(reverseLook);

        int fallback = Math.floorMod(maid.getUUID().hashCode(), 4);
        return LOCAL_SEARCH_DIRECTIONS[fallback];
    }

    private static Vec3 getDangerPosition(EntityMaid maid, DamageSource source) {
        if (source == null) return null;
        Entity causingEntity = source.getEntity();
        if (causingEntity != null && causingEntity != maid) return causingEntity.position();
        Entity directEntity = source.getDirectEntity();
        if (directEntity != null && directEntity != maid) return directEntity.position();
        Vec3 sourcePosition = source.getSourcePosition();
        return sourcePosition != null && isFinite(sourcePosition) ? sourcePosition : null;
    }

    private static Vec3 findLocalSafeDirection(EntityMaid maid) {
        Level level = maid.level();
        BlockPos origin = maid.blockPosition();
        Vec3 best = null;
        int bestScore = Integer.MIN_VALUE;

        for (Vec3 direction : LOCAL_SEARCH_DIRECTIONS) {
            int dx = (int) Math.round(direction.x * 3.0D);
            int dz = (int) Math.round(direction.z * 3.0D);
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos feet = origin.offset(dx, dy, dz);
                BlockPos head = feet.above();
                BlockPos support = feet.below();
                if (!level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()) continue;
                if (!level.getBlockState(head).getCollisionShape(level, head).isEmpty()) continue;
                if (level.getBlockState(support).getCollisionShape(level, support).isEmpty()) continue;
                if (isLava(level, feet) || isLava(level, head) || isLava(level, support)) continue;

                int score = 100 - Math.abs(dy) * 5;
                for (int ox = -1; ox <= 1; ox++) {
                    for (int oz = -1; oz <= 1; oz++) {
                        if (isLava(level, feet.offset(ox, 0, oz))) score -= 10;
                    }
                }
                if (score > bestScore) {
                    bestScore = score;
                    best = direction;
                }
            }
        }
        return best;
    }

    private static boolean isLava(Level level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.LAVA);
    }

    private static Vec3 normalizeHorizontal(Vec3 vector) {
        double length = Math.sqrt(vector.x * vector.x + vector.z * vector.z);
        return new Vec3(vector.x / length, 0.0D, vector.z / length);
    }

    private static boolean isSafeHorizontalDirection(Vec3 vector) {
        return vector != null && isFinite(vector)
                && vector.x * vector.x + vector.z * vector.z > MIN_HORIZONTAL_LENGTH_SQUARED;
    }

    private static boolean isFinite(Vec3 vector) {
        return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }

    private static void playEffects(EntityMaid maid) {
        if (!(maid.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(ParticleTypes.CLOUD, maid.getX(), maid.getY() + 0.2D, maid.getZ(),
                16, 0.35D, 0.15D, 0.35D, 0.08D);
        serverLevel.sendParticles(ParticleTypes.FLAME, maid.getX(), maid.getY() + 0.15D, maid.getZ(),
                8, 0.25D, 0.10D, 0.25D, 0.04D);
        serverLevel.playSound(null, maid.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.NEUTRAL, 0.8F, 1.15F);
    }

    private static final class JetState {
        private long cooldownUntil;
        private long lowHealthCooldownUntil;
        private long fallProtectionUntil;
        private boolean fallProtection;
        private long horizontalImpulseAt;
        private Vec3 horizontalDirection;
        private boolean horizontalImpulsePending;
    }
}
