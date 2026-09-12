package com.gly091020.CyberMaid.mixin.cyberware;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CyberMaid.util.HandleCyberwareUserDataWithoutPlayer;
import com.maxwell.cyber_ware_port.common.item.cyberware.cranium.CorticalStackItem;
import com.maxwell.cyber_ware_port.init.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CorticalStackItem.class)
public abstract class CorticalStackItemMixin {
    @Inject(method = "onLivingDeath", at = @At("HEAD"), cancellable = true)
    private void cyberMaid$onLivingDeath(LivingDeathEvent event, ItemStack stack, LivingEntity wearer, CallbackInfo ci) {
        if (wearer instanceof Player) return;
        ci.cancel();

        if (wearer.level().isClientSide || !(wearer instanceof EntityMaid maid)) return;

        int totalXp = maid.getExperience();
        if (totalXp <= 0) return;

        ItemStack capsule = new ItemStack(ModItems.EXP_CAPSULE.get());
        CompoundTag tag = new CompoundTag();
        tag.putInt("xp", totalXp);
        capsule.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        HandleCyberwareUserDataWithoutPlayer.drop(maid, capsule);
        maid.setExperience(0);
    }
}
