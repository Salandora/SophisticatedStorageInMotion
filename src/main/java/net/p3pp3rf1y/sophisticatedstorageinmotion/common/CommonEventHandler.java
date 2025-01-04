package net.p3pp3rf1y.sophisticatedstorageinmotion.common;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.p3pp3rf1y.sophisticatedcore.event.common.PlayerEvents;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
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
		UseEntityCallback.EVENT.register(TierUpgradeHandler::onTierUpgradeInteract);
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
}
