package net.p3pp3rf1y.sophisticatedstorageinmotion.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.world.item.Items;
import net.p3pp3rf1y.sophisticatedcore.crafting.ShapelessBasedRecipeBuilder;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;

import java.util.function.Consumer;

public class StorageInMotionRecipeProvider extends FabricRecipeProvider {
	public StorageInMotionRecipeProvider(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void buildRecipes(Consumer<FinishedRecipe> consumer) {
		SpecialRecipeBuilder.special(ModItems.UNCRAFT_MOVING_STORAGE_SERIALIZER).save(consumer, SophisticatedStorageInMotion.getRegistryName("uncraft_moving_storage"));

		ShapelessBasedRecipeBuilder.shapeless(ModItems.STORAGE_MINECART, ModItems.MOVING_STORAGE_FROM_STORAGE_SERIALIZER)
				.requires(Items.MINECART)
				.requires(ModBlocks.ALL_STORAGE_TAG)
				.unlockedBy("has_sophisticated_storage", has(ModBlocks.ALL_STORAGE_TAG))
				.save(consumer);
	}
}
