package com.gly091020.CyberMaid.mixin.maidspell;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.yimeng261.maidspell.spell.data.MaidArsNouveauSpellData;
import com.github.yimeng261.maidspell.spell.providers.ArsNouveauProvider;
import com.gly091020.CyberMaid.compat.maidspell.MaidSpellManaCompat;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.util.ManaUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArsNouveauProvider.class)
public class ArsNouveauProviderMixin {
    @Inject(method = "ensureManaCapability", at = @At("HEAD"), cancellable = true)
    private void cyberMaid$cancelManaRefill(EntityMaid maid, Spell spell, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "initiateCasting",
            at = @At(value = "INVOKE", target = "Lcom/github/yimeng261/maidspell/spell/data/MaidArsNouveauSpellData;setSpellCooldown(Ljava/lang/String;ILcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;)V"),
            cancellable = true)
    private void cyberMaid$checkMana(EntityMaid maid, CallbackInfo ci) {
        MaidArsNouveauSpellData data = MaidArsNouveauSpellData.getOrCreate(maid);
        Spell spell = data.getCurrentSpell();
        if (spell == null) return;

        if (!MaidSpellManaCompat.canCastMaidArsSpell(maid, cyberMaid$manaCost(maid, data, spell))) {
            data.resetCastingState();
            ci.cancel();
        }
    }

    @Inject(method = "completeCasting", at = @At("HEAD"), cancellable = true)
    private void cyberMaid$consumeMana(EntityMaid maid, CallbackInfo ci) {
        MaidArsNouveauSpellData data = MaidArsNouveauSpellData.getOrCreate(maid);
        Spell spell = data.getCurrentSpell();
        if (spell == null) return;

        if (!MaidSpellManaCompat.consumeMaidArsSpellMana(maid, cyberMaid$manaCost(maid, data, spell))) {
            data.resetCastingState();
            ci.cancel();
        }
    }

    private static int cyberMaid$manaCost(EntityMaid maid, MaidArsNouveauSpellData data, Spell spell) {
        int baseCost = spell.getCost() - ManaUtil.getPlayerDiscounts(maid, spell, data.getCurrentSpellBook());
        return MaidSpellManaCompat.scaleSpellManaCost(maid, Math.max(0, baseCost));
    }
}
