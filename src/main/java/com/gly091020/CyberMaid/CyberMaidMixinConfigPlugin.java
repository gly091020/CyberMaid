package com.gly091020.CyberMaid;

import com.gly091020.CyberMaid.compat.CompatMods;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CyberMaidMixinConfigPlugin implements IMixinConfigPlugin {
    private static final String PACKAGE = "com.gly091020.CyberMaid.mixin.";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (isModMixin(mixinClassName, "maidspell")) {
            if (!CompatMods.MAID_SPELL.isLoaded()) return false;
            if (mixinClassName.endsWith("IronsSpellbooksProviderMixin")) return CompatMods.IRONS_SPELLBOOKS.isLoaded();
            if (mixinClassName.endsWith("ArsNouveauProviderMixin")) return CompatMods.ARS_NOUVEAU.isLoaded();
        }
        return true;
    }

    private static boolean isModMixin(String mixinClassName, String dirName) {
        return mixinClassName.contains(PACKAGE + dirName);
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
