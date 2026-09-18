package com.gly091020.CyberMaid.compat.maidspell;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CyberMaid.compat.CompatMods;
import com.maxwell.cyber_ware_port.api.json.CyberwareAPI;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class MaidSpellManaCompat {
    private MaidSpellManaCompat() {
    }

    public static int getActiveInstallCount(EntityMaid maid, Item item) {
        if (!maid.hasData(CyberwareCapabilityProvider.CYBERWARE_DATA)) return 0;
        CyberwareUserData data = maid.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        int count = 0;
        for (int i = 0; i < data.getInstalledCyberware().getSlots(); i++) {
            ItemStack stack = data.getInstalledCyberware().getStackInSlot(i);
            if (!stack.is(item)) continue;
            ICyberware cyberware = CyberwareAPI.getCyberware(stack);
            if (cyberware != null && CyberwareUserData.isItemPowered(data, cyberware, stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static double getSpellManaMultiplier(EntityMaid maid) {
        int count = Math.min(getActiveInstallCount(maid, MaidSpellItems.SPELL_MANA_REDUCER.get()),
                SpellManaReducerItem.MAX_INSTALLS);
        return Math.pow(1.0D - SpellManaReducerItem.MANA_REDUCTION_PER_INSTALL, count);
    }

    public static int scaleSpellManaCost(EntityMaid maid, int manaCost) {
        if (manaCost <= 0) return 0;
        return (int) Math.ceil(manaCost * getSpellManaMultiplier(maid));
    }

    public static void tickMaidSpellMana(EntityMaid maid) {
        if (CompatMods.IRONS_SPELLBOOKS.isLoaded()) IronsSpellManaCompat.tickMana(maid);
        if (CompatMods.ARS_NOUVEAU.isLoaded() && CompatMods.MAID_SPELL.isLoaded()) ArsSpellManaCompat.tickMana(maid);
    }

    public static void registerManaCapability(RegisterCapabilitiesEvent event) {
        if (!CompatMods.ARS_NOUVEAU.isLoaded()) return;
        ArsSpellManaCompat.registerCapability(event);
    }

    public static boolean canCastMaidSpell(EntityMaid maid, int manaCost) {
        return !CompatMods.IRONS_SPELLBOOKS.isLoaded() || IronsSpellManaCompat.canCast(maid, manaCost);
    }

    public static void consumeMaidSpellMana(EntityMaid maid, int manaCost) {
        if (!CompatMods.IRONS_SPELLBOOKS.isLoaded()) return;
        IronsSpellManaCompat.consume(maid, manaCost);
    }

    public static boolean canCastMaidArsSpell(EntityMaid maid, int manaCost) {
        return !CompatMods.ARS_NOUVEAU.isLoaded() || ArsSpellManaCompat.canCast(maid, manaCost);
    }

    public static boolean consumeMaidArsSpellMana(EntityMaid maid, int manaCost) {
        return !CompatMods.ARS_NOUVEAU.isLoaded() || ArsSpellManaCompat.tryConsume(maid, manaCost);
    }

    public static void syncMaidSpellManaData(EntityMaid maid) {
        if (!CompatMods.IRONS_SPELLBOOKS.isLoaded()) return;
        IronsSpellManaCompat.syncCastingData(maid);
    }

    static boolean hasManaDebt(EntityMaid maid) {
        return getActiveInstallCount(maid, MaidSpellItems.SPELL_MANA_DEBT.get()) > 0;
    }

    static float manaFloor(EntityMaid maid, int maxMana) {
        return hasManaDebt(maid) ? -maxMana : 0.0F;
    }
}
