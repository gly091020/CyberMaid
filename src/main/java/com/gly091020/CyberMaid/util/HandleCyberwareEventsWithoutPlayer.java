package com.gly091020.CyberMaid.util;

import com.maxwell.cyber_ware_port.api.json.CyberwareAPI;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.function.BiConsumer;

public class HandleCyberwareEventsWithoutPlayer {
    public static void dispatch(LivingEntity entity, BiConsumer<ICyberware, ItemStack> action) {
        if (entity == null || !entity.hasData(CyberwareCapabilityProvider.CYBERWARE_DATA)) return;
        CyberwareUserData data = entity.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        ItemStackHandler handler = data.getInstalledCyberware();

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            ICyberware cyberware = CyberwareAPI.getCyberware(stack);
            if (!stack.isEmpty() && cyberware != null && CyberwareUserData.isItemPowered(data, cyberware, stack)) {
                action.accept(cyberware, stack);
            }
        }
    }
}
