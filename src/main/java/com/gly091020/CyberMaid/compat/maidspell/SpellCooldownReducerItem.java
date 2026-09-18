package com.gly091020.CyberMaid.compat.maidspell;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CyberMaid.api.MaidCyberwareItem;
import com.gly091020.CyberMaid.compat.CompatMods;
import com.maxwell.cyber_ware_port.api.json.CyberwareAPI;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.RobosurgeonBlockEntity;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public class SpellCooldownReducerItem extends MaidCyberwareItem {
    public static final int MAX_INSTALLS = 9;
    public static final double COOLDOWN_REDUCTION_PER_INSTALL = 0.10D;
    public static final int ENERGY_PER_SECOND = 1;

    public SpellCooldownReducerItem() {
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
        if (!(wearer instanceof EntityMaid maid) || maid.level().isClientSide) return;
        if (!CompatMods.MAID_SPELL.isLoaded()) return;
        MaidSpellCooldownCompat.tick(maid, stack);
    }

    public static int getInstalledCount(EntityMaid maid) {
        if (!maid.hasData(CyberwareCapabilityProvider.CYBERWARE_DATA)) return 0;
        CyberwareUserData data = maid.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        int count = 0;
        for (int i = 0; i < data.getInstalledCyberware().getSlots(); i++) {
            ItemStack stack = data.getInstalledCyberware().getStackInSlot(i);
            if (!stack.is(MaidSpellItems.SPELL_COOLDOWN_REDUCER.get())) continue;
            ICyberware cyberware = CyberwareAPI.getCyberware(stack);
            if (cyberware != null && CyberwareUserData.isItemPowered(data, cyberware, stack)) {
                count += stack.getCount();
            }
        }
        return Math.min(count, MAX_INSTALLS);
    }

    public static double getCooldownMultiplier(EntityMaid maid) {
        return Math.pow(1.0D - COOLDOWN_REDUCTION_PER_INSTALL, getInstalledCount(maid));
    }

    public static int scaleCooldown(EntityMaid maid, int cooldownTicks) {
        if (cooldownTicks <= 0) return 0;
        return (int) Math.ceil(cooldownTicks * getCooldownMultiplier(maid));
    }

    public static int scaleAttackInterval(EntityMaid maid, int intervalTicks) {
        if (intervalTicks <= 1) return 1;
        return Math.max(1, (int) Math.ceil(intervalTicks * getCooldownMultiplier(maid)));
    }
}
