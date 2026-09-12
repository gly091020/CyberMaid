package com.gly091020.CyberMaid.mixin.cyberware;

import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.item.base.CyberwareItem;
import com.maxwell.cyber_ware_port.common.item.cyberware.cranium.ThreatMatrixItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThreatMatrixItem.class)
public abstract class ThreatMatrixItemMixin {
    @Inject(method = "onLivingIncomingDamage", at = @At("HEAD"), cancellable = true)
    private void cyberMaid$onLivingIncomingDamage(LivingIncomingDamageEvent event, ItemStack stack, LivingEntity wearer, CallbackInfo ci) {
        if (wearer instanceof Player) return;
        ci.cancel();

        if (event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)
                || event.getSource().is(DamageTypeTags.IS_FIRE)
                || event.getSource().is(DamageTypeTags.IS_FALL)) {
            return;
        }

        boolean isLightlyArmored = wearer.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
                && wearer.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
        if (!isLightlyArmored || wearer.getRandom().nextFloat() >= 0.3F) return;

        IEnergyStorage energyStorage = wearer.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        if (((CyberwareItem) (Object) this).tryConsumeEventEnergy(energyStorage, stack)) {
            event.setCanceled(true);
            wearer.level().playSound(null, wearer.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 2.0F);
        }
    }
}
