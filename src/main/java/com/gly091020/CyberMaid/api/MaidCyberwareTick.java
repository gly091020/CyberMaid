package com.gly091020.CyberMaid.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 玩家以外生物的 cyberware 每 tick 扩展入口。
 */
public interface MaidCyberwareTick {
    void onMaidTick(LivingEntity wearer, ItemStack stack);
}
