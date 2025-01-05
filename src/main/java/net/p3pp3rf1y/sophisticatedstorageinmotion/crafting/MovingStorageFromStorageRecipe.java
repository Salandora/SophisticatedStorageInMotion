package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.crafting.IWrapperRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.RecipeWrapperSerializer;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.Optional;

public class MovingStorageFromStorageRecipe extends ShapelessRecipe implements IWrapperRecipe<ShapelessRecipe> {
	private final ShapelessRecipe compose;

	public MovingStorageFromStorageRecipe(ShapelessRecipe compose) {
		super(compose.getGroup(), compose.category(), compose.result, compose.getIngredients());
		this.compose = compose;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return super.matches(input, level) && getStorage(input).map(c -> !WoodStorageBlockItem.isPacked(c)).orElse(false);
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	private Optional<ItemStack> getStorage(CraftingInput inv) {
		for (int slot = 0; slot < inv.size(); slot++) {
			ItemStack slotStack = inv.getItem(slot);
			if (slotStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof StorageBlockBase) {
				return Optional.of(slotStack);
			}
		}
		return Optional.empty();
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		ItemStack movingStorageItem = super.assemble(input, registries);
		getStorage(input).ifPresent(storage -> MovingStorageItem.setStorageItem(movingStorageItem, storage));
		return movingStorageItem;
	}

	@Override
	public ShapelessRecipe getCompose() {
		return compose;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModItems.MOVING_STORAGE_FROM_STORAGE_SERIALIZER.get();
	}

	public static class Serializer extends RecipeWrapperSerializer<ShapelessRecipe, MovingStorageFromStorageRecipe> {
		public Serializer() {
			super(MovingStorageFromStorageRecipe::new, RecipeSerializer.SHAPELESS_RECIPE);
		}
	}
}
