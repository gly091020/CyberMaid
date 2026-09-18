package com.gly091020.CyberMaid.compat.maidspell;

import com.gly091020.CyberMaid.api.MaidCyberwareItem;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.RobosurgeonBlockEntity;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class SpellManaReducerItem extends MaidCyberwareItem {
    public static final int MAX_INSTALLS = 9;
    public static final double MANA_REDUCTION_PER_INSTALL = 0.10D;
    public static final int ENERGY_PER_SECOND = 1;

    public SpellManaReducerItem() {
        super(new Builder(2, RobosurgeonBlockEntity.SLOT_BRAIN)
                .maxInstall(MAX_INSTALLS)
                .energy(ENERGY_PER_SECOND, 0, 0, ICyberware.StackingRule.LINEAR));
    }

    @Override
    public boolean canToggle(ItemStack stack) {
        return true;
    }

    @Override
    public void onMaidTick(LivingEntity wearer, ItemStack stack) {
    }
}
