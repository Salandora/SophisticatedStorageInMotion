package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedcore.util.BlockItemBase;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MovingStorageIngredient extends AbstractIngredient {
	private final Holder<Item> storageItem;
	private final Holder<? extends Item> movingStorageItem;

	private MovingStorageIngredient(Holder<? extends Item> movingStorageItem, Holder<Item> storageItem) {
		super(getMovingStorageStacks(movingStorageItem, storageItem));

		this.storageItem = storageItem;
		this.movingStorageItem = movingStorageItem;
	}

	private static Stream<? extends Value> getMovingStorageStacks(Holder<? extends Item> movingStorageItem, Holder<Item> storageItem) {
		final ItemStack[] movingStorageStacks;
		List<ItemStack> storageItemCreativeTabItems = new ArrayList<>();
		if (storageItem.value() instanceof BlockItemBase itemBase) {
			itemBase.addCreativeTabItems(storageItemCreativeTabItems::add);
		}
		List<ItemStack> movingStorages = new ArrayList<>();
		storageItemCreativeTabItems.forEach(storageItemStack -> {
			ItemStack movingStorageStack = new ItemStack(movingStorageItem.get());
			MovingStorageItem.setStorageItem(movingStorageStack, storageItemStack);
			movingStorages.add(movingStorageStack);
		});
		movingStorageStacks = movingStorages.toArray(new ItemStack[0]);
		return Stream.of(movingStorageStacks).map(Ingredient.ItemValue::new);
	}

	public static MovingStorageIngredient of(Holder<? extends Item> movingStorageItem, Item storageItem) {
		return new MovingStorageIngredient(movingStorageItem, ForgeRegistries.ITEMS.getHolder(ForgeRegistries.ITEMS.getKey(storageItem)).orElseThrow());
	}

	@Override
	public boolean test(@Nullable ItemStack itemStack) {
		return itemStack != null && itemStack.getItem() instanceof MovingStorageItem && MovingStorageItem.getStorageItem(itemStack).getItem() == storageItem.value();
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("type", CraftingHelper.getID(Serializer.INSTANCE).toString());
		json.addProperty("movingStorageItem", ForgeRegistries.ITEMS.getKey(movingStorageItem.value()).toString());
		json.addProperty("storageItem", ForgeRegistries.ITEMS.getKey(storageItem.value()).toString());

		return json;
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return Serializer.INSTANCE;
	}

	public static class Serializer implements IIngredientSerializer<MovingStorageIngredient> {
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public MovingStorageIngredient parse(FriendlyByteBuf buffer) {
			return new MovingStorageIngredient(fromRegistryName(buffer.readUtf()), fromRegistryName(buffer.readUtf()));
		}

		@Override
		public MovingStorageIngredient parse(JsonObject json) {
			return new MovingStorageIngredient(fromRegistryName(json.get("movingStorageItem").getAsString()), fromRegistryName(json.get("storageItem").getAsString()));
		}

		@Override
		public void write(FriendlyByteBuf buffer, MovingStorageIngredient ingredient) {
			buffer.writeUtf(ForgeRegistries.ITEMS.getKey(ingredient.movingStorageItem.value()).toString());
			buffer.writeUtf(ForgeRegistries.ITEMS.getKey(ingredient.storageItem.value()).toString());
		}

		private Holder<Item> fromRegistryName(String registryName) {
			return ForgeRegistries.ITEMS.getHolder(new ResourceLocation(registryName)).orElseThrow();
		}
	}
}
