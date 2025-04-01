package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.util.BlockItemBase;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.ArrayList;
import java.util.List;

public class MovingStorageIngredient implements CustomIngredient {
	public static final CustomIngredientSerializer<MovingStorageIngredient> SERIALIZER = new Serializer();

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
		return itemStack.getItem() == movingStorageItem.value() && MovingStorageItem.getStorageItem(itemStack).getItem() == storageItem.value();
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		return List.of(movingStorageStacks);
	}

	@Override
	public boolean requiresTesting() {
		return true;
	}

	private Holder<Item> getMovingStorageItem() {
		return movingStorageItem;
	}

	private Holder<Item> getStorageItem() {
		return storageItem;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	private static class Serializer implements CustomIngredientSerializer<MovingStorageIngredient> {
		public static final MapCodec<MovingStorageIngredient> CODEC = RecordCodecBuilder.mapCodec(
				instance -> instance.group(
								ItemStack.ITEM_NON_AIR_CODEC.fieldOf("moving_storage_item").forGetter(ingredient -> ingredient.movingStorageItem),
								ItemStack.ITEM_NON_AIR_CODEC.fieldOf("storage_item").forGetter(ingredient -> ingredient.storageItem)
						)
						.apply(instance, MovingStorageIngredient::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, MovingStorageIngredient> PACKET_CODEC = StreamCodec.composite(
				ByteBufCodecs.holderRegistry(Registries.ITEM), MovingStorageIngredient::getMovingStorageItem,
				ByteBufCodecs.holderRegistry(Registries.ITEM), MovingStorageIngredient::getStorageItem,
				MovingStorageIngredient::new
		);

		@Override
		public ResourceLocation getIdentifier() {
			return SophisticatedStorageInMotion.getRL("moving_storage");
		}

		@Override
		public MapCodec<MovingStorageIngredient> getCodec(boolean allowEmpty) {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, MovingStorageIngredient> getPacketCodec() {
			return PACKET_CODEC;
		}
	}
}
