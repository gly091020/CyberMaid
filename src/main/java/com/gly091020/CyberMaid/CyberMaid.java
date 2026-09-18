package com.gly091020.CyberMaid;

import com.gly091020.CyberMaid.network.CyberMaidNetwork;
import com.gly091020.CyberMaid.registry.CyberMaidEntities;
import com.gly091020.CyberMaid.registry.CyberMaidItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(CyberMaid.MODID)
public class CyberMaid {
    public static final String MODID = "cyber_maid";
    public CyberMaid(IEventBus modBus){
        CyberMaidItems.register(modBus);
        CyberMaidEntities.register(modBus);
        modBus.addListener(CyberMaidItems::addToCreativeTab);
        modBus.addListener(CyberMaidNetwork::register);
    }
}
