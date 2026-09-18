package com.gly091020.CyberMaid.mixin.cyberware;

import com.gly091020.CyberMaid.api.MaidCyberwareItem;
import com.gly091020.CyberMaid.util.HandleRobosurgeonBlockEntityWithoutPlayer;
import com.maxwell.cyber_ware_port.api.json.CyberwareAPI;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.RobosurgeonBlockEntity;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.surgeon.SurgeryManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RobosurgeonBlockEntity.class)
public class RobosurgeonMaidItemMixin {
    @Inject(method = "checkRequirements", at = @At("HEAD"), cancellable = true)
    private void cyberMaid$checkRequirements(ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        ItemStackHandler handler = ((RobosurgeonBlockEntity) (Object) this).getItemHandler();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()
                    && !SurgeryManager.isGhost(stack)
                    && CyberwareAPI.getCyberware(stack) instanceof MaidCyberwareItem) {
                if (HandleRobosurgeonBlockEntityWithoutPlayer.shouldNotify(player.level())) {
                    player.sendSystemMessage(Component.translatable("cyberware.risk.surgery_blocked",
                            Component.translatable("cyberware.risk.maid_only")).withStyle(ChatFormatting.RED));
                }
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
