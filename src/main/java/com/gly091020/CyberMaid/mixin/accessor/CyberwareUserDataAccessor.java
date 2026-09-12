package com.gly091020.CyberMaid.mixin.accessor;

import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// 你这不如访问加宽
@Mixin(CyberwareUserData.class)
public interface CyberwareUserDataAccessor {
    @Accessor
    int getRespawnGracePeriod();

    @Accessor
    void setLastProduction(int v);

    @Accessor
    void setLastConsumption(int v);

    @Accessor("isPowered")
    boolean getIsPowered();

    @Accessor("isPowered")
    void setIsPowered(boolean powered);

    @Accessor
    void setNeedsCapacityUpdate(boolean v);

    @Accessor
    boolean getNeedsCapacityUpdate();

    @Accessor
    void setToleranceImmunityTime(int v);

    @Accessor
    void setCurrentEnergy(int v);

    @Accessor
    void setMaxEnergy(int v);
}
