package net.p3pp3rf1y.sophisticatedstorageinmotion.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedcore.crafting.ShapeBasedRecipeBuilder;
import net.p3pp3rf1y.sophisticatedcore.crafting.ShapelessBasedRecipeBuilder;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageIngredient;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

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

		addStorageBoatFromStorageRecipes(consumer);

		ShapelessBasedRecipeBuilder.shapeless(MovingStorageItem.createWithStorage(new ItemStack(ModItems.STORAGE_MINECART), WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.CHEST_ITEM), WoodType.OAK)))
				.requires(Items.CHEST_MINECART)
				.requires(Items.REDSTONE_TORCH)
				.unlockedBy("has_chest_minecart", has(Items.CHEST_MINECART))
				.save(consumer, SophisticatedStorageInMotion.getRL("chest_minecart_to_storage_minecart"));

		addVanillaChestBoatConversionRecipes(consumer);

		addTierUpgradeRecipes(consumer, ModItems.STORAGE_MINECART);
		addTierUpgradeRecipes(consumer, ModItems.STORAGE_BOAT);
	}

	private void addVanillaChestBoatConversionRecipes(Consumer<FinishedRecipe> consumer) {
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.OAK, Items.OAK_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.SPRUCE, Items.SPRUCE_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.BIRCH, Items.BIRCH_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.JUNGLE, Items.JUNGLE_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.ACACIA, Items.ACACIA_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.DARK_OAK, Items.DARK_OAK_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.MANGROVE, Items.MANGROVE_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.BAMBOO, Items.BAMBOO_CHEST_RAFT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.CHERRY, Items.CHERRY_CHEST_BOAT);
	}

	private static void addVanillaChestBoatConversionRecipe(Consumer<FinishedRecipe> consumer, Boat.Type boatType, Item vanillaChestBoat) {
		ShapelessBasedRecipeBuilder.shapeless(MovingStorageItem.createWithStorage(StorageBoatItem.setBoatType(new ItemStack(ModItems.STORAGE_BOAT), boatType), WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.CHEST_ITEM), WoodType.OAK)))
				.requires(vanillaChestBoat)
				.requires(Items.REDSTONE_TORCH)
				.unlockedBy("has_" + BuiltInRegistries.ITEM.getKey(vanillaChestBoat).getPath(), has(vanillaChestBoat))
				.save(consumer, SophisticatedStorageInMotion.getRL(boatType.getName() + "_storage_" + (boatType == Boat.Type.BAMBOO ? "raft" : "boat") + "_from_vanilla"));
	}

	private void addStorageBoatFromStorageRecipes(Consumer<FinishedRecipe> consumer) {
		StorageBoatItem.SUPPORTED_BOAT_TYPES.forEach((boatType, baseBoat) -> {
			ShapelessBasedRecipeBuilder.shapeless(StorageBoatItem.setBoatType(new ItemStack(ModItems.STORAGE_BOAT), boatType), ModItems.MOVING_STORAGE_FROM_STORAGE_SERIALIZER)
					.requires(baseBoat.get())
					.requires(ModBlocks.ALL_STORAGE_TAG)
					.unlockedBy("has_sophisticated_storage", has(ModBlocks.ALL_STORAGE_TAG))
					.save(consumer, SophisticatedStorageInMotion.getRL(boatType.getName() + "_storage_" + (boatType == Boat.Type.BAMBOO ? "raft" : "boat")));
		});
	}

	private static void addTierUpgradeRecipes(Consumer<FinishedRecipe> consumer, Item movingStorageItem) {
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.CHEST_ITEM, ModBlocks.COPPER_CHEST_ITEM, ConventionalItemTags.COPPER_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.BARREL_ITEM, ModBlocks.COPPER_BARREL_ITEM, ConventionalItemTags.COPPER_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.SHULKER_BOX_ITEM, ModBlocks.COPPER_SHULKER_BOX_ITEM, ConventionalItemTags.COPPER_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_1_ITEM, ModBlocks.LIMITED_COPPER_BARREL_1_ITEM, ConventionalItemTags.COPPER_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_2_ITEM, ModBlocks.LIMITED_COPPER_BARREL_2_ITEM, ConventionalItemTags.COPPER_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_3_ITEM, ModBlocks.LIMITED_COPPER_BARREL_3_ITEM, ConventionalItemTags.COPPER_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_4_ITEM, ModBlocks.LIMITED_COPPER_BARREL_4_ITEM, ConventionalItemTags.COPPER_INGOTS);

		addCheaperMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.COPPER_CHEST_ITEM, ModBlocks.IRON_CHEST_ITEM, ConventionalItemTags.IRON_INGOTS);
		addCheaperMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.COPPER_BARREL_ITEM, ModBlocks.IRON_BARREL_ITEM, ConventionalItemTags.IRON_INGOTS);
		addCheaperMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.COPPER_SHULKER_BOX_ITEM, ModBlocks.IRON_SHULKER_BOX_ITEM, ConventionalItemTags.IRON_INGOTS);
		addCheaperMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_COPPER_BARREL_1_ITEM, ModBlocks.LIMITED_IRON_BARREL_1_ITEM, ConventionalItemTags.IRON_INGOTS);
		addCheaperMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_COPPER_BARREL_2_ITEM, ModBlocks.LIMITED_IRON_BARREL_2_ITEM, ConventionalItemTags.IRON_INGOTS);
		addCheaperMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_COPPER_BARREL_3_ITEM, ModBlocks.LIMITED_IRON_BARREL_3_ITEM, ConventionalItemTags.IRON_INGOTS);
		addCheaperMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_COPPER_BARREL_4_ITEM, ModBlocks.LIMITED_IRON_BARREL_4_ITEM, ConventionalItemTags.IRON_INGOTS);

		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.CHEST_ITEM, ModBlocks.IRON_CHEST_ITEM, ConventionalItemTags.IRON_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.BARREL_ITEM, ModBlocks.IRON_BARREL_ITEM, ConventionalItemTags.IRON_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.SHULKER_BOX_ITEM, ModBlocks.IRON_SHULKER_BOX_ITEM, ConventionalItemTags.IRON_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_1_ITEM, ModBlocks.LIMITED_IRON_BARREL_1_ITEM, ConventionalItemTags.IRON_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_2_ITEM, ModBlocks.LIMITED_IRON_BARREL_2_ITEM, ConventionalItemTags.IRON_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_3_ITEM, ModBlocks.LIMITED_IRON_BARREL_3_ITEM, ConventionalItemTags.IRON_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_4_ITEM, ModBlocks.LIMITED_IRON_BARREL_4_ITEM, ConventionalItemTags.IRON_INGOTS);

		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.IRON_CHEST_ITEM, ModBlocks.GOLD_CHEST_ITEM, ConventionalItemTags.GOLD_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.IRON_BARREL_ITEM, ModBlocks.GOLD_BARREL_ITEM, ConventionalItemTags.GOLD_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.IRON_SHULKER_BOX_ITEM, ModBlocks.GOLD_SHULKER_BOX_ITEM, ConventionalItemTags.GOLD_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_IRON_BARREL_1_ITEM, ModBlocks.LIMITED_GOLD_BARREL_1_ITEM, ConventionalItemTags.GOLD_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_IRON_BARREL_2_ITEM, ModBlocks.LIMITED_GOLD_BARREL_2_ITEM, ConventionalItemTags.GOLD_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_IRON_BARREL_3_ITEM, ModBlocks.LIMITED_GOLD_BARREL_3_ITEM, ConventionalItemTags.GOLD_INGOTS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_IRON_BARREL_4_ITEM, ModBlocks.LIMITED_GOLD_BARREL_4_ITEM, ConventionalItemTags.GOLD_INGOTS);

		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.GOLD_CHEST_ITEM, ModBlocks.DIAMOND_CHEST_ITEM, ConventionalItemTags.DIAMONDS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.GOLD_BARREL_ITEM, ModBlocks.DIAMOND_BARREL_ITEM, ConventionalItemTags.DIAMONDS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.GOLD_SHULKER_BOX_ITEM, ModBlocks.DIAMOND_SHULKER_BOX_ITEM, ConventionalItemTags.DIAMONDS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_GOLD_BARREL_1_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM, ConventionalItemTags.DIAMONDS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_GOLD_BARREL_2_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM, ConventionalItemTags.DIAMONDS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_GOLD_BARREL_3_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM, ConventionalItemTags.DIAMONDS);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_GOLD_BARREL_4_ITEM, ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM, ConventionalItemTags.DIAMONDS);

		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.DIAMOND_CHEST_ITEM, ModBlocks.NETHERITE_CHEST_ITEM);
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.DIAMOND_BARREL_ITEM, ModBlocks.NETHERITE_BARREL_ITEM);
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.DIAMOND_SHULKER_BOX_ITEM, ModBlocks.NETHERITE_SHULKER_BOX_ITEM);
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM);
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM);
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM);
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM);
	}

	private static void addCheaperMovingStorageTierUpgradeRecipe(Consumer<FinishedRecipe> consumer, Item movingStorageItem, Item storageItem, Item upgradedStorageItem, TagKey<Item> material) {
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, storageItem, upgradedStorageItem, material, builder -> builder.pattern(" M ").pattern("MSM").pattern(" M "));
	}

	private static void addMovingStorageTierUpgradeRecipe(Consumer<FinishedRecipe> consumer, Item movingStorageItem, Item storageItem, Item upgradedStorageItem, TagKey<Item> material) {
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, storageItem, upgradedStorageItem, material, builder -> builder.pattern("MMM").pattern("MSM").pattern("MMM"));
	}

	private static void addMovingStorageTierUpgradeRecipe(Consumer<FinishedRecipe> consumer, Item movingStorageItem, Item storageItem, Item upgradedStorageItem, TagKey<Item> material, UnaryOperator<ShapeBasedRecipeBuilder> patternInit) {
		String storageItemPath = BuiltInRegistries.ITEM.getKey(storageItem).getPath();
		patternInit.apply(ShapeBasedRecipeBuilder.shaped(MovingStorageItem.createWithStorage(new ItemStack(movingStorageItem), new ItemStack(upgradedStorageItem)), ModItems.MOVING_STORAGE_TIER_UPGRADE_SHAPED_RECIPE_SERIALIZER))
				.define('S', MovingStorageIngredient.of(BuiltInRegistries.ITEM.wrapAsHolder(movingStorageItem), storageItem).toVanilla())
				.define('M', material)
				.unlockedBy("has_" + storageItemPath, has(storageItem))
				.save(consumer, SophisticatedStorageInMotion.getRL(BuiltInRegistries.ITEM.getKey(movingStorageItem).getPath() + "_with_" + storageItemPath + "_to_" + BuiltInRegistries.ITEM.getKey(upgradedStorageItem).getPath()));
	}

	private static void addMovingStorageDiamondToNetheriteTierUpgradeRecipe(Consumer<FinishedRecipe> consumer, Item movingStorageItem, Item storageItem, Item upgradedStorageItem) {
		String storageItemPath = BuiltInRegistries.ITEM.getKey(storageItem).getPath();
		ShapelessBasedRecipeBuilder.shapeless(MovingStorageItem.createWithStorage(new ItemStack(movingStorageItem), new ItemStack(upgradedStorageItem)), ModItems.MOVING_STORAGE_TIER_UPGRADE_SHAPELESS_RECIPE_SERIALIZER)
				.requires(MovingStorageIngredient.of(BuiltInRegistries.ITEM.wrapAsHolder(movingStorageItem), storageItem).toVanilla())
				.requires(ConventionalItemTags.NETHERITE_INGOTS)
				.unlockedBy("has_" + storageItemPath, has(storageItem))
				.save(consumer, SophisticatedStorageInMotion.getRL(BuiltInRegistries.ITEM.getKey(movingStorageItem).getPath() + "_with_" + storageItemPath + "_to_" + BuiltInRegistries.ITEM.getKey(upgradedStorageItem).getPath()));
	}
}
