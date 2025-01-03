package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.Optional;

public class UncraftMovingStorageRecipe extends CustomRecipe {

	public UncraftMovingStorageRecipe(ResourceLocation registryName, CraftingBookCategory category) {
		super(registryName, category);
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	private Optional<ItemStack> getMovingStorage(CraftingContainer inv) {
		for (int slot = 0; slot < inv.getContainerSize(); slot++) {
			ItemStack slotStack = inv.getItem(slot);
			if (slotStack.getItem() instanceof MovingStorageItem) {
				return Optional.of(slotStack);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean matches(CraftingContainer inv, Level level) {
		boolean hasMovingStorage = false;
		for (int slot = 0; slot < inv.getContainerSize(); slot++) {
			ItemStack slotStack = inv.getItem(slot);
			if (!hasMovingStorage && slotStack.getItem() instanceof MovingStorageItem) {
				hasMovingStorage = true;
			} else if (!slotStack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
		return getMovingStorage(input).map(MovingStorageItem::getStorageItem).orElse(ItemStack.EMPTY);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 1;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
		NonNullList<ItemStack> remainingItems = NonNullList.create();
		getMovingStorage(input).ifPresent(movingStorage -> {
			if (movingStorage.getItem() instanceof MovingStorageItem movingStorageItem) {
				remainingItems.add(movingStorageItem.getUncraftRemainingItem());
			}
		});

		return remainingItems;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModItems.UNCRAFT_MOVING_STORAGE_SERIALIZER;
	}
}
