package com.gly091020.CyberMaid.datagen;

import com.gly091020.CyberMaid.CyberMaid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = CyberMaid.MODID)
public class CyberMaidDataGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var output = event.getGenerator().getPackOutput();
        if (event.includeClient()) {
            event.addProvider(new CyberMaidItemModelProvider(output));
        }
        if (event.includeServer()) {
            event.addProvider(new CyberMaidRecipeProvider(output, event.getLookupProvider()));
            event.addProvider(new CyberMaidItemTagProvider(output, event.getLookupProvider(), event.getExistingFileHelper()));
        }
    }
}
