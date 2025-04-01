package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.neoforged.jarjar.nio.util.Lazy;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageBoat;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;

public class StorageBoatItemRenderer extends MovingStorageItemRenderer<StorageBoat> {
	public static final Lazy<StorageBoatItemRenderer> STORAGE_BOAT_ITEM_RENDERER = Lazy.of(() -> new StorageBoatItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));
	public StorageBoatItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
		super(blockEntityRenderDispatcher, entityModelSet);
	}

	@Override
	protected void setMovingStoragePropertiesFromStack(StorageBoat movingStorage, ItemStack stack) {
		movingStorage.setVariant(StorageBoatItem.getBoatType(stack));
	}

	@Override
	protected StorageBoat instantiateMovingStorage(Minecraft mc) {
		return new StorageBoat(mc.level);
	}

	public static IClientItemExtensions getItemRenderProperties() {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return STORAGE_BOAT_ITEM_RENDERER.get();
			}
		};
	}
}
