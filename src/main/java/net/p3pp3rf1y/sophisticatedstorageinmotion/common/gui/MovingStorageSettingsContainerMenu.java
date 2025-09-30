package net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui;

import com.github.salandora.sophisticatedlibrary.network.PacketDistributor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.MovingStorageContentsPayload;

public class MovingStorageSettingsContainerMenu extends SettingsContainerMenu<IStorageWrapper> {
	private final int entityId;
	private CompoundTag lastSettingsNbt = null;

	protected MovingStorageSettingsContainerMenu(int windowId, Player player, int entityId) {
		this(ModEntities.MOVING_STORAGE_SETTINGS_CONTAINER_TYPE.get(), windowId, player, entityId);
	}

	protected MovingStorageSettingsContainerMenu(MenuType<?> menuType, int windowId, Player player, int entityId) {
		super(menuType, windowId, player, getWrapper(player.level(), entityId));
		this.entityId = entityId;
	}

	private static IStorageWrapper getWrapper(Level level, int entityId) {
		if (!(level.getEntity(entityId) instanceof StorageMinecart storageMinecart)) {
			return NoopStorageWrapper.INSTANCE;
		}

		return storageMinecart.getStorageHolder().getStorageWrapper();
	}

	@Override
	public void detectSettingsChangeAndReload() {
		if (player.level().isClientSide) {
			storageWrapper.getContentsUuid().ifPresent(uuid -> {
				MovingStorageData storage = MovingStorageData.get(uuid);
				if (storage.removeUpdatedStorageSettingsFlag(uuid)) {
					storageWrapper.getSettingsHandler().reloadFrom(storage.getContents().getCompound(MovingStorageWrapper.SETTINGS_TAG));
				}
			});
		}
	}

	public static MovingStorageSettingsContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MovingStorageSettingsContainerMenu(windowId, playerInventory.player, buffer.readInt());
	}

	public int getEntityId() {
		return entityId;
	}

	private void sendStorageSettingsToClient() {
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
						PacketDistributor.sendToPlayer(serverPlayer, new MovingStorageContentsPayload(uuid, settingsContents));
					}
				}
			});
		}
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		sendStorageSettingsToClient();
	}
}
