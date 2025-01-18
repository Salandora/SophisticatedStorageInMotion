package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.fabricmc.api.ClientModInitializer;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntitiesClient;

public class SophisticatedStorageInMotionClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientEventHandler.registerHandlers();
		ModEntitiesClient.registerHandlers();
	}
}
