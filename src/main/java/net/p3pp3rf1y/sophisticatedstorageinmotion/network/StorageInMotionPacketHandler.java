package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;

public class StorageInMotionPacketHandler extends PacketHandler {
	public static void init() {
		registerC2SMessage(OpenMovingStorageInventoryMessage.class, OpenMovingStorageInventoryMessage::new);
		registerS2CMessage(MovingStorageContentsMessage.class, MovingStorageContentsMessage::new);
	}
}
