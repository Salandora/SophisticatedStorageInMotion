package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;

import javax.annotation.Nullable;

public class StorageMinecartItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
	@Nullable
	private static StorageMinecart MINECART = null;

	@Override
	public void render(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}

		StorageMinecart minecart = getStorageMinecart(mc);
		minecart.getStorageHolder().setStorageItemFrom(stack);

		poseStack.pushPose();
		poseStack.translate(0.5, 0, 0.5);
		mc.getEntityRenderDispatcher().render(minecart, 0, 0, 0, 0, 0, poseStack, buffer, packedLight);
		poseStack.popPose();
	}

	private static StorageMinecart getStorageMinecart(Minecraft mc) {
		if (MINECART == null) {
			MINECART = new StorageMinecart(ModEntities.STORAGE_MINECART.get(), mc.level);
		}

		return MINECART;
	}
}
