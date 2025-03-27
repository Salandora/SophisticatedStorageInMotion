package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

class StaticBlockEntityTintGetter implements BlockAndTintGetter {
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
