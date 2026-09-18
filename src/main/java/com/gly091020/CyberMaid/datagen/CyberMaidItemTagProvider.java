package com.gly091020.CyberMaid.datagen;

import com.gly091020.CyberMaid.CyberMaid;
import com.gly091020.CyberMaid.registry.CyberMaidItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class CyberMaidItemTagProvider extends ItemTagsProvider {
    private static final TagKey<Item> CYBERWARE_LEGS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("cyber_ware_port", "cyberware/legs"));
    private static final TagKey<Item> CYBER_FAIRY_DROPS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(CyberMaid.MODID, "cyber_fairy_drops"));

    public CyberMaidItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                    ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()),
                CyberMaid.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(CYBERWARE_LEGS).add(
                CyberMaidItems.POWERED_PROSTHETIC_LEGS.get(),
                CyberMaidItems.HYDRAULIC_JOINTS.get(),
                CyberMaidItems.SHOCK_ABSORBER.get(),
                CyberMaidItems.EMERGENCY_JET_ESCAPE.get());
        tag(CYBER_FAIRY_DROPS).add(
                CyberMaidItems.POWERED_PROSTHETIC_LEGS.get(),
                CyberMaidItems.HYDRAULIC_JOINTS.get(),
                CyberMaidItems.SHOCK_ABSORBER.get(),
                CyberMaidItems.EMERGENCY_JET_ESCAPE.get());
    }
}
