package com.gly091020.CyberMaid.compat.maidspell;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.common.capability.ManaCap;
import com.hollingsworth.arsnouveau.common.capability.ManaData;
import com.hollingsworth.arsnouveau.setup.config.ServerConfig;
import com.hollingsworth.arsnouveau.setup.registry.AttachmentsRegistry;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

final class ArsSpellManaCompat {
    private static final double TICKS_PER_SECOND = 20.0D;

    private ArsSpellManaCompat() {
    }

    static void registerCapability(RegisterCapabilitiesEvent event) {
        event.registerEntity(CapabilityRegistry.MANA_CAPABILITY, InitEntities.MAID.get(),
                (entity, context) -> entity instanceof EntityMaid maid ? new ManaCap(maid) : null);
    }

    static void tickMana(EntityMaid maid) {
        int interval = Math.max(1, ServerConfig.REGEN_INTERVAL.get());
        if (maid.tickCount % interval != 0) return;

        ManaData data = maid.getExistingData(AttachmentsRegistry.MANA_ATTACHMENT).orElse(null);
        if (data == null) return;

        int maxMana = maxMana(maid, data);
        data.setMaxMana(maxMana);

        double mana = data.getMana();
        if (mana > maxMana) {
            data.setMana(maxMana);
            return;
        }
        double regen = manaRegen(maid, data) / Math.max(1.0D, TICKS_PER_SECOND / interval);
        data.setMana(Math.min(maxMana, mana + regen));
    }

    static boolean canCast(EntityMaid maid, int manaCost) {
        ManaData data = manaData(maid);
        return data.getMana() - manaCost >= MaidSpellManaCompat.manaFloor(maid, data.getMaxMana());
    }

    static boolean tryConsume(EntityMaid maid, int manaCost) {
        if (manaCost <= 0) return true;

        ManaData data = manaData(maid);
        double floor = MaidSpellManaCompat.manaFloor(maid, data.getMaxMana());
        double mana = data.getMana();
        if (mana - manaCost < floor) return false;
        data.setMana(Math.max(mana - manaCost, floor));
        return true;
    }

    private static ManaData manaData(EntityMaid maid) {
        ManaData data = maid.getData(AttachmentsRegistry.MANA_ATTACHMENT);
        int maxMana = maxMana(maid, data);
        if (data.getMaxMana() != maxMana) {
            boolean newlyCreated = data.getMaxMana() <= 0;
            data.setMaxMana(maxMana);
            if (newlyCreated) data.setMana(maxMana);
        }
        if (data.getMana() > maxMana) data.setMana(maxMana);
        return data;
    }

    private static int maxMana(EntityMaid maid, ManaData data) {
        double maxMana = ServerConfig.INIT_MAX_MANA.get()
                + data.getGlyphBonus() * (double) ServerConfig.GLYPH_MAX_BONUS.get()
                + data.getBookTier() * (double) ServerConfig.TIER_MAX_BONUS.get();
        AttributeInstance attribute = maid.getAttribute(PerkAttributes.MAX_MANA);
        if (attribute != null) maxMana += attribute.getValue();
        return (int) Math.max(1.0D, maxMana);
    }

    private static double manaRegen(EntityMaid maid, ManaData data) {
        double regen = ServerConfig.INIT_MANA_REGEN.get()
                + data.getGlyphBonus() * ServerConfig.GLYPH_REGEN_BONUS.get()
                + data.getBookTier() * (double) ServerConfig.TIER_REGEN_BONUS.get();
        AttributeInstance attribute = maid.getAttribute(PerkAttributes.MANA_REGEN_BONUS);
        if (attribute != null) regen += attribute.getValue();
        return regen;
    }
}
