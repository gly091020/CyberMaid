package com.gly091020.CyberMaid.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class CyberMaidNetwork {
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(MaidCyberwareTogglePacket.TYPE, MaidCyberwareTogglePacket.STREAM_CODEC, MaidCyberwareTogglePacket::handle);
    }
}
