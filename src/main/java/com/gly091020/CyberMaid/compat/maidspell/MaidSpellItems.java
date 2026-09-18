package com.gly091020.CyberMaid.compat.maidspell;

import com.gly091020.CyberMaid.CyberMaid;
import com.maxwell.cyber_ware_port.init.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MaidSpellItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CyberMaid.MODID);

    public static final DeferredItem<SpellCooldownReducerItem> SPELL_COOLDOWN_REDUCER =
            ITEMS.register("spell_cooldown_reducer", SpellCooldownReducerItem::new);
    public static final DeferredItem<SpellManaReducerItem> SPELL_MANA_REDUCER =
            ITEMS.register("spell_mana_reducer", SpellManaReducerItem::new);
    public static final DeferredItem<SpellManaDebtItem> SPELL_MANA_DEBT =
            ITEMS.register("spell_mana_debt", SpellManaDebtItem::new);

    private MaidSpellItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(MaidSpellItems::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(ModItems.CW_TABS.getKey())) {
            event.accept(SPELL_COOLDOWN_REDUCER);
            event.accept(SPELL_MANA_REDUCER);
            event.accept(SPELL_MANA_DEBT);
        }
    }
}
