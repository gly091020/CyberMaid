package com.gly091020.CyberMaid.api;

import com.maxwell.cyber_ware_port.common.item.base.CyberwareItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

public abstract class MaidCyberwareItem extends CyberwareItem implements MaidCyberwareTick {
    public MaidCyberwareItem(Builder builder) {
        super(builder);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("cyberware.tooltip.maid_only").withStyle(ChatFormatting.GRAY));
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
