package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.ShulkerBoxBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageBoat;
import org.joml.Quaternionf;

public class StorageBoatRenderer extends EntityRenderer<StorageBoat> {
	private final BoatRenderer boatRenderer;
	public StorageBoatRenderer(EntityRendererProvider.Context context) {
		super(context);
		boatRenderer = new BoatRenderer(context, false);
	}

	@Override
	public ResourceLocation getTextureLocation(StorageBoat storageBoat) {
		return boatRenderer.getModelWithLocation(storageBoat).getFirst();
	}

	@Override
	public void render(StorageBoat storageBoat, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		super.render(storageBoat, entityYaw, partialTicks, poseStack, buffer, packedLight);

		poseStack.pushPose();
		poseStack.translate(0,  storageBoat.getVariant().isRaft() ? 8/16F : 3/16F, 0);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
		float interpolatedHurtTime = (float)storageBoat.getHurtTime() - partialTicks;
		float interpolatedDamage = storageBoat.getDamage() - partialTicks;
		if (interpolatedDamage < 0.0F) {
			interpolatedDamage = 0.0F;
		}

		if (interpolatedHurtTime > 0.0F) {
			poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(interpolatedHurtTime) * interpolatedHurtTime * interpolatedDamage / 10.0F * (float)storageBoat.getHurtDir()));
		}

		float bubbleAngle = storageBoat.getBubbleAngle(partialTicks);
		if (!Mth.equal(bubbleAngle, 0.0F)) {
			poseStack.mulPose((new Quaternionf()).setAngleAxis(storageBoat.getBubbleAngle(partialTicks) * 0.017453292F, 1.0F, 0.0F, 1.0F));
		}

		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(180));
		poseStack.mulPose(Axis.XP.rotationDegrees(180));
		poseStack.scale(6/7F, 6/7F, 6/7F);
		StorageBlockEntity renderBlockEntity = storageBoat.getStorageHolder().getRenderBlockEntity();
		poseStack.translate(-0.5F, 0, (renderBlockEntity instanceof BarrelBlockEntity || renderBlockEntity instanceof ShulkerBoxBlockEntity ? 0 : 1/16F) + 0.02F);
		StorageBlockRenderer.renderStorageBlock(partialTicks, poseStack, buffer, packedLight, renderBlockEntity);
		poseStack.popPose();
		boatRenderer.render(storageBoat, entityYaw, partialTicks, poseStack, buffer, packedLight);
	}
}
