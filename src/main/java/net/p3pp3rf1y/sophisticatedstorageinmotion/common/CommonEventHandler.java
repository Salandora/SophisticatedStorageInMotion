package net.p3pp3rf1y.sophisticatedstorageinmotion.common;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.event.common.PlayerEvents;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.UUID;

public class CommonEventHandler {
	private CommonEventHandler() {
	}

	public static void registerHandlers() {
		PlayerEvents.ITEM_CRAFTED.register(CommonEventHandler::onMovingStorageUncrafted);
		eventBus.addListener(CommonEventHandler::onMovingStorageCraftedFromShulkerBox);
		UseEntityCallback.EVENT.register(TierUpgradeHandler::onTierUpgradeInteract);
	}

	private static void onMovingStorageUncrafted(Player player, ItemStack result, Container craftMatrix) {
		if (player.level().isClientSide() || !(result.getItem() instanceof StorageBlockItem) || !isUncraftedFromSingleMovingStorage(player.getInventory())) {
			return;
		}

		NBTHelper.getUniqueId(result, StorageWrapper.UUID_TAG).ifPresent(storageId -> {
			MovingStorageData storageData = MovingStorageData.get(storageId);
			CompoundTag contents = storageData.getContents();
			contents.put(StorageWrapper.RENDER_INFO_TAG, NBTHelper.getCompound(result, StorageWrapper.RENDER_INFO_TAG).orElse(new CompoundTag()));
			CompoundTag fullContents = new CompoundTag();
			fullContents.put(StorageBlockEntity.STORAGE_WRAPPER_TAG, contents);

			ItemContentsStorage.get().setStorageContents(storageId, fullContents);

			storageData.removeStorageContents();
		});
	}

	private static boolean isUncraftedFromSingleMovingStorage(Container inventory) {
		boolean hasMovingStorage = false;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);

			if (!hasMovingStorage && stack.getItem() instanceof MovingStorageItem) {
				hasMovingStorage = true;
			} else if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private static void onMovingStorageCraftedFromShulkerBox(PlayerEvent.ItemCraftedEvent event) {
		Level level = event.getEntity().level();
		ItemStack result = event.getCrafting();

		if (level.isClientSide()) {
			return;
		}

		if (!isCraftedFromShulkerBox(event.getInventory())) {
			return;
		}

		ItemStack storageItem = MovingStorageItem.getStorageItem(result);
		if (storageItem.getItem() instanceof ShulkerBoxItem) {
			UUID uuid = NBTHelper.getUniqueId(storageItem, "uuid").orElse(null);
			if (uuid != null) {
				ItemContentsStorage itemContentsStorage = ItemContentsStorage.get();
				CompoundTag contentsNbt = itemContentsStorage.getOrCreateStorageContents(uuid).getCompound(StorageBlockEntity.STORAGE_WRAPPER_TAG);
				CompoundTag migratedContentsNbt = new CompoundTag();
				migratedContentsNbt.put(StorageWrapper.CONTENTS_TAG, contentsNbt.getCompound(StorageWrapper.CONTENTS_TAG));
				migratedContentsNbt.put(StorageWrapper.SETTINGS_TAG, contentsNbt.getCompound(StorageWrapper.SETTINGS_TAG));
				MovingStorageData.get(uuid).setContents(migratedContentsNbt);
				storageItem.getOrCreateTag().put(StorageWrapper.RENDER_INFO_TAG, contentsNbt.getCompound(StorageWrapper.RENDER_INFO_TAG));
				MovingStorageItem.setStorageItem(storageItem, result);
				itemContentsStorage.removeStorageContents(uuid);
			}
			MovingStorageItem.setStorageItem(result, storageItem);
		}
	}

	private static boolean isCraftedFromShulkerBox(Container craftingGrid) {
		boolean foundShulker = false;
		for (int slot = 0; slot < craftingGrid.getContainerSize(); slot++) {
			if (craftingGrid.getItem(slot).getItem() instanceof ShulkerBoxItem) {
				foundShulker = true;
			}
		}
		return foundShulker;
	}
}
