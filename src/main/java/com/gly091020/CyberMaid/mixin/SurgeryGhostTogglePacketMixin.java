package com.gly091020.CyberMaid.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.maxwell.cyber_ware_port.CyberWare;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.RobosurgeonBlockEntity;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.network.SurgeryGhostTogglePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(SurgeryGhostTogglePacket.class)
public class SurgeryGhostTogglePacketMixin {
    @Shadow
    @Final
    private BlockPos pos;

    @Shadow
    @Final
    private int slotId;

    @WrapOperation(method = "handle", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/handling/IPayloadContext;enqueueWork(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;"))
    public CompletableFuture<Void> handleMaid(IPayloadContext instance, Runnable runnable, Operation<CompletableFuture<Void>> original, @Local(name = "ctx") IPayloadContext ctx){
        return instance.enqueueWork(() -> {
            Player p = ctx.player();
            BlockEntity be = p.level().getBlockEntity(pos);
            if (!(be instanceof RobosurgeonBlockEntity tile))return;
            if(tile.getLevel() == null)return;

            LivingEntity player = null;
            var maid = tile.getLevel().getEntities(EntityTypeTest.forClass(EntityMaid.class), AABB.encapsulatingFullBlocks(tile.getBlockPos().below(), tile.getBlockPos().below(2)), Entity::isAlive);
            if(!maid.isEmpty()){
                player = maid.getFirst();
            }

            if (player == null && p instanceof ServerPlayer serverPlayer) {
                player = serverPlayer;
            }

            if(player == null)return;
            if (player.level().isLoaded(pos)) {
                ItemStackHandler itemHandler = tile.getItemHandler();
                ItemStack currentStack = itemHandler.getStackInSlot(slotId);
                boolean changed = false;
                if (!currentStack.isEmpty() && currentStack.getOrDefault(CyberWare.GHOST_COMPONENT, false)) {
                    itemHandler.setStackInSlot(slotId, ItemStack.EMPTY);
                    changed = true;
                } else if (currentStack.isEmpty()) {
                    CyberwareUserData data = player.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
                    ItemStackHandler body = data.getInstalledCyberware();
                    ItemStack installed = body.getStackInSlot(slotId);
                    if (!installed.isEmpty()) {
                        ItemStack ghost = installed.copy();
                        ghost.set(CyberWare.GHOST_COMPONENT, true);
                        itemHandler.setStackInSlot(slotId, ghost);
                        changed = true;
                    }
                }
                if (changed) {
                    tile.setChanged();
                    player.level().sendBlockUpdated(this.pos, tile.getBlockState(), tile.getBlockState(), 3);
                }
            }
        });
    }
}
