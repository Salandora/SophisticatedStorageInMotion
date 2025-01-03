package net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ISyncedContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.MovingStorageContentsMessage;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.StorageInMotionPacketHandler;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;

public class MovingStorageContainerMenu<T extends Entity & IMovingStorageEntity> extends StorageContainerMenuBase<IStorageWrapper> implements ISyncedContainer {
	protected final WeakReference<T> storageEntity;

	@Nullable
	private CompoundTag lastSettingsNbt = null;

	public MovingStorageContainerMenu(int containerId, Player player, int entityId) {
		this(ModEntities.MOVING_STORAGE_CONTAINER_TYPE, containerId, player, entityId);
	}

	public MovingStorageContainerMenu(MenuType<?> menuType, int containerId, Player player, int entityId) {
		super(menuType, containerId, player, getWrapper(player.level(), entityId), NoopStorageWrapper.INSTANCE, -1, false);
		if (!(player.level().getEntity(entityId) instanceof IMovingStorageEntity movingStorageEntity)) {
			throw new IllegalArgumentException("Incorrect entity with id " + entityId + " expected to find IMovingStorageEntity");
		}
		storageEntity = new WeakReference<T>((T) movingStorageEntity);
		movingStorageEntity.getStorageHolder().startOpen(player);
	}

	public Optional<T> getStorageEntity() {
		return Optional.ofNullable(storageEntity.get());
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		getStorageEntity().ifPresent(storageEntity -> storageEntity.getStorageHolder().stopOpen(player));
	}

	private static IStorageWrapper getWrapper(Level level, int entityId) {
		if (!(level.getEntity(entityId) instanceof StorageMinecart storageMinecart)) {
			return NoopStorageWrapper.INSTANCE;
		}

		return storageMinecart.getStorageHolder().getStorageWrapper();
	}

	public static MovingStorageContainerMenu<?> fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MovingStorageContainerMenu<>(windowId, playerInventory.player, buffer.readInt());
	}

	@Override
	public Optional<BlockPos> getBlockPosition() {
		return Optional.empty();
	}

	@Override
	public Optional<Entity> getEntity() {
		return getStorageEntity().map(e -> e);
	}

	@Override
	protected StorageContainerMenuBase<IStorageWrapper>.StorageUpgradeSlot instantiateUpgradeSlot(UpgradeHandler upgradeHandler, int slotIndex) {
		return new StorageUpgradeSlot(upgradeHandler, slotIndex) {
			@Override
			protected void onUpgradeChanged() {
				if (player.level().isClientSide()) {
					return;
				}
				storageWrapper.getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class).itemsChanged();
			}
		};
	}

	@Override
	public void openSettings() {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			sendToServer(data -> data.putString(ACTION_TAG, "openSettings"));
			return;
		}
		getStorageEntity().ifPresent(entity ->
				NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((w, p, pl) -> instantiateSettingsContainerMenu(w, pl, entity.getId()),
						Component.translatable(StorageTranslationHelper.INSTANCE.translGui("settings.title"))), buffer -> buffer.writeInt(entity.getId()))
		);
	}

	protected MovingStorageSettingsContainerMenu instantiateSettingsContainerMenu(int windowId, Player player, int entityId) {
		return new MovingStorageSettingsContainerMenu(windowId, player, entityId);
	}

	@Override
	protected boolean storageItemHasChanged() {
		return false; //the stack is only used for internal tracking in moving entities so it can't be swapped away by a player
	}

	@Override
	public boolean detectSettingsChangeAndReload() {
		if (player.level().isClientSide) {
			return storageWrapper.getContentsUuid().map(uuid -> {
				MovingStorageData storage = MovingStorageData.get(uuid);
				if (storage.removeUpdatedStorageSettingsFlag(uuid)) {
					storageWrapper.getSettingsHandler().reloadFrom(storage.getContents().getCompound(MovingStorageWrapper.SETTINGS_TAG));
					return true;
				}
				return false;
			}).orElse(false);
		}
		return false;
	}

	@Override
	public boolean stillValid(Player player) {
		return getStorageEntity().map(se -> player.distanceToSqr(se.position()) <= 64.0D).orElse(false); //TODO if packing is allowed check if not packed here
	}

	@Override
	protected void sendStorageSettingsToClient() {
		if (player.level().isClientSide) {
			return;
		}

		if (lastSettingsNbt == null || !lastSettingsNbt.equals(storageWrapper.getSettingsHandler().getNbt())) {
			lastSettingsNbt = storageWrapper.getSettingsHandler().getNbt().copy();

			storageWrapper.getContentsUuid().ifPresent(uuid -> {
				CompoundTag settingsContents = new CompoundTag();
				CompoundTag settingsNbt = storageWrapper.getSettingsHandler().getNbt();
				if (!settingsNbt.isEmpty()) {
					settingsContents.put(MovingStorageWrapper.SETTINGS_TAG, settingsNbt);
					if (player instanceof ServerPlayer serverPlayer) {
						PacketHandler.sendToClient(serverPlayer, new MovingStorageContentsMessage(uuid, settingsContents));
					}
				}
			});
		}
	}

	public float getSlotFillPercentage(int slot) {
		IMovingStorageEntity entity = storageEntity.get();
		if (entity == null) {
			return 0;
		}
		List<Float> slotFillRatios = entity.getStorageHolder().getStorageWrapper().getRenderInfo().getItemDisplayRenderInfo().getSlotFillRatios();
		return slot > -1 && slot < slotFillRatios.size() ? slotFillRatios.get(slot) : 0;
	}
}
