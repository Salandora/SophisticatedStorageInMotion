package net.p3pp3rf1y.sophisticatedstorageinmotion.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.jetbrains.annotations.Nullable;

public interface SophisticatedBaseRailBlock {
	RailShape sophisticated_getRailDirection(BlockState var1, BlockGetter var2, BlockPos var3, @Nullable AbstractMinecart var4);
}
