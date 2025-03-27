package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.world.item.ItemStack;

public interface IMovingStorageEntity {
	ItemStack getStorageItem();

	void setStorageItem(ItemStack storageItem);

	EntityStorageHolder<?> getStorageHolder();

	ItemStack getDropStack();
}
