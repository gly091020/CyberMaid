package com.gly091020.CyberMaid.datagen;

import com.gly091020.CyberMaid.CyberMaid;
import com.gly091020.CyberMaid.registry.CyberMaidItems;
import com.maxwell.cyber_ware_port.common.block.cwb.recipe.EngineeringRecipe;
import com.maxwell.cyber_ware_port.init.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CyberMaidRecipeProvider extends RecipeProvider {
    private static final float BLUEPRINT_CHANCE = 0.15F;

    public CyberMaidRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        engineering(output, "powered_prosthetic_legs", CyberMaidItems.POWERED_PROSTHETIC_LEGS.get(),
                drop(ModItems.COMPONENT_TITANIUM.get(), 1, 1.0F),
                drop(ModItems.COMPONENT_ACTUATOR.get(), 1, 1.0F),
                drop(ModItems.COMPONENT_MICROELECTRIC.get(), 1, 0.5F));

        engineering(output, "hydraulic_joints", CyberMaidItems.HYDRAULIC_JOINTS.get(),
                drop(ModItems.COMPONENT_TITANIUM.get(), 1, 1.0F),
                drop(ModItems.COMPONENT_ACTUATOR.get(), 1, 1.0F),
                drop(ModItems.COMPONENT_PLATING.get(), 1, 0.8F));

        engineering(output, "shock_absorber", CyberMaidItems.SHOCK_ABSORBER.get(),
                drop(ModItems.COMPONENT_TITANIUM.get(), 1, 1.0F),
                drop(ModItems.COMPONENT_ACTUATOR.get(), 1, 0.8F),
                drop(Items.WHITE_WOOL, 1, 0.8F));

        engineering(output, "emergency_jet_escape", CyberMaidItems.EMERGENCY_JET_ESCAPE.get(),
                drop(ModItems.COMPONENT_ACTUATOR.get(), 1, 1.0F),
                drop(ModItems.COMPONENT_MICROELECTRIC.get(), 1, 1.0F),
                drop(Items.BLAZE_POWDER, 2, 0.8F),
                drop(Items.IRON_INGOT, 1, 0.8F));
    }

    private static void engineering(RecipeOutput output, String name, Item input, EngineeringRecipe.OutputEntry... drops) {
        accept(output, name + "_engineering",
                new EngineeringRecipe(Ingredient.of(input), List.of(drops), BLUEPRINT_CHANCE));
    }

    private static void accept(RecipeOutput output, String path, net.minecraft.world.item.crafting.Recipe<?> recipe) {
        output.accept(ResourceLocation.fromNamespaceAndPath(CyberMaid.MODID, path), recipe, null);
    }

    private static EngineeringRecipe.OutputEntry drop(Item item, int count, float chance) {
        return new EngineeringRecipe.OutputEntry(new ItemStack(item, count), chance);
    }
}
