package com.gly091020.CyberMaid.compat.maidspell;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.yimeng261.maidspell.spell.data.MaidSlashBladeData;
import com.maxwell.cyber_ware_port.api.json.CyberwareAPI;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import net.minecraft.world.item.ItemStack;

final class MaidSpellCooldownCompat {
    private static final int COOLDOWN_UPDATE_INTERVAL = 20;
    private static final int COOLDOWN_UPDATE_AMOUNT = 20;

    private MaidSpellCooldownCompat() {
    }

    static void tick(EntityMaid maid, ItemStack stack) {
        if (maid.tickCount % COOLDOWN_UPDATE_INTERVAL != 0) return;
        if (!isFirstActiveStack(maid, stack)) return;

        MaidSlashBladeData data = MaidSlashBladeData.getOrCreate(maid.getUUID());
        if (!data.isOnCooldown()) return;

        double multiplier = SpellCooldownReducerItem.getCooldownMultiplier(maid);
        int extra = (int) Math.round(COOLDOWN_UPDATE_AMOUNT / multiplier) - COOLDOWN_UPDATE_AMOUNT;
        if (extra > 0) {
            data.setCooldown(Math.max(0, data.getCooldown() - extra));
        }
    }

    private static boolean isFirstActiveStack(EntityMaid maid, ItemStack stack) {
        CyberwareUserData data = maid.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        for (int i = 0; i < data.getInstalledCyberware().getSlots(); i++) {
            ItemStack installed = data.getInstalledCyberware().getStackInSlot(i);
            if (!installed.is(MaidSpellItems.SPELL_COOLDOWN_REDUCER.get())) continue;
            ICyberware cyberware = CyberwareAPI.getCyberware(installed);
            if (cyberware == null || !CyberwareUserData.isItemPowered(data, cyberware, installed)) continue;
            return installed == stack;
        }
        return false;
    }
}
