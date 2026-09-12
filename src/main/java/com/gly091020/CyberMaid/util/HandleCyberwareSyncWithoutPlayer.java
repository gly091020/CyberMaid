package com.gly091020.CyberMaid.util;

import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.network.SyncCyberwareDataPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class HandleCyberwareSyncWithoutPlayer {
    public static void syncToPlayer(ServerPlayer player, LivingEntity entity) {
        SyncCyberwareDataPacket packet = createPacket(entity);
        if (packet != null) {
            PacketDistributor.sendToPlayer(player, packet);
        }
    }

    public static void syncToTrackingPlayers(LivingEntity entity) {
        SyncCyberwareDataPacket packet = createPacket(entity);
        if (packet != null) {
            PacketDistributor.sendToPlayersTrackingEntity(entity, packet);
        }
    }

    private static SyncCyberwareDataPacket createPacket(LivingEntity entity) {
        CyberwareUserData data = entity.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        if (!data.isInitialized()) return null;

        CompoundTag tag = data.serializeNBT(entity.registryAccess());
        tag.putInt("MaxTolerance", data.getMaxTolerance(entity));
        return new SyncCyberwareDataPacket(tag, entity.getId());
    }
}
