package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.util.ColorHelper;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.*;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.*;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.EntityStorageHolder;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class MovingStorageItem extends ItemBase {
	public MovingStorageItem(Properties properties) {
		super(properties);
	}

	public static void setStorageItem(ItemStack storageItem, ItemStack movingStorageItem) {
		NBTHelper.setCompoundNBT(movingStorageItem, EntityStorageHolder.STORAGE_ITEM_TAG, storageItem.serializeNBT());
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
	public void onCraftedBy(ItemStack stack, Level level, Player player) {
		super.onCraftedBy(stack, level, player);
		if (level.isClientSide()) {
			return;
		}
		ItemStack storageItem = MovingStorageItem.getStorageItem(stack);
		if (storageItem.getItem() instanceof ShulkerBoxItem) {
			StackStorageWrapper shulkerStorageWrapper = new StackStorageWrapper(storageItem) {
				@Override
				public String getStorageType() {
					return "shulker_box";
				}

				@Override
				public Component getDisplayName() {
					return Component.translatable(storageItem.getDescriptionId());
				}

				@Override
				protected boolean isAllowedInStorage(ItemStack stack) {
					Block block = Block.byItem(stack.getItem());
					return !(block instanceof ShulkerBoxBlock) && !(block instanceof net.minecraft.world.level.block.ShulkerBoxBlock) && !net.p3pp3rf1y.sophisticatedstorage.Config.SERVER.shulkerBoxDisallowedItems.isItemDisallowed(stack.getItem());
				}
			};
			shulkerStorageWrapper.getContentsUuid().ifPresent(id -> {
				ItemContentsStorage itemContentsStorage = ItemContentsStorage.get();
				CompoundTag contentsNbt = itemContentsStorage.getOrCreateStorageContents(id).getCompound(StorageBlockEntity.STORAGE_WRAPPER_TAG);
				CompoundTag migratedContentsNbt = new CompoundTag();
				migratedContentsNbt.put(StorageWrapper.CONTENTS_TAG, contentsNbt.getCompound(StorageWrapper.CONTENTS_TAG));
				migratedContentsNbt.put(StorageWrapper.SETTINGS_TAG, contentsNbt.getCompound(StorageWrapper.SETTINGS_TAG));
				MovingStorageData.get(id).setContents(migratedContentsNbt);
				storageItem.getOrCreateTag().put(StorageWrapper.RENDER_INFO_TAG, contentsNbt.getCompound(StorageWrapper.RENDER_INFO_TAG));
				MovingStorageItem.setStorageItem(storageItem, stack);
				itemContentsStorage.removeStorageContents(id);
			});
			MovingStorageItem.setStorageItem(storageItem, stack);
		}
	}

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		if (Config.COMMON.enabledItems.isItemEnabled(this)) {
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.BARREL_ITEM), WoodType.SPRUCE)));
			ItemStack limitedIStack = new ItemStack(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM);
			if (limitedIStack.getItem() instanceof ITintableBlockItem tintableBlockItem) {
				tintableBlockItem.setMainColor(limitedIStack, ColorHelper.getColor(DyeColor.YELLOW.getTextureDiffuseColors()));
				tintableBlockItem.setAccentColor(limitedIStack, ColorHelper.getColor(DyeColor.LIME.getTextureDiffuseColors()));
			}
			itemConsumer.accept(createWithStorage(limitedIStack));
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_COPPER_BARREL_2), WoodType.BIRCH)));
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_IRON_BARREL_3), WoodType.ACACIA)));
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_DIAMOND_BARREL_4), WoodType.CRIMSON)));
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.NETHERITE_CHEST_ITEM), WoodType.BAMBOO)));
			itemConsumer.accept(createWithStorage(new ItemStack(ModBlocks.IRON_SHULKER_BOX_ITEM)));
		}
	}

	private ItemStack createWithStorage(ItemStack storageStack) {
		ItemStack stack = new ItemStack(this);
		MovingStorageItem.setStorageItem(storageStack, stack);
		return stack;
	}

	@Override
	public Component getName(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).<Component>map(storageItem -> Component.translatable(getDescriptionId(), storageItem.getHoverName())).orElse(super.getName(stack));
	}
}
