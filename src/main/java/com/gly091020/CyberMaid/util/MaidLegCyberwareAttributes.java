package com.gly091020.CyberMaid.util;

import com.gly091020.CyberMaid.item.HydraulicJointsItem;
import com.gly091020.CyberMaid.item.PoweredProstheticLegsItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.ArrayList;
import java.util.List;

public final class MaidLegCyberwareAttributes {
    private static final String CYBERWARE_NAMESPACE = "cyber_ware_port";

    private MaidLegCyberwareAttributes() {
    }

    public static void removeAll(LivingEntity maid) {
        removeByPathPrefix(maid, PoweredProstheticLegsItem.MOVEMENT_SPEED_MODIFIER_PATH + "_slot_");
        removeByPathPrefix(maid, HydraulicJointsItem.STEP_HEIGHT_MODIFIER_PATH + "_slot_");
    }

    public static void removePoweredLegs(LivingEntity maid) {
        removeByPathPrefix(maid, PoweredProstheticLegsItem.MOVEMENT_SPEED_MODIFIER_PATH + "_slot_");
    }

    public static void removeHydraulicJoints(LivingEntity maid) {
        removeByPathPrefix(maid, HydraulicJointsItem.STEP_HEIGHT_MODIFIER_PATH + "_slot_");
    }

    private static void removeByPathPrefix(LivingEntity entity, String pathPrefix) {
        for (AttributeInstance instance : entity.getAttributes().getSyncableAttributes()) {
            List<ResourceLocation> removals = new ArrayList<>();
            instance.getModifiers().forEach(modifier -> {
                ResourceLocation id = modifier.id();
                if (CYBERWARE_NAMESPACE.equals(id.getNamespace()) && id.getPath().startsWith(pathPrefix)) {
                    removals.add(id);
                }
            });
            removals.forEach(instance::removeModifier);
        }
    }
}
