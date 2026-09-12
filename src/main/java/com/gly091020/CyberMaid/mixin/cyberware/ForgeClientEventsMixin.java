package com.gly091020.CyberMaid.mixin.cyberware;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CyberMaid.client.MaidCyberwareMenuScreen;
import com.maxwell.cyber_ware_port.client.ForgeClientEvents;
import com.maxwell.cyber_ware_port.client.KeyInit;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.init.ModItems;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.client.event.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeClientEvents.class)
public abstract class ForgeClientEventsMixin {
    @Inject(method = "onKeyInput", at = @At("HEAD"), cancellable = true)
    private static void cyberMaid$onKeyInput(InputEvent.Key event, CallbackInfo ci) {
        if (event.getAction() != InputConstants.PRESS) return;
        if (event.getKey() != KeyInit.MENU_KEY.getKey().getValue()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) return;
        if (!(minecraft.hitResult instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof EntityMaid maid)) return;

        CyberwareUserData data = minecraft.player.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        if (!data.isCyberwareInstalled(ModItems.CYBER_EYE.get())) return;
        if (!KeyInit.MENU_KEY.consumeClick()) return;

        minecraft.setScreen(new MaidCyberwareMenuScreen(maid.getId()));
        ci.cancel();
    }
}
