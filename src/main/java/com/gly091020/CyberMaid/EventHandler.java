package com.gly091020.CyberMaid;

import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import static com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider.CYBERWARE_DATA;

@EventBusSubscriber
public class EventHandler {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(Capabilities.EnergyStorage.ENTITY, InitEntities.MAID.get(), (player, side) -> player.getData(CYBERWARE_DATA.get()));
    }
}
