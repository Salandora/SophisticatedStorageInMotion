package net.p3pp3rf1y.sophisticatedstorageinmotion.common;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.event.common.PlayerEvents;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StackStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CommonEventHandler {
	private CommonEventHandler() {
	}

	public static void registerHandlers() {
		PlayerEvents.ITEM_CRAFTED.register(CommonEventHandler::onMovingStorageUncrafted);
		PlayerEvents.ITEM_CRAFTED.register(CommonEventHandler::onMovingStorageCraftedFromShulkerBox);
		UseEntityCallback.EVENT.register(TierUpgradeHandler::onTierUpgradeInteract);
		UseEntityCallback.EVENT.register(StorageToolHandler::onStorageToolInteract);
	}

	private static void onMovingStorageUncrafted(Player player, ItemStack result, Container craftMatrix) {
		if (player.level().isClientSide() || !(result.getItem() instanceof StorageBlockItem) || !isUncraftedFromSingleMovingStorage(player.getInventory())) {
			return;
		}

		@Nullable UUID storageId = result.sophisticatedCore_get(ModCoreDataComponents.STORAGE_UUID);

		if (storageId == null) {
			return;
		}

		MovingStorageData storageData = MovingStorageData.get(storageId);
		CompoundTag contents = storageData.getContents();
		contents.put(StorageWrapper.RENDER_INFO_TAG, result.sophisticatedCore_getOrDefault(ModCoreDataComponents.RENDER_INFO_TAG, CustomData.EMPTY).copyTag());
		CompoundTag fullContents = new CompoundTag();
		fullContents.put(StorageBlockEntity.STORAGE_WRAPPER_TAG, contents);

		ItemContentsStorage.get().setStorageContents(storageId, fullContents);

		storageData.removeStorageContents();
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

	private static void onMovingStorageCraftedFromShulkerBox(Player player, ItemStack result, Container craftMatrix) {
		Level level = player.level();

		if (level.isClientSide()) {
			return;
		}

		if (!isCraftedFromShulkerBox(craftMatrix)) {
			return;
		}

		ItemStack storageItem = MovingStorageItem.getStorageItem(result);
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
					MovingStorageItem.setStorageItem(result, storageItem);
					itemContentsStorage.removeStorageContents(id);
				});
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
