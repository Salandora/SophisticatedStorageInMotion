package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.Optional;

public class UncraftMovingStorageRecipe extends CustomRecipe {

	public UncraftMovingStorageRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	private Optional<ItemStack> getMovingStorage(CraftingInput inv) {
		for (int slot = 0; slot < inv.size(); slot++) {
			ItemStack slotStack = inv.getItem(slot);
			if (slotStack.getItem() instanceof MovingStorageItem) {
				return Optional.of(slotStack);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean matches(CraftingInput inv, Level level) {
		boolean hasMovingStorage = false;
		for (int slot = 0; slot < inv.size(); slot++) {
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
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		return getMovingStorage(input).map(MovingStorageItem::getStorageItem).orElse(ItemStack.EMPTY);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 1;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
		NonNullList<ItemStack> remainingItems = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
		for (int slot = 0; slot < inv.size(); slot++) {
			ItemStack slotStack = inv.getItem(slot);
			if (slotStack.getItem() instanceof MovingStorageItem movingStorageItem) {
				remainingItems.set(slot, movingStorageItem.getUncraftRemainingItem(slotStack));
			}
		}
		return remainingItems;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModItems.UNCRAFT_MOVING_STORAGE_SERIALIZER.get();
	}
}
