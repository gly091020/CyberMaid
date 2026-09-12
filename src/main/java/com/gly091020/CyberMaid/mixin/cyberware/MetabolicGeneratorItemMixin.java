package com.gly091020.CyberMaid.mixin.cyberware;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.item.cyberware.lower_organs.MetabolicGeneratorItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MetabolicGeneratorItem.class)
public abstract class MetabolicGeneratorItemMixin {
    @Inject(method = "onSystemTick", at = @At("HEAD"), cancellable = true)
    private void cyberMaid$onSystemTick(LivingEntity wearer, ItemStack stack, CallbackInfo ci) {
        if (wearer instanceof Player) return;
        ci.cancel();

        if (!(wearer instanceof EntityMaid maid) || maid.tickCount % 20 != 0) return;

        CyberwareUserData data = maid.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        int genAmount = 200;
        if (maid.getHunger() > 6
                && data.getEnergyStored() < data.getMaxEnergyStored()
                && data.receiveEnergy(genAmount, true) == genAmount) {
            maid.setHunger(maid.getHunger() - 1);
            data.receiveEnergy(genAmount, false);
        }
    }
}
