package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.util.ColorHelper;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.ITintableBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.EntityStorageHolder;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class MovingStorageItem extends ItemBase {
	public MovingStorageItem(Properties properties) {
		super(properties);
	}

	public static void setStorageItem(ItemStack movingStorageItem, ItemStack storageItem) {
		NBTHelper.setCompoundNBT(movingStorageItem, EntityStorageHolder.STORAGE_ITEM_TAG, storageItem.save(new CompoundTag()));
	}

	public static Optional<Item> getStorageItemType(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).map(ItemStack::getItem);
	}

	public static Optional<WoodType> getStorageItemWoodType(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).flatMap(WoodStorageBlockItem::getWoodType);
	}

	public static Optional<Integer> getStorageItemMainColor(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).flatMap(StorageBlockItem::getMainColorFromStack);
	}

	public static Optional<Integer> getStorageItemAccentColor(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).flatMap(StorageBlockItem::getAccentColorFromStack);
	}

	public static boolean isStorageItemFlatTopBarrel(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).map(BarrelBlockItem::isFlatTop).orElse(false);
	}

	public static ItemStack getStorageItem(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).orElse(ItemStack.EMPTY);
	}

	public abstract ItemStack getUncraftRemainingItem();

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		if (Config.COMMON.enabledItems.isItemEnabled(this)) {
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.BARREL_ITEM), WoodType.SPRUCE)));
			ItemStack limitedIStack = new ItemStack(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM);
			if (limitedIStack.getItem() instanceof ITintableBlockItem tintableBlockItem) {
				tintableBlockItem.setMainColor(limitedIStack, ColorHelper.getColor(DyeColor.YELLOW.getTextureDiffuseColors()));
				tintableBlockItem.setAccentColor(limitedIStack, ColorHelper.getColor(DyeColor.LIME.getTextureDiffuseColors()));
			}
			itemConsumer.accept(createWithStorage(this, limitedIStack));
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_COPPER_BARREL_2), WoodType.BIRCH)));
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_IRON_BARREL_3), WoodType.ACACIA)));
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_DIAMOND_BARREL_4), WoodType.CRIMSON)));
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.NETHERITE_CHEST_ITEM), WoodType.BAMBOO)));
			itemConsumer.accept(createWithStorage(this, new ItemStack(ModBlocks.IRON_SHULKER_BOX_ITEM)));
		}
	}

	public static ItemStack createWithStorage(Item movingStorageItem, ItemStack storageStack) {
		ItemStack movingStorage = new ItemStack(movingStorageItem);
		MovingStorageItem.setStorageItem(movingStorage, storageStack);
		return movingStorage;
	}

	@Override
	public Component getName(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).<Component>map(storageItem -> Component.translatable(getDescriptionId(), storageItem.getHoverName())).orElse(super.getName(stack));
	}
}
