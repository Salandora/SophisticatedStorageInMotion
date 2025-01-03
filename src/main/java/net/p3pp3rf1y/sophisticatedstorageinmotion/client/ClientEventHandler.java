package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;

public class  ClientEventHandler{

	public static void registerHandlers() {
		ClientEventHandler.registerClientExtensions();
	}

	private static void registerClientExtensions() {
		BuiltinItemRendererRegistry.INSTANCE.register(ModItems.STORAGE_MINECART.get(), new StorageMinecartItemRenderer());
	}
}
