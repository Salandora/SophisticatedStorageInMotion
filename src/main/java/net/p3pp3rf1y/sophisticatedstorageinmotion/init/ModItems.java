package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.DispenserBlock;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.*;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageMinecartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModItems {
	static List<Item> ITEMS = new ArrayList<>(); // Must be up here!

	public static final Item STORAGE_MINECART = register("storage_minecart", StorageMinecartItem::new);
	public static final Item STORAGE_BOAT = register("storage_boat", StorageBoatItem::new);

	public static final RecipeSerializer<?> MOVING_STORAGE_FROM_STORAGE_SERIALIZER = registerRecipeSerializer("moving_storage_from_storage", MovingStorageFromStorageRecipe.Serializer::new);
	public static final RecipeSerializer<? extends CraftingRecipe> UNCRAFT_MOVING_STORAGE_SERIALIZER = registerRecipeSerializer("uncraft_moving_storage", () -> new SimpleCraftingRecipeSerializer<>(UncraftMovingStorageRecipe::new));
	public static final RecipeSerializer<?> MOVING_STORAGE_TIER_UPGRADE_SHAPED_RECIPE_SERIALIZER = registerRecipeSerializer("moving_storage_tier_upgrade_shaped_recipe", MovingStorageTierUpgradeShapedRecipe.Serializer::new);
	public static final RecipeSerializer<?> MOVING_STORAGE_TIER_UPGRADE_SHAPELESS_RECIPE_SERIALIZER = registerRecipeSerializer("moving_storage_tier_upgrade_shapeless_recipe", MovingStorageTierUpgradeShapelessRecipe.Serializer::new);

	public static CreativeModeTab CREATIVE_TAB = FabricItemGroup.builder().icon(() -> new ItemStack(STORAGE_MINECART))
					.title(Component.translatable("itemGroup.sophisticatedstorageinmotion"))
					.displayItems((featureFlags, output) ->
							ITEMS.stream()
									.filter(i -> i instanceof ItemBase)
									.forEach(i -> ((ItemBase) i).addCreativeTabItems(output::accept)))
					.build();

	public static void registerHandlers() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, SophisticatedStorageInMotion.getRL("item_group"), CREATIVE_TAB);
		registerRecipeIngredients();
	}

	private static void registerRecipeIngredients() {
		CustomIngredientSerializer.register(MovingStorageIngredient.Serializer.INSTANCE);
	}

	public static void registerDispenseBehavior() {
		DispenserBlock.registerBehavior(STORAGE_MINECART, StorageMinecartItem.DISPENSE_ITEM_BEHAVIOR);
		DispenserBlock.registerBehavior(STORAGE_BOAT, StorageBoatItem.DISPENSE_ITEM_BEHAVIOR);
	}

	public static <T extends Item> T register(String id, Supplier<T> supplier) {
		T item = supplier.get();
		ITEMS.add(item);
		return Registry.register(BuiltInRegistries.ITEM, SophisticatedStorageInMotion.getRL(id), item);
	}
	public static <T extends RecipeSerializer<?>> T registerRecipeSerializer(String id, Supplier<T> supplier) {
		return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, SophisticatedStorageInMotion.getRL(id), supplier.get());
	}
}
