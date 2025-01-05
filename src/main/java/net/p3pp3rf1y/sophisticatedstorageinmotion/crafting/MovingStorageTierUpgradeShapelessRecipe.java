package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.IWrapperRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.RecipeWrapperSerializer;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.Optional;

public class MovingStorageTierUpgradeShapelessRecipe extends ShapelessRecipe implements IWrapperRecipe<ShapelessRecipe> {
	private final ShapelessRecipe compose;

	public MovingStorageTierUpgradeShapelessRecipe(ShapelessRecipe compose) {
		super(compose.getGroup(), compose.category(), compose.result, compose.getIngredients());
		this.compose = compose;
	}

	@Override
	public ShapelessRecipe getCompose() {
		return compose;
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		ItemStack upgradedMovingStorage = super.assemble(input, registries);
		getOriginalMovingStorage(input).ifPresent(originalMovingStorage -> {
			ItemStack originalStorageItem = MovingStorageItem.getStorageItem(originalMovingStorage);
			ItemStack upgradedStorageItem = MovingStorageItem.getStorageItem(upgradedMovingStorage);
			upgradedStorageItem.applyComponents(originalStorageItem.getComponents());
			upgradedStorageItem.set(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS, MovingStorageWrapper.getDefaultNumberOfInventorySlots(upgradedMovingStorage));
			upgradedStorageItem.set(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS, MovingStorageWrapper.getDefaultNumberOfUpgradeSlots(upgradedMovingStorage));
			MovingStorageItem.setStorageItem(upgradedMovingStorage, upgradedStorageItem);
		});
		return upgradedMovingStorage;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	private Optional<ItemStack> getOriginalMovingStorage(CraftingInput input) {
		for (int slot = 0; slot < input.size(); slot++) {
			ItemStack slotStack = input.getItem(slot);
			if (slotStack.getItem() instanceof MovingStorageItem) {
				return Optional.of(slotStack);
			}
		}

		return Optional.empty();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModItems.MOVING_STORAGE_TIER_UPGRADE_SHAPELESS_RECIPE_SERIALIZER.get();
	}

	public static class Serializer extends RecipeWrapperSerializer<ShapelessRecipe, MovingStorageTierUpgradeShapelessRecipe> {
		public Serializer() {
			super(MovingStorageTierUpgradeShapelessRecipe::new, RecipeSerializer.SHAPELESS_RECIPE);
		}
	}
}
