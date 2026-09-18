package com.gly091020.CyberMaid.mixin.maidspell;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.yimeng261.maidspell.Config;
import com.github.yimeng261.maidspell.spell.SimplifiedSpellCaster;
import com.gly091020.CyberMaid.compat.maidspell.SpellCooldownReducerItem;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SimplifiedSpellCaster.class)
public class SimplifiedSpellCasterMixin {
    @Shadow
    @Final
    private EntityMaid maid;

    @Redirect(method = "melee_tick",
            at = @At(value = "FIELD", target = "Lcom/github/yimeng261/maidspell/Config;meleeAttackInterval:I", opcode = Opcodes.GETSTATIC))
    private int cyberMaid$scaleMeleeInterval() {
        return SpellCooldownReducerItem.scaleAttackInterval(maid, Config.meleeAttackInterval);
    }

    @Redirect(method = "far_tick",
            at = @At(value = "FIELD", target = "Lcom/github/yimeng261/maidspell/Config;farAttackInterval:I", opcode = Opcodes.GETSTATIC))
    private int cyberMaid$scaleFarInterval() {
        return SpellCooldownReducerItem.scaleAttackInterval(maid, Config.farAttackInterval);
    }
}
