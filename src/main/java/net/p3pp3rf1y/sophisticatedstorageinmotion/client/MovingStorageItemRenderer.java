package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;

import javax.annotation.Nullable;

public abstract class MovingStorageItemRenderer<T extends Entity & IMovingStorageEntity> extends BlockEntityWithoutLevelRenderer {
	@Nullable
	private T movingStorage = null;
	public MovingStorageItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
		super(blockEntityRenderDispatcher, entityModelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}

		T movingStorage = getMovingStorage(mc);
		setMovingStoragePropertiesFromStack(movingStorage, stack);
		movingStorage.getStorageHolder().setStorageItemFrom(stack, false);

		poseStack.pushPose();
		poseStack.translate(0.5, 0, 0.5);
		mc.getEntityRenderDispatcher().render(movingStorage, 0, 0, 0, 0, 0, poseStack, buffer, packedLight);
		poseStack.popPose();
	}

	protected abstract void setMovingStoragePropertiesFromStack(T movingStorage, ItemStack stack);

	private T getMovingStorage(Minecraft mc) {
		if (movingStorage == null) {
			movingStorage = instantiateMovingStorage(mc);
		}

		return movingStorage;
	}

	protected abstract T instantiateMovingStorage(Minecraft mc);
}
