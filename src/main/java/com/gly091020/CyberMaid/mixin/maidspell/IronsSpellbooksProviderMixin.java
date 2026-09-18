package com.gly091020.CyberMaid.mixin.maidspell;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.yimeng261.maidspell.spell.data.MaidIronsSpellData;
import com.github.yimeng261.maidspell.spell.providers.IronsSpellbooksProvider;
import com.gly091020.CyberMaid.compat.maidspell.MaidSpellManaCompat;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IronsSpellbooksProvider.class)
public class IronsSpellbooksProviderMixin {
    @Inject(method = "actualCasting", at = @At("HEAD"), cancellable = true)
    private void cyberMaid$checkMana(EntityMaid maid, SpellSlot spellData, CallbackInfo ci) {
        AbstractSpell spell = spellData.getSpell();
        if (spell == null) return;

        MaidSpellManaCompat.syncMaidSpellManaData(maid);
        int cost = MaidSpellManaCompat.scaleSpellManaCost(maid, spell.getManaCost(spellData.getLevel()));
        if (!MaidSpellManaCompat.canCastMaidSpell(maid, cost)) {
            ci.cancel();
        }
    }

    @Inject(method = "completeCasting", at = @At("HEAD"))
    private void cyberMaid$consumeMana(EntityMaid maid, CallbackInfo ci) {
        SpellSlot spellData = MaidIronsSpellData.getOrCreate(maid).getCurrentCastingSpell();
        if (spellData == null || spellData.getSpell() == null) return;

        int cost = MaidSpellManaCompat.scaleSpellManaCost(maid, spellData.getSpell().getManaCost(spellData.getLevel()));
        MaidSpellManaCompat.consumeMaidSpellMana(maid, cost);
    }
}
