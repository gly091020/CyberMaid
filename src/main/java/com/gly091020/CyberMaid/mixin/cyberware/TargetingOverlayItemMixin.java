package com.gly091020.CyberMaid.mixin.cyberware;

import com.gly091020.CyberMaid.api.MaidCyberwareTick;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import com.maxwell.cyber_ware_port.common.item.cyberware.eye.TargetingOverlayItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TargetingOverlayItem.class)
public abstract class TargetingOverlayItemMixin implements MaidCyberwareTick {
    @Override
    public void onMaidTick(LivingEntity wearer, ItemStack stack) {
        if (wearer instanceof Player || wearer.level().isClientSide) return;
        if (!((ICyberware) this).isActive(stack)) return;

        double range = 32.0;
        AABB area = wearer.getBoundingBox().inflate(range);
        for (LivingEntity target : wearer.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != wearer && e.isAlive())) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false));
        }
    }
}
