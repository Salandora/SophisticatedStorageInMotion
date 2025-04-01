package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.client.render.BarrelBakedModelBase;

public class StorageBlockRenderer {
	static void renderStorageBlock(float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, StorageBlockEntity renderBlockEntity) {
		BlockState state = renderBlockEntity.getBlockState();
		Minecraft minecraft = Minecraft.getInstance();
		if (renderBlockEntity instanceof BarrelBlockEntity barrel) {
			BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();
			BakedModel bakedModel = blockRenderer.getBlockModel(barrel.getBlockState());
			ModelData modelData = BarrelBakedModelBase.getModelDataFromBlockEntity(barrel);
			BlockAndTintGetter wrappedLevel = new StaticBlockEntityTintGetter(minecraft.level, renderBlockEntity, packedLight); //TODO try to optimize not to create a new instance all the time, perhaps level keyed cache for these and then only setting blockentity in the render call
			for (RenderType renderType : bakedModel.getRenderTypes(state, RandomSource.create(42L), modelData)) {
				VertexConsumer vertexConsumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(renderType, false));
				RandomSource randomsource = RandomSource.create();
				randomsource.setSeed(42L);
				blockRenderer.getModelRenderer().tesselateWithoutAO(wrappedLevel, bakedModel, barrel.getBlockState(), BlockPos.ZERO, poseStack, vertexConsumer, false, randomsource, state.getSeed(BlockPos.ZERO), OverlayTexture.NO_OVERLAY, modelData, renderType);
			}
		}

		BlockEntityRenderer<StorageBlockEntity> renderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(renderBlockEntity);
		if (renderer != null) {
			renderer.render(renderBlockEntity, partialTicks, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
		}
	}
}
