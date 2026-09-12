package com.gly091020.CyberMaid.network;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CyberMaid.util.HandleCyberwareEventsWithoutPlayer;
import com.gly091020.CyberMaid.util.HandleCyberwareSyncWithoutPlayer;
import com.gly091020.CyberMaid.util.HandleCyberwareUserDataWithoutPlayer;
import com.maxwell.cyber_ware_port.api.json.CyberwareAPI;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.item.base.BodyPartType;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MaidCyberwareTogglePacket(int maidId, int slotId) implements CustomPacketPayload {
    public static final Type<MaidCyberwareTogglePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("cyber_maid", "toggle_maid_cyberware"));
    public static final StreamCodec<ByteBuf, MaidCyberwareTogglePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MaidCyberwareTogglePacket::maidId,
            ByteBufCodecs.VAR_INT, MaidCyberwareTogglePacket::slotId,
            MaidCyberwareTogglePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            EntityMaid maid = HandleCyberwareEventsWithoutPlayer.findMaid(player, maidId, false);
            if (maid == null || !HandleCyberwareEventsWithoutPlayer.hasCyberEye(player)) return;

            CyberwareUserData data = maid.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
            if (slotId < 0 || slotId >= data.getInstalledCyberware().getSlots()) return;

            ItemStack stack = data.getInstalledCyberware().getStackInSlot(slotId);
            ICyberware cyberware = CyberwareAPI.getCyberware(stack);
            if (stack.isEmpty() || cyberware == null || !cyberware.canToggle(stack)) return;

            if (!cyberware.isActive(stack)) {
                for (int i = 0; i < data.getInstalledCyberware().getSlots(); i++) {
                    if (i == slotId) continue;
                    ItemStack other = data.getInstalledCyberware().getStackInSlot(i);
                    ICyberware otherCyberware = CyberwareAPI.getCyberware(other);
                    if (!other.isEmpty() && otherCyberware != null && otherCyberware.isActive(other)
                            && (cyberware.getBodyPartType(stack) != BodyPartType.NONE && cyberware.getBodyPartType(stack) == otherCyberware.getBodyPartType(other)
                            || cyberware.isIncompatible(stack, other)
                            || otherCyberware.isIncompatible(other, stack))) {
                        player.sendSystemMessage(Component.translatable("cyberware.message.conflict_active").withStyle(ChatFormatting.RED));
                        return;
                    }
                }
            }

            cyberware.toggle(stack);
            HandleCyberwareUserDataWithoutPlayer.recalculateCapacity(maid, data);
            HandleCyberwareSyncWithoutPlayer.syncToTrackingPlayers(maid);
        });
    }
}
