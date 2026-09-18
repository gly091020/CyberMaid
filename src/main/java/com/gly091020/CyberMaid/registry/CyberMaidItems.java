package com.gly091020.CyberMaid.registry;

import com.gly091020.CyberMaid.CyberMaid;
import com.gly091020.CyberMaid.item.EmergencyJetEscapeItem;
import com.gly091020.CyberMaid.item.HydraulicJointsItem;
import com.gly091020.CyberMaid.item.PoweredProstheticLegsItem;
import com.gly091020.CyberMaid.item.ShockAbsorberItem;
import com.maxwell.cyber_ware_port.init.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CyberMaidItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CyberMaid.MODID);

    public static final DeferredItem<PoweredProstheticLegsItem> POWERED_PROSTHETIC_LEGS =
            ITEMS.register("powered_prosthetic_legs", PoweredProstheticLegsItem::new);
    public static final DeferredItem<HydraulicJointsItem> HYDRAULIC_JOINTS =
            ITEMS.register("hydraulic_joints", HydraulicJointsItem::new);
    public static final DeferredItem<ShockAbsorberItem> SHOCK_ABSORBER =
            ITEMS.register("shock_absorber", ShockAbsorberItem::new);
    public static final DeferredItem<EmergencyJetEscapeItem> EMERGENCY_JET_ESCAPE =
            ITEMS.register("emergency_jet_escape", EmergencyJetEscapeItem::new);
    public static final DeferredItem<DeferredSpawnEggItem> CYBER_FAIRY_SPAWN_EGG =
            ITEMS.register("cyber_fairy_spawn_egg", () -> new DeferredSpawnEggItem(
                    CyberMaidEntities.CYBER_FAIRY, 0x2E2A3A, 0x6FE3FF,
                    new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));

    private CyberMaidItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(ModItems.CW_TABS.getKey())) {
            event.accept(POWERED_PROSTHETIC_LEGS);
            event.accept(HYDRAULIC_JOINTS);
            event.accept(SHOCK_ABSORBER);
            event.accept(EMERGENCY_JET_ESCAPE);
            ItemStack lastCyberEgg = new ItemStack(ModItems.CYBER_WITHER_SPAWN_EGG.get());
            if (hasEntry(event, lastCyberEgg)) {
                event.insertAfter(lastCyberEgg, new ItemStack(CYBER_FAIRY_SPAWN_EGG.get()),
                        CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            } else {
                event.accept(CYBER_FAIRY_SPAWN_EGG);
            }
        } else if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) {
            event.accept(CYBER_FAIRY_SPAWN_EGG);
        }
    }

    private static boolean hasEntry(BuildCreativeModeTabContentsEvent event, ItemStack stack) {
        for (ItemStack entry : event.getParentEntries()) {
            if (ItemStack.isSameItemSameComponents(entry, stack)) return true;
        }
        return false;
    }
}
