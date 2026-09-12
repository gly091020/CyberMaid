package com.gly091020.CyberMaid.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.maxwell.cyber_ware_port.client.screen.ScannerScreen;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScannerScreen.class)
public class ScannerScreenMixin {
    @WrapOperation(method = "addRandomLog", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    public int addMoreLog(RandomSource instance, int i, Operation<Integer> original){
        return instance.nextInt(100);
    }
}
