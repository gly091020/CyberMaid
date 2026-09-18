package com.gly091020.CyberMaid.compat;

import net.neoforged.fml.loading.LoadingModList;

public enum CompatMods {
    MAID_SPELL("touhou_little_maid_spell"),
    IRONS_SPELLBOOKS("irons_spellbooks"),
    ARS_NOUVEAU("ars_nouveau");

    private final String id;
    private final boolean loaded;

    CompatMods(String id) {
        this.id = id;
        this.loaded = LoadingModList.get().getModFileById(id) != null;
    }

    public String getId() {
        return id;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
