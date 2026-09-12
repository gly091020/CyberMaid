package com.gly091020.CyberMaid.mixin.cyberware;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CyberMaid.client.MaidMenuTarget;
import com.gly091020.CyberMaid.network.MaidCyberwareTogglePacket;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.maxwell.cyber_ware_port.client.upgrades.cybereye.CyberwareMenuScreen;
import com.maxwell.cyber_ware_port.common.network.ToggleCyberwarePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CyberwareMenuScreen.class)
public abstract class CyberwareMenuScreenMixin implements MaidMenuTarget {
    @Unique
    private int cyberMaid$targetId = 0;

    @Override
    public int getMaidTargetId() {
        return cyberMaid$targetId;
    }

    @Override
    public void setMaidTargetId(int id) {
        cyberMaid$targetId = id;
    }

    @Override
    public void refreshMaidMenu() {
        Minecraft minecraft = Minecraft.getInstance();
        ((CyberwareMenuScreen) (Object) this).init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    }

    @WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getData(Lnet/neoforged/neoforge/attachment/AttachmentType;)Ljava/lang/Object;"))
    private Object cyberMaid$maidData(LocalPlayer player, AttachmentType<?> type, Operation<Object> original) {
        Minecraft minecraft = Minecraft.getInstance();
        if (cyberMaid$targetId != 0 && minecraft.level != null) {
            Entity entity = minecraft.level.getEntity(cyberMaid$targetId);
            if (entity instanceof EntityMaid maid) {
                return maid.getData(type);
            }
        }
        return original.call(player, type);
    }

    @WrapOperation(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/PacketDistributor;sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;[Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"))
    private void cyberMaid$toggle(CustomPacketPayload payload, CustomPacketPayload[] payloads, Operation<Void> original) {
        if (cyberMaid$targetId != 0 && payload instanceof ToggleCyberwarePacket packet) {
            PacketDistributor.sendToServer(new MaidCyberwareTogglePacket(cyberMaid$targetId, packet.slotId()));
            return;
        }
        original.call(payload, payloads);
    }
}
