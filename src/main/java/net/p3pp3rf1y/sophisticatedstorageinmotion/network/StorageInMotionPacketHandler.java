package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;

public class StorageInMotionPacketHandler extends PacketHandler {
	public static final StorageInMotionPacketHandler INSTANCE = new StorageInMotionPacketHandler(SophisticatedStorageInMotion.MOD_ID);

	private StorageInMotionPacketHandler(String modId) {
		super(modId);
	}

	@Override
	public void init() {
		registerMessage(OpenMovingStorageInventoryMessage.class, OpenMovingStorageInventoryMessage::encode, OpenMovingStorageInventoryMessage::decode, OpenMovingStorageInventoryMessage::onMessage);
		registerMessage(MovingStorageContentsMessage.class, MovingStorageContentsMessage::encode, MovingStorageContentsMessage::decode, MovingStorageContentsMessage::onMessage);
		registerMessage(RequestMovingStorageInventoryContentsMessage.class, RequestMovingStorageInventoryContentsMessage::encode, RequestMovingStorageInventoryContentsMessage::decode, RequestMovingStorageInventoryContentsMessage::onMessage);
	}
}
