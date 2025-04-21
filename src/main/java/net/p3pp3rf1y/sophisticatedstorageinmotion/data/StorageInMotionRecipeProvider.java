package net.p3pp3rf1y.sophisticatedstorageinmotion.data;

import io.github.fabricators_of_create.porting_lib.util.DeferredHolder;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedcore.crafting.SCShapedRecipeBuilder;
import net.p3pp3rf1y.sophisticatedcore.crafting.SCShapelessRecipeBuilder;
import net.p3pp3rf1y.sophisticatedcore.crafting.ShapeBasedRecipeBuilder;
import net.p3pp3rf1y.sophisticatedcore.crafting.ShapelessBasedRecipeBuilder;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.*;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

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

		addStorageBoatFromStorageRecipes(recipeOutput);

		SCShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MovingStorageItem.createWithStorage(new ItemStack(ModItems.STORAGE_MINECART.get()), WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.CHEST_ITEM.get()), WoodType.OAK)))
				.requires(Items.CHEST_MINECART)
				.requires(Items.REDSTONE_TORCH)
				.unlockedBy("has_chest_minecart", has(Items.CHEST_MINECART))
				.save(recipeOutput, SophisticatedStorageInMotion.getRegistryName("chest_minecart_to_storage_minecart"));

		addVanillaChestBoatConversionRecipes(recipeOutput);

		addTierUpgradeRecipes(recipeOutput, ModItems.STORAGE_MINECART);
		addTierUpgradeRecipes(recipeOutput, ModItems.STORAGE_BOAT);
	}

	private void addVanillaChestBoatConversionRecipes(RecipeOutput recipeOutput) {
		addVanillaChestBoatConversionRecipe(recipeOutput, Boat.Type.OAK, Items.OAK_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(recipeOutput, Boat.Type.SPRUCE, Items.SPRUCE_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(recipeOutput, Boat.Type.BIRCH, Items.BIRCH_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(recipeOutput, Boat.Type.JUNGLE, Items.JUNGLE_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(recipeOutput, Boat.Type.ACACIA, Items.ACACIA_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(recipeOutput, Boat.Type.DARK_OAK, Items.DARK_OAK_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(recipeOutput, Boat.Type.MANGROVE, Items.MANGROVE_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(recipeOutput, Boat.Type.BAMBOO, Items.BAMBOO_CHEST_RAFT);
		addVanillaChestBoatConversionRecipe(recipeOutput, Boat.Type.CHERRY, Items.CHERRY_CHEST_BOAT);
	}

	private static void addVanillaChestBoatConversionRecipe(RecipeOutput recipeOutput, Boat.Type boatType, Item vanillaChestBoat) {
		SCShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MovingStorageItem.createWithStorage(StorageBoatItem.setBoatType(new ItemStack(ModItems.STORAGE_BOAT.get()), boatType), WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.CHEST_ITEM.get()), WoodType.OAK)))
				.requires(vanillaChestBoat)
				.requires(Items.REDSTONE_TORCH)
				.unlockedBy("has_" + BuiltInRegistries.ITEM.getKey(vanillaChestBoat).getPath(), has(vanillaChestBoat))
				.save(recipeOutput, SophisticatedStorageInMotion.getRegistryName(boatType.getName() + "_storage_" + (/*boatType.isRaft()*/boatType == Boat.Type.BAMBOO ? "raft" : "boat") + "_from_vanilla"));
	}

	private void addStorageBoatFromStorageRecipes(RecipeOutput recipeOutput) {
		StorageBoatItem.SUPPORTED_BOAT_TYPES.forEach((boatType, baseBoat) -> {
			ShapelessBasedRecipeBuilder.shapeless(StorageBoatItem.setBoatType(new ItemStack(ModItems.STORAGE_BOAT.get()), boatType), MovingStorageFromStorageRecipe::new)
					.requires(baseBoat.get())
					.requires(ModBlocks.ALL_STORAGE_TAG)
					.unlockedBy("has_sophisticated_storage", has(ModBlocks.ALL_STORAGE_TAG))
					.save(recipeOutput, SophisticatedStorageInMotion.getRegistryName(boatType.getName() + "_storage_" + (/*boatType.isRaft()*/boatType == Boat.Type.BAMBOO ? "raft" : "boat")));
		});
	}

	private static void addTierUpgradeRecipes(RecipeOutput recipeOutput, DeferredHolder<Item, ?> movingStorageItem) {
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.CHEST_ITEM.get(), ModBlocks.COPPER_CHEST_ITEM.get(), ConventionalItemTags.COPPER_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.BARREL_ITEM.get(), ModBlocks.COPPER_BARREL_ITEM.get(), ConventionalItemTags.COPPER_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.SHULKER_BOX_ITEM.get(), ModBlocks.COPPER_SHULKER_BOX_ITEM.get(), ConventionalItemTags.COPPER_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_BARREL_1_ITEM.get(), ModBlocks.LIMITED_COPPER_BARREL_1_ITEM.get(), ConventionalItemTags.COPPER_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_BARREL_2_ITEM.get(), ModBlocks.LIMITED_COPPER_BARREL_2_ITEM.get(), ConventionalItemTags.COPPER_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_BARREL_3_ITEM.get(), ModBlocks.LIMITED_COPPER_BARREL_3_ITEM.get(), ConventionalItemTags.COPPER_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_BARREL_4_ITEM.get(), ModBlocks.LIMITED_COPPER_BARREL_4_ITEM.get(), ConventionalItemTags.COPPER_INGOTS);

		addCheaperMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.COPPER_CHEST_ITEM.get(), ModBlocks.IRON_CHEST_ITEM.get(), ConventionalItemTags.IRON_INGOTS);
		addCheaperMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.COPPER_BARREL_ITEM.get(), ModBlocks.IRON_BARREL_ITEM.get(), ConventionalItemTags.IRON_INGOTS);
		addCheaperMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.COPPER_SHULKER_BOX_ITEM.get(), ModBlocks.IRON_SHULKER_BOX_ITEM.get(), ConventionalItemTags.IRON_INGOTS);
		addCheaperMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_COPPER_BARREL_1_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_1_ITEM.get(), ConventionalItemTags.IRON_INGOTS);
		addCheaperMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_COPPER_BARREL_2_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_2_ITEM.get(), ConventionalItemTags.IRON_INGOTS);
		addCheaperMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_COPPER_BARREL_3_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_3_ITEM.get(), ConventionalItemTags.IRON_INGOTS);
		addCheaperMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_COPPER_BARREL_4_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_4_ITEM.get(), ConventionalItemTags.IRON_INGOTS);

		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.CHEST_ITEM.get(), ModBlocks.IRON_CHEST_ITEM.get(), ConventionalItemTags.IRON_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.BARREL_ITEM.get(), ModBlocks.IRON_BARREL_ITEM.get(), ConventionalItemTags.IRON_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.SHULKER_BOX_ITEM.get(), ModBlocks.IRON_SHULKER_BOX_ITEM.get(), ConventionalItemTags.IRON_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_BARREL_1_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_1_ITEM.get(), ConventionalItemTags.IRON_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_BARREL_2_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_2_ITEM.get(), ConventionalItemTags.IRON_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_BARREL_3_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_3_ITEM.get(), ConventionalItemTags.IRON_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_BARREL_4_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_4_ITEM.get(), ConventionalItemTags.IRON_INGOTS);

		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.IRON_CHEST_ITEM.get(), ModBlocks.GOLD_CHEST_ITEM.get(), ConventionalItemTags.GOLD_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.IRON_BARREL_ITEM.get(), ModBlocks.GOLD_BARREL_ITEM.get(), ConventionalItemTags.GOLD_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.IRON_SHULKER_BOX_ITEM.get(), ModBlocks.GOLD_SHULKER_BOX_ITEM.get(), ConventionalItemTags.GOLD_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_IRON_BARREL_1_ITEM.get(), ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get(), ConventionalItemTags.GOLD_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_IRON_BARREL_2_ITEM.get(), ModBlocks.LIMITED_GOLD_BARREL_2_ITEM.get(), ConventionalItemTags.GOLD_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_IRON_BARREL_3_ITEM.get(), ModBlocks.LIMITED_GOLD_BARREL_3_ITEM.get(), ConventionalItemTags.GOLD_INGOTS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_IRON_BARREL_4_ITEM.get(), ModBlocks.LIMITED_GOLD_BARREL_4_ITEM.get(), ConventionalItemTags.GOLD_INGOTS);

		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.GOLD_CHEST_ITEM.get(), ModBlocks.DIAMOND_CHEST_ITEM.get(), ConventionalItemTags.DIAMOND_GEMS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.GOLD_BARREL_ITEM.get(), ModBlocks.DIAMOND_BARREL_ITEM.get(), ConventionalItemTags.DIAMOND_GEMS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.GOLD_SHULKER_BOX_ITEM.get(), ModBlocks.DIAMOND_SHULKER_BOX_ITEM.get(), ConventionalItemTags.DIAMOND_GEMS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get(), ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM.get(), ConventionalItemTags.DIAMOND_GEMS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_GOLD_BARREL_2_ITEM.get(), ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM.get(), ConventionalItemTags.DIAMOND_GEMS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_GOLD_BARREL_3_ITEM.get(), ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM.get(), ConventionalItemTags.DIAMOND_GEMS);
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_GOLD_BARREL_4_ITEM.get(), ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM.get(), ConventionalItemTags.DIAMOND_GEMS);

		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.DIAMOND_CHEST_ITEM.get(), ModBlocks.NETHERITE_CHEST_ITEM.get());
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.DIAMOND_BARREL_ITEM.get(), ModBlocks.NETHERITE_BARREL_ITEM.get());
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.DIAMOND_SHULKER_BOX_ITEM.get(), ModBlocks.NETHERITE_SHULKER_BOX_ITEM.get());
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM.get(), ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM.get());
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM.get(), ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM.get());
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM.get(), ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM.get());
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(recipeOutput, movingStorageItem, ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM.get(), ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM.get());
	}

	private static void addCheaperMovingStorageTierUpgradeRecipe(RecipeOutput recipeOutput, DeferredHolder<Item, ?> movingStorageItem, Item storageItem, Item upgradedStorageItem, TagKey<Item> material) {
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, storageItem, upgradedStorageItem, material, builder -> builder.pattern(" M ").pattern("MSM").pattern(" M "));
	}

	private static void addMovingStorageTierUpgradeRecipe(RecipeOutput recipeOutput, DeferredHolder<Item, ?> movingStorageItem, Item storageItem, Item upgradedStorageItem, TagKey<Item> material) {
		addMovingStorageTierUpgradeRecipe(recipeOutput, movingStorageItem, storageItem, upgradedStorageItem, material, builder -> builder.pattern("MMM").pattern("MSM").pattern("MMM"));
	}

	private static void addMovingStorageTierUpgradeRecipe(RecipeOutput recipeOutput, DeferredHolder<Item, ?> movingStorageItem, Item storageItem, Item upgradedStorageItem, TagKey<Item> material, UnaryOperator<SCShapedRecipeBuilder> patternInit) {
		String storageItemPath = BuiltInRegistries.ITEM.getKey(storageItem).getPath();
		Holder<Item> movingStorageItemHolder = BuiltInRegistries.ITEM.getHolder(movingStorageItem.getKey()).orElseThrow();
		patternInit.apply(ShapeBasedRecipeBuilder.shaped(MovingStorageItem.createWithStorage(new ItemStack(movingStorageItem.value()), new ItemStack(upgradedStorageItem)), MovingStorageTierUpgradeShapedRecipe::new))
				.define('S', MovingStorageIngredient.of(movingStorageItemHolder, storageItem).toVanilla())
				.define('M', material)
				.unlockedBy("has_" + storageItemPath, has(storageItem))
				.save(recipeOutput, SophisticatedStorageInMotion.getRegistryName(movingStorageItem.getKey().location().getPath() + "_with_" + storageItemPath + "_to_" + BuiltInRegistries.ITEM.getKey(upgradedStorageItem).getPath()));
	}

	private static void addMovingStorageDiamondToNetheriteTierUpgradeRecipe(RecipeOutput recipeOutput, DeferredHolder<Item, ?> movingStorageItem, Item storageItem, Item upgradedStorageItem) {
		String storageItemPath = BuiltInRegistries.ITEM.getKey(storageItem).getPath();
		Holder<Item> movingStorageItemHolder = BuiltInRegistries.ITEM.getHolder(movingStorageItem.getKey()).orElseThrow();
		ShapelessBasedRecipeBuilder.shapeless(MovingStorageItem.createWithStorage(new ItemStack(movingStorageItem.value()), new ItemStack(upgradedStorageItem)), MovingStorageTierUpgradeShapelessRecipe::new)
				.requires(MovingStorageIngredient.of(movingStorageItemHolder, storageItem).toVanilla())
				.requires(ConventionalItemTags.NETHERITE_INGOTS)
				.unlockedBy("has_" + storageItemPath, has(storageItem))
				.save(recipeOutput, SophisticatedStorageInMotion.getRegistryName(movingStorageItem.getKey().location().getPath() + "_with_" + storageItemPath + "_to_" + BuiltInRegistries.ITEM.getKey(upgradedStorageItem).getPath()));
	}
}
