package com.gly091020.CyberMaid.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CyberMaid.util.HandleRobosurgeonBlockEntityWithoutPlayer;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.RobosurgeonBlockEntity;
import com.maxwell.cyber_ware_port.common.container.RobosurgeonMenu;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RobosurgeonMenu.class)
public class RobosurgeonMenuMixin {
    @Shadow
    @Final
    public RobosurgeonBlockEntity blockEntity;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/inventory/ContainerData;)V", at = @At("RETURN"))
    public void addData(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data, CallbackInfo ci){
        if (inv.player.level().isClientSide)return;
        if (entity.getLevel() == null)return;
        var maid = entity.getLevel().getEntities(EntityTypeTest.forClass(EntityMaid.class), AABB.encapsulatingFullBlocks(entity.getBlockPos().below(), entity.getBlockPos().below(2)), Entity::isAlive);
        if (maid.isEmpty()) return;
        HandleRobosurgeonBlockEntityWithoutPlayer.populateGhostItems(maid.getFirst(), blockEntity);
    }
}
