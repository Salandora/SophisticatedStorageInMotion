package net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;

public class MovingLimitedBarrelSettingsContainerMenu extends MovingStorageSettingsContainerMenu {
	protected MovingLimitedBarrelSettingsContainerMenu(int windowId, Player player, int entityId) {
		super(ModEntities.MOVING_LIMITED_BARREL_SETTINGS_CONTAINER_TYPE, windowId, player, entityId);
	}

	public static MovingLimitedBarrelSettingsContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MovingLimitedBarrelSettingsContainerMenu(windowId, playerInventory.player, buffer.readInt());
	}
}
