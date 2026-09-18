package com.gly091020.CyberMaid.item;

import com.gly091020.CyberMaid.util.MaidLegCyberwareAttributes;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class PoweredProstheticLegsItem extends MaidLegCyberwareItem {
    public static final String MOVEMENT_SPEED_MODIFIER_PATH = "cyber_maid_powered_prosthetic_legs_speed";
    public static final double MOVEMENT_SPEED_BONUS = 0.10D;
    public static final int ENERGY_PER_SECOND = 4;

    public PoweredProstheticLegsItem() {
        super(legBuilder(5)
                .energy(ENERGY_PER_SECOND, 0, 0, ICyberware.StackingRule.STATIC)
                .addAttribute(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_MODIFIER_PATH,
                        MOVEMENT_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    public boolean canToggle(ItemStack stack) {
        return true;
    }

    @Override
    public void onRemoved(LivingEntity wearer, ItemStack stack) {
        MaidLegCyberwareAttributes.removePoweredLegs(wearer);
    }
}
