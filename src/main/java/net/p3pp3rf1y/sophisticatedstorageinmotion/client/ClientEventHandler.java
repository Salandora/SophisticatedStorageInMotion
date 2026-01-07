package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.github.salandora.sophisticatedfabriclib.event.api.v0.client.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.PaintbrushMovingStorageOverlay;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import javax.annotation.Nullable;

public class ClientEventHandler {
	private ClientEventHandler() {
	}

	public static void registerHandlers() {
		ClientEventHandler.registerClientExtensions();
		ClientEventHandler.registerOverlay();
		ClientEventHandler.registerTooltipComponent();

		ClientLifecycleEvents.CLIENT_LEVEL_LOAD.register(ClientMovingStorageContentsTooltip::onWorldLoad);

		net.p3pp3rf1y.sophisticatedstorage.client.ClientEventHandler.addSortScreenMatcher(screen -> screen instanceof MovingStorageScreen);
	}

	private static void registerClientExtensions() {
		BuiltinItemRendererRegistry.INSTANCE.register(ModItems.STORAGE_MINECART, new StorageMinecartItemRenderer());
		BuiltinItemRendererRegistry.INSTANCE.register(ModItems.STORAGE_BOAT, new StorageBoatItemRenderer());
	}

	private static void registerOverlay() {
		HudRenderCallback.EVENT.register(PaintbrushMovingStorageOverlay.HUD_PAINTBRUSH_INFO);
	}

	private static void registerTooltipComponent() {
		TooltipComponentCallback.EVENT.register(ClientEventHandler::registerTooltipComponent);
	}
	@Nullable
	private static ClientTooltipComponent registerTooltipComponent(TooltipComponent data) {
		if (data instanceof MovingStorageItem.MovingStorageContentsTooltip movingStorageContentsTooltip) {
			return new ClientMovingStorageContentsTooltip(movingStorageContentsTooltip);
		}
		return null;
	}
}
