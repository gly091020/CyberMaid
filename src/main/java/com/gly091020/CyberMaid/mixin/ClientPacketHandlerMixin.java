package com.gly091020.CyberMaid.mixin;

import com.gly091020.CyberMaid.client.MaidMenuTarget;
import com.maxwell.cyber_ware_port.client.upgrades.cybereye.CyberwareMenuScreen;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.network.ClientPacketHandler;
import com.maxwell.cyber_ware_port.common.network.SyncCyberwareDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketHandler.class)
public class ClientPacketHandlerMixin {
    @Inject(method = "handleSyncPacket", at = @At("HEAD"), cancellable = true)
    private static void cyberMaid$handleSyncPacket(SyncCyberwareDataPacket msg, IPayloadContext ctx, CallbackInfo ci) {
        ci.cancel();
        ctx.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) return;

            Entity entity = minecraft.level.getEntity(msg.entityId());
            if (entity instanceof LivingEntity living) {
                CyberwareUserData data = living.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
                data.deserializeNBT(living.registryAccess(), msg.data());
            }

            if (minecraft.screen instanceof CyberwareMenuScreen screen
                    && ((MaidMenuTarget) screen).getMaidTargetId() == msg.entityId()) {
                ((MaidMenuTarget) screen).refreshMaidMenu();
            }
        });
    }
}
