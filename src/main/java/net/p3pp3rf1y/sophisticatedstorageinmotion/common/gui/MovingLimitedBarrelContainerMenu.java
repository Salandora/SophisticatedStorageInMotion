package net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;

import java.util.List;

public class MovingLimitedBarrelContainerMenu<T extends Entity & IMovingStorageEntity> extends MovingStorageContainerMenu<T> {
	public MovingLimitedBarrelContainerMenu(int containerId, Player player, int entityId) {
		super(ModEntities.MOVING_LIMITED_BARREL_CONTAINER_TYPE, containerId, player, entityId);
	}

	public static MovingLimitedBarrelContainerMenu<?> fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MovingLimitedBarrelContainerMenu<>(windowId, playerInventory.player, buffer.readInt());
	}

	@Override
	protected MovingStorageSettingsContainerMenu instantiateSettingsContainerMenu(int windowId, Player player, int entityId) {
		return new MovingLimitedBarrelSettingsContainerMenu(windowId, player, entityId);
	}

	@Override
	public List<Integer> getSlotOverlayColors(int slot) {
		return List.of();
	}
}
