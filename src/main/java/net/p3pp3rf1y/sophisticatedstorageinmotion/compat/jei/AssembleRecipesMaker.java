package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.jei;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageFromStorageRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AssembleRecipesMaker {
	private AssembleRecipesMaker() {}

	public static List<RecipeHolder<CraftingRecipe>> getShapelessCraftingRecipes(ItemBase item) {
		RecipeConstructor<MovingStorageFromStorageRecipe> constructRecipe = (originalRecipe, ingredients, result) -> new ShapelessRecipe("", CraftingBookCategory.MISC, result, ingredients);
		return getCraftingRecipes(item, constructRecipe, MovingStorageFromStorageRecipe.class);
	}

	@NotNull
	private static <T extends CraftingRecipe> List<RecipeHolder<CraftingRecipe>> getCraftingRecipes(ItemBase item, RecipeConstructor<T> constructRecipe, Class<T> originalRecipeClass) {
		return ClientRecipeHelper.transformAllRecipesOfTypeIntoMultiple(RecipeType.CRAFTING, originalRecipeClass, recipe -> {
			List<RecipeHolder<CraftingRecipe>> itemGroupRecipes = new ArrayList<>();

/*			List<ItemStack> results = new ArrayList<>();
			item.addCreativeTabItems(results::add);

			for (ItemStack result : results) {
				ItemStack storageItem = StorageMinecartItem.getStorageItem(result);

				NonNullList<Ingredient> ingredients = recipe.getIngredients();
				NonNullList<Ingredient> ingredientsCopy = NonNullList.createWithCapacity(ingredients.size());
				int i = 0;
				for (Ingredient ingredient : ingredients) {
					if (ingredient.getValues().length > 0 && ingredient.getValues()[0] instanceof Ingredient.ItemValue itemValue && itemValue.item().is(ModBlocks.ALL_STORAGE_TAG)) {
						ingredientsCopy.add(i, Ingredient.of(storageItem));
					} else {
						ingredientsCopy.add(i, ingredient);
					}
					i++;
				}
				ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "assemble_moving_storage_" + BuiltInRegistries.ITEM.getKey(result.getItem()).getPath() + result.getComponentsPatch().toString().toLowerCase(Locale.ROOT).replaceAll("[{\",}:>=@\\[\\]\\s]", "_"));
				itemGroupRecipes.add(new RecipeHolder<>(id, constructRecipe.construct(recipe, ingredientsCopy, result)));
			}*/

			int storageIngredientIndex = -1;

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

			NonNullList<Ingredient> ingredientsTemplate = NonNullList.createWithCapacity(ingredients.size());
			List<ItemStack> storageItems = new ArrayList<>();
			int i = 0;
			for (Ingredient ingredient : ingredients) {
				if (ingredient.sophisticated_getValues().length > 0 && ingredient.sophisticated_getValues()[0] instanceof Ingredient.ItemValue itemValue && itemValue.item().getItem() instanceof StorageBlockItem) {
					storageItems = expandStorageItems(ingredient.getItems());
					storageIngredientIndex = i;
					ingredientsTemplate.add(i, Ingredient.EMPTY);
				} else {
					ingredientsTemplate.add(i, ingredient);
					if (!ingredient.isEmpty()) {
						ItemStack[] ingredientItems = ingredient.getItems();
						craftinginventory.setItem(i, ingredientItems[0]);
					}
				}
				i++;
			}

			for (ItemStack storageItem : storageItems) {
				NonNullList<Ingredient> ingredientsCopy = NonNullList.createWithCapacity(ingredientsTemplate.size());
				ingredientsCopy.addAll(ingredientsTemplate);
				ingredientsCopy.set(storageIngredientIndex, Ingredient.of(storageItem));
				craftinginventory.setItem(storageIngredientIndex, storageItem.copy());

				ItemStack result = ClientRecipeHelper.assemble(recipe, craftinginventory.asCraftInput());
				ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "assemble_moving_storage_" + BuiltInRegistries.ITEM.getKey(result.getItem()).getPath() + result.getComponentsPatch().toString().toLowerCase(Locale.ROOT).replaceAll("[{\",}:>=@\\[\\]\\s]", "_"));
				itemGroupRecipes.add(new RecipeHolder<>(id, constructRecipe.construct(recipe, ingredientsCopy, result)));
			}

			return itemGroupRecipes;
		});
	}

	private static List<ItemStack> expandStorageItems(ItemStack[] items) {
		List<ItemStack> storageItems = new ArrayList<>();
		Set<Item> alreadyExpanded = new HashSet<>();

		for (ItemStack item : items) {
			if (!alreadyExpanded.add(item.getItem())) {
				continue;
			}

			if (item.getItem() instanceof StorageBlockItem storageBlockItem) {
				storageBlockItem.addCreativeTabItems(storageItems::add);
			}
		}

		return storageItems;
	}

	private interface RecipeConstructor<T extends Recipe<?>> {
		CraftingRecipe construct(T originalRecipe, NonNullList<Ingredient> ingredients, ItemStack result);
	}
}
