package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.util.BlockItemBase;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MovingStorageIngredient implements CustomIngredient {
	private final Holder<Item> storageItem;
	private final Holder<? extends Item> movingStorageItem;

	private MovingStorageIngredient(Holder<? extends Item> movingStorageItem, Holder<Item> storageItem) {
		super();

		this.storageItem = storageItem;
		this.movingStorageItem = movingStorageItem;
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		List<ItemStack> storageItemCreativeTabItems = new ArrayList<>();
		if (storageItem.value() instanceof BlockItemBase itemBase) {
			itemBase.addCreativeTabItems(storageItemCreativeTabItems::add);
		}
		List<ItemStack> movingStorages = new ArrayList<>();
		storageItemCreativeTabItems.forEach(storageItemStack -> {
			ItemStack movingStorageStack = new ItemStack(movingStorageItem.value());
			MovingStorageItem.setStorageItem(movingStorageStack, storageItemStack);
			movingStorages.add(movingStorageStack);
		});

		return movingStorages;
	}

	public static MovingStorageIngredient of(Holder<? extends Item> movingStorageItem, Item storageItem) {
		return new MovingStorageIngredient(movingStorageItem, BuiltInRegistries.ITEM.wrapAsHolder(storageItem));
	}

	@Override
	public boolean test(@Nullable ItemStack itemStack) {
		return itemStack != null && itemStack.getItem() instanceof MovingStorageItem && MovingStorageItem.getStorageItem(itemStack).getItem() == storageItem.value();
	}

	@Override
	public boolean requiresTesting() {
		return false;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	public static class Serializer implements CustomIngredientSerializer<MovingStorageIngredient> {
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public ResourceLocation getIdentifier() {
			return SophisticatedStorage.getRL("moving_storage");
		}

		@Override
		public MovingStorageIngredient read(FriendlyByteBuf buffer) {
			return new MovingStorageIngredient(fromRegistryName(buffer.readInt()), fromRegistryName(buffer.readInt()));
		}

		@Override
		public MovingStorageIngredient read(JsonObject json) {
			return new MovingStorageIngredient(fromRegistryName(json.get("movingStorageItem").getAsInt()), fromRegistryName(json.get("storageItem").getAsInt()));
		}

		@Override
		public void write(JsonObject json, MovingStorageIngredient ingredient) {
			json.addProperty("movingStorageItem", BuiltInRegistries.ITEM.getId(ingredient.movingStorageItem.value()));
			json.addProperty("storageItem", BuiltInRegistries.ITEM.getId(ingredient.storageItem.value()));
		}

		@Override
		public void write(FriendlyByteBuf buffer, MovingStorageIngredient ingredient) {
			buffer.writeInt(BuiltInRegistries.ITEM.getId(ingredient.movingStorageItem.value()));
			buffer.writeInt(BuiltInRegistries.ITEM.getId(ingredient.storageItem.value()));
		}

		private Holder<Item> fromRegistryName(int id) {
			return BuiltInRegistries.ITEM.getHolder(id).orElseThrow();
		}
	}
}
