package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;

public class ClientEventHandler {
	private ClientEventHandler() {
	}

	public static void registerHandlers() {
		ClientEventHandler.registerClientExtensions();{
		ClientEventHandler.registerOverlay();
		ClientEventHandler.registerTooltipComponent();

		IEventBus eventBus = MinecraftForge.EVENT_BUS;
		eventBus.addListener(ClientMovingStorageContentsTooltip::onWorldLoad);

		net.p3pp3rf1y.sophisticatedstorage.client.ClientEventHandler.addSortScreenMatcher(screen -> screen instanceof MovingStorageScreen);
	}

	private static void registerClientExtensions() {
		BuiltinItemRendererRegistry.INSTANCE.register(ModItems.STORAGE_MINECART, new StorageMinecartItemRenderer());
	}

	private static void registerOverlay(RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "paintbrush_moving_storage_info", PaintbrushMovingStorageOverlay.HUD_PAINTBRUSH_INFO);
	}

	private static void registerTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(MovingStorageItem.MovingStorageContentsTooltip.class, ClientMovingStorageContentsTooltip::new);
	}
}
