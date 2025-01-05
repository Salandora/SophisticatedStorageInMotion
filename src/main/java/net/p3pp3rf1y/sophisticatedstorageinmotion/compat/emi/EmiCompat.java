package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.p3pp3rf1y.sophisticatedcore.compat.emi.EmiGridMenuInfo;
import net.p3pp3rf1y.sophisticatedcore.compat.emi.EmiSettingsGhostDragDropHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.emi.EmiStorageGhostDragDropHandler;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

@EmiEntrypoint
public class EmiCompat implements EmiPlugin {
	@Override
	public void register(EmiRegistry registry) {
		registry.addExclusionArea(MovingStorageScreen.class, (screen, consumer) -> {
			screen.getUpgradeSlotsRectangle().ifPresent(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
			screen.getUpgradeSettingsControl().getTabRectangles().forEach(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
			screen.getSortButtonsRectangle().ifPresent(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
		});

		registry.addExclusionArea(MovingStorageSettingsScreen.class, (screen, consumer) -> {
			if (screen == null || screen.getSettingsTabControl() == null) { // Due to how Emi collects the exclusion area this can be null
				return;
			}
			screen.getSettingsTabControl().getTabRectangles().forEach(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
		});

		registry.addDragDropHandler(MovingStorageScreen.class, new EmiStorageGhostDragDropHandler<>());
		registry.addDragDropHandler(MovingStorageSettingsScreen.class, new EmiSettingsGhostDragDropHandler<>());

		registerRecipes(registry, AssembleRecipesMaker.getShapelessCraftingRecipes(ModItems.STORAGE_MINECART.get()));

		Comparison movingStorageComparison = Comparison.compareData(emiStack -> {
			CompoundTag tag = new CompoundTag();
			ItemStack stack = emiStack.getItemStack();
			MovingStorageItem.getStorageItemType(stack).ifPresent(storageItemType -> tag.putString("storageItemType", storageItemType.toString()));
			MovingStorageItem.getStorageItemWoodType(stack).ifPresent(woodName -> tag.putString("woodName", woodName.name()));
			MovingStorageItem.getStorageItemMainColor(stack).ifPresent(mainColor -> tag.putInt("mainColor", mainColor));
			MovingStorageItem.getStorageItemAccentColor(stack).ifPresent(accentColor -> tag.putInt("accentColor", accentColor));
			tag.putBoolean("flatTop", MovingStorageItem.isStorageItemFlatTopBarrel(stack));
			return tag;
		});

		registry.setDefaultComparison(ModItems.STORAGE_MINECART.get(), movingStorageComparison);

		registry.addRecipeHandler(ModEntities.MOVING_STORAGE_CONTAINER_TYPE.get(), EmiGridMenuInfo.crafting());
		registry.addRecipeHandler(ModEntities.MOVING_STORAGE_CONTAINER_TYPE.get(), EmiGridMenuInfo.stonecutting());
		registry.addRecipeHandler(ModEntities.MOVING_STORAGE_CONTAINER_TYPE.get(), EmiGridMenuInfo.smithing());
	}

	private static void registerRecipes(EmiRegistry registry, Iterable<RecipeHolder<CraftingRecipe>> recipes) {
		recipes.forEach(holder -> {
			Recipe<?> recipe = holder.value();
			NonNullList<Ingredient> ingredients = recipe.getIngredients();
			NonNullList<EmiIngredient> ingredientsCopy = NonNullList.createWithCapacity(ingredients.size());
			ingredients.forEach(ingredient -> ingredientsCopy.add(EmiIngredient.of(ingredient)));
			registry.addRecipe(new EmiCraftingRecipe(
					ingredientsCopy,
					EmiStack.of(recipe.getResultItem(null)),
					ResourceLocation.fromNamespaceAndPath(holder.id().getNamespace(), "/" + holder.id().getPath())
				)
			);
		});
	}
}
