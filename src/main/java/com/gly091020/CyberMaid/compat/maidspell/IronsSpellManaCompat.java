package com.gly091020.CyberMaid.compat.maidspell;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.yimeng261.maidspell.spell.data.MaidIronsSpellData;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import net.minecraft.util.Mth;

final class IronsSpellManaCompat {
    private static final int MANA_REGEN_TICKS = 10;

    private IronsSpellManaCompat() {
    }

    static void tickMana(EntityMaid maid) {
        if (maid.tickCount % MANA_REGEN_TICKS != 0) return;
        MagicData magicData = MagicData.getPlayerMagicData(maid);
        int maxMana = (int) maid.getAttributeValue(AttributeRegistry.MAX_MANA);
        if (maxMana <= 0) return;

        float mana = magicData.getMana();
        if (mana >= maxMana) return;

        float regen = (float) maid.getAttributeValue(AttributeRegistry.MANA_REGEN) * (float) maxMana * 0.01F
                * ServerConfigs.MANA_REGEN_MULTIPLIER.get().floatValue();
        magicData.setMana(Mth.clamp(mana + regen, MaidSpellManaCompat.manaFloor(maid, maxMana), maxMana));
    }

    static boolean canCast(EntityMaid maid, int manaCost) {
        MagicData magicData = MagicData.getPlayerMagicData(maid);
        int maxMana = (int) maid.getAttributeValue(AttributeRegistry.MAX_MANA);
        return magicData.getMana() - manaCost >= MaidSpellManaCompat.manaFloor(maid, maxMana);
    }

    static void consume(EntityMaid maid, int manaCost) {
        MagicData magicData = MagicData.getPlayerMagicData(maid);
        int maxMana = (int) maid.getAttributeValue(AttributeRegistry.MAX_MANA);
        magicData.setMana(Math.max(magicData.getMana() - manaCost, MaidSpellManaCompat.manaFloor(maid, maxMana)));
        syncCastingData(maid);
    }

    static void syncCastingData(EntityMaid maid) {
        MaidIronsSpellData.getOrCreate(maid).getMagicData().setMana(MagicData.getPlayerMagicData(maid).getMana());
    }
}
