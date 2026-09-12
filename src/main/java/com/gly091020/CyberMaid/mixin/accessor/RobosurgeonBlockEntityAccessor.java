package com.gly091020.CyberMaid.mixin.accessor;

import com.maxwell.cyber_ware_port.common.block.robosurgeon.RobosurgeonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RobosurgeonBlockEntity.class)
public interface RobosurgeonBlockEntityAccessor {
    @Invoker("findPatient")
    LivingEntity invokeFindPatient(BlockPos chamberPos);

    @Invoker("resetProgress")
    void invokeResetProgress();

    @Accessor
    int getProgress();

    @Accessor
    int getMaxProgress();

    @Accessor
    void setProgress(int v);
}
