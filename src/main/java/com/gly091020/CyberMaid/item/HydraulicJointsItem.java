package com.gly091020.CyberMaid.item;

import com.gly091020.CyberMaid.util.MaidLegCyberwareAttributes;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class HydraulicJointsItem extends MaidLegCyberwareItem {
    public static final String STEP_HEIGHT_MODIFIER_PATH = "cyber_maid_hydraulic_joints_step_height";
    public static final double STEP_HEIGHT_BONUS = 2.40D;
    public static final int ENERGY_PER_SECOND = 2;

    public HydraulicJointsItem() {
        super(legBuilder(5)
                .energy(ENERGY_PER_SECOND, 0, 0, ICyberware.StackingRule.STATIC)
                .addAttribute(Attributes.STEP_HEIGHT, STEP_HEIGHT_MODIFIER_PATH,
                        STEP_HEIGHT_BONUS, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    public boolean canToggle(ItemStack stack) {
        return true;
    }

    @Override
    public void onRemoved(LivingEntity wearer, ItemStack stack) {
        MaidLegCyberwareAttributes.removeHydraulicJoints(wearer);
    }
}
