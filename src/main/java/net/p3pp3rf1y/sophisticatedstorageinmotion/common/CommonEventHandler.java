package net.p3pp3rf1y.sophisticatedstorageinmotion.common;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.p3pp3rf1y.sophisticatedcore.event.common.PlayerEvents;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.PaintbrushItem;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
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
		UseEntityCallback.EVENT.register(CommonEventHandler::onPacked);
		UseEntityCallback.EVENT.register(CommonEventHandler::onPaintbrushInteract);
		UseEntityCallback.EVENT.register(CommonEventHandler::onStorageUpgradeInteract);
	}

	private static InteractionResult onStorageUpgradeInteract(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		ItemStack itemInHand = player.getItemInHand(hand);
		if (!(entity instanceof IMovingStorageEntity movingStorage) || !(itemInHand.getItem() instanceof UpgradeItemBase<?>)) {
			return InteractionResult.PASS;
		}

		if (StorageBlockBase.tryAddSingleUpgrade(player, hand, itemInHand, movingStorage.getStorageHolder().getStorageWrapper())) {
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private static InteractionResult onPaintbrushInteract(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		ItemStack itemInHand = player.getItemInHand(hand);
		if (!(entity instanceof IMovingStorageEntity movingStorage) || itemInHand.getItem() != ModItems.PAINTBRUSH) {
			return InteractionResult.PASS;
		}

		if (!(movingStorage.getStorageItem().getItem() instanceof BlockItem blockItem)) {
			return InteractionResult.PASS;
		}
		BlockState state = blockItem.getBlock().defaultBlockState();
		SoundEvent placeSound = state.getSoundType().getPlaceSound();
		if (PaintbrushItem.paint(player, itemInHand, movingStorage.getStorageHolder(), movingStorage.getStorageHolder().getStorageWrapper(),
				entity.position(), Direction.UP, placeSound)) {
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private static InteractionResult onPacked(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		ItemStack itemInHand = player.getItemInHand(hand);
		if (!(entity instanceof IMovingStorageEntity movingStorage) || itemInHand.getItem() != ModItems.PACKING_TAPE || Config.COMMON.dropPacked.get()) {
			return InteractionResult.PASS;
		}

		if (movingStorage.getStorageHolder().pack()) {
			if (!player.isCreative()) {
				itemInHand.setDamageValue(itemInHand.getDamageValue() + 1);
				if (itemInHand.getDamageValue() >= itemInHand.getMaxDamage()) {
					player.setItemInHand(hand, ItemStack.EMPTY);
				}
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
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
