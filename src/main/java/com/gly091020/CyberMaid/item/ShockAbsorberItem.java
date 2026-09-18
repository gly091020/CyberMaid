package com.gly091020.CyberMaid.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

public class ShockAbsorberItem extends MaidLegCyberwareItem {
    public static final float FALL_DAMAGE_REDUCTION = 0.60F;
    public static final float FALL_DAMAGE_MULTIPLIER = 1.0F - FALL_DAMAGE_REDUCTION;
    public static final double IMPACT_SPREAD_RADIUS = 3.0D;
    public static final double IMPACT_KNOCKBACK_STRENGTH = 2.0D;

    public ShockAbsorberItem() {
        super(legBuilder(5));
    }

    public static void spreadFallImpact(EntityMaid maid, float fallDistance, float damageMultiplier) {
        if (maid.level().isClientSide) return;
        float rawFallDamage = calculateRawFallDamage(maid, fallDistance, damageMultiplier);
        if (rawFallDamage <= 0.0F) return;

        AABB area = maid.getBoundingBox().inflate(IMPACT_SPREAD_RADIUS);
        for (LivingEntity target : maid.level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != maid && entity.isAlive() && isHostile(entity)
                        && maid.distanceToSqr(entity) <= IMPACT_SPREAD_RADIUS * IMPACT_SPREAD_RADIUS)) {
            target.hurt(maid.damageSources().fall(), rawFallDamage);
            knockAwayFromMaid(maid, target);
        }
    }

    private static float calculateRawFallDamage(EntityMaid maid, float fallDistance, float damageMultiplier) {
        if (maid.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) return 0.0F;
        double safeFallDistance = maid.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
        double fallDamageMultiplier = maid.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER);
        return Math.max(0, Mth.ceil((fallDistance - safeFallDistance)
                * damageMultiplier * fallDamageMultiplier));
    }

    private static boolean isHostile(LivingEntity entity) {
        return entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER;
    }

    private static void knockAwayFromMaid(EntityMaid maid, LivingEntity target) {
        double x = maid.getX() - target.getX();
        double z = maid.getZ() - target.getZ();
        if (x * x + z * z < 1.0E-6D) {
            int fallback = Math.floorMod(target.getUUID().hashCode(), 4);
            x = fallback == 0 ? 1.0D : fallback == 1 ? -1.0D : 0.0D;
            z = fallback == 2 ? 1.0D : fallback == 3 ? -1.0D : 0.0D;
        }
        target.knockback(IMPACT_KNOCKBACK_STRENGTH, x, z);
        target.hasImpulse = true;
    }
}
