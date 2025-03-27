package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageBoat;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;

public class StorageBoatItemRenderer extends MovingStorageItemRenderer<StorageBoat> {
	public StorageBoatItemRenderer() {
	}

	@Override
	protected void setMovingStoragePropertiesFromStack(StorageBoat movingStorage, ItemStack stack) {
		movingStorage.setVariant(StorageBoatItem.getBoatType(stack));
	}

	@Override
	protected StorageBoat instantiateMovingStorage(Minecraft mc) {
		return new StorageBoat(mc.level);
	}
}
