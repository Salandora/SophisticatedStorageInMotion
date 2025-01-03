package net.p3pp3rf1y.sophisticatedstorageinmotion.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.world.item.Items;
import net.p3pp3rf1y.sophisticatedcore.crafting.ShapelessBasedRecipeBuilder;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageFromStorageRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.UncraftMovingStorageRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;

import java.util.concurrent.CompletableFuture;

public class StorageInMotionRecipeProvider extends FabricRecipeProvider {
	public StorageInMotionRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public void buildRecipes(RecipeOutput recipeOutput) {
		SpecialRecipeBuilder.special(UncraftMovingStorageRecipe::new).save(recipeOutput, SophisticatedStorageInMotion.getRegistryName("uncraft_moving_storage"));

		ShapelessBasedRecipeBuilder.shapeless(ModItems.STORAGE_MINECART.get(), MovingStorageFromStorageRecipe::new)
				.requires(Items.MINECART)
				.requires(ModBlocks.ALL_STORAGE_TAG)
				.unlockedBy("has_sophisticated_storage", has(ModBlocks.ALL_STORAGE_TAG))
				.save(recipeOutput);
	}
}
