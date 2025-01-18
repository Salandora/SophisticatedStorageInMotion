package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.p3pp3rf1y.sophisticatedcore.util.model.ModelData;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.ShulkerBoxBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.client.render.BarrelBakedModelBase;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;
import org.jetbrains.annotations.Nullable;

public class StorageMinecartRenderer extends MinecartRenderer<StorageMinecart> {
	public StorageMinecartRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.MINECART);
	}

	@Override
	protected void renderMinecartContents(StorageMinecart entity, float partialTicks, BlockState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		StorageBlockEntity renderBlockEntity = entity.getStorageHolder().getRenderBlockEntity();

		Minecraft minecraft = Minecraft.getInstance();

		BlockAndTintGetter wrappedLevel = new StaticBlockEntityTintGetter(minecraft.level, renderBlockEntity, packedLight); //TODO try to optimize not to create a new instance all the time, perhaps level keyed cache for these and then only setting blockentity in the render call

		poseStack.pushPose();
		double yOffset = 0;
		if (renderBlockEntity instanceof BarrelBlockEntity || renderBlockEntity instanceof ShulkerBoxBlockEntity) {
			yOffset -= 2 / 16D;
		}
		poseStack.translate(0, yOffset, 0);
		if (renderBlockEntity instanceof BarrelBlockEntity barrel) {
			BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();
			BakedModel bakedModel = blockRenderer.getBlockModel(barrel.getBlockState());

			ModelData modelData = BarrelBakedModelBase.getModelDataFromBlockEntity(barrel);
			// Fabric: Added to set the model data for the barrel baked model
			BarrelBakedModelBase.setModelData(barrel.getBlockState(), modelData);

			//for (RenderType renderType : bakedModel.getRenderTypes(state, RandomSource.create(42L), modelData)) {
				//VertexConsumer vertexConsumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(renderType, false));
				VertexConsumer vertexConsumer = buffer.getBuffer(ItemBlockRenderTypes.getRenderType(barrel.getBlockState(), false));
				RandomSource randomsource = RandomSource.create();
				randomsource.setSeed(42L);
				blockRenderer.getModelRenderer().tesselateWithoutAO(wrappedLevel, bakedModel, barrel.getBlockState(), BlockPos.ZERO, poseStack, vertexConsumer, false, randomsource, state.getSeed(BlockPos.ZERO), OverlayTexture.NO_OVERLAY);
			//}
		}

		BlockEntityRenderer<StorageBlockEntity> renderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(renderBlockEntity);
		if (renderer != null) {
			renderer.render(renderBlockEntity, partialTicks, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
		}
		poseStack.popPose();
	}

	private static class StaticBlockEntityTintGetter implements BlockAndTintGetter {
		private final BlockAndTintGetter level;
		private final BlockEntity blockEntity;
		private final int packedLight;

		public StaticBlockEntityTintGetter(BlockAndTintGetter level, BlockEntity blockEntity, int packedLight) {
			this.level = level;
			this.blockEntity = blockEntity;
			this.packedLight = packedLight;
		}

		@Override
		public float getShade(Direction direction, boolean b) {
			return level.getShade(direction, b);
		}

		@Override
		public LevelLightEngine getLightEngine() {
			return level.getLightEngine();
		}

		@Override
		public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
			return level.getBlockTint(blockPos, colorResolver);
		}

		@Nullable
		@Override
		public BlockEntity getBlockEntity(BlockPos blockPos) {
			return blockPos == BlockPos.ZERO ? blockEntity : level.getBlockEntity(blockPos);
		}

		@Override
		public BlockState getBlockState(BlockPos blockPos) {
			return blockPos == BlockPos.ZERO ? blockEntity.getBlockState() : level.getBlockState(blockPos);
		}

		@Override
		public FluidState getFluidState(BlockPos blockPos) {
			return level.getFluidState(blockPos);
		}

		@Override
		public int getHeight() {
			return level.getHeight();
		}

		@Override
		public int getMinBuildHeight() {
			return level.getMinBuildHeight();
		}

		@Override
		public int getLightEmission(BlockPos pos) {
			return 0;
		}

		@Override
		public int getBrightness(LightLayer lightType, BlockPos blockPos) {
			return lightType == LightLayer.SKY ? packedLight >> 20 : packedLight >> 4 & 15;
		}

		@Override
		public int getRawBrightness(BlockPos blockPos, int amount) {
			int skyValue = packedLight >> 20;
			int blockValue = packedLight >> 4 & 15;
			return Math.max(blockValue, skyValue - amount);
		}
	}
}
