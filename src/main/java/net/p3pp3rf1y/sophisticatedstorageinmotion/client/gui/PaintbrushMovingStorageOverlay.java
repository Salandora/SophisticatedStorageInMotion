package net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelMaterial;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.PaintbrushItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PaintbrushMovingStorageOverlay {

	private static Optional<PaintbrushItem.ItemRequirements> ITEM_REQUIREMENTS_CACHE = Optional.empty();
	private static int lastEntityIdCached = -1;
	@Nullable
	private static ItemStack lastPaintbrushCached = null;

	public static <T extends Entity & IMovingStorageEntity> Optional<PaintbrushItem.ItemRequirements> getItemRequirementsFor(ItemStack paintbrush, Player player, Entity entity) {
		if (!(entity instanceof IMovingStorageEntity)) {
			return Optional.empty();
		}

		@SuppressWarnings("unchecked")
		T movingStorage = (T) entity;

		if (movingStorage.getId() != lastEntityIdCached || paintbrush != lastPaintbrushCached) {
			ITEM_REQUIREMENTS_CACHE = getItemRequirements(paintbrush, player, movingStorage);
			lastEntityIdCached = movingStorage.getId();
			lastPaintbrushCached = paintbrush;
		}
		return ITEM_REQUIREMENTS_CACHE;
	}

	private static <T extends Entity & IMovingStorageEntity> Optional<PaintbrushItem.ItemRequirements> getItemRequirements(ItemStack paintbrush, Player player, T movingStorage) {
		Map<BarrelMaterial, ResourceLocation> materialsToApply = new HashMap<>(PaintbrushItem.getBarrelMaterials(paintbrush));
		if (!materialsToApply.isEmpty()) {
			if (!movingStorage.getStorageHolder().canHoldMaterials()) {
				return Optional.empty();
			}

			return PaintbrushItem.getItemRequirements(paintbrush, player, PaintbrushItem.getMaterialHolderPartsNeeded(materialsToApply, movingStorage.getStorageHolder()));
		} else {
			int mainColorToSet = PaintbrushItem.getMainColor(paintbrush);
			int accentColorToSet = PaintbrushItem.getAccentColor(paintbrush);
			return PaintbrushItem.getDyeItemRequirements(paintbrush, player, PaintbrushItem.getStorageDyePartsNeeded(mainColorToSet, accentColorToSet, movingStorage.getStorageHolder().getStorageWrapper()));
		}
	}

	public static final LayeredDraw.Layer HUD_PAINTBRUSH_INFO = (guiGraphics, deltaTracker) -> {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen != null) {
			if (!mc.screen.isPauseScreen()) {
				lastEntityIdCached = -1;
				lastPaintbrushCached = null;
			}
			return;
		}

		LocalPlayer player = mc.player;
		Level level = mc.level;
		if (player == null || level == null || !(mc.hitResult instanceof EntityHitResult entityHitResult) || !(entityHitResult.getEntity() instanceof IMovingStorageEntity)) {
			return;
		}

		InventoryHelper.getItemFromEitherHand(player, ModItems.PAINTBRUSH.get()).flatMap(paintbrush -> getItemRequirementsFor(paintbrush, player, entityHitResult.getEntity()))
				.ifPresent(itemRequirements -> {
					if (itemRequirements.itemsMissing().isEmpty()) {
						return;
					}

					Component missingItems = StorageTranslationHelper.INSTANCE.translItemOverlayMessage(ModItems.PAINTBRUSH.get(), "missing_items");
					Font font = mc.font;
					int i = font.width(missingItems);
					int x = (guiGraphics.guiWidth() - i) / 2;
					int y = guiGraphics.guiHeight() - 75 - 10;
					guiGraphics.drawStringWithBackdrop(font, missingItems, x + 1, y, DyeColor.WHITE.getTextColor(), 0xFFFFFFFF);

					x = (guiGraphics.guiWidth() - itemRequirements.itemsMissing().size() * 18) / 2;
					for (ItemStack missingItem : itemRequirements.itemsMissing()) {
						guiGraphics.renderItem(missingItem, x, y + 10);
						guiGraphics.renderItemDecorations(font, missingItem, x, y + 10);
						x += 18;
					}
				});
	};
}
