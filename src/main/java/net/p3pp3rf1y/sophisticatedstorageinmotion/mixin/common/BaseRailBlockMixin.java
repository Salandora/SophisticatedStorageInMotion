package net.p3pp3rf1y.sophisticatedstorageinmotion.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.p3pp3rf1y.sophisticatedstorageinmotion.extensions.SophisticatedBaseRailBlock;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BaseRailBlock.class)
public abstract class BaseRailBlockMixin implements SophisticatedBaseRailBlock {
	@Shadow public abstract Property<RailShape> getShapeProperty();

	@Override
	public RailShape sophisticated_getRailDirection(BlockState state, BlockGetter world, BlockPos pos, @Nullable AbstractMinecart cart) {
		return state.getValue(this.getShapeProperty());
	}
}
