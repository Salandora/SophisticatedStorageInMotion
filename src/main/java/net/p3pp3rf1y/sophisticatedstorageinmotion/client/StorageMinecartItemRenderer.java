package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;

public class StorageMinecartItemRenderer extends MovingStorageItemRenderer<StorageMinecart> {
	/*public static final Lazy<StorageMinecartItemRenderer> STORAGE_MINECART_ITEM_RENDERER = Lazy.of(() -> new StorageMinecartItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));

	public StorageMinecartItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
		super(blockEntityRenderDispatcher, entityModelSet);
	}*/

	@Override
	protected void setMovingStoragePropertiesFromStack(StorageMinecart movingStorage, ItemStack stack) {
		//noop
	}

	/*public static IClientItemExtensions getItemRenderProperties() {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return STORAGE_MINECART_ITEM_RENDERER.get();
			}
		};
	}*/

	@Override
	protected StorageMinecart instantiateMovingStorage(Minecraft mc) {
		return new StorageMinecart(mc.level);
	}
}
