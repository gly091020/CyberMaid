package com.gly091020.CyberMaid.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CyberMaid.api.MaidCyberwareItem;
import com.maxwell.cyber_ware_port.api.json.CyberwareAPI;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.RobosurgeonBlockEntity;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import com.maxwell.cyber_ware_port.init.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class MaidLegCyberwareItem extends MaidCyberwareItem {
    protected MaidLegCyberwareItem(Builder builder) {
        super(builder);
    }

    protected static Builder legBuilder(int essenceCost) {
        return new Builder(essenceCost, RobosurgeonBlockEntity.SLOT_LEGS)
                .maxInstall(1)
                .requires(ModItems.CYBER_LEG_LEFT, ModItems.CYBER_LEG_RIGHT);
    }

    public static ItemStack findOperationalStack(EntityMaid maid, Item item) {
        if (!maid.hasData(CyberwareCapabilityProvider.CYBERWARE_DATA)) return ItemStack.EMPTY;
        CyberwareUserData data = maid.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        for (int i = 0; i < data.getInstalledCyberware().getSlots(); i++) {
            ItemStack stack = data.getInstalledCyberware().getStackInSlot(i);
            if (!stack.is(item)) continue;
            ICyberware cyberware = CyberwareAPI.getCyberware(stack);
            if (cyberware != null && CyberwareUserData.isItemPowered(data, cyberware, stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onMaidTick(LivingEntity wearer, ItemStack stack) {
    }
}
