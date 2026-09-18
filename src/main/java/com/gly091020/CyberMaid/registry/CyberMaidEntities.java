package com.gly091020.CyberMaid.registry;

import com.gly091020.CyberMaid.CyberMaid;
import com.gly091020.CyberMaid.entity.CyberFairy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CyberMaidEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, CyberMaid.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<CyberFairy>> CYBER_FAIRY =
            ENTITY_TYPES.register("cyber_fairy", () -> EntityType.Builder.of(CyberFairy::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(CyberMaid.MODID + ":cyber_fairy"));

    private CyberMaidEntities() {
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
