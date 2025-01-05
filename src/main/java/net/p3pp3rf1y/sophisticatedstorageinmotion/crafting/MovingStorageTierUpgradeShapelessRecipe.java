package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
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

public class MovingStorageTierUpgradeShapelessRecipe extends ShapelessRecipe implements IWrapperRecipe<ShapelessRecipe> {
	public static final Set<ResourceLocation> REGISTERED_RECIPES = new LinkedHashSet<>();
	private final ShapelessRecipe compose;

	public MovingStorageTierUpgradeShapelessRecipe(ShapelessRecipe compose) {
		super(compose.getId(), compose.getGroup(), compose.category(), compose.result, compose.getIngredients());
		this.compose = compose;
		REGISTERED_RECIPES.add(compose.getId());
	}

	@Override
	public ShapelessRecipe getCompose() {
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
		return ModItems.MOVING_STORAGE_TIER_UPGRADE_SHAPELESS_RECIPE_SERIALIZER.get();
	}

	public static class Serializer extends RecipeWrapperSerializer<ShapelessRecipe, MovingStorageTierUpgradeShapelessRecipe> {
		public Serializer() {
			super(MovingStorageTierUpgradeShapelessRecipe::new, RecipeSerializer.SHAPELESS_RECIPE);
		}
	}
}
