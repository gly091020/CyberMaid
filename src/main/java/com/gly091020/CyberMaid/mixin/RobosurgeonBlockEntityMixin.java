package com.gly091020.CyberMaid.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CyberMaid.util.HandleRobosurgeonBlockEntityWithoutPlayer;
import com.gly091020.CyberMaid.util.MixinUtil;
import com.llamalad7.mixinextras.sugar.Local;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.RobosurgeonBlockEntity;
import com.maxwell.cyber_ware_port.common.block.surgerychamber.SurgeryChamberBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RobosurgeonBlockEntity.class)
public class RobosurgeonBlockEntityMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lcom/maxwell/cyber_ware_port/common/block/robosurgeon/RobosurgeonBlockEntity;findPatient(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/entity/LivingEntity;"), cancellable = true)
    private static void handleLivingEntity(Level level, BlockPos pos, BlockState state, RobosurgeonBlockEntity entity, CallbackInfo ci, @Local(name = "chamberPos") BlockPos chamberPos, @Local(name = "chamber") SurgeryChamberBlockEntity chamber){
        var extraBE = MixinUtil.extraRobosurgeonBlockEntity(entity);
        LivingEntity patient = extraBE.invokeFindPatient(chamberPos);
        if (!chamber.isOpen() && patient instanceof EntityMaid maid && maid.getOwner() != null) {
            ci.cancel();
            boolean wantsSurgery = HandleRobosurgeonBlockEntityWithoutPlayer.needsSurgery(maid, entity);
            if (wantsSurgery && HandleRobosurgeonBlockEntityWithoutPlayer.checkRequirements(maid, entity)) {
                extraBE.setProgress(extraBE.getProgress() + 1);
                entity.setChanged();
                if (extraBE.getProgress() % 20 == 0) {
                    maid.hurt(level.damageSources().magic(), 1.0F);
                    level.playSound(null, chamberPos, SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.5F, 1.0F);
                    if (extraBE.getProgress() % 40 == 0) {
                        level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.3F, 1.5F);
                        if (extraBE.getProgress() % 80 == 0) {
                            level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.BLOCKS, 0.2F, 0.8F);
                        }
                    }
                }

                if (extraBE.getProgress() >= extraBE.getMaxProgress()) {
                    HandleRobosurgeonBlockEntityWithoutPlayer.performSurgery(maid, entity);
                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.5F, 2.0F);
                    level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5F, 1.0F);
                    extraBE.invokeResetProgress();
                    chamber.setDoorState(true);
                }
            } else {
                if (extraBE.getProgress() > 0) {
                    extraBE.invokeResetProgress();
                    chamber.setDoorState(true);
                }
                if (wantsSurgery) {
                    HandleRobosurgeonBlockEntityWithoutPlayer.notifyBlockedSurgery(maid, entity);
                }
            }

        }
    }
}
