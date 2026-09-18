package com.gly091020.CyberMaid.datagen;

import com.gly091020.CyberMaid.CyberMaid;
import com.gly091020.CyberMaid.registry.CyberMaidItems;
import com.maxwell.cyber_ware_port.common.block.cwb.recipe.AssemblyRecipe;
import com.maxwell.cyber_ware_port.common.block.cwb.recipe.EngineeringRecipe;
import com.maxwell.cyber_ware_port.init.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.Tags;

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

        assembly(output, "powered_prosthetic_legs", CyberMaidItems.POWERED_PROSTHETIC_LEGS.get(),
                input(Ingredient.of(ModItems.COMPONENT_TITANIUM.get()), 2),
                input(Ingredient.of(ModItems.COMPONENT_ACTUATOR.get()), 2),
                input(Ingredient.of(ModItems.COMPONENT_MICROELECTRIC.get()), 1));

        assembly(output, "hydraulic_joints", CyberMaidItems.HYDRAULIC_JOINTS.get(),
                input(Ingredient.of(ModItems.COMPONENT_TITANIUM.get()), 2),
                input(Ingredient.of(ModItems.COMPONENT_ACTUATOR.get()), 2),
                input(Ingredient.of(ModItems.COMPONENT_PLATING.get()), 2));

        assembly(output, "shock_absorber", CyberMaidItems.SHOCK_ABSORBER.get(),
                input(Ingredient.of(ModItems.COMPONENT_TITANIUM.get()), 2),
                input(Ingredient.of(ModItems.COMPONENT_ACTUATOR.get()), 1),
                input(Ingredient.of(ItemTags.WOOL), 2));

        assembly(output, "emergency_jet_escape", CyberMaidItems.EMERGENCY_JET_ESCAPE.get(),
                input(Ingredient.of(ModItems.COMPONENT_ACTUATOR.get()), 2),
                input(Ingredient.of(ModItems.COMPONENT_MICROELECTRIC.get()), 2),
                input(Ingredient.of(Items.BLAZE_POWDER), 4),
                input(Ingredient.of(Tags.Items.INGOTS_IRON), 2));
    }

    private static void engineering(RecipeOutput output, String name, Item input, EngineeringRecipe.OutputEntry... drops) {
        accept(output, name + "_engineering",
                new EngineeringRecipe(Ingredient.of(input), List.of(drops), BLUEPRINT_CHANCE));
    }

    private static void assembly(RecipeOutput output, String name, Item result, AssemblyRecipe.SizedIngredient... inputs) {
        accept(output, name + "_assembly", new AssemblyRecipe(List.of(inputs), new ItemStack(result)));
    }

    private static AssemblyRecipe.SizedIngredient input(Ingredient ingredient, int count) {
        return new AssemblyRecipe.SizedIngredient(ingredient, count);
    }

    private static void accept(RecipeOutput output, String path, Recipe<?> recipe) {
        output.accept(ResourceLocation.fromNamespaceAndPath(CyberMaid.MODID, path), recipe, null);
    }

    private static EngineeringRecipe.OutputEntry drop(Item item, int count, float chance) {
        return new EngineeringRecipe.OutputEntry(new ItemStack(item, count), chance);
    }
}
