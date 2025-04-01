package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.StorageBoatRenderer;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.StorageMinecartRenderer;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingLimitedBarrelScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingLimitedBarrelSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageSettingsScreen;

public class ModEntitiesClient {
	public static void registerHandlers() {
		ModEntitiesClient.registerEntityRenderers();
		ModEntitiesClient.onMenuScreenRegister();
	}

	private static void registerEntityRenderers() {
		// event.registerEntityRenderer(ModEntities.STORAGE_MINECART.get(), StorageMinecartRenderer::new);
		// event.registerEntityRenderer(ModEntities.STORAGE_BOAT.get(), StorageBoatRenderer::new);
		EntityRendererRegistry.register(ModEntities.STORAGE_MINECART.get(), StorageMinecartRenderer::new);
		EntityRendererRegistry.register(ModEntities.STORAGE_BOAT.get(), StorageBoatRenderer::new);
	}

	private static void onMenuScreenRegister() {
		// event.register(ModEntities.MOVING_STORAGE_CONTAINER_TYPE.get(), MovingStorageScreen::constructScreen);
		// event.register(ModEntities.MOVING_STORAGE_SETTINGS_CONTAINER_TYPE.get(), MovingStorageSettingsScreen::constructScreen);
		// event.register(ModEntities.MOVING_LIMITED_BARREL_CONTAINER_TYPE.get(), MovingLimitedBarrelScreen::new);
		// event.register(ModEntities.MOVING_LIMITED_BARREL_SETTINGS_CONTAINER_TYPE.get(), MovingLimitedBarrelSettingsScreen::new);
		MenuScreens.register(ModEntities.MOVING_STORAGE_CONTAINER_TYPE.get(), MovingStorageScreen::constructScreen);
		MenuScreens.register(ModEntities.MOVING_STORAGE_SETTINGS_CONTAINER_TYPE.get(), MovingStorageSettingsScreen::constructScreen);
		MenuScreens.register(ModEntities.MOVING_LIMITED_BARREL_CONTAINER_TYPE.get(), MovingLimitedBarrelScreen::new);
		MenuScreens.register(ModEntities.MOVING_LIMITED_BARREL_SETTINGS_CONTAINER_TYPE.get(), MovingLimitedBarrelSettingsScreen::new);
	}
}
