package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.fabricmc.api.ClientModInitializer;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntitiesClient;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.StorageInMotionPacketHandler;

public class SophisticatedStorageInMotionClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		StorageInMotionPacketHandler.INSTANCE.initClientListener();
		ClientEventHandler.registerHandlers();
		ModEntitiesClient.registerHandlers();
	}
}
