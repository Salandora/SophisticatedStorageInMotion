package net.p3pp3rf1y.sophisticatedstorageinmotion.common;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.*;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageToolItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.EntityStorageHolder;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;

import java.util.function.Consumer;

public class StorageToolHandler {
	private StorageToolHandler() {
	}

	public static void onStorageToolInteract(PlayerInteractEvent.EntityInteract event) {
		Player player = event.getEntity();
		if (!(event.getTarget() instanceof IMovingStorageEntity movingStorageEntity)) {
			return;
		}

		InteractionResult result = InventoryHelper.getItemFromEitherHand(player, ModItems.STORAGE_TOOL.get())
				.map(storageTool -> tryStorageToolInteract(movingStorageEntity, storageTool)).orElse(InteractionResult.PASS);

		if (result.consumesAction()) {
			event.setCanceled(true);
			event.setCancellationResult(result);
		}
	}

	private static InteractionResult tryStorageToolInteract(IMovingStorageEntity movingStorageEntity, ItemStack storageTool) {
		StorageToolItem.Mode mode = StorageToolItem.getMode(storageTool);
		switch (mode) {
			case LOCK -> {
				if (tryToggling(movingStorageEntity, ILockable.class, ILockable::toggleLock)) {
					return InteractionResult.SUCCESS;
				}
			}
			case COUNT_DISPLAY -> {
				if (tryToggling(movingStorageEntity, ICountDisplay.class, ICountDisplay::toggleCountVisibility)) {
					return InteractionResult.SUCCESS;
				}
			}
			case LOCK_DISPLAY -> {
				if (tryToggling(movingStorageEntity, ILockable.class, ILockable::toggleLockVisibility)) {
					return InteractionResult.SUCCESS;
				}
			}
			case TIER_DISPLAY -> {
				if (tryToggling(movingStorageEntity, ITierDisplay.class, ITierDisplay::toggleTierVisiblity)) {
					return InteractionResult.SUCCESS;
				}
			}
			case UPGRADES_DISPLAY -> {
				if (tryToggling(movingStorageEntity, IUpgradeDisplay.class, IUpgradeDisplay::toggleUpgradesVisiblity)) {
					return InteractionResult.SUCCESS;
				}
			}
			case FILL_LEVEL_DISPLAY -> {
				if (tryToggling(movingStorageEntity, IFillLevelDisplay.class, IFillLevelDisplay::toggleFillLevelVisibility)) {
					return InteractionResult.SUCCESS;
				}
			}
		}
		return InteractionResult.PASS;
	}

	private static <T> boolean tryToggling(IMovingStorageEntity movingStorageEntity, Class<T> clazz, Consumer<T> doToggle) {
		EntityStorageHolder<?> storageHolder = movingStorageEntity.getStorageHolder();
		if (clazz.isInstance(storageHolder)) {
			doToggle.accept(clazz.cast(storageHolder));
			return true;
		}
		return false;
	}
}
