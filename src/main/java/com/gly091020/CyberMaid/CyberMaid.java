package com.gly091020.CyberMaid;

import com.gly091020.CyberMaid.network.CyberMaidNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(CyberMaid.MODID)
public class CyberMaid {
    public static final String MODID = "cyber_maid";
    public CyberMaid(IEventBus modBus, ModContainer container){
        modBus.addListener(CyberMaidNetwork::register);
    }
}
