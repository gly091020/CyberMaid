package com.gly091020.CyberMaid.mixin;

import com.maxwell.cyber_ware_port.api.event.CyberwareEvents;
import com.maxwell.cyber_ware_port.common.block.charger.ChargerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChargerBlockEntity.class)
public abstract class ChargerBlockEntityMixin {
    @Shadow
    private boolean isDrainMode;

    @Shadow
    protected abstract void modifyEnergy(int amount);

    @Shadow
    public abstract IEnergyStorage getEnergyStorage();

    @Inject(method = "handlePlayerEnergyTransfer", at = @At("RETURN"))
    public void chargeMaid(Level level, BlockPos pos, CallbackInfo ci){
        AABB area = (new AABB(pos)).inflate(0.2, 1.0F, 0.2);

        for(LivingEntity player : level.getEntitiesOfClass(LivingEntity.class, area)) {
            CyberwareEvents.Recharge event = new CyberwareEvents.Recharge(player, (ChargerBlockEntity)(Object)this, isDrainMode);
            NeoForge.EVENT_BUS.post(event);
            if (!event.isCanceled()) {
                IEnergyStorage userData = player.getCapability(Capabilities.EnergyStorage.ENTITY, null);
                if (userData != null) {
                    int maxTransfer = 10000;
                    if (isDrainMode) {
                        int extracted = userData.extractEnergy(maxTransfer, true);
                        int space = getEnergyStorage().getMaxEnergyStored() - getEnergyStorage().getEnergyStored();
                        int toReceive = Math.min(extracted, space);
                        if (toReceive > 0) {
                            userData.extractEnergy(toReceive, false);
                            modifyEnergy(toReceive);
                        }
                    } else {
                        int available = getEnergyStorage().getEnergyStored();
                        int received = userData.receiveEnergy(Math.min(available, maxTransfer), true);
                        if (received > 0) {
                            modifyEnergy(-received);
                            userData.receiveEnergy(received, false);
                        }
                    }
                }
            }
        }
    }
}
