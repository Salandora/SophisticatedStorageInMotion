package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.p3pp3rf1y.sophisticatedcore.util.BlockItemBase;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MovingStorageIngredient implements ICustomIngredient {
	public static final MapCodec<MovingStorageIngredient> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					ItemStack.ITEM_NON_AIR_CODEC.fieldOf("moving_storage_item").forGetter(ingredient -> ingredient.movingStorageItem),
							ItemStack.ITEM_NON_AIR_CODEC.fieldOf("storage_item").forGetter(ingredient -> ingredient.storageItem)
					)
					.apply(instance, MovingStorageIngredient::new)
	);
	private final Holder<Item> movingStorageItem;
	private final Holder<Item> storageItem;
	private final ItemStack[] movingStorageStacks;

	private MovingStorageIngredient(Holder<Item> movingStorageItem, Holder<Item> storageItem) {
		this.movingStorageItem = movingStorageItem;
		this.storageItem = storageItem;
		List<ItemStack> storageItemCreativeTabItems = new ArrayList<>();
		if (storageItem.value() instanceof BlockItemBase itemBase) {
			itemBase.addCreativeTabItems(storageItemCreativeTabItems::add);
		}
		List<ItemStack> movingStorages = new ArrayList<>();
		storageItemCreativeTabItems.forEach(storageItemStack -> {
			ItemStack movingStorageStack = new ItemStack(movingStorageItem);
			MovingStorageItem.setStorageItem(movingStorageStack, storageItemStack);
			movingStorages.add(movingStorageStack);
		});
		movingStorageStacks = movingStorages.toArray(new ItemStack[0]);
	}

	public static MovingStorageIngredient of(Holder<Item> movingStorageItem, Item storageItem) {
		return new MovingStorageIngredient(movingStorageItem, BuiltInRegistries.ITEM.getHolder(BuiltInRegistries.ITEM.getKey(storageItem)).orElseThrow());
	}

	@Override
	public boolean test(ItemStack itemStack) {
		return itemStack.getItem() instanceof MovingStorageItem && MovingStorageItem.getStorageItem(itemStack).getItem() == storageItem.value();
	}

	@Override
	public Stream<ItemStack> getItems() {
		return Stream.of(movingStorageStacks);
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public IngredientType<?> getType() {
		return ModItems.MOVING_STORAGE_INGREDIENT_TYPE.get();
	}
}
