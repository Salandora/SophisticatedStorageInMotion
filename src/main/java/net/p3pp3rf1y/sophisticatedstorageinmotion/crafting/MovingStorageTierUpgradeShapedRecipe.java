package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.IWrapperRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.RecipeWrapperSerializer;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class MovingStorageTierUpgradeShapedRecipe extends ShapedRecipe implements IWrapperRecipe<ShapedRecipe> {
	public static final Set<ResourceLocation> REGISTERED_RECIPES = new LinkedHashSet<>();
	private final ShapedRecipe compose;

	public MovingStorageTierUpgradeShapedRecipe(ShapedRecipe compose) {
		super(compose.getId(), compose.getGroup(), compose.category(), compose.getWidth(), compose.getHeight(), compose.getIngredients(), compose.result);
		this.compose = compose;
		REGISTERED_RECIPES.add(compose.getId());
	}

	@Override
	public ShapedRecipe getCompose() {
		return compose;
	}

	@Override
	public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
		ItemStack upgradedMovingStorage = super.assemble(input, registries);
		getOriginalMovingStorage(input).ifPresent(originalMovingStorage -> {
			ItemStack originalStorageItem = MovingStorageItem.getStorageItem(originalMovingStorage);
			ItemStack upgradedStorageItem = MovingStorageItem.getStorageItem(upgradedMovingStorage);
			upgradedStorageItem.setTag(originalStorageItem.getTag());
			NBTHelper.setInteger(upgradedStorageItem, StorageWrapper.NUMBER_OF_INVENTORY_SLOTS_TAG, MovingStorageWrapper.getDefaultNumberOfInventorySlots(upgradedStorageItem));
			NBTHelper.setInteger(upgradedStorageItem, StorageWrapper.NUMBER_OF_UPGRADE_SLOTS_TAG, MovingStorageWrapper.getDefaultNumberOfUpgradeSlots(upgradedStorageItem));
			MovingStorageItem.setStorageItem(upgradedMovingStorage, upgradedStorageItem);
		});
		return upgradedMovingStorage;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	private Optional<ItemStack> getOriginalMovingStorage(CraftingContainer input) {
		for (int slot = 0; slot < input.getContainerSize(); slot++) {
			ItemStack slotStack = input.getItem(slot);
			if (slotStack.getItem() instanceof MovingStorageItem) {
				return Optional.of(slotStack);
			}
		}

		return Optional.empty();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModItems.MOVING_STORAGE_TIER_UPGRADE_SHAPED_RECIPE_SERIALIZER.get();
	}

	public static class Serializer extends RecipeWrapperSerializer<ShapedRecipe, MovingStorageTierUpgradeShapedRecipe> {
		public Serializer() {
			super(MovingStorageTierUpgradeShapedRecipe::new, RecipeSerializer.SHAPED_RECIPE);
		}
	}
}
