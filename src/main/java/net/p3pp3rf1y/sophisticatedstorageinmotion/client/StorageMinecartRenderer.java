package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.ShulkerBoxBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;

public class StorageMinecartRenderer extends MinecartRenderer<StorageMinecart> {
	public StorageMinecartRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.MINECART);
	}

	@Override
	protected void renderMinecartContents(StorageMinecart entity, float partialTicks, BlockState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		StorageBlockEntity renderBlockEntity = entity.getStorageHolder().getRenderBlockEntity();

		poseStack.pushPose();
		double yOffset = 0;
		if (renderBlockEntity instanceof BarrelBlockEntity || renderBlockEntity instanceof ShulkerBoxBlockEntity) {
			yOffset -= 2 / 16D;
		}
		poseStack.translate(0, yOffset, 0);

		StorageBlockRenderer.renderStorageBlock(partialTicks, poseStack, buffer, packedLight, renderBlockEntity);
		poseStack.popPose();
	}
}
