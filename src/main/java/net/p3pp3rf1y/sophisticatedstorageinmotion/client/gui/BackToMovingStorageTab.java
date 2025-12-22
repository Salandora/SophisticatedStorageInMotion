package net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui;

import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.Tab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ImageButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.*;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.OpenMovingStorageInventoryMessage;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.StorageInMotionPacketHandler;

public class BackToMovingStorageTab extends Tab {
	private static final TextureBlitData ICON = new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(64, 80), Dimension.SQUARE_16);

	private final int entityId;

	protected BackToMovingStorageTab(Position position, int entityId) {
		super(position, Component.translatable(StorageTranslationHelper.INSTANCE.translGui("back_to_storage.tooltip")),
				onTabIconClicked -> new ImageButton(new Position(position.x() + 1, position.y() + 4), Dimension.SQUARE_16, ICON, onTabIconClicked));
		this.entityId = entityId;
	}

	@Override
	protected void onTabIconClicked(int button) {
		StorageInMotionPacketHandler.INSTANCE.sendToServer(new OpenMovingStorageInventoryMessage(entityId));
	}
}
