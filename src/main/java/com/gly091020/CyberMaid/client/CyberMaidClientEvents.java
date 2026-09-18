package com.gly091020.CyberMaid.client;

import com.gly091020.CyberMaid.CyberMaid;
import com.gly091020.CyberMaid.client.renderer.CyberFairyRenderer;
import com.gly091020.CyberMaid.registry.CyberMaidEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = CyberMaid.MODID, value = Dist.CLIENT)
public class CyberMaidClientEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CyberMaidEntities.CYBER_FAIRY.get(), CyberFairyRenderer::new);
    }
}
