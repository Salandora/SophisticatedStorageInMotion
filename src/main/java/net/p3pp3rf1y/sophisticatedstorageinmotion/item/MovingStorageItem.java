package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedstorage.block.ITintableBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.*;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModDataComponents;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class MovingStorageItem extends ItemBase {
	public MovingStorageItem(Properties properties) {
		super(properties);
	}

	public static void setStorageItem(ItemStack storageItem, ItemStack movingStorageItem) {
		movingStorageItem.sophisticatedCore_set(ModDataComponents.STORAGE_ITEM, SimpleItemContent.copyOf(storageItem));
	}

	public static Optional<Item> getStorageItemType(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.sophisticatedCore_get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents == null ? Optional.empty() : Optional.of(storageItemContents.getItem());
	}

	public static Optional<WoodType> getStorageItemWoodType(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.sophisticatedCore_get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents == null ? Optional.empty() : WoodStorageBlockItem.getWoodType(storageItemContents);
	}

	public static Optional<Integer> getStorageItemMainColor(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.sophisticatedCore_get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents == null ? Optional.empty() : StorageBlockItem.getMainColorFromComponentHolder(storageItemContents);
	}

	public static Optional<Integer> getStorageItemAccentColor(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.sophisticatedCore_get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents == null ? Optional.empty() : StorageBlockItem.getAccentColorFromComponentHolder(storageItemContents);
	}

	public static boolean isStorageItemFlatTopBarrel(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.sophisticatedCore_get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents != null && BarrelBlockItem.isFlatTop(storageItemContents);
	}

	public static ItemStack getStorageItem(ItemStack stack) {
		return stack.sophisticatedCore_getOrDefault(ModDataComponents.STORAGE_ITEM, SimpleItemContent.EMPTY).copy();
	}

	public abstract ItemStack getUncraftRemainingItem();

	@Override
	public void onCraftedPostProcess(ItemStack stack, Level level) {
		super.onCraftedPostProcess(stack, level);
		if (level.isClientSide()) {
			return;
		}
		ItemStack storageItem = MovingStorageItem.getStorageItem(stack);
		if (storageItem.getItem() instanceof ShulkerBoxItem) {
			StackStorageWrapper shulkerStorageWrapper = StackStorageWrapper.fromStack(level.registryAccess(), storageItem);
			shulkerStorageWrapper.getContentsUuid().ifPresent(id -> {
				ItemContentsStorage itemContentsStorage = ItemContentsStorage.get();
				CompoundTag contentsNbt = itemContentsStorage.getOrCreateStorageContents(id).getCompound(StorageBlockEntity.STORAGE_WRAPPER_TAG);
				CompoundTag migratedContentsNbt = new CompoundTag();
				migratedContentsNbt.put(StorageWrapper.CONTENTS_TAG, contentsNbt.getCompound(StorageWrapper.CONTENTS_TAG));
				migratedContentsNbt.put(StorageWrapper.SETTINGS_TAG, contentsNbt.getCompound(StorageWrapper.SETTINGS_TAG));
				MovingStorageData.get(id).setContents(migratedContentsNbt);
				storageItem.sophisticatedCore_set(ModCoreDataComponents.RENDER_INFO_TAG, CustomData.of(contentsNbt.getCompound(StorageWrapper.RENDER_INFO_TAG)));
				MovingStorageItem.setStorageItem(storageItem, stack);
				itemContentsStorage.removeStorageContents(id);
			});
			MovingStorageItem.setStorageItem(storageItem, stack);
		}
	}

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		if (Config.COMMON.enabledItems.isItemEnabled(this)) {
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.BARREL_ITEM.get()), WoodType.SPRUCE)));
			ItemStack limitedIStack = new ItemStack(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get());
			if (limitedIStack.getItem() instanceof ITintableBlockItem tintableBlockItem) {
				tintableBlockItem.setMainColor(limitedIStack, DyeColor.YELLOW.getTextureDiffuseColor());
				tintableBlockItem.setAccentColor(limitedIStack, DyeColor.LIME.getTextureDiffuseColor());
			}
			itemConsumer.accept(createWithStorage(limitedIStack));
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_COPPER_BARREL_2.get()), WoodType.BIRCH)));
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_IRON_BARREL_3.get()), WoodType.ACACIA)));
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_DIAMOND_BARREL_4.get()), WoodType.CRIMSON)));
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.NETHERITE_CHEST_ITEM.get()), WoodType.BAMBOO)));
			itemConsumer.accept(createWithStorage(new ItemStack(ModBlocks.IRON_SHULKER_BOX_ITEM.get())));
		}
	}

	private ItemStack createWithStorage(ItemStack storageStack) {
		ItemStack stack = new ItemStack(this);
		MovingStorageItem.setStorageItem(storageStack, stack);
		return stack;
	}

	@Override
	public Component getName(ItemStack stack) {
		SimpleItemContent storageItemContent = stack.sophisticatedCore_get(ModDataComponents.STORAGE_ITEM);
		return storageItemContent != null ? Component.translatable(getDescriptionId(), storageItemContent.copy().getHoverName()) : super.getName(stack);
	}
}
