package com.gly091020.CyberMaid.api;

import com.maxwell.cyber_ware_port.common.item.base.CyberwareItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public abstract class MaidCyberwareItem extends CyberwareItem implements MaidCyberwareTick {
    public MaidCyberwareItem(Builder builder) {
        super(builder);
    }

    @Override
    public final void onPlayerTick(PlayerTickEvent.Post event, ItemStack stack, LivingEntity wearer) {

    }

    @Override
    public final void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event, ItemStack stack, LivingEntity wearer) {

    }

    @Override
    public final void onHarvestCheck(PlayerEvent.HarvestCheck event, ItemStack stack, LivingEntity wearer) {

    }

    @Override
    public final void onBreakSpeed(PlayerEvent.BreakSpeed event, ItemStack stack, LivingEntity wearer) {

    }
}
