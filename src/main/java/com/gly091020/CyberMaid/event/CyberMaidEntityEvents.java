package com.gly091020.CyberMaid.event;

import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import com.gly091020.CyberMaid.CyberMaid;
import com.gly091020.CyberMaid.entity.CyberFairy;
import com.gly091020.CyberMaid.registry.CyberMaidEntities;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = CyberMaid.MODID)
public class CyberMaidEntityEvents {
    // 女仆妖精刷出时替换成赛博妖精的概率
    private static final float CYBER_FAIRY_CHANCE = 0.25F;

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(CyberMaidEntities.CYBER_FAIRY.get(), CyberFairy.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(CyberMaidEntities.CYBER_FAIRY.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) ->
                        EntityFairy.checkFairySpawnRules(EntityFairy.TYPE, level, spawnType, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    // 女仆妖精刷出时有 25% 变成赛博妖精
    @SubscribeEvent
    public static void replaceFairySpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof EntityFairy fairy) || fairy instanceof CyberFairy) return;
        if (fairy.getRandom().nextFloat() >= CYBER_FAIRY_CHANCE) return;

        CyberFairy cyberFairy = CyberMaidEntities.CYBER_FAIRY.get().create(fairy.level());
        if (cyberFairy == null) return;

        cyberFairy.moveTo(fairy.getX(), fairy.getY(), fairy.getZ(), fairy.getYRot(), fairy.getXRot());
        cyberFairy.finalizeSpawn(event.getLevel(), event.getDifficulty(), event.getSpawnType(), event.getSpawnData());
        fairy.level().addFreshEntity(cyberFairy);
        fairy.discard();
        event.setCanceled(true);
    }
}
