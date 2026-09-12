package com.gly091020.CyberMaid.util;

import com.gly091020.CyberMaid.mixin.accessor.CyberwareUserDataAccessor;
import com.gly091020.CyberMaid.mixin.accessor.RobosurgeonBlockEntityAccessor;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.RobosurgeonBlockEntity;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;

public class MixinUtil {
    public static CyberwareUserDataAccessor extraCyberwareUserData(CyberwareUserData data){
        return (CyberwareUserDataAccessor) data;
    }

    public static RobosurgeonBlockEntityAccessor extraRobosurgeonBlockEntity(RobosurgeonBlockEntity be){
        return (RobosurgeonBlockEntityAccessor) be;
    }
}
