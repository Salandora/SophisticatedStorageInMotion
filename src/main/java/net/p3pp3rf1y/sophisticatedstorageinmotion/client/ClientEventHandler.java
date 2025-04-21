package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.PaintbrushMovingStorageOverlay;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import javax.annotation.Nullable;

public class  ClientEventHandler{

	public static void registerHandlers() {
		ClientEventHandler.registerClientExtensions();
		ClientEventHandler.registerOverlay();
		ClientEventHandler.registerTooltipComponent();
	}

	private static void registerClientExtensions() {
		// event.registerItem(StorageMinecartItemRenderer.getItemRenderProperties(), ModItems.STORAGE_MINECART.get());
		// event.registerItem(StorageBoatItemRenderer.getItemRenderProperties(), ModItems.STORAGE_BOAT.get());

		BuiltinItemRendererRegistry.INSTANCE.register(ModItems.STORAGE_MINECART.get(), new StorageMinecartItemRenderer());
		BuiltinItemRendererRegistry.INSTANCE.register(ModItems.STORAGE_BOAT.get(), new StorageBoatItemRenderer());
	}

	private static void registerOverlay() {
		// event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "paintbrush_moving_storage_info"), PaintbrushMovingStorageOverlay.HUD_PAINTBRUSH_INFO);

		HudRenderCallback.EVENT.register(PaintbrushMovingStorageOverlay.HUD_PAINTBRUSH_INFO);
	}

	private static void registerTooltipComponent() {
		// event.register(MovingStorageItem.MovingStorageContentsTooltip.class, ClientMovingStorageContentsTooltip::new);

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
