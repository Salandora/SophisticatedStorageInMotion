package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.jei;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageTierUpgradeShapedRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageTierUpgradeShapelessRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MovingStorageTierUpgradeRecipesMaker {
	private MovingStorageTierUpgradeRecipesMaker() {}

	public static List<CraftingRecipe> getShapedCraftingRecipes() {
		RecipeConstructor<MovingStorageTierUpgradeShapedRecipe> constructRecipe = (originalRecipe, id, ingredients, result) -> new ShapedRecipe(id, "", CraftingBookCategory.MISC, originalRecipe.getRecipeWidth(), originalRecipe.getRecipeHeight(), ingredients, result);

		return getCraftingRecipes(constructRecipe, MovingStorageTierUpgradeShapedRecipe.REGISTERED_RECIPES, MovingStorageTierUpgradeShapedRecipe.class);
	}

	public static List<CraftingRecipe> getShapelessCraftingRecipes() {
		RecipeConstructor<MovingStorageTierUpgradeShapelessRecipe> constructRecipe = (originalRecipe, id, ingredients, result) -> new ShapelessRecipe(id, "", CraftingBookCategory.MISC, result, ingredients);
		return getCraftingRecipes(constructRecipe, MovingStorageTierUpgradeShapelessRecipe.REGISTERED_RECIPES, MovingStorageTierUpgradeShapelessRecipe.class);
	}

	@NotNull
	private static <T extends CraftingRecipe> List<CraftingRecipe> getCraftingRecipes(RecipeConstructor<T> constructRecipe, Set<ResourceLocation> registeredRecipes, Class<T> originalRecipeClass) {
		return ClientRecipeHelper.getAndTransformAvailableItemGroupRecipes(registeredRecipes, originalRecipeClass, recipe -> {
			List<CraftingRecipe> itemGroupRecipes = new ArrayList<>();
			getStorageItems(recipe).forEach(storageItem -> {
				NonNullList<Ingredient> ingredients = recipe.getIngredients();
				CraftingContainer craftinginventory = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
					@Override
					public ItemStack quickMoveStack(Player player, int index) {
						return ItemStack.EMPTY;
					}

					public boolean stillValid(Player playerIn) {
						return false;
					}
				}, 3, 3);
				NonNullList<Ingredient> ingredientsCopy = NonNullList.createWithCapacity(ingredients.size());
				int i = 0;
				for (Ingredient ingredient : ingredients) {
					ItemStack[] ingredientItems = ingredient.getItems();
					if (ingredientItems.length > 0 && ingredientItems[0].getItem() instanceof MovingStorageItem) {
						ItemStack movingStorage = ingredientItems[0].copy();
						MovingStorageItem.setStorageItem(movingStorage, storageItem);
						ingredientsCopy.add(i, Ingredient.of(movingStorage));
						craftinginventory.setItem(i, movingStorage.copy());
					} else {
						ingredientsCopy.add(i, ingredient);
						if (!ingredient.isEmpty()) {
							craftinginventory.setItem(i, ingredientItems[0]);
						}
					}
					i++;
				}
				ItemStack result = ClientRecipeHelper.assemble(recipe, craftinginventory);
				ResourceLocation id = SophisticatedStorageInMotion.getRL("tier_upgrade_" + ForgeRegistries.ITEMS.getKey(storageItem.getItem()).getPath() + result.getOrCreateTag().toString().toLowerCase(Locale.ROOT).replaceAll("[{\",}:>=@\\[\\]\\s]", "_"));
				itemGroupRecipes.add(constructRecipe.construct(recipe, id, ingredientsCopy, result));
			});
			return itemGroupRecipes;
		});
	}

	private static List<ItemStack> getStorageItems(CraftingRecipe recipe) {
		NonNullList<ItemStack> storageItems = NonNullList.create();
		for (Ingredient ingredient : recipe.getIngredients()) {
			ItemStack[] ingredientItems = ingredient.getItems();
			for (ItemStack ingredientItem : ingredientItems) {
				Item item = ingredientItem.getItem();
				if (item instanceof MovingStorageItem && MovingStorageItem.getStorageItem(ingredientItem).getItem() instanceof StorageBlockItem storageBlockItem) {
					storageItems.add(MovingStorageItem.getStorageItem(ingredientItem));
				}
			}
		}

		return storageItems;
	}

	private interface RecipeConstructor<T extends Recipe<?>> {
		CraftingRecipe construct(T originalRecipe, ResourceLocation id, NonNullList<Ingredient> ingredients, ItemStack result);
	}
}
