package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;

public class StorageMinecartItemRenderer extends MovingStorageItemRenderer<StorageMinecart> {
	public StorageMinecartItemRenderer() {
	}

	@Override
	protected void setMovingStoragePropertiesFromStack(StorageMinecart movingStorage, ItemStack stack) {
		//noop
	}

	@Override
	protected StorageMinecart instantiateMovingStorage(Minecraft mc) {
		return new StorageMinecart(mc.level);
	}
}
