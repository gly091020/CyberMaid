package com.gly091020.CyberMaid.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidAndItemTransformEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.gly091020.CyberMaid.util.HandleCyberwareEventsWithoutPlayer;
import com.gly091020.CyberMaid.util.HandleCyberwareSyncWithoutPlayer;
import com.gly091020.CyberMaid.util.HandleCyberwareUserDataWithoutPlayer;
import com.maxwell.cyber_ware_port.CyberWare;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.item.base.BodyPartType;
import com.maxwell.cyber_ware_port.common.util.CyberwareBodyStatus;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Collection;
import java.util.Iterator;

@EventBusSubscriber
public class MaidCapabilityEvents {
    @SubscribeEvent
    public static void onPlayerLoggedIn(EntityJoinLevelEvent event) {
        if(event.getEntity() instanceof EntityMaid maid){
            CyberwareUserData cap = maid.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
            if (!cap.isInitialized()) {
                cap.fillWithHumanParts();
            }
            HandleCyberwareUserDataWithoutPlayer.recalculateCapacity(maid, cap);
        }
    }

    @SubscribeEvent
    public static void initMaidData(MaidAndItemTransformEvent.ToMaid event){
        if(event.getItem().is(InitItems.FILM)) {
            var data = event.getData();
            if (!data.contains("neoforge:attachments")) return;
            var a = data.getCompound("neoforge:attachments");
            var key = CyberwareCapabilityProvider.CYBERWARE_DATA.getId().toString();
            if (!a.contains(key)) return;

            // 手动复原改造数据
            // 943的神秘代码
            var cyberwareUserData = new CyberwareUserData();
            cyberwareUserData.deserializeNBT(event.getMaid().registryAccess(), a.getCompound(key));
            event.getMaid().setData(CyberwareCapabilityProvider.CYBERWARE_DATA.get(), cyberwareUserData);
        }
        var data = event.getMaid().getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        HandleCyberwareUserDataWithoutPlayer.recalculateCapacity(event.getMaid(), data);
    }

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof EntityMaid) {
            Collection<ItemEntity> drops = event.getDrops();
            Iterator<ItemEntity> iterator = drops.iterator();

            while(iterator.hasNext()) {
                ItemStack stack = iterator.next().getItem();
                if (stack.getOrDefault(CyberWare.GHOST_COMPONENT, false)) {
                    iterator.remove();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof EntityMaid maid) {
            CyberwareUserData data = maid.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
            CyberwareBodyStatus status = new CyberwareBodyStatus(data.getInstalledCyberware());
            if (!status.hasPart(BodyPartType.SKIN)) {
                event.setAmount(event.getAmount() * 1.5F);
            }
        }

        if (!(entity instanceof Player) && !entity.level().isClientSide) {
            HandleCyberwareEventsWithoutPlayer.dispatch(entity, (cyberware, stack) -> {
                if (!event.isCanceled()) {
                    cyberware.onLivingIncomingDamage(event, stack, entity);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide) return;
        double range = 16.0;
        AABB area = new AABB(
                event.getTargetX() - range, event.getTargetY() - range, event.getTargetZ() - range,
                event.getTargetX() + range, event.getTargetY() + range, event.getTargetZ() + range
        );
        for (LivingEntity wearer : event.getEntity().level().getEntitiesOfClass(LivingEntity.class, area, e -> !(e instanceof Player))) {
            HandleCyberwareEventsWithoutPlayer.dispatch(wearer, (cyberware, stack) -> cyberware.onEntityTeleport(event, stack, wearer));
            if (event.isCanceled()) return;
        }
    }

    @SubscribeEvent
    public static void onLivingTickPost(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (living instanceof Player) return;
        if(!(living instanceof EntityMaid))return;  // 暂时性代码
        if (!living.hasData(CyberwareCapabilityProvider.CYBERWARE_DATA)) return;

        CyberwareUserData data = living.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        if (!data.isInitialized()) return;
        HandleCyberwareUserDataWithoutPlayer.tick(living, data);
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof EntityMaid maid && event.getEntity() instanceof ServerPlayer player) {
            HandleCyberwareSyncWithoutPlayer.syncToPlayer(player, maid);
        }
    }
}
