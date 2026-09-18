package com.gly091020.CyberMaid.datagen;

import com.gly091020.CyberMaid.registry.CyberMaidItems;
import com.gly091020.CyberMaid.compat.maidspell.MaidSpellItems;
import com.google.gson.JsonElement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class CyberMaidItemModelProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public CyberMaidItemModelProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Map<ResourceLocation, Supplier<JsonElement>> models = new LinkedHashMap<>();
        // 贴图直接复用前置模组已有的物品贴图
        model(models, CyberMaidItems.POWERED_PROSTHETIC_LEGS.getId(), "cyber_ware_port:item/foot_upgrades_spurs");
        model(models, CyberMaidItems.HYDRAULIC_JOINTS.getId(), "cyber_ware_port:item/foot_upgrades_wheels");
        model(models, CyberMaidItems.SHOCK_ABSORBER.getId(), "cyber_ware_port:item/leg_upgrades_fall_damage");
        model(models, CyberMaidItems.EMERGENCY_JET_ESCAPE.getId(), "cyber_ware_port:item/leg_upgrades_jump_boost");
        model(models, MaidSpellItems.SPELL_COOLDOWN_REDUCER.getId(), "cyber_ware_port:item/brain_upgrades_neural_contextualizer");
        model(models, MaidSpellItems.SPELL_MANA_REDUCER.getId(), "cyber_ware_port:item/brain_upgrades_matrix");
        model(models, MaidSpellItems.SPELL_MANA_DEBT.getId(), "cyber_ware_port:item/brain_upgrades_radio");
        // 刷怪蛋沿用原模组妖精蛋的图标
        model(models, CyberMaidItems.CYBER_FAIRY_SPAWN_EGG.getId(), "touhou_little_maid:item/fairy_spawn_egg");

        return CompletableFuture.allOf(models.entrySet().stream()
                .map(entry -> DataProvider.saveStable(cache, entry.getValue().get(), pathProvider.json(entry.getKey())))
                .toArray(CompletableFuture[]::new));
    }

    private static void model(Map<ResourceLocation, Supplier<JsonElement>> models, ResourceLocation id, String texture) {
        ModelTemplates.FLAT_ITEM.create(id, TextureMapping.layer0(ResourceLocation.parse(texture)), models::put);
    }

    @Override
    public String getName() {
        return "CyberMaid item models";
    }
}
