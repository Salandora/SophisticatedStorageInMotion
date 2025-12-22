package net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.settings.StorageSettingsTabControlBase;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.OpenMovingStorageInventoryMessage;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.StorageInMotionPacketHandler;

public class MovingStorageSettingsScreen extends StorageSettingsScreen {
	private final int entityId;

	public MovingStorageSettingsScreen(SettingsContainerMenu<?> screenContainer, Inventory inv, Component title) {
		super(screenContainer, inv, title);
		this.entityId = screenContainer instanceof MovingStorageSettingsContainerMenu menu ? menu.getEntityId() : -1;
	}

	@Override
	protected StorageSettingsTabControlBase initializeTabControl() {
		return new MovingStorageSettingsTabControl(this, new Position(leftPos + imageWidth, topPos + 4));
	}

	@Override
	protected void sendStorageInventoryScreenOpenMessage() {
		StorageInMotionPacketHandler.INSTANCE.sendToServer(new OpenMovingStorageInventoryMessage(entityId));
	}

	public static MovingStorageSettingsScreen constructScreen(SettingsContainerMenu<?> screenContainer, Inventory inventory, Component title) {
		return new MovingStorageSettingsScreen(screenContainer, inventory, title);
	}
}
